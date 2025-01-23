package akhil.DataUnlimited.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.model.parameter.ParameterStore;
import akhil.DataUnlimited.model.parameter.VirtualFileParam;
import akhil.DataUnlimited.util.CommandLineFindReplace;
import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.FormatConversion;
import akhil.DataUnlimited.util.StringOps;
import akhil.DataUnlimited.util.DULogger;

public class Processor {
	private final static Logger lo = LogManager.getLogger(Processor.class.getName());
	private List<Group> template;
	private Date d = new Date();
	private ParameterStore ps;
	private List<String> log;
	public boolean toLog = false;

	public void addLog(int level, String msg) {
		if (level <= DULogger.getLogLevel())
			log.add(msg);
	}

	public ParameterStore getParameterStore() {
		return ps;
	}

	private Processor(Map<String, String> hm) {
		ps = new ParameterStore(this, hm);
	}

	public Processor(VirtualFileParam vfp, Map<String, String> hm) {
		ps = new ParameterStore(vfp, this, hm);
	}

	public Processor(VirtualFileParam vfp, List<String> log, Map<String, String> hm) {
		this(vfp, hm);
		this.log = log;
		toLog = true;
	}

	public Processor(List<String> log, Map<String, String> hm) {
		this(hm);
		this.log = log;
		toLog = true;
	}

	public Date getDate() {
		return d;
	}

	private void createTemplate(String str, Map<Integer, Integer> pairs, Processor p) {
		template = Group.createGroups(str, pairs, this);
	}

	public String generateData(String str, String numberOfFiles) {
		StringBuilder sbx = new StringBuilder();

		Map<Integer, Integer> pairs = Parser.getStartEndPositionMap(str, Types.GROUPSTARTDELIMITER,
				Types.GROUPENDDELIMITER, this);

		createTemplate(str, pairs, this);

		if (numberOfFiles.length() > 0) {
			try {
				int numoffile = Integer.parseInt(numberOfFiles);
				if (numoffile > 1) {
					sbx.append("<dms-data-files>\n");
					for (int i = 1; i <= numoffile; i++) {
						StringBuilder sb = new StringBuilder();
						sb.append("<data-file>");
						for (Group c : template) {
							sb.append(c.getValue(i));
						}
						sb.append("</data-file>\n");
						sbx.append(sb.toString());
					}
					sbx.append("</dms-data-files>\n");
				} else {
					for (Group c : template) {
						sbx.append(c.getValue(1));
					}
				}
				return sbx.toString();
			} catch (NumberFormatException e) {
				return "ERROR: Number of files could not be parsed as a number... @ " + new Date();
			}
		} else {
			return "ERROR: Check number of files";
		}
	}

	public void generateData(String str, String numberOfFiles, String path, String fileExtension) {
		if (Types.getInstance().getIsUI())
			DULogger.log(400, "INFO: Process starting ... " + new Date().toString());
		else
			lo.info("INFO: Process starting ... " + new Date().toString());

		Map<Integer, Integer> pairs = Parser.getStartEndPositionMap(str, Types.GROUPSTARTDELIMITER,
				Types.GROUPENDDELIMITER, this);

		createTemplate(str, pairs, this);

		if (numberOfFiles.length() > 0) {
			try {
				long numoffile = Long.parseLong(numberOfFiles);
				if (Types.getInstance().getIsUI())
					DULogger.log(400, "INFO: Attempting to create files... @ " + new Date());
				else
					lo.info("INFO: Attempting to create files... @ " + new Date());

				boolean fileCreated = false;
				int count = 0;
				String name = "result_";
				for (int i = 1; i <= numoffile; i++) {
					StringBuilder sb = new StringBuilder();
					for (Group c : template) {
						sb.append(c.getValue(i));
					}
					String tofile = "";

					if (fileExtension.contains("/")) {
						List<String> fileExtensionSplit = StringOps.fastSplit(fileExtension, "/");
						name = fileExtensionSplit.get(0).trim();
						fileExtension = fileExtensionSplit.get(1).trim();
					}

					if (fileExtension.contains("xml"))
						tofile = FormatConversion.prettyPrint(sb.toString());
					else if (fileExtension.contains("json"))
						tofile = FormatConversion.prettyPrint(sb.toString(), true);
					else
						tofile = sb.toString();

					if (path != null && !path.isEmpty() && !tofile.trim().isEmpty()) {
						FileOperation.writeFile(path, name + i + "." + fileExtension, tofile);
						fileCreated = true;
						count++;
					} else {
						if (Types.getInstance().getIsUI())
							DULogger.log(200, "ERROR:Check save file path");
						else
							lo.error("ERROR:Check save file path");
					}

				}
				if (fileCreated) {
					if (Types.getInstance().getIsUI())
						DULogger.log(400, "INFO: Files Created... last file name is " + name + "_" + count + "."
								+ fileExtension + " @ Time:" + new Date().toString());
					else
						lo.info("INFO: Files Created... last file name is " + name + count + "." + fileExtension
								+ " @ Time:" + new Date().toString());

				}
			} catch (NumberFormatException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: Number of files could not be parsed as a number... @ " + new Date());
				else
					lo.error("ERROR: Number of files could not be parsed as a number... @ " + new Date());
			}
		} else {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, "ERROR: Check number of files");
			else
				lo.error("ERROR: Check number of files");

		}
	}

	public void generateData(String str, String numberOfFiles, String path, String fileExtension,
			List<String> fileLevelReplacements) {
		if (Types.getInstance().getIsUI())
			DULogger.log(400, "INFO: Process starting @... " + new Date().toString());
		else
			lo.info("INFO: Process starting @... " + new Date().toString());

		Map<Integer, Integer> pairs = Parser.getStartEndPositionMap(str, Types.GROUPSTARTDELIMITER,
				Types.GROUPENDDELIMITER, this);

		createTemplate(str, pairs, this);

		if (numberOfFiles.length() > 0) {
			try {
				long numoffile = Long.parseLong(numberOfFiles);
				if (Types.getInstance().getIsUI())
					DULogger.log(400, "INFO: Attempting to create files... @ " + new Date());
				else
					lo.info("INFO: Attempting to create files... @ " + new Date());

				boolean fileCreated = false;
				int count = 0;
				String name = "result_";
				for (int i = 1; i <= numoffile; i++) {
					List<String> toPrint = new ArrayList<String>();
					for (Group c : template) {
						toPrint.add(c.getValue(i));
					}
					String strToPrint = String.join("", toPrint);

					if (fileExtension.contains("/")) {
						List<String> fileExtensionSplit = StringOps.fastSplit(fileExtension, "/");
						name = fileExtensionSplit.get(0).trim();
						fileExtension = fileExtensionSplit.get(1).trim();
					}

					if (fileExtension.contains("xml"))
						strToPrint = FormatConversion
								.prettyPrint(CommandLineFindReplace.findReplace(strToPrint, fileLevelReplacements));
					else if (fileExtension.contains("json"))
						strToPrint = FormatConversion.prettyPrint(
								CommandLineFindReplace.findReplace(strToPrint, fileLevelReplacements), true);
					else
						strToPrint = CommandLineFindReplace.findReplace(strToPrint, fileLevelReplacements);

					if (path != null && !path.isEmpty()) {
						FileOperation.writeFile(path, name + i + "." + fileExtension, strToPrint);
						fileCreated = true;
						count++;
					} else {
						if (Types.getInstance().getIsUI())
							DULogger.log(200, "ERROR:Check save file path");
						else
							lo.error("ERROR:Check save file path");

					}
				}
				if (fileCreated) {
					if (Types.getInstance().getIsUI())
						DULogger.log(400, "INFO: Files Created... last file name is " + name + count + "."
								+ fileExtension + " @ Time:" + new Date().toString());
					else
						lo.info("INFO: Files Created... last file name is " + name + count + "." + fileExtension
								+ " @ Time:" + new Date().toString());

				}
			} catch (NumberFormatException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: Number of file could not be parsed as a number... @ " + new Date());
				else
					lo.error("ERROR: Number of file could not be parsed as a number... @ " + new Date());
			}
		} else {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, "ERROR:Check number of files");
			else
				lo.error("ERROR:Check number of files");

		}
	}

	public String checkSize(String str) {
		if (Types.getInstance().getIsUI())
			DULogger.log(400, "INFO: Process starting ... " + new Date().toString());
		else
			lo.info("INFO: Process starting ... " + new Date().toString());

		Map<Integer, Integer> pairs = Parser.getStartEndPositionMap(str, Types.GROUPSTARTDELIMITER,
				Types.GROUPENDDELIMITER, this);

		Map<Integer, Integer> segpairs = Parser.getStartEndPositionMap(str, Types.SEGMENTSTARTDELIMITER,
				Types.SEGMENTENDDELIMITER, this);

		for (Integer grpstart : pairs.keySet()) {
			for (Map.Entry<Integer, Integer> segsted : segpairs.entrySet()) {
				if (segsted.getKey() < grpstart && grpstart < segsted.getValue()) {
					if (Types.getInstance().getIsUI())
						DULogger.log(200, "ERROR: Group start found inside a Segment. Segment start position: "
								+ segsted.getKey() + ". Group start position: " + grpstart);
					else
						lo.error("ERROR: Group start found inside a Segment. Segment start position: "
								+ segsted.getKey() + ". Group start position: " + grpstart);
					return null;
				}
			}
		}

		createTemplate(str, pairs, this);
		if (Types.getInstance().getIsUI())
			DULogger.log(400, "INFO: Template created ... " + new Date().toString());
		else
			lo.info("INFO: Template created ... " + new Date().toString());

		StringBuilder sb = new StringBuilder();

		for (Group c : template) {
			String s = c.getValue(1);
			sb.append(s);
		}
		if (Types.getInstance().getIsUI())
			DULogger.log(400,
					"\n INFO: IN CASE, GROUPS HAVE A REPETITION RANGE E.G. \"#GrOuP#XYZ#1-10^^\" , MINIMUM FILE SIZE (MINIMUM GrOuP ITERATIONS) WILL BE PRINTED...\n\n");
		else
			lo.info("\nINFO: IN CASE, GROUPS HAVE A REPETITION RANGE E.G. \"#GrOuP#XYZ#1-10^^\" , MINIMUM FILE SIZE (MINIMUM GrOuP ITERATIONS) WILL BE PRINTED...\n\n");
		if (Types.getInstance().getIsUI())
			DULogger.log(400, "INFO: Template created ... " + new Date().toString());
		else
			lo.info("INFO: Size obtained ... " + new Date().toString());

		return sb.toString();
	}

	public String generateDatax(String str) {
		Map<Integer, Integer> pairs = Parser.getStartEndPositionMap(str, Types.GROUPSTARTDELIMITER,
				Types.GROUPENDDELIMITER, this);
		createTemplate(str, pairs, this);

		StringBuilder sb = new StringBuilder();
		for (Group c : template) {
			String s = c.getValue(1);
			sb.append(s);
		}
		return sb.toString();
	}
}

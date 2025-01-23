package akhil.DataUnlimited.dataextractor.hierarchicaldoc;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import akhil.DataUnlimited.model.parameter.VirtualFileParam;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.CommandLineFindReplace;
import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.StringOps;
import akhil.DataUnlimited.util.FormatConversion;

public class HierarchyDelimitedFile {
	private static final Logger lo = LogManager.getLogger(HierarchyDelimitedFile.class.getName());
	private static final String DMSDELIM = "<DMSDELIM>";
	private static final String DMSNEWLINE = "<DMSNEWLINE>";
	private VirtualFileParam vfp;
	private List<List<String>> ls = null;

	public VirtualFileParam getVFP() {
		return vfp;
	}

	private boolean toLog = false;

	public boolean isToLog() {
		return toLog;
	}

	private List<String> log;

	public void addLog(int level, String msg) {
		if (level <= DULogger.getLogLevel())
			log.add(msg);
	}

	public HierarchyDelimitedFile() {
		vfp = new VirtualFileParam();
	}

	public HierarchyDelimitedFile(List<String> logForApi) {
		this.log = logForApi;
		toLog = true;
		vfp = new VirtualFileParam(log, toLog);
	}

	public VirtualFileParam extract(String inputFile, List<String> mapperData, List<String> findReplace) {
		ls = new ArrayList<>();
		for (String s : mapperData)
			ls.add(StringOps.fastSplit(s, DMSDELIM));
		return extractCore(inputFile, ls, false, findReplace);
	}

	public VirtualFileParam extract(String inputFile, List<String> mapperFile, boolean isInputFileParamActuallyTheData,
			List<String> findReplace) {
		List<List<String>> x = new ArrayList<>();
		for (String s : mapperFile)
			x.add(StringOps.fastSplit(s, DMSDELIM));
		return extractCore(inputFile, x, isInputFileParamActuallyTheData, findReplace);
	}

	public VirtualFileParam extractCore(String inputFile, List<List<String>> ls2, boolean isInputFileActuallyTheData,
			List<String> findReplace) {

		String data;
		if (isInputFileActuallyTheData) {
			data = inputFile;
			String trimdata = data.trim();
			if (trimdata.startsWith("{") && trimdata.endsWith("}") && FormatConversion.isJSONValid(data))
				data = FormatConversion.jsonToXML(data); // if data is JSON, it is converted to XML
		} else {
			data = FileOperation.getFileContentAsString(inputFile);
			String trimdata = data.trim();
			if (trimdata.startsWith("{") && trimdata.endsWith("}") && FormatConversion.isJSONValid(data))
				data = FormatConversion.jsonToXML(data); // if data is JSON, it is converted to XML
			File f = new File(inputFile);
			data = "<dmsv-input-file-name>" + f.getName() + "</dmsv-input-file-name>\n" + data; // adding the filename
																								// to XML, for cases
																								// when data extraction
																								// needs to happen from
																								// filename
		}
		String msg = "Starting data extraction @ " + new Date() + "\nINFO: Using input file as : \n" + data;
		if (Types.getInstance().getIsUI()) {
			DULogger.log(400, msg);
		} else
			lo.debug(msg);
		if (toLog)
			addLog(500, msg);
		if (findReplace != null) {
			data = CommandLineFindReplace.findReplace(data, findReplace);
			if (data == null)
				return null;
		}
		ls = ls2;

		for (List<String> s : ls) {
			if (s.get(0).equals("node") && s.get(1).equalsIgnoreCase("top")) {
				String datax = data.replace("\n", DMSNEWLINE).replace("\r", "");
				List<String> internalNodeData = new ArrayList<>();
				List<String> sequence = new ArrayList<>();
				for (List<String> t : ls) {
					if (!String.join(DMSDELIM, t).equals(String.join(DMSDELIM, s))
							&& t.get(2).equalsIgnoreCase(s.get(1))) {

						if (t.get(0).equals("node")) {
							if (t.get(3).equals("singleline")) {
								internalNodeData.addAll(UtilityFunctions.getInternalNodeData(datax,
										StringOps.fastSplit(t.get(4), ","), Arrays.asList(DMSNEWLINE)));
								datax = UtilityFunctions.removeInternalNodeData(datax,
										StringOps.fastSplit(t.get(4), ","), Arrays.asList(DMSNEWLINE));
							} else {
								internalNodeData.addAll(UtilityFunctions.getInternalNodeData(datax,
										StringOps.fastSplit(t.get(4), ","), StringOps.fastSplit(t.get(5), ",")));
								datax = UtilityFunctions.removeInternalNodeData(datax,
										StringOps.fastSplit(t.get(4), ","), StringOps.fastSplit(t.get(5), ","));
							}
						} else {
							sequence.add(String.join(DMSDELIM, t));
						}

					}
				}
				new Node(s.get(1), datax, sequence, internalNodeData, ls, this);
			}

		}
		return vfp;
	}
}

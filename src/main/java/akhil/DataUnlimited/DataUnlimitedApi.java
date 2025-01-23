package akhil.DataUnlimited;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.dataextractor.hierarchicaldoc.HierarchyDelimitedFile;
import akhil.DataUnlimited.dataextractor.hierarchicaldoc.UtilityFunctions;
import akhil.DataUnlimited.model.Processor;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.model.parameter.GlobalVirtualFileParameters;
import akhil.DataUnlimited.model.parameter.VirtualFileParam;
import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.LogStackTrace;
import akhil.DataUnlimited.util.SQLite;

import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.StringOps;

public class DataUnlimitedApi {
	private static final Logger lo = LogManager.getLogger(DataUnlimitedApi.class.getName());
	private static final String DMSDBCONFSTART = "<dms-dbconf>";
	private static final String DMSDBCONFEND = "</dms-dbconf>";
	List<String> logForApi = new ArrayList<>();

	public DataUnlimitedApi() {
		Types.getInstance();
	}

	public DataUnlimitedApi(String datalibdir) {
		this();
		Types.getInstance().init(datalibdir);
	}

	public void generateData(List<String> param) {
		DataUnlimitedCLIProcessor.runCoreDMS(param);
	}

	private String removeCommentsFromDMS(String dmsScript) {
		return dmsScript.replaceAll("(?s)" + Types.DMSCOMMENTSTART + "(.+?)" + Types.DMSCOMMENTEND, "")
				.replaceAll("(?s)" + Types.DMSCOMMENTSTARTOLD + "(.+?)" + Types.DMSCOMMENTENDOLD, "")
				.replaceAll("\r", "");
	}

	private String removeDBConf(String dmsScript) {
		return dmsScript.replaceAll("(?s)" + DMSDBCONFSTART + "(.+?)" + DMSDBCONFEND, "");
	}

	public String getRandomString(int width) {
		String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ";
		try {
			Random rand = SecureRandom.getInstanceStrong();
			StringBuilder result = new StringBuilder();
			while (width > 0) {
				result.append(characters.charAt(rand.nextInt(characters.length())));
				width--;
			}
			return result.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	private Map<String, String> dbConfigurations(String dmsScript) {
		if (dmsScript.contains(DMSDBCONFSTART) && dmsScript.contains(DMSDBCONFEND)) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(400, "INFO: DB Configuration found...");
			} else
				lo.info("INFO: DB Configuration found...");
			Map<String, String> hm = new HashMap<>();
			List<String> confs = UtilityFunctions.getInBetweenFast(dmsScript, DMSDBCONFSTART, DMSDBCONFEND, true);
			for (String s : confs) {
				hm.put(UtilityFunctions.getInBetweenFast(s, "<dms-confname>", "</dms-confname>", true).get(0)
						.trim(),
						UtilityFunctions.getInBetweenFast(s, "<dms-conf>", "</dms-conf>", true).get(0).trim());
			}
			for (Map.Entry<String, String> entry : hm.entrySet()) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(400, "INFO: DB conf : " + entry.getKey() + "........." + entry.getValue());
				} else
					lo.info("INFO: DB conf : " + entry.getKey() + "........." + entry.getValue());
			}
			return hm;
		} else
			return null;

	}

	public String getVFFromGlobalVirtualFileParam(String vfname) {
		if (vfname.length() > 0) {
			if (GlobalVirtualFileParameters.getInstance().getVFP().hasVF(vfname)) {
				List<String> ls = GlobalVirtualFileParameters.getInstance().getVFP().getVirtualFiles(vfname);
				StringBuilder sb = new StringBuilder();
				for (String s : ls)
					sb.append(s + '\n');
				return sb.toString();
			} else
				return null;
		} else {
			List<String> names = GlobalVirtualFileParameters.getInstance().getVFP().getVirtualFileNames();
			StringBuilder sb = new StringBuilder();
			sb.append("<dmsv-virtualfile-names>\n");
			for (String s : names)
				sb.append("<file>" + s + "</file>\n");
			sb.append("</dmsv-virtualfile-names>");
			return sb.toString();
		}
	}

	public boolean removeVFFromGlobalVirtualFileParam(String vfname) {
		return GlobalVirtualFileParameters.getInstance().deleteVF(vfname);
	}

	public String createGlobalVirtualFileParam(String vfdata, boolean overwriteExisting) {
		VirtualFileParam vfp = processVirtualFileParam(vfdata, true);
		StringBuilder sb = new StringBuilder();
		if (vfp != null) {
			List<String> names = vfp.getVirtualFileNames();

			for (String name : names) {
				if (GlobalVirtualFileParameters.getInstance().getVFP().hasVF(name) && !overwriteExisting)
					sb.append("Processing: " + name + ".Global Virtual File found. Not overwriting.\n");
				else if (GlobalVirtualFileParameters.getInstance().getVFP().hasVF(name) && overwriteExisting) {
					if (GlobalVirtualFileParameters.getInstance().setVFP(vfp))
						sb.append("Processing: " + name + ". Global Virtual File found. Overwriting the file.\n");
					else
						sb.append("Processing: " + name
								+ ". Global Virtual File found. Error occurred while processing file.\n");
				} else {
					if (GlobalVirtualFileParameters.getInstance().setVFP(vfp))
						sb.append("Processing: " + name
								+ ". New Global Virtual File definition found. Creating new file.\n");
					else
						sb.append(
								"Processing: " + name + ". Error occurred while processing new Global Virtual File.\n");
				}
			}

		} else
			sb.append("No valid Global Virtual File Data found");

		return sb.toString();
	}

	public List<String> processSqliteTable(String dmsScript) {

		String origScript = new String(dmsScript);

		List<String> toReturn = null;
		if (dmsScript.contains("<sqlite-table-data-filepath>") && dmsScript.contains("</sqlite-table-data-filepath>")) {
			dmsScript = FileOperation.getFileContentAsString(UtilityFunctions
					.getInBetweenFast(dmsScript, "<sqlite-table-data-filepath>", "</sqlite-table-data-filepath>", true)
					.get(0).trim());
		}
		if (dmsScript.contains("<dms-sqlite-table>") && dmsScript.contains("</dms-sqlite-table>")) {

			if (!dmsScript.contains("<dms-tablename>") || !dmsScript.contains("</dms-tablename>")
					|| !dmsScript.contains("<dms-delimiter>") || !dmsScript.contains("</dms-delimiter>")
					|| !dmsScript.contains("<dms-columnnames>") || !dmsScript.contains("</dms-columnnames>")
					|| !dmsScript.contains("<dms-tabledata>") || !dmsScript.contains("</dms-tabledata>")) {
				String msg = "ERROR: Improper sqlite table definition found. Table definitions should have <dms-tablename>, <dms-delimiter>, <dms-columnnames> and <dms-tabledata> ...\n";
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, msg);
				} else
					lo.error(msg);
				return null;
			} else {
				toReturn = new ArrayList<>();
				List<String> dbDetails = UtilityFunctions.getInBetweenFast(dmsScript, "<dms-sqlite-table>",
						"</dms-sqlite-table>", true);

				for (String s : dbDetails) {
					dmsScript = s.trim();
					String db = Types.DBURL;
					String delim = UtilityFunctions
							.getInBetweenFast(dmsScript, "<dms-delimiter>", "</dms-delimiter>", true).get(0).trim();
					String tablename = UtilityFunctions
							.getInBetweenFast(dmsScript, "<dms-tablename>", "</dms-tablename>", true).get(0).trim();
					String newTableName = tablename + getRandomString(10);
					origScript = origScript.replaceAll(tablename, newTableName);
					List<String> columnnames = StringOps.fastSplit(UtilityFunctions
							.getInBetweenFast(dmsScript, "<dms-columnnames>", "</dms-columnnames>", true).get(0).trim(),
							delim);

					StringBuilder sb = new StringBuilder();

					sb.append("CREATE TABLE IF NOT EXISTS ");
					sb.append(newTableName);
					sb.append("( ");
					for (String col : columnnames) {
						sb.append(col);
						sb.append(" TEXT NOT NULL, ");
					}
					sb.append(")");
					String query = sb.toString().replace(", )", ")");
					String msg = "INFO:" + query;
					if (Types.getInstance().getIsUI()) {
						DULogger.log(400, msg);
					} else
						lo.info(msg);
					SQLite.ddlQuery(db, query);

					List<String> datarows = StringOps.fastSplit(
							UtilityFunctions.getInBetweenFast(dmsScript, "<dms-tabledata>", "</dms-tabledata>", true)
									.get(0).trim().replace("\r", ""),
							"\n");

					sb = new StringBuilder();
					sb.append("INSERT INTO ");
					sb.append(newTableName);
					sb.append(" VALUES (");
					for (int i = 0; i < columnnames.size(); i++) {
						sb.append("?, ");
					}
					query = sb.toString().trim();
					query = query.substring(0, query.length() - 1) + ")";
					msg = "INFO:" + query;
					if (Types.getInstance().getIsUI()) {
						DULogger.log(400, msg);
					} else
						lo.info(msg);
					for (String datarow : datarows) {
						List<String> data = StringOps.fastSplit(datarow, delim);
						SQLite.dmlQuery(db, query, data.toArray(new String[0]));
					}

					sb = new StringBuilder();
					sb.append(db);
					sb.append("<DMSDELIM>");
					sb.append("DROP TABLE ");
					sb.append(newTableName);
					toReturn.add(sb.toString());
				}
				toReturn.add(origScript);
				return toReturn;
			}
		} else
			return null;

	}

	public VirtualFileParam processVirtualFileParam(String dmsScript, boolean gvf) {
		String dmsvfs;
		String dmsvfe;
		String dmsvirtualfilestart;
		String dmsvirtualfileend;

		if (gvf) {
			dmsvfs = "<dms-g-vf>";
			dmsvirtualfilestart = "<dms-global-virtualfile>";
			dmsvirtualfileend = "</dms-global-virtualfile>";
			dmsvfe = "</dms-g-vf>";
		} else {
			dmsvfs = "<dms-vf>";
			dmsvfe = "</dms-vf>";
			dmsvirtualfilestart = "<dms-virtualfile>";
			dmsvirtualfileend = "</dms-virtualfile>";
		}

		if ((dmsScript.toLowerCase().contains(dmsvirtualfilestart) || dmsScript.toLowerCase().contains(dmsvfs))
				&& (dmsScript.toLowerCase().contains(dmsvirtualfileend) || dmsScript.toLowerCase().contains(dmsvfe))) {
			VirtualFileParam vfp = new VirtualFileParam();

			List<String> virtualfiledata;
			if (dmsScript.contains(dmsvfs))
				virtualfiledata = UtilityFunctions.getInBetweenFast(dmsScript, dmsvfs, dmsvfe, true);
			else
				virtualfiledata = UtilityFunctions.getInBetweenFast(dmsScript, dmsvirtualfilestart, dmsvirtualfileend,
						true);

			for (String s : virtualfiledata) {
				String name = "";
				String data = "";

				if (s.contains("dms-vfname") && s.contains("dms-vfdata")) {
					name = UtilityFunctions.getInBetweenFast(s, "<dms-vfname>", "</dms-vfname>", true).get(0);
					data = UtilityFunctions.getInBetweenFast(s, "<dms-vfdata>", "</dms-vfdata>", true).get(0);
				} else {
					name = UtilityFunctions.getInBetweenFast(s, "<dms-virtualfilename>", "</dms-virtualfilename>", true)
							.get(0);
					data = UtilityFunctions.getInBetweenFast(s, "<dms-virtualfiledata>", "</dms-virtualfiledata>", true)
							.get(0);
				}
				if (data != null && name != null) {
					data = data.replace("\r", "");
					List<String> dataList = StringOps.fastSplit(data, "\n");
					vfp.setIndex(name);
					for (String x : dataList)
						if (x.length() > 0 && !x.startsWith("#"))
							vfp.getVirtualFiles(name).add(x);
				} else
					return null;
			}
			return vfp;
		} else
			return null;
	}

	public List<String> generateData(String dmsScript, String numOfFiles) {
		dmsScript = removeCommentsFromDMS(dmsScript);
		List<String> toReturn = new ArrayList<>();
		List<String> tableParse = processSqliteTable(dmsScript);
		if (tableParse != null) {
			dmsScript = tableParse.get(tableParse.size() - 1);
			tableParse.remove(tableParse.size() - 1);
		}
		Map<String, String> hm = dbConfigurations(dmsScript);
		if (hm != null)
			dmsScript = removeDBConf(dmsScript);
		VirtualFileParam vfp = processVirtualFileParam(dmsScript, false);
		
		toReturn.add(new Processor(vfp, logForApi, hm).generateData(dmsScript, numOfFiles));
		StringBuilder sb = new StringBuilder();
		sb.append("<dms-log>\n");
		for (String s : logForApi)
			sb.append(s + "\n");
		sb.append("</dms-log>\n");
		toReturn.add(sb.toString());
		if (tableParse != null) {
			for (String s : tableParse) {
				List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
				SQLite.ddlQuery(drop.get(0), drop.get(1));
			}
		}
		return toReturn;
	}

	public void generateData(String configString, boolean deleteExistingFiles, boolean deleteAllExistingFiles,
			List<String> fileLevelReplacements, boolean fileLevelReplacement, VirtualFileParam vfp) {
		generateData(configString, deleteExistingFiles, deleteAllExistingFiles, fileLevelReplacements,
				fileLevelReplacement, "", vfp);
	}

	public void generateData(String str, String numberOfFiles, String path, String fileExtension) {
		str = removeCommentsFromDMS(str);
		VirtualFileParam vfp = processVirtualFileParam(str, false);
		List<String> tableParse = processSqliteTable(str);
		if (tableParse != null) {
			str = tableParse.get(tableParse.size() - 1);
			tableParse.remove(tableParse.size() - 1);
		}
		Map<String, String> hm = dbConfigurations(str);
		if (hm != null)
			str = removeDBConf(str);
		
		new Processor(vfp, hm).generateData(str, numberOfFiles, path, fileExtension);
		if (tableParse != null) {
			for (String s : tableParse) {
				List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
				SQLite.ddlQuery(drop.get(0), drop.get(1));
			}
		}
	}

	public void generateData(String configString, boolean deleteExistingFiles, boolean deleteAllExistingFiles,
			List<String> fileLevelReplacements, boolean fileLevelReplacement, String fileNameAdd,
			VirtualFileParam vfp) {
		List<String> values = StringOps.fastSplit(configString, Types.DMSDELIM);
		String str = "";
		if (values.size() < 3) {
			StringBuilder sb = new StringBuilder();
			sb.append("Printing passed values:\n");
			for (String s : values)
				sb.append(s + "\n");
			sb.append("Value print complete\n");
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200,
						"ERROR: Improper config element, at least 3 parameters are required... DMS file path/name, generated file location and number of files...\n"
								+ sb.toString());
			} else
				lo.error(
						"ERROR: Improper config element, at least 3 parameters are required... DMS file path/name, generated file location and number of files...\n"
								+ sb.toString());
		} else {
			String path = values.get(1);
			String numberOfFiles = values.get(2);
			String fileExtension = "txt";
			if (values.size() == 3) {
				str = FileOperation.getFileContentAsString(values.get(0));
			} else if (values.size() == 4) {
				str = FileOperation.getFileContentAsString(values.get(0));
				fileExtension = values.get(3);
			} else if (values.size() == 6) {
				str = FileOperation.getFileContentAsString(values.get(0), values.get(4), values.get(5),
						Types.DMSSUBDELIM);
				fileExtension = values.get(3);
			} else {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, "ERROR: Improper config element, number of parameters should be 3 or 4 or 6...");
				} else
					lo.error("ERROR: Improper config element, number of parameters should be 3 or 4 or 6...");
			}

			if (fileNameAdd.length() > 0) {
				fileExtension = fileNameAdd + "." + fileExtension;
			}
			for (String configelement : values) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(400, "CONFIG: " + configelement);
				} else
					lo.info("CONFIG: " + configelement);
			}
			if (str != null) {
				str = removeCommentsFromDMS(str);
				File f = new File(path);
				if (!f.isDirectory()) {
					if (Types.getInstance().getIsUI()) {
						DULogger.log(300, "WARNING: Specified directory not found : " + configString);
						DULogger.log(400, "INFO: Creating directory... : " + f.mkdirs());
					} else {
						lo.warn("WARNING: Specified directory not found : " + configString);
						lo.info("INFO: Creating directory... : " + f.mkdirs());
					}
				}
				if (deleteExistingFiles) {
					if (Types.getInstance().getIsUI()) {
						DULogger.log(400,
								"INFO: Deleting existing files with extension " + fileExtension + " @ " + new Date());
					} else
						lo.info("INFO: Deleting existing files with extension " + fileExtension + " @ " + new Date());
					FileOperation.deleteFile(path, fileExtension);
				}
				if (deleteAllExistingFiles) {
					if (Types.getInstance().getIsUI()) {
						DULogger.log(400, "INFO: Deleting all existing files at..." + path + " @ " + new Date());
					} else
						lo.info("INFO: Deleting all existing files at..." + path + " @ " + new Date());
					FileOperation.deleteFile(path, "");
				}
				
				List<String> tableParse = processSqliteTable(str);
				if (tableParse != null) {
					str = tableParse.get(tableParse.size() - 1);
					tableParse.remove(tableParse.size() - 1);
				}
				
				Map<String, String> hm = dbConfigurations(str);
				if (hm != null)
					str = removeDBConf(str);
				VirtualFileParam vfpx = processVirtualFileParam(str, false);
				
				if (vfpx != null) {
					if (vfp != null)
						vfp.mergeVirtualFileParam(vfpx);
					else
						vfp = vfpx;
				}

				final String strx = str;
				final String fileExtensionx = fileExtension;
				final List<String> fileLevelReplacementsx = fileLevelReplacements;
				final boolean fileLevelReplacementx = fileLevelReplacement;

				if (!fileLevelReplacementx) {
					new Processor(vfp, hm).generateData(strx, numberOfFiles, path, fileExtensionx);
				} else {
					new Processor(vfp, hm).generateData(strx, numberOfFiles, path, fileExtensionx,
							fileLevelReplacementsx);

				}
				if (tableParse != null) {
					for (String s : tableParse) {
						List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
						SQLite.ddlQuery(drop.get(0), drop.get(1));
					}
				}
			} else {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, "ERROR: DMS file could not be read... @ " + new Date());
				} else
					lo.error("ERROR: DMS file could not be read... @ " + new Date());

			}

		}

	}

	public void generateData(String path, String generateIn, String numberOfFiles, String fileExtension,
			boolean deleteExistingFilesGenerateIn, String replaceThese, String replaceWith) {
		String str = "";
		if (fileExtension.length() == 0) {
			fileExtension = "txt";
		}
		if (replaceThese.length() == 0) {
			str = FileOperation.getFileContentAsString(path);
		} else {
			str = FileOperation.getFileContentAsString(path, replaceThese, replaceWith, Types.DMSDELIM);
		}
		str = removeCommentsFromDMS(str);
		File f = new File(generateIn);
		if (!f.isDirectory()) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(300, "WARNING: Specified directory not found : " + generateIn);
				DULogger.log(400, "INFO: Creating directory... : " + f.mkdirs());
			} else {
				lo.warn("WARNING: Specified directory not found : " + generateIn);
				lo.info("INFO: Creating directory... : " + f.mkdirs());
			}
		}
		if (deleteExistingFilesGenerateIn) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(400, "INFO: Deleting all existing files at..." + generateIn + " @ " + new Date());
			} else
				lo.info("INFO: Deleting all existing files at..." + generateIn + " @ " + new Date());
			FileOperation.deleteFile(generateIn, "");
		}
		List<String> tableParse = processSqliteTable(str);
		if (tableParse != null) {
			str = tableParse.get(tableParse.size() - 1);
			tableParse.remove(tableParse.size() - 1);
		}
		Map<String, String> hm = dbConfigurations(str);
		if (hm != null)
			str = removeDBConf(str);
		VirtualFileParam vfp = processVirtualFileParam(str, false);
		
		new Processor(vfp, hm).generateData(str, numberOfFiles, generateIn, fileExtension);
		if (tableParse != null) {
			for (String s : tableParse) {
				List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
				SQLite.ddlQuery(drop.get(0), drop.get(1));
			}
		}
	}

	// extract transform on many or single files, to generate output files
	public void processExtractTransform(String generateFileLocation, String extractInput, String dmsScript,
			String extractMapperFile, String extractParamDir, String fileExtension, boolean isDMSScriptDataOrFile) {
		if (generateFileLocation.length() != 0 && extractInput.length() != 0 && dmsScript.length() != 0) {

			File f = new File(extractInput);
			String str = null;
			final List<String> mapperContent = new ArrayList<>();
			if (isDMSScriptDataOrFile)
				str = removeCommentsFromDMS(dmsScript);
			else
				str = removeCommentsFromDMS(FileOperation.getFileContentAsString(dmsScript));
			if (extractMapperFile.length() == 0)
				mapperContent.addAll(UtilityFunctions.getLineInBetween(str, Types.DMSEXTRACTMAPPERSTART,
						Types.DMSEXTRACTMAPPEREND, true, true));
			else {
				String mapperstr = removeCommentsFromDMS(FileOperation.getContentAsString(extractMapperFile, "utf8"));
				mapperContent.addAll(UtilityFunctions.getLineInBetween(mapperstr, Types.DMSEXTRACTMAPPERSTART,
						Types.DMSEXTRACTMAPPEREND, true, true));
			}
			List<String> tableParse = processSqliteTable(str);
			String dmsScriptx;
			if (tableParse != null) {
				dmsScriptx = tableParse.get(tableParse.size() - 1);
				tableParse.remove(tableParse.size() - 1);
			} else
				dmsScriptx = str;
			Map<String, String> hm = dbConfigurations(str);
			if (hm != null)
				str = removeDBConf(str);

			if (f.isDirectory()) {
				List<Integer> count = new ArrayList<>();
				count.add(Integer.valueOf(0));
				String strx = str;
				try (Stream<Path> filePathStream = Files.walk(Paths.get(extractInput))) {
					filePathStream.forEach(filePath -> {
						if (filePath.toFile().exists() && !filePath.toFile().isDirectory()) {
							VirtualFileParam vfp = new HierarchyDelimitedFile(logForApi).extract(filePath.toString(),
									mapperContent, false, null);
							if (extractParamDir != null && extractParamDir.length() > 0)
								vfp.printToFile(filePath.toString(), extractParamDir);
							String fileExtensionx = count.get(0).toString() + "." + fileExtension;
							VirtualFileParam vfpx = processVirtualFileParam(strx, false);
							
							if (vfpx != null)
								vfp.mergeVirtualFileParam(vfpx);

							new Processor(vfp, hm).generateData(dmsScriptx, "1", generateFileLocation, fileExtensionx);
							count.add(count.get(0) + 1);
							count.remove(0);
							if (tableParse != null) {
								for (String s : tableParse) {
									List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
									SQLite.ddlQuery(drop.get(0), drop.get(1));
								}
							}
						}
					});
				} catch (IOException e) {
					String msg = "ERROR: File not found during extractor directory parsing.\n";
					if (Types.getInstance().getIsUI()) {
						DULogger.log(200, msg + LogStackTrace.get(e));
					} else
						lo.error(msg + LogStackTrace.get(e));
				}
			} else {
				VirtualFileParam vfp = new HierarchyDelimitedFile(logForApi).extract(extractInput, mapperContent, false,
						null);
				new Processor(vfp, hm).generateData(str, "1", generateFileLocation, fileExtension);
			}
		} else {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200,
						"Error: Generate In folder, Input for extraction, DMS script and extract type are all mandatory parameters...");
			} else
				lo.error(
						"Error: Generate In folder, Input for extraction, DMS script and extract type are all mandatory parameters...");
		}
	}

	public List<String> getVirtualResponseWithLog(String extractInputFile, String dmsScript, String extractMapperFile,
			boolean isInputDataOrFile, boolean isDMSScriptDataOrFile) {
		List<String> toReturn = new ArrayList<>();
		String str = null;

		if (extractInputFile != null && extractMapperFile != null && dmsScript != null) {

			List<String> mapperContent = new ArrayList<>();

			if (isDMSScriptDataOrFile)
				str = removeCommentsFromDMS(dmsScript);
			else
				str = removeCommentsFromDMS(FileOperation.getFileContentAsString(dmsScript));
			if (extractMapperFile.length() == 0)
				mapperContent.addAll(UtilityFunctions.getLineInBetween(str, Types.DMSEXTRACTMAPPERSTART,
						Types.DMSEXTRACTMAPPEREND, true, true));
			else {
				String mapperstr = removeCommentsFromDMS(FileOperation.getContentAsString(extractMapperFile, "utf8"));
				mapperContent.addAll(UtilityFunctions.getLineInBetween(mapperstr, Types.DMSEXTRACTMAPPERSTART,
						Types.DMSEXTRACTMAPPEREND, true, true));
			}
			try {
				VirtualFileParam vfp = null;
				if (!mapperContent.isEmpty()) {
					vfp = new HierarchyDelimitedFile().extract(extractInputFile, mapperContent, isInputDataOrFile,
							null);
				} else {
					String msg = "WARNING: Extract mapper script could not be extracted from DMS Script. Mapper content should exist within "
							+ Types.DMSEXTRACTMAPPERSTART + " and " + Types.DMSEXTRACTMAPPEREND;
					if (Types.getInstance().getIsUI()) {
						DULogger.log(300, msg);
					} else
						lo.warn(msg);
				}

				List<String> tableParse = processSqliteTable(dmsScript);
				if (tableParse != null) {
					dmsScript = tableParse.get(tableParse.size() - 1);
					tableParse.remove(tableParse.size() - 1);
				}
				
				Map<String, String> hm = dbConfigurations(str);
				if (hm != null)
					str = removeDBConf(str);
				VirtualFileParam vfpx = processVirtualFileParam(str, false);
				
				if (vfpx != null) {
					if (vfp != null)
						vfp.mergeVirtualFileParam(vfpx);
					else
						vfp = vfpx;
				}
				toReturn.add(new Processor(vfp, logForApi, hm).generateDatax(str));
				StringBuilder sb = new StringBuilder();
				for (String s : logForApi)
					sb.append(s + "\n");
				toReturn.add(sb.toString());
				if (tableParse != null) {
					for (String s : tableParse) {
						List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
						SQLite.ddlQuery(drop.get(0), drop.get(1));
					}
				}
			} catch (Exception e) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, "ERROR: Error occurred.\n" + LogStackTrace.get(e));
				} else
					lo.error("ERROR: Error occurred.\n" + LogStackTrace.get(e));
			}
		} else {
			toReturn.add("ERROR: Some or all of the essential input parameters to getVirtualResponse method are null");
		}
		return toReturn;
	}

	// 1 transform at a time, good for use in APIs
	public String getVirtualResponse(String extractInputFile, String dmsScript, String extractMapperFile,
			boolean isInputDataOrFile, boolean isDMSScriptDataOrFile, String externalParams) {
		String str = null;
		String toReturn = null;
		if (extractInputFile != null && extractMapperFile != null && dmsScript != null) {

			List<String> mapperContent = new ArrayList<>();

			if (isDMSScriptDataOrFile)
				str = removeCommentsFromDMS(dmsScript);
			else
				str = removeCommentsFromDMS(FileOperation.getFileContentAsString(dmsScript));
			if (extractMapperFile.length() == 0)
				mapperContent.addAll(UtilityFunctions.getLineInBetween(str, Types.DMSEXTRACTMAPPERSTART,
						Types.DMSEXTRACTMAPPEREND, true, true));
			else {
				String mapperstr = removeCommentsFromDMS(FileOperation.getContentAsString(extractMapperFile, "utf8"));
				mapperContent.addAll(UtilityFunctions.getLineInBetween(mapperstr, Types.DMSEXTRACTMAPPERSTART,
						Types.DMSEXTRACTMAPPEREND, true, true));
			}
			VirtualFileParam vfp = null;
			try {
				if (!mapperContent.isEmpty()) {
					vfp = new HierarchyDelimitedFile().extract(extractInputFile, mapperContent, isInputDataOrFile,
							null);
				} else {
					String msg = "WARNING: Extract mapper script could not be extracted from DMS Script. Mapper content should exist within "
							+ Types.DMSEXTRACTMAPPERSTART + " and " + Types.DMSEXTRACTMAPPEREND;
					if (Types.getInstance().getIsUI()) {
						DULogger.log(300, msg);
					} else
						lo.warn(msg);
				}
				List<String> tableParse = processSqliteTable(dmsScript);
				if (tableParse != null) {
					dmsScript = tableParse.get(tableParse.size() - 1);
					tableParse.remove(tableParse.size() - 1);
				}
				
				Map<String, String> hm = dbConfigurations(str);
				if (hm != null)
					str = removeDBConf(str);
				VirtualFileParam vfpx = processVirtualFileParam(str, false);
				
				if (vfpx != null) {
					if (vfp != null)
						vfp.mergeVirtualFileParam(vfpx);
					else
						vfp = vfpx;
				}
				if (externalParams!=null && !externalParams.isEmpty())
				{
					String externalParamVFP = StringOps.append("<dms-vf>\n<dms-vfname>externalParam.txt</dms-vfname>\n<dms-vfdata>\n",externalParams,"\n</dms-vfdata>\n</dms-vf>");
					vfpx = processVirtualFileParam(externalParamVFP, false);
					if (vfpx != null) {
						if (vfp != null)
							vfp.mergeVirtualFileParam(vfpx);
						else
							vfp = vfpx;
					}
				}
				toReturn = new Processor(vfp, hm).generateDatax(str);
				if (tableParse != null) {
					for (String s : tableParse) {
						List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
						SQLite.ddlQuery(drop.get(0), drop.get(1));
					}
				}
			} catch (Exception e) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, "ERROR: Error occurred.\n" + LogStackTrace.get(e));
				} else
					lo.error("ERROR: Error occurred.\n" + LogStackTrace.get(e));
			}

		} else {
			String msg = "ERROR: Some or all of the essential input parameters to getVirtualResponse method are null.\n";
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, msg);
			} else
				lo.error(msg);
		}
		return toReturn;
	}
	public String getVirtualResponse(String extractInputFile, String dmsScript, String extractMapperFile,
			boolean isInputDataOrFile, boolean isDMSScriptDataOrFile) {
		String str = null;
		String toReturn = null;
		if (extractInputFile != null && extractMapperFile != null && dmsScript != null) {

			List<String> mapperContent = new ArrayList<>();

			if (isDMSScriptDataOrFile)
				str = removeCommentsFromDMS(dmsScript);
			else
				str = removeCommentsFromDMS(FileOperation.getFileContentAsString(dmsScript));
			if (extractMapperFile.length() == 0)
				mapperContent.addAll(UtilityFunctions.getLineInBetween(str, Types.DMSEXTRACTMAPPERSTART,
						Types.DMSEXTRACTMAPPEREND, true, true));
			else {
				String mapperstr = removeCommentsFromDMS(FileOperation.getContentAsString(extractMapperFile, "utf8"));
				mapperContent.addAll(UtilityFunctions.getLineInBetween(mapperstr, Types.DMSEXTRACTMAPPERSTART,
						Types.DMSEXTRACTMAPPEREND, true, true));
			}
			VirtualFileParam vfp = null;
			try {
				if (!mapperContent.isEmpty()) {
					vfp = new HierarchyDelimitedFile().extract(extractInputFile, mapperContent, isInputDataOrFile,
							null);
				} else {
					String msg = "WARNING: Extract mapper script could not be extracted from DMS Script. Mapper content should exist within "
							+ Types.DMSEXTRACTMAPPERSTART + " and " + Types.DMSEXTRACTMAPPEREND;
					if (Types.getInstance().getIsUI()) {
						DULogger.log(300, msg);
					} else
						lo.warn(msg);
				}
				
				List<String> tableParse = processSqliteTable(dmsScript);
				if (tableParse != null) {
					dmsScript = tableParse.get(tableParse.size() - 1);
					tableParse.remove(tableParse.size() - 1);
				}
				
				Map<String, String> hm = dbConfigurations(str);
				if (hm != null)
					str = removeDBConf(str);
				VirtualFileParam vfpx = processVirtualFileParam(str, false);

				if (vfpx != null) {
					if (vfp != null)
						vfp.mergeVirtualFileParam(vfpx);
					else
						vfp = vfpx;
				}
				
				toReturn = new Processor(vfp, hm).generateDatax(str);
				if (tableParse != null) {
					for (String s : tableParse) {
						List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
						SQLite.ddlQuery(drop.get(0), drop.get(1));
					}
				}
			} catch (Exception e) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, "ERROR: Error occurred.\n" + LogStackTrace.get(e));
				} else
					lo.error("ERROR: Error occurred.\n" + LogStackTrace.get(e));
			}

		} else {
			String msg = "ERROR: Some or all of the essential input parameters to getVirtualResponse method are null.\n";
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, msg);
			} else
				lo.error(msg);
		}
		return toReturn;
	}

	public String checkSize(String str, boolean printOnCheck) {
		str = removeCommentsFromDMS(str);
		
		List<String> tableParse = processSqliteTable(str);
		if (tableParse != null) {
			str = tableParse.get(tableParse.size() - 1);
			tableParse.remove(tableParse.size() - 1);
		}
		
		Map<String, String> hm = dbConfigurations(str);
		if (hm != null)
			str = removeDBConf(str);
		
		VirtualFileParam vfp = processVirtualFileParam(str, false);
		String toReturn = new Processor(vfp, hm).checkSize(str);
		if (tableParse != null) {
			for (String s : tableParse) {
				List<String> drop = StringOps.fastSplit(s, "<DMSDELIM>");
				SQLite.ddlQuery(drop.get(0), drop.get(1));
			}
		}
		return toReturn;
	}

	// run extracts on input string, file, directory and write parameter on UI or to
	// files
	public void extractWithMapper(String extractInputFile, String extractMapperFile, String extractParamDir,
			List<String> extractFileReplacements, boolean isInputDataOrFile, boolean isMapperFileOrData) {

		final List<String> mapperContent = new ArrayList<>();

		if (isMapperFileOrData) {
			String mapperstr = removeCommentsFromDMS(FileOperation.getContentAsString(extractMapperFile, "utf8"));
			mapperContent.addAll(UtilityFunctions.getLineInBetween(mapperstr, Types.DMSEXTRACTMAPPERSTART,
					Types.DMSEXTRACTMAPPEREND, true, true));
		} else {
			String mapperstr = removeCommentsFromDMS(extractMapperFile);
			mapperContent.addAll(UtilityFunctions.getLineInBetween(mapperstr, Types.DMSEXTRACTMAPPERSTART,
					Types.DMSEXTRACTMAPPEREND, true, true));
		}
		File f = new File(extractInputFile);
		if (f.isDirectory()) {
			try (Stream<Path> filePathStream = Files.walk(Paths.get(extractInputFile))) {
				filePathStream.forEach(filePath -> {
					if (filePath.toFile().exists()) {
						VirtualFileParam vfp = new HierarchyDelimitedFile(logForApi).extract(filePath.toString(),
								mapperContent, false, extractFileReplacements);
						if (extractParamDir != null && extractParamDir.length() > 0)
							vfp.printToFile(filePath.toString(), extractParamDir);
						else
							vfp.print();
					}
				});
			} catch (IOException e) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200,
							"ERROR: File not found during extractor directory parsing.\n" + LogStackTrace.get(e));
				} else
					lo.error("ERROR: File not found during extractor directory parsing.\n" + LogStackTrace.get(e));
			} catch (Exception e) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, "ERROR: Error occurred.\n" + LogStackTrace.get(e));
				} else
					lo.error("ERROR: Error occurred.\n" + LogStackTrace.get(e));
			}
		} else {
			try {
				VirtualFileParam vfp = new HierarchyDelimitedFile(logForApi).extract(extractInputFile, mapperContent,
						isInputDataOrFile, extractFileReplacements);
				if (extractParamDir != null && extractParamDir.length() > 0)
					vfp.printToFile("", extractParamDir);
				else
					vfp.print();
			} catch (Exception e) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, "ERROR: Error occurred.\n" + LogStackTrace.get(e));
				} else
					lo.error("ERROR: Error occurred.\n" + LogStackTrace.get(e));
			}
		}
	}

	public String extractWithMapper(String extractInput, String extractMapper, List<String> extractFileReplacements) {

		final List<String> mapperContent = new ArrayList<>();
		String mapperstr = removeCommentsFromDMS(extractMapper);
		mapperContent.addAll(UtilityFunctions.getLineInBetween(mapperstr, Types.DMSEXTRACTMAPPERSTART,
				Types.DMSEXTRACTMAPPEREND, true, true));

		VirtualFileParam vfp = new HierarchyDelimitedFile(logForApi).extract(extractInput, mapperContent, true,
				extractFileReplacements);
		vfp.print();
		StringBuilder sb = new StringBuilder();
		for (String s : vfp.getLog())
			sb.append(s + "\n");
		return sb.toString();
	}
}

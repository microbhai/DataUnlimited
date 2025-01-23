package akhil.DataUnlimited;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.dataextractor.hierarchicaldoc.HierarchyDelimitedFile;
import akhil.DataUnlimited.model.parameter.VirtualFileParam;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.GzipUtil;
import akhil.DataUnlimited.util.LogStackTrace;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.StringOps;

public class DataUnlimitedCLIProcessor {
	private static final Logger lo = LogManager.getLogger(DataUnlimitedCLIProcessor.class.getName());
	private static final String DATALIB = "-datalib";
	private static final String ENCODE = "-encode";
	private static final String EXTRACTINPUT = "-extractinput";
	private static final String ENCDDELSRC = "-encodedelsrc";
	private static final String PARALLEL = "-parallel";
	private static final String EXTREPLACE = "-extractreplace";
	private static final String EXTTYPE = "hierarchydelimited";
	private static final String EXTRACTTYPE = "-extracttype";
	private static final String EXTRACTPARAMDIR = "-extractparamdir";
	private static final String EXTRACTMAPPER = "-extractmapper";

	public static void runCoreDMS(List<String> param) {

		if (param.contains("-f") || param.contains(ENCODE) || param.contains(ENCDDELSRC) || param.contains("-ls")
				|| param.contains(EXTRACTINPUT)) {
			String configFileName = "";
			String listFile = "";
			String encodingConfigFileName = "";

			String extractInputFile = null;
			String extractMapperFile = null;
			String extractParamDir = null;
			String extractType = null;

			int parallelLimit = 5;

			boolean extractFileReplacement = false;
			boolean deleteExistingFiles = false;
			boolean deleteAllExistingFiles = false;
			boolean encodingTBD = false;
			boolean deleteSrcDecodeFiles = false;
			boolean paramConfigReplacement = false;
			boolean fileLevelReplacement = false;
			boolean useConfigFile = false;

			List<String> fileLevelReplacements = null;
			List<String> paramReplacements = null;
			List<String> extractFileReplacements = null;

			if (param.contains("-delete")) {
				deleteExistingFiles = true;
				param.remove("-delete");
			}

			if (param.contains("-deleteall")) {
				deleteAllExistingFiles = true;
				param.remove("-deleteall");
			}
			if (deleteAllExistingFiles && deleteExistingFiles) {
				lo.info("INFO: Both -deleteall and -delete are specified in the parameters, so all files will be deleted irrespective of the file extension filter");
				System.out.println(
						"INFO: Both -deleteall and -delete are specified in the parameters, so all files will be deleted irrespective of the file extension filter");

				deleteExistingFiles = false;
			}
			if (param.contains("-ls")) {
				listFile = param.get(param.indexOf("-ls") + 1);
				param.remove(param.indexOf("-ls") + 1);
				param.remove("-ls");
			}
			if (param.contains(PARALLEL)) {
				parallelLimit = Integer.parseInt(param.get(param.indexOf(PARALLEL) + 1));
				param.remove(param.indexOf(PARALLEL) + 1);
				param.remove(PARALLEL);
			}
			if (param.contains(EXTRACTINPUT)) {
				extractInputFile = param.get(param.indexOf(EXTRACTINPUT) + 1);
				param.remove(param.indexOf(EXTRACTINPUT) + 1);
				param.remove(EXTRACTINPUT);
			} else {
				if (param.contains(EXTREPLACE)) {
					lo.error("ERROR: Invalid " + EXTREPLACE + " parameter in absence of -extractinput ");
					System.out.println("ERROR: Invalid " + EXTREPLACE + " parameter in absence of -extractinput ");
				}

				if (param.contains(EXTRACTTYPE)) {
					param.remove(param.indexOf(EXTRACTTYPE) + 1);
					param.remove(EXTRACTTYPE);
					lo.error("ERROR: Invalid " + EXTRACTTYPE + " parameter in absence of -extractinput ");
					System.out.println("ERROR: Invalid " + EXTRACTTYPE + " parameter in absence of -extractinput ");
				}

				if (param.contains(EXTRACTPARAMDIR)) {
					param.remove(param.indexOf(EXTRACTPARAMDIR) + 1);
					param.remove(EXTRACTPARAMDIR);
					lo.error("ERROR: Invalid " + EXTRACTPARAMDIR + " parameter in absence of -extractinput ");
					System.out.println("ERROR: Invalid " + EXTRACTPARAMDIR + " parameter in absence of -extractinput ");
				}

				if (param.contains(EXTRACTMAPPER)) {
					param.remove(param.indexOf(EXTRACTMAPPER) + 1);
					param.remove(EXTRACTMAPPER);
					lo.error("ERROR: Invalid " + EXTRACTMAPPER + " parameter in absence of -extractinput ");
					System.out.println("ERROR: Invalid " + EXTRACTMAPPER + " parameter in absence of -extractinput ");
				}

			}
			if (extractInputFile != null && extractInputFile.length() > 0) {
				if (param.contains(EXTREPLACE)) {
					extractFileReplacement = true;
					extractFileReplacements = new ArrayList<>();
				}
				if (param.contains(EXTRACTMAPPER)) {
					extractMapperFile = param.get(param.indexOf(EXTRACTMAPPER) + 1);
					param.remove(param.indexOf(EXTRACTMAPPER) + 1);
					param.remove(EXTRACTMAPPER);
				} else {
					lo.warn("WARNING:" + EXTRACTMAPPER
							+ " value not available. Extract functionality requires extract mapper. If -f is specified, extract-map information will be taken from inside the DMS script");
					System.out.println("WARNING:" + EXTRACTMAPPER
							+ " value not available. Extract functionality requires extract mapper. If -f is specified, extract-map information will be taken from inside the DMS script");
				}
				if (param.contains(EXTRACTPARAMDIR)) {
					extractParamDir = param.get(param.indexOf(EXTRACTPARAMDIR) + 1);
					param.remove(param.indexOf(EXTRACTPARAMDIR) + 1);
					param.remove(EXTRACTPARAMDIR);
				} else {
					lo.warn("WARNING: " + EXTRACTPARAMDIR
							+ " value not available. Parameter values won't be printed to files.");
					System.out.println("WARNING: " + EXTRACTPARAMDIR
							+ " value not available. Parameter values won't be printed to files.");
				}

				if (extractInputFile != null && param.contains(EXTRACTTYPE)) {
					extractType = param.get(param.indexOf(EXTRACTTYPE) + 1);
					param.remove(param.indexOf(EXTRACTTYPE) + 1);
					param.remove(EXTRACTTYPE);
				} else {
					lo.warn("WARNING: " + EXTRACTTYPE
							+ " value not available. Value will be defaulted to hierarchydelimited, currenly the only supported type.");
					System.out.println("WARNING: " + EXTRACTTYPE
							+ " value not available. Value will be defaulted to hierarchydelimited, currenly the only supported type.");

					extractType = EXTTYPE;
				}
			}
			if (param.contains("-f")) {
				configFileName = param.get(param.indexOf("-f") + 1);
				param.remove(param.indexOf("-f") + 1);
				param.remove("-f");
				useConfigFile = true;
			}
			if (param.contains(ENCODE)) {
				encodingTBD = true;
				encodingConfigFileName = param.get(param.indexOf(ENCODE) + 1);
				param.remove(param.indexOf(ENCODE) + 1);
				param.remove(ENCODE);
			}
			if (param.contains(ENCDDELSRC)) {
				encodingTBD = true;
				deleteSrcDecodeFiles = true;
				encodingConfigFileName = param.get(param.indexOf(ENCDDELSRC) + 1);
				param.remove(param.indexOf(ENCDDELSRC) + 1);
				param.remove(ENCDDELSRC);
			}
			if (param.contains(DATALIB)) {
				String datalib = param.get(param.indexOf(DATALIB) + 1);
				param.remove(param.indexOf(DATALIB) + 1);
				param.remove(DATALIB);
				Types.getInstance().init(datalib);
			}
			if (param.contains("-r")) {
				fileLevelReplacement = true;
				fileLevelReplacements = new ArrayList<>();
			}

			if (param.contains("-s")) {
				paramConfigReplacement = true;
				paramReplacements = new ArrayList<>();
			}

			if (paramConfigReplacement || fileLevelReplacement || extractFileReplacement) {
				if (paramConfigReplacement) {
					int i = param.indexOf("-s") + 1;
					while (i != param.size()) {
						if (param.get(i).equals("-r") || param.get(i).equals(EXTREPLACE))
							break;
						paramReplacements.add(param.get(i++));
					}
				}
				if (fileLevelReplacement) {
					int i = param.indexOf("-r") + 1;
					while (i != param.size()) {
						if (param.get(i).equals("-s") || param.get(i).equals(EXTREPLACE))
							break;
						fileLevelReplacements.add(param.get(i++));
					}
				}
				if (extractFileReplacement) {
					int i = param.indexOf("-er") + 1;
					while (i != param.size()) {
						if (param.get(i).equals("-r") || param.get(i).equals("-s"))
							break;
						extractFileReplacements.add(param.get(i++));
					}
				}
			}
			// extract parameters to files
			if (extractInputFile != null && extractMapperFile != null && extractParamDir != null
					&& extractType.equals(EXTTYPE) && extractType != null) {
				List<String> mapperContentInDMSFile = FileOperation.getContentAsList(extractMapperFile, "utf8",
						Types.DMSEXTRACTMAPPERSTART, Types.DMSEXTRACTMAPPEREND, true, true);
				if (new File(extractInputFile).isDirectory()) { // if directory iterate through files
					List<Integer> count = new ArrayList<>();
					count.add(Integer.valueOf(0));
					final List<String> extractFileReplacementsx = extractFileReplacements;
					final String extractParamDirx = extractParamDir;
					try (Stream<Path> filePathStream = Files.walk(Paths.get(extractInputFile))) {
						filePathStream.filter(Files::isRegularFile).forEach(filePath -> {
							if (filePath.toFile().exists()) {
								VirtualFileParam vfp = new HierarchyDelimitedFile().extract(filePath.toString(),
										mapperContentInDMSFile, extractFileReplacementsx);
								vfp.printToFile(filePath.toString(), extractParamDirx);
							}
						});
					} catch (IOException e) {
						lo.error("ERROR: File not found during extractor directory parsing.\n" + LogStackTrace.get(e));
						System.out.println(
								"ERROR: File not found during extractor directory parsing.\n" + LogStackTrace.get(e));
					}
				} else { // if not directory, use a file name

					VirtualFileParam vfp = new HierarchyDelimitedFile().extract(extractInputFile,
							mapperContentInDMSFile, extractFileReplacements);
					vfp.printToFile("", extractParamDir);
				}
			}

			if (extractInputFile != null && extractMapperFile == null && extractType != null
					&& configFileName.length() > 0) {
				useConfigFile = false;
				if (extractType.equals(EXTTYPE)) {
					List<String> config = processConfig(configFileName, paramConfigReplacement, paramReplacements);
					if (config == null)
						return;
					if (new File(extractInputFile).isDirectory()) {
						lo.info("INFO: Input directory being checked for input files...");
						System.out.println("INFO: Input directory being checked for input files...");
						final boolean deleteExistingFilesx = deleteExistingFiles;
						final boolean deleteAllExistingFilesx = deleteAllExistingFiles;
						final List<String> fileLevelReplacementsx = fileLevelReplacements;
						final boolean fileLevelReplacementx = fileLevelReplacement;
						final List<String> extractFileReplacementsx = extractFileReplacements;
						final String extractParamDirx = extractParamDir;

						for (String s : config) {
							if (s.length() > 0) {
								List<String> mapperContentInDMSFile = FileOperation.getContentAsList(
										StringOps.fastSplit(s, Types.DMSDELIM).get(0), "utf8",
										Types.DMSEXTRACTMAPPERSTART, Types.DMSEXTRACTMAPPEREND, true, true);
								List<Integer> count = new ArrayList<>();
								count.add(Integer.valueOf(0));
								try (Stream<Path> filePathStream = Files.walk(Paths.get(extractInputFile))) {
									filePathStream.filter(Files::isRegularFile).forEach(filePath -> {
										if (filePath.toFile().exists()) {
											VirtualFileParam vfp = new HierarchyDelimitedFile().extract(
													filePath.toString(), mapperContentInDMSFile, false,
													extractFileReplacementsx);
											if (extractParamDirx != null && extractParamDirx.length() > 0)
												vfp.printToFile(filePath.toString(), extractParamDirx);
											new DataUnlimitedApi().generateData(s, deleteExistingFilesx, deleteAllExistingFilesx,
													fileLevelReplacementsx, fileLevelReplacementx,
													count.get(0).toString(), vfp);
											count.add(count.get(0) + 1);
											count.remove(0);
										}
									});
								} catch (IOException e) {
									lo.error("ERROR: File not found during extractor directory parsing.\n"
											+ LogStackTrace.get(e));
									System.out.println("ERROR: File not found during extractor directory parsing.\n"
											+ LogStackTrace.get(e));
								}
							}
						}
					} else {
						for (String s : config) {
							if (s.length() > 0) {
								List<String> mapperContentInDMSFile = FileOperation.getContentAsList(
										StringOps.fastSplit(s, Types.DMSDELIM).get(0), "utf8",
										Types.DMSEXTRACTMAPPERSTART, Types.DMSEXTRACTMAPPEREND, true, true);
								VirtualFileParam vfp = new HierarchyDelimitedFile().extract(extractInputFile,
										mapperContentInDMSFile, false, extractFileReplacements);
								if (extractParamDir != null && extractParamDir.length() > 0)
									vfp.printToFile("", extractParamDir);
								new DataUnlimitedApi().generateData(s, deleteExistingFiles, deleteAllExistingFiles,
										fileLevelReplacements, fileLevelReplacement, vfp);
							}
						}
					}
				}
			}

			if (configFileName.length() > 0 && useConfigFile) // DMS will be used for data generation based on
																// configuration file specified with -f
			{

				List<String> config = processConfig(configFileName, paramConfigReplacement, paramReplacements);
				if (config == null)
					return;
				final boolean deleteExistingFilesx = deleteExistingFiles;
				final boolean deleteAllExistingFilesx = deleteAllExistingFiles;
				final List<String> fileLevelReplacementsx = fileLevelReplacements;
				final boolean fileLevelReplacementx = fileLevelReplacement;
				if (config.size() < parallelLimit)
					parallelLimit = config.size();
				ExecutorService pool = Executors.newFixedThreadPool(parallelLimit);
				for (String s : config) {
					Runnable r = () -> new DataUnlimitedApi().generateData(s, deleteExistingFilesx, deleteAllExistingFilesx,
							fileLevelReplacementsx, fileLevelReplacementx, null);

					pool.execute(r);
				}
				pool.shutdown();
			} else {
				if (useConfigFile) {
					lo.error(
							"ERROR: No config batch File might have been specified with -f flag, check input arguments...");
					System.out.println(
							"ERROR: No config batch File might have been specified with -f flag, check input arguments...");
				}

			}

			if (listFile.length() > 0) // DMS will be creating file list for various directories. Comma separated
										// directory and filenames are expected in the input file and we can have more
										// than 1 entry using DMSCONFIGITEMDELIM
			{
				String configString = FileOperation.getContentAsString(listFile, "utf8");
				configString = configString
						.replaceAll("(?s)" + Types.DMSCONFIGCOMMENTSTART + "(.+?)" + Types.DMSCONFIGCOMMENTEND, "")
						.replaceAll("\r", "").replaceAll("\n", "");
				List<String> config = StringOps.fastSplit(configString, Types.DMSCONFIGITEMDELIM);
				for (String s : config) {
					if (s.length() > 0) {
						List<String> df = StringOps.fastSplit(s, ",");
						if (df.size() >= 2) {
							lo.info("INFO: Listing file operation... deleting existing file: " + df.get(1));
							System.out.println("INFO: Listing file operation... deleting existing file: " + df.get(1));
							FileOperation.deleteFile(df.get(1), "");
						}
					}
				}
				lo.info("INFO: Listing file in directories... using list config file: " + listFile);
				System.out.println("INFO: Listing file in directories... using list config file: " + listFile);

				for (String s : config) {
					if (s.length() > 0) {
						String[] df = s.split(",");
						if (df.length == 2)
							FileOperation.writeFile("", df[1], FileOperation.getListofFiles(df[0], true, false), true); // directory
																														// name
																														// -->
																														// file
																														// name
																														// ,
																														// 1
																														// to
																														// 1,
																														// no
																														// recursing
																														// in
																														// directories
						else if (df.length == 3) {
							FileOperation.writeFile("", df[1], FileOperation.getListofFiles(df[0], true, false), true,
									Boolean.parseBoolean(df[2])); // append to existing file or not
						} else if (df.length == 4) {
							FileOperation.writeFile("", df[1],
									FileOperation.getListofFiles(df[0], Boolean.parseBoolean(df[3]), false), true,
									Boolean.parseBoolean(df[2])); // append or not via [2], print directory names or not
																	// via [3]
						} else if (df.length == 5) {
							FileOperation.writeFile("", df[1], FileOperation.getListofFiles(df[0],
									Boolean.parseBoolean(df[3]), Boolean.parseBoolean(df[4])), true,
									Boolean.parseBoolean(df[2])); // append or not via [2], print directory names or not
																	// via [3], recurse or not via [4]
						} else {
							lo.error(
									"ERROR: Improper number of parameters specified in -ls configuration input file...");
							System.out.println(
									"ERROR: Improper number of parameters specified in -ls configuration input file...");
						}
					}
				}
			}

			if (encodingTBD) {
				String configString = FileOperation.getContentAsString(encodingConfigFileName, "utf8");
				configString = configString
						.replaceAll("(?s)" + Types.DMSCONFIGCOMMENTSTART + "(.+?)" + Types.DMSCONFIGCOMMENTEND, "")
						.replaceAll("\r", "").replaceAll("\n", "");
				List<String> config = StringOps.fastSplit(configString, Types.DMSCONFIGITEMDELIM);

				for (String s : config) {
					if (s.length() > 0) {
						List<String> values = StringOps.fastSplit(s, Types.DMSDELIM);

						if (values.size() < 4) {
							lo.error(
									"ERROR: Improper config element, at least 4 parameters are required... source encoding, destination encoding, source dir, destination dir, and optionally file extension for generated files...");
							System.out.println(
									"ERROR: Improper config element, at least 4 parameters are required... source encoding, destination encoding, source dir, destination dir, and optionally file extension for generated files...");
						} else {
							String srcCode = values.get(0);
							String destCode = values.get(1);
							File srcDir = new File(values.get(2));
							File destDir = new File(values.get(3));
							String ext = "txt";
							if (values.size() == 5) {
								ext = values.get(4);
							}
							if (srcDir != null && destDir != null) {
								int i = 0;
								for (final File fileEntry : srcDir.listFiles()) {
									if (!fileEntry.isDirectory()) {
										String content = FileOperation.getContentAsString(
												srcDir.getPath() + File.separator + fileEntry.getName(), srcCode);

										if (destCode.contains("gzipbase64") || destCode.contains("GZIPBASE64"))
											FileOperation.writeFile(destDir.getPath(),
													fileEntry.getName() + "to_" + destCode + "." + ext,
													GzipUtil.compressgzipbase64(content));
										else if (destCode.contains("base64") || destCode.contains("BASE64"))
											FileOperation.writeFile(destDir.getPath(),
													fileEntry.getName() + "to_" + destCode + "." + ext,
													GzipUtil.encodebase64(content));
										else
											FileOperation.writeFile(destDir.getPath(),
													fileEntry.getName() + "to_" + destCode + "." + ext, content,
													destCode);

										if (i % 10 == 0) {
											lo.info("INFO: Printing info every 10 files... file writen to destination dir "
													+ fileEntry.getName() + "to_" + destCode + "." + ext);
											System.out.println(
													"INFO: Printing info every 10 files... file writen to destination dir "
															+ fileEntry.getName() + "to_" + destCode + "." + ext);

										}
									} else {
										lo.warn("WARNING: Skipping " + fileEntry.getName() + " as it is directory...");
										System.out.println(
												"WARNING: Skipping " + fileEntry.getName() + " as it is directory...");
									}
									i++;
								}
								lo.info("INFO: File connversion operation complete. Please check destination directory...");
								System.out.println(
										"INFO: File connversion operation complete. Please check destination directory...");
							} else {
								lo.error("ERROR: Either source or destination directory was not chosen...");
								System.out.println("ERROR: Either source or destination directory was not chosen...");
							}

							if (deleteSrcDecodeFiles) {
								lo.info("INFO: Deleting all src encoded files at..." + srcDir.getAbsolutePath() + " @ "
										+ new Date());
								System.out.println("INFO: Deleting all src encoded files at..."
										+ srcDir.getAbsolutePath() + " @ " + new Date());
								FileOperation.deleteFile(srcDir.getAbsolutePath(), "");
							}
						}
					}
				}
			}
		} else {
			lo.error(
					"ERROR: DMS API or Non-UI mode can't run without essential parameters/flags, -f or -encode or -encodedelsrc or -ls or -extractinput ... program will terminate here, please check input arguments...");
			System.out.println(
					"ERROR: DMS API or Non-UI mode can't run without essential parameters/flags, -f or -encode or -encodedelsrc or -ls or -extractinput ... program will terminate here, please check input arguments...");
		}
	}

	private static String removeCommentsFromConfig(String config) {

		if (config != null)
			return config.replaceAll("(?s)" + Types.DMSCONFIGCOMMENTSTART + "(.+?)" + Types.DMSCONFIGCOMMENTEND, "")
					.replaceAll("\r", "").replaceAll("\n", "");
		else {
			lo.error(
					"ERROR: The config file content is null. The \".batch\" file may not be reachable. Please check the file path.");
			System.out.println(
					"ERROR: The config file content is null. The \".batch\" file may not be reachable. Please check the file path.");
			return null;
		}
	}

	private static List<String> processConfig(String configFileName, boolean paramConfigReplacement,
			List<String> paramReplacements) {
		String configString = FileOperation.getContentAsString(configFileName, "utf8");
		configString = removeCommentsFromConfig(configString);
		if (configString != null) {
			if (paramConfigReplacement) {
				for (String fr : paramReplacements) {
					if (fr != null && !fr.isEmpty()) {
						if (Types.getInstance().getIsUI()) {
							DULogger.log(400, fr);
						} else
							lo.info(fr);

						String find = fr.substring(0, fr.indexOf(Types.DMSCMDLINENAMEVALUESEPARATOR))
								.replace(Types.DMSSPACE, " ");
						String replace = fr.substring(fr.indexOf(Types.DMSCMDLINENAMEVALUESEPARATOR) + 3, fr.length())
								.replace(Types.DMSSPACE, " ");
						if (find.startsWith("{") && find.endsWith("}") && find.length() > 3)
							find = find.substring(1, find.length() - 1);
						if (replace.startsWith("{") && replace.endsWith("}") && replace.length() > 2)
							replace = replace.substring(1, replace.length() - 1);
						if (replace.equals(Types.DMSNULL) || replace.equals(Types.DMSNULLLOWER)) {
							configString = configString.replace("${" + find + "}", "");
						} else {
							configString = configString.replace("${" + find + "}", replace);
						}
					} else {
						if (Types.getInstance().getIsUI()) {
							DULogger.log(400, "bad replacement info found");
						} else
							lo.info("bad replacement info found");
					}
				}
			}
			return StringOps.fastSplit(configString, Types.DMSCONFIGITEMDELIM);
		} else {
			lo.error("ERROR: No config from .batch file could be read. Please check file path.");
			System.out.println("ERROR: No config from .batch file could be read. Please check file path.");

			return null;
		}
	}
}

package akhil.DataUnlimited.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;

public class FileOperation {
	private FileOperation() {
	}

	private static final Logger lo = LogManager.getLogger(FileOperation.class.getName());
	private static final String IOEXCEPTIONMSG = "ERROR: IO Exception.\n";

	public static void moveFile(String srcDir, String srcFile, String targetDir, String targetFile) {
		try {
			String srcPath = null;
			if (srcDir != null)
				srcPath = StringOps.append(srcDir, File.separator, srcFile);
			else
				srcPath = srcFile;

			Files.createDirectories(Paths.get(targetDir));
			String targetPath = StringOps.append(targetDir, File.separator, targetFile);
			Path temp = Files.move(Paths.get(srcPath), Paths.get(targetPath));

			if (temp == null) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: Failed to move file.\n");
				else
					lo.error("ERROR: Failed to move file.\n");
			}

		} catch (IOException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, "ERROR: IO Exception moving file.\n" + LogStackTrace.get(e));
			else
				lo.error("ERROR: IO Exception moving file.\n" + LogStackTrace.get(e));
		}
	}

	public static void deleteFile(String filePath, boolean recursive, int milliSeconds) {

		File f = new File(filePath);
		if (f.isDirectory()) {

			List<String> files = getListofFiles(filePath, true, recursive);

			for (String fi : files) {
				File fx = new File(fi);
				long diff = new Date().getTime() - fx.lastModified();
				if (diff > milliSeconds) {
					fx.delete();

				}
			}
		} else {
			if (Types.getInstance().getIsUI())
				DULogger.log(300, "File path is not a folder/directory. No action will be taken.");
			else
				lo.warn("File path is not a folder/directory. No action will be taken.");
		}

	}

	public static void deleteFile(String name, String fileExtension) {
		File f = new File(name);
		if (f.isDirectory()) {
			File[] contents;
			if (fileExtension.length() > 0) {
				final String ext = fileExtension;
				contents = f.listFiles((dir, filename) -> filename.endsWith(ext));
			} else
				contents = f.listFiles();
			for (File f1 : contents) {
				deleteFile(f1.getAbsolutePath(), "");
			}
			try {
				Files.delete(f.toPath());
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: IO Exception deleting files.\n" + LogStackTrace.get(e));
				else
					lo.error("ERROR: IO Exception deleting files.\n" + LogStackTrace.get(e));
			}
		} else
			try {
				Files.delete(f.toPath());
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}

	}

	public static String getFileContentAsString(String filePath) {
		List<String> list = getContentAsList(filePath, "UTF-8");
		return String.join(System.lineSeparator(), list);
	}

	public static List<String> getListofFiles(String filePath, boolean printDirName, boolean recursive) {
		List<String> toReturn = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(filePath))) {
			for (Path entry : stream) {
				if (!entry.toFile().isDirectory())
					toReturn.add(entry.toFile().getAbsolutePath());
				else {
					if (printDirName)
						toReturn.add(entry.toFile().getAbsolutePath());

					if (recursive)
						toReturn.addAll(getListofFiles(entry.toFile().getAbsolutePath(), printDirName, true));
				}
			}

		} catch (IOException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
			else
				lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
		}
		return toReturn;
	}

	public static File[] getListofFiles(String filePath, String filter) {
		File dir = new File(filePath);
		return dir.listFiles((d, name) -> name.endsWith(filter));

	}

	public static String getContentAsString(String filePath, String encoding) {
		List<String> list = getContentAsList(filePath, encoding);
		return String.join(System.lineSeparator(), list);
	}

	public static String getFileContentAsString(String filePath, String replaceStrings, String replaceWithStrings,
			String delim) // DMS script replacements.
	{
		String content = getFileContentAsString(filePath);
		return StringOps.findReplace(content, replaceStrings, replaceWithStrings, delim);
	}

	public static List<String> getContentAsList(String filePath, String filterLineCSV, String replaceCharacters) {
		List<String> split = null;
		List<String> split1 = null;

		if (filterLineCSV != null && filterLineCSV.length() > 0)
			split = StringOps.fastSplit(filterLineCSV, ",");
		if (replaceCharacters != null && replaceCharacters.length() > 0)
			split1 = StringOps.fastSplit(replaceCharacters, ",");

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));) {
			List<String> list = new ArrayList<>();
			String str;
			while ((str = br.readLine()) != null) {
				boolean flag = true;
				if (split != null && !split.isEmpty()) {
					for (String s : split) {
						if (str.contains(s))
							flag = false;
					}
				}
				if (flag) {
					if (split1 != null && !split1.isEmpty()) {
						for (String s : split1) {
							str = str.replace(s, "");
						}
					}
					list.add(str);
				}
			}
			return list;
		} catch (IOException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
			else
				lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			return new ArrayList<>();
		}
	}

	public static List<String> getContentAsList(String filePath, String encoding) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding));) {
			List<String> list = new ArrayList<>();
			String str;
			while ((str = br.readLine()) != null) {
				list.add(str);
			}
			return list;
		} catch (IOException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
			else
				lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			return new ArrayList<>();
		}
	}

	public static List<String> getContentAsList(String filePath, String encoding, String lineStart, String lineEnd,
			boolean excludeStartEnd, boolean excludeEmptyLines) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding));) {
			List<String> list = new ArrayList<>();
			String str;
			boolean flag = false;
			while ((str = br.readLine()) != null) {
				if (str.contains(lineEnd)) {
					flag = false;
					if (!excludeStartEnd)
						list.add(str);
				}
				if (str.contains(lineStart)) {
					flag = true;
					if (excludeStartEnd)
						continue;
				}
				if (flag) {
					if (excludeEmptyLines) {
						if (str.length() > 0)
							list.add(str);
					} else
						list.add(str);
				}

			}
			return list;
		} catch (IOException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
			else
				lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			return new ArrayList<>();
		}
	}

	public static List<String> getContentAsList(String filePath, String encoding, boolean excludeBlankLines) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding));) {
			List<String> list = new ArrayList<>();
			String str;
			while ((str = br.readLine()) != null) {
				if (excludeBlankLines) {
					if (str.length() > 0)
						list.add(str);
				} else
					list.add(str);
			}
			return list;
		} catch (IOException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
			else
				lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			return new ArrayList<>();
		}
	}

	public static void writeFile(String filePath, List<String> toPrint, boolean unixLineSeparator) {
		try (BufferedWriter pw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));) {
			for (String line : toPrint) {
				pw.append(line);
				if (unixLineSeparator)
					pw.append("\n");
				else
					pw.append(System.lineSeparator());
			}
			pw.flush();
		} catch (IOException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
			else
				lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
		}
	}

	public static void writeFile(String filePath, String fileName, String toPrint, String encoding) {
		if (filePath.length() > 0) {
			File directory = new File(filePath);
			if (!directory.exists())
				directory.mkdirs();
			try (BufferedWriter pw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(filePath + File.separator + fileName), encoding));) {
				pw.append(toPrint);
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		} else {
			try (BufferedWriter pw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileName), encoding));) {
				pw.append(toPrint);
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		}

	}

	public static void writeFile(String filePath, String fileName, List<String> toPrint, boolean unixLineSeparator) {
		if (filePath.length() > 0) {
			File directory = new File(filePath);
			if (!directory.exists())
				directory.mkdirs();
			try (BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filePath + File.separator + fileName), StandardCharsets.UTF_8));) {
				for (String line : toPrint) {
					pw.append(line);
					if (unixLineSeparator)
						pw.append("\n");
					else
						pw.append(System.lineSeparator());
				}
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		} else {
			try (BufferedWriter pw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));) {
				for (String line : toPrint) {
					pw.append(line);
					if (unixLineSeparator)
						pw.append("\n");
					else
						pw.append(System.lineSeparator());
				}
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		}
	}

	public static void writeFile(String filePath, String fileName, List<String> toPrint, boolean unixLineSeparator,
			boolean append) {
		if (filePath.length() > 0) {
			File directory = new File(filePath);
			if (!directory.exists())
				directory.mkdirs();
			try (BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filePath + File.separator + fileName, append), StandardCharsets.UTF_8));) {
				for (String line : toPrint) {
					pw.append(line);
					if (unixLineSeparator)
						pw.append("\n");
					else
						pw.append(System.lineSeparator());
				}
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		} else {
			try (BufferedWriter pw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileName, append), StandardCharsets.UTF_8));) {
				for (String line : toPrint) {
					pw.append(line);
					if (unixLineSeparator)
						pw.append("\n");
					else
						pw.append(System.lineSeparator());
				}
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		}
	}

	public static void writeFile(String filePath, String fileName, String toPrint) {

		if (filePath.length() > 0) {
			File directory = new File(filePath);
			if (!directory.exists())
				directory.mkdirs();

			try (BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filePath + File.separator + fileName), StandardCharsets.UTF_8));) {
				pw.append(toPrint);
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		} else {
			try (BufferedWriter pw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));) {
				pw.append(toPrint);
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		}
	}

	public static void writeFile(String filePath, String fileName, String toPrint, boolean append) {

		if (filePath.length() > 0) {
			File directory = new File(filePath);
			if (!directory.exists())
				directory.mkdirs();

			try (BufferedWriter pw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filePath + File.separator + fileName, append), StandardCharsets.UTF_8));) {
				pw.append(toPrint);
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		} else {
			try (BufferedWriter pw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileName, true), StandardCharsets.UTF_8));) {
				pw.append(toPrint);
				pw.flush();
			} catch (IOException e) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, IOEXCEPTIONMSG + LogStackTrace.get(e));
				else
					lo.error(IOEXCEPTIONMSG + LogStackTrace.get(e));
			}
		}
	}
}

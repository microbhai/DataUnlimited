package akhil.DataUnlimited.dataextractor.hierarchicaldoc;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.LogStackTrace;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.GzipUtil;
import akhil.DataUnlimited.util.JSEngine;
import akhil.DataUnlimited.util.StringOps;
import akhil.DataUnlimited.model.Parser;
import akhil.DataUnlimited.model.types.Types;

public class UtilityFunctions {
	private UtilityFunctions() {
	}

	private static final String DMSDOUBLEQUOTE = "'<DMSDOUBLEQUOTE>'";
	private static final String DMSNEWLINE = "<DMSNEWLINE>";
	private static final String LENFUNC = "len()";
	private static final Logger lo = LogManager.getLogger(UtilityFunctions.class.getName());

	public static List<String> getInBetween(String str, String startPattern, String endPattern) {
		return Parser.getInBetween(str, startPattern, endPattern);
	}

	public static List<String> getInBetween(String str, String startPattern, String endPattern,
			boolean excludeStartEndPattern) {
		return Parser.getInBetween(str, startPattern, endPattern, excludeStartEndPattern);
	}

	public static List<String> getInBetweenFast(String str, String startPattern, String endPattern,
			boolean excludeStartEndPattern) {
		return Parser.getInBetweenFast(str, startPattern, endPattern, excludeStartEndPattern);
	}

	public static String removeInternalNodeData(String data, List<String> start, List<String> end) {
		try {
			Map<Integer, Integer> tm;
			if (end.get(0).equals(DMSNEWLINE))
				tm = getStartEndPositions(data, end, getStartPositions(data, start, false));
			else {
				List<Integer> stpos = getStartPositions(data, start, false);
				List<Integer> edpos = getStartPositions(data, end, true);
				tm = getStartEndPositionsMap(stpos, edpos);
			}
			StringBuilder sb = new StringBuilder();
			int max = data.length() - 1;
			for (int i = 0; i <= max; i++) {
				boolean flag = true;
				for (Map.Entry<Integer, Integer> m : tm.entrySet()) {
					if (i >= m.getKey() && i < m.getValue())
						flag = false;
				}
				if (flag)
					sb.append(data.charAt(i));

			}
			return sb.toString();
		} catch (Exception e) {
			String msg = "Error removing internal node data. Using older version of code now." + LogStackTrace.get(e);
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			return removeInternalNodeData_old(data, start, end);
		}
	}

	public static String removeInternalNodeData_old(String data, List<String> start, List<String> end) {
		Map<Integer, Integer> tm = getStartEndPositions(data, end, getStartPositions_old(data, start));
		StringBuilder sb = new StringBuilder();
		int max = data.length() - 1;
		for (int i = 0; i <= max; i++) {
			boolean flag = true;
			for (Map.Entry<Integer, Integer> m : tm.entrySet()) {
				if (i >= m.getKey() && i < m.getValue())
					flag = false;
			}
			if (flag)
				sb.append(data.charAt(i));
		}
		return sb.toString();
	}

	public static List<Integer> getStartPositions_old(String data, List<String> toFind) {
		List<Integer> toReturn = new ArrayList<>();

		for (String s : toFind) {
			int count = 0;
			while (count <= data.length()) {
				int i = data.indexOf(s, count);
				if (i < 0)
					break;
				else {
					toReturn.add(i);
					count = i + s.length() + 1;
				}
			}
		}
		Collections.sort(toReturn);
		return toReturn;
	}

	public static List<String> getInternalNodeData_old(String data, List<String> start, List<String> end) {
		List<String> ls = new ArrayList<>();
		Map<Integer, Integer> tm = getStartEndPositions(data, end, getStartPositions_old(data, start));
		for (Map.Entry<Integer, Integer> i : tm.entrySet()) {

			ls.add(data.substring(i.getKey(), i.getValue()));
			if (Types.getInstance().getIsUI())
				DULogger.log(500, "Internal Node data extract: \n" + data.substring(i.getKey(), i.getValue()));
			else
				lo.debug("Internal Node data extract: \n" + data.substring(i.getKey(), i.getValue()));

		}
		return ls;
	}

	public static List<String> getInternalNodeData(String data, List<String> start, List<String> end) {
		try {
			List<String> ls = new ArrayList<>();
			Map<Integer, Integer> tm;
			if (end.get(0).equals(DMSNEWLINE))
				tm = getStartEndPositions(data, end, getStartPositions(data, start, false));
			else {
				List<Integer> stpos = getStartPositions(data, start, false);
				List<Integer> edpos = getStartPositions(data, end, true);
				tm = getStartEndPositionsMap(stpos, edpos);
			}
			for (Map.Entry<Integer, Integer> i : tm.entrySet()) {

				ls.add(data.substring(i.getKey(), i.getValue()));
				if (Types.getInstance().getIsUI())
					DULogger.log(500, "Internal Node data extract: \n" + data.substring(i.getKey(), i.getValue()));
				else
					lo.debug("Internal Node data extract: \n" + data.substring(i.getKey(), i.getValue()));

			}
			return ls;
		} catch (Exception e) {
			String msg = "Error paring internal node data. Using older version of code now." + LogStackTrace.get(e);
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			return getInternalNodeData_old(data, start, end);
		}
	}

	public static Map<Integer, Integer> getStartEndPositionsMap(List<Integer> stpos, List<Integer> edpos) {
		List<Integer> stposcopy = new ArrayList<>();
		stposcopy.addAll(stpos);

		Map<Integer, Integer> store = new HashMap<>();
		Map<Integer, Integer> toReturn = new TreeMap<>();

		for (Integer ed : edpos) {
			int counter = 0;
			for (Integer st : stpos) {

				if (st < ed)
					counter++;
			}
			int startPos = stpos.get(counter - 1);
			stpos.remove(counter - 1);
			store.put(startPos, ed);
		}
		stpos = stposcopy;
		Set<Integer> discardKeys = new HashSet<>();
		for (Map.Entry<Integer, Integer> entryx : store.entrySet()) {
			for (Map.Entry<Integer, Integer> entryy : store.entrySet()) {
				if (entryx.getKey() != entryy.getKey()) {
					if (entryx.getKey() > entryy.getKey() && entryx.getValue() < entryy.getValue()) {
						discardKeys.add(entryx.getKey());
					}
				}
			}
		}
		stpos.removeAll(discardKeys);
		for (Integer in : stpos)
			toReturn.put(in, store.get(in));

		return toReturn;
	}

	public static List<Integer> getStartPositions(String data, List<String> toFind, boolean addFindLength) {
		List<Integer> toReturn = new ArrayList<>();

		for (String s : toFind) {
			int count = 0;
			while (count <= data.length()) {
				int i = data.indexOf(s, count);
				if (i < 0)
					break;
				else {
					if (addFindLength)
						toReturn.add(i + s.length());
					else
						toReturn.add(i);
					count = i + s.length();
				}
			}
		}
		Collections.sort(toReturn);
		return toReturn;
	}

	public static Map<Integer, Integer> getStartEndPositions(String data, List<String> toFind,
			List<Integer> startPositions) {
		Map<Integer, Integer> toReturn = new TreeMap<>();
		boolean includeEnd = true;
		for (Integer i : startPositions) {
			int index = i + 1;
			for (String s : toFind) {
				if (s.contains("(exclude)")) {
					includeEnd = false;
					s = s.replace("(exclude)", "");
				}
				int foundAt = data.indexOf(s, index);
				if (foundAt >= 0) {
					if (includeEnd) {
						toReturn.put(i, foundAt + s.length());
					} else {
						toReturn.put(i, foundAt);
					}
					break;
				}
			}
		}
		return toReturn;
	}

	public static void deleteParamFiles(String dir) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
		File[] files = FileOperation.getListofFiles(dir, "_param.txt");
		if (files != null && files.length > 0) {
			for (File f1 : files) {
				FileOperation.deleteFile(f1.getAbsolutePath(), "");
			}
		}
		File[] files1 = FileOperation.getListofFiles(dir, "_node.txt");
		if (files1 != null && files1.length > 0) {
			for (File f2 : files1) {
				FileOperation.deleteFile(f2.getAbsolutePath(), "");
			}
		}
	}

	public static String transform(String val, String operation) {
		String toReturn = "null";
		if (operation.length() == 0 || operation == null) {
			if (Types.getInstance().getIsUI())
				DULogger.log(300, "WARNING: Empty transformation function. No transformation will be performed...");
			else
				lo.warn("WARNING: Empty transformation function. No transformation will be performed...");

			return val;
		}
		boolean hasDoubleQuote = false;
		boolean setDefault = false;
		String defaultValue = null;
		try {
			if (operation.contains("\"")) {
				operation = operation.replace("\\\"", DMSDOUBLEQUOTE);
				hasDoubleQuote = true;
			}
			if (operation.contains("split")) {
				List<String> mapStrings = UtilityFunctions.getInBetweenFast(operation, "\"", "\"", true);
				List<String> split = StringOps.fastSplit(val, mapStrings.get(0));
				int i = Integer.parseInt(mapStrings.get(1)) - 1;
				if (split.get(i) != null)
					return split.get(i);
				else
					return "null";
			}
			if (operation.contains("regexReplace")) {
				List<String> mapStrings = UtilityFunctions.getInBetweenFast(operation, "\"", "\"", true);
				return val.replaceAll(mapStrings.get(0), mapStrings.get(1));
			}
			if (operation.contains("replace")) {
				List<String> mapStrings = UtilityFunctions.getInBetweenFast(operation, "\"", "\"", true);
				return val.replace(mapStrings.get(0), mapStrings.get(1));
			}
			if (operation.contains("upperCase")) {
				return val.toUpperCase();
			}
			if (operation.contains("lowerCase")) {
				return val.toLowerCase();
			}
			if (operation.contains("gzipbase64")) {
				return GzipUtil.decompressgzipbase64(val);
			}
			if (operation.contains("decode")) {
				List<String> mapStrings = UtilityFunctions.getInBetweenFast(operation, "\"", "\"", true);
				Map<String, String> map;
				if (mapStrings.size() % 2 != 0 && mapStrings.get(mapStrings.size() - 1).equalsIgnoreCase("default")) {
					setDefault = true;
					mapStrings.remove(mapStrings.size() - 1);
				}
				if (mapStrings.size() % 2 == 0 && mapStrings.get(mapStrings.size() - 2).equalsIgnoreCase("default")) {
					setDefault = true;
					defaultValue = mapStrings.get(mapStrings.size() - 1);
					mapStrings.remove(mapStrings.size() - 1);
					mapStrings.remove(mapStrings.size() - 1);
				}
				if (mapStrings.size() % 2 == 0) {
					map = new HashMap<>();
					for (int i = 0; i < mapStrings.size(); i++) {
						String key = null;
						String value = null;
						if (i % 2 == 0) {
							if (hasDoubleQuote) {
								key = mapStrings.get(i).replace(DMSDOUBLEQUOTE, "\"");
								value = mapStrings.get(i + 1).replace(DMSDOUBLEQUOTE, "\"");
							} else {
								key = mapStrings.get(i);
								value = mapStrings.get(i + 1);
							}
						}
						if (key != null && value != null) {
							map.put(key, value);
						}
					}
					toReturn = map.get(val);
					if (toReturn != null)
						return toReturn;
					else {
						if (setDefault) {
							if (defaultValue == null)
								return val;
							else
								return defaultValue;
						}
					}
				} else {
					if (Types.getInstance().getIsUI())
						DULogger.log(200, "ERROR: Key/Values for decode must be an even count... " + operation);
					else
						lo.error("ERROR: Key/Values for decode must be an even count... " + operation);

					return val.trim();
				}

			}
			if (operation.contains("trim")) {
				toReturn = val.trim();
			}
			if (operation.contains("padzero")) {
				operation = operation.replace("multiply", "");
				operation = operation.trim().substring(1, operation.length() - 1);
				List<String> format = UtilityFunctions.getInBetweenFast(operation, "\"", "\"", true);
				String fm = "%0" + format.get(0) + "d";
				toReturn = String.format(fm, Long.valueOf(val));
			}
			if (operation.contains("multiply")) {
				operation = operation.replace("multiply", "");
				operation = operation.trim().substring(1, operation.length() - 1);
				List<String> format = UtilityFunctions.getInBetweenFast(operation, "\"", "\"", true);
				Double i = Double.parseDouble(format.get(0));
				Double d = Double.parseDouble(val);
				DecimalFormat df = new DecimalFormat(format.get(1));
				toReturn = df.format(d * i);
			}
			if (operation.contains("substring")) {
				List<String> format = UtilityFunctions.getInBetweenFast(operation, "\"", "\"", true);
				int start;
				int end;
				try {
					start = Integer.parseInt(format.get(0));
				} catch (NumberFormatException e) {
					String arg1 = format.get(0).trim();
					if (arg1.contains(LENFUNC)) {
						
						String len = Integer.toString(val.length());
						String evalExpression = arg1.replace(LENFUNC, len);
						start = Integer.parseInt(JSEngine.eval("eval (\"" + evalExpression + "\")").toString());
						
					} else if (arg1.startsWith("string(")) {
						String st = arg1.substring(7, arg1.length() - 1);
						start = val.indexOf(st);
						if (format.size() > 2 && format.get(2).equals("true"))
							start = start + st.length();
					} else {
						start = val.indexOf(format.get(0));
						if (format.size() > 2 && format.get(2).equals("true"))
							start = start + format.get(0).length();
					}
				}
				try {
					end = Integer.parseInt(format.get(1));
				} catch (NumberFormatException e) {
					String arg2 = format.get(1).trim();
					if (arg2.contains(LENFUNC)) {
							
						String len = Integer.toString(val.length());
						String evalExpression = format.get(1).replace(LENFUNC, len);
						end = Integer.parseInt(JSEngine.eval("eval (\"" + evalExpression + "\")").toString());
						
					} else if (arg2.startsWith("string(")) {
						String ed = arg2.substring(7, arg2.length() - 1);
						end = val.indexOf(ed);
					} else
						end = val.indexOf(format.get(1));
				}
				toReturn = val.substring(start, end);
			}
			if (operation.contains("date")) {
				operation = operation.replace("date", "");
				operation = operation.trim().substring(1, operation.length() - 1);
				List<String> format = UtilityFunctions.getInBetweenFast(operation, "\"", "\"", true);
				String df1 = null;
				String df2 = null;
				if (hasDoubleQuote) {
					df1 = format.get(0).replace(DMSDOUBLEQUOTE, "\"");
					df2 = format.get(1).replace(DMSDOUBLEQUOTE, "\"");
				} else {
					df1 = format.get(0);
					df2 = format.get(1);
				}
				SimpleDateFormat format1 = new SimpleDateFormat(df1);
				SimpleDateFormat format2 = new SimpleDateFormat(df2);
				try {
					Date dt = format1.parse(val);
					toReturn = format2.format(dt);
				} catch (ParseException e) {
					if (Types.getInstance().getIsUI())
						DULogger.log(200, "ERROR: Date value couldn't be parsed in the specified format..." + operation
								+ "..." + val + "\n" + LogStackTrace.get(e));
					else
						lo.error("ERROR: Date value couldn't be parsed in the specified format..." + operation + "..."
								+ val + "\n" + LogStackTrace.get(e));

				}
			}
		} catch (Exception e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200,
						"ERROR: Exception in Transformation function... entered function couldn't be understood..."
								+ operation + "\n" + e.getMessage() + "\n" + LogStackTrace.get(e));
			else
				lo.error("ERROR: Exception in Transformation function... entered function couldn't be understood..."
						+ operation + "\n" + e.getMessage() + "\n" + LogStackTrace.get(e));
		}

		return toReturn;
	}

	public static List<String> getLineInBetween(String data, String lineStart, String lineEnd, boolean excludeStartEnd,
			boolean excludeEmptyLines) {
		List<String> datalines = StringOps.fastSplit(data.replace("\r", ""), "\n");
		List<String> list = new ArrayList<>();
		boolean flag = false;
		int i = 0;
		while (i < datalines.size()) {
			if (datalines.get(i).contains(lineEnd)) {
				flag = false;
				if (!excludeStartEnd)
					list.add(datalines.get(i));
			}
			if (datalines.get(i).contains(lineStart)) {
				flag = true;
				if (excludeStartEnd) {
					i++;
					continue;
				}
			}
			if (flag) {
				if (excludeEmptyLines) {
					if (datalines.get(i).length() > 0)
						list.add(datalines.get(i));
				} else
					list.add(datalines.get(i));
			}
			i++;
		}
		return list;
	}

}

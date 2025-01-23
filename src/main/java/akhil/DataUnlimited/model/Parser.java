package akhil.DataUnlimited.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;

public class Parser {
	private Parser() {
	}

	private static final Logger lo = LogManager.getLogger(Parser.class.getName());

	public static int countSubstring(String data, String toFind) {
		int count = 0;
		int fromIndex = 0;

		while ((fromIndex = data.indexOf(toFind, fromIndex)) != -1) {

			count++;
			fromIndex++;

		}
		return count;
	}

	public static boolean isTopLevel(Integer i, Map<Integer, Integer> pairs) {
		boolean flag = true;
		for (Map.Entry<Integer, Integer> j : pairs.entrySet()) {

			if (!i.equals(j.getKey()) && !pairs.get(i).equals(j.getValue()) && i > j.getKey()
					&& pairs.get(i) < j.getValue()) {
				flag = false;
			}
		}

		return flag;
	}

	public static Map<Integer, Integer> hasNesting(Integer i, Map<Integer, Integer> pairs) {
		Map<Integer, Integer> internal = new TreeMap<>();
		for (Map.Entry<Integer, Integer> j : pairs.entrySet()) {
			if (!i.equals(j.getKey()) && !pairs.get(i).equals(j.getValue()) && i < j.getKey()
					&& pairs.get(i) > j.getValue()) {
				internal.put(j.getKey(), j.getValue());
			}
		}
		return internal;
	}

	public static Map<Integer, Integer> getStartEndPositionMap(String data, String start, String end, Processor p) {
		return getMappingOnPositions(getPositions(data, start, start, end, end), start, end, p);
	}

	public static Map<Integer, Integer> getMappingOnPositions(Map<Integer, String> positions, String start, String end,
			Processor p) {
		// Method is used to get location of recurring patterns in pairs, such as
		// opening and closing of brackets.
		// Which start of bracket matches with which close of bracket in terms of their
		// index in data string.
		// This requires data created by getPositions method below.

		Map<Integer, Integer> pairs = new TreeMap<>();
		LinkedList<Integer> keyStack = new LinkedList<>();
		for (Map.Entry<Integer, String> i : positions.entrySet()) {
			if (i.getValue().equals(start)) {
				keyStack.add(i.getKey());
			}
			if (i.getValue().equals(end)) {
				if (keyStack.isEmpty()) {
					String msg = "ERROR: " + end + " (end) encountered before appropriate " + start
							+ " (start). Error in DMS script syntax";
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					if (p.toLog)
						p.addLog(200, msg);
				} else {
					Integer x = keyStack.removeLast();
					pairs.put(x, i.getKey());
					String msg = "DEBUG: Group creation. Position " + x + " paired with " + i.getKey() + ".";
					if (Types.getInstance().getIsUI())
						DULogger.log(600, msg);
					else
						lo.debug(msg);
					if (p.toLog)
						p.addLog(600, msg);
				}
			}
		}
		if (!keyStack.isEmpty()) {
			String msg = "ERROR: Some of the " + start + " (start) didn't meet corresponding " + end
					+ " (end). Error in DMS script syntax";
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (p.toLog)
				p.addLog(200, msg);
		}

		return pairs;

	}

	public static List<String> getInBetween(String str, String startPattern, String endPattern) {
		// Method is used to read data in between 2 recurring patterns
		List<String> inBetween = new ArrayList<>();
		Pattern pattern = Pattern.compile(Pattern.quote(startPattern) + "(.*?)" + Pattern.quote(endPattern),
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			inBetween.add(matcher.group());
		}
		return inBetween;

	}

	public static List<String> getInBetween(String str, String startPattern, String endPattern,
			boolean excludeStartEndPattern) {
		// Method is used to read data in between 2 recurring patterns
		List<String> inBetween = new ArrayList<>();
		Pattern pattern = Pattern.compile(Pattern.quote(startPattern) + "(.*?)" + Pattern.quote(endPattern),
				Pattern.DOTALL);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			if (excludeStartEndPattern)
				inBetween.add(matcher.group().replaceAll(Pattern.quote(startPattern), "")
						.replaceAll(Pattern.quote(endPattern), ""));
			else
				inBetween.add(matcher.group());
		}
		return inBetween;

	}

	public static List<String> getInBetweenFast(String str, String startPattern, String endPattern,
			boolean excludeStartEndPattern) {
		List<String> toReturn = new ArrayList<>();
		int index = 0;
		int is;
		int ie;

		while (index <= str.length() && str.indexOf(startPattern, index) >= 0 && str.indexOf(endPattern, index) >= 0) {

			if (excludeStartEndPattern) {
				is = str.indexOf(startPattern, index) + startPattern.length();
				ie = str.indexOf(endPattern, is);
				index = ie + +endPattern.length();
			} else {
				is = str.indexOf(startPattern, index);
				ie = str.indexOf(endPattern, is + startPattern.length()) + endPattern.length();
				index = ie;
			}
			if (ie < 0 || is < 0)
				break;

			toReturn.add(str.substring(is, ie));

		}
		return toReturn;
	}

	public static Map<Integer, String> getPositions(String str, String... searchAndNames) {
		// This method is used for searching strings and returning their index location
		// in the data string str.
		// String to be searched for is given a name. Example usage is getPositions with
		// arguments ( "this is a good day today","is","ISLOCATION","day","DAYLOCATION"
		// )
		// will give a map { { 5,"ISLOCATION" }, { 16,"DAYLOCATION" } }

		int length = searchAndNames.length;

		List<String> list = new ArrayList<>();
		list.addAll(Arrays.asList(searchAndNames));
		Map<Integer, String> positions = new TreeMap<>();
		int currIndex = 0;

		for (int i = 0; i < length / 2; i++) {
			String find = list.get(0);
			String name = list.get(1);

			currIndex = 0;

			while (currIndex < str.length() && currIndex >= 0) {
				currIndex = str.indexOf(find, currIndex);
				if (currIndex < 0) {
					break;
				} else {
					positions.put(currIndex, name);
					currIndex++;
				}
			}

			list.remove(0);
			list.remove(0);
		}
		return positions;
	}
}

package akhil.DataUnlimited.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.Parser;
import akhil.DataUnlimited.model.types.Types;

public class StringOps {

	private StringOps() {
	}

	private static final Logger lo = LogManager.getLogger(StringOps.class.getName());

	public static String append(String... asManyStrings) {
		StringBuilder sb = new StringBuilder();
		for (String s : asManyStrings) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static List<String> fastSplit(String xyz, String delim) {
		List<String> as = new ArrayList<>();
		int index = 0;
		int delimLength = delim.length();
		if (delim.equals("\\t")) {
			char delm = '\t';
			while (index <= xyz.lastIndexOf(delm)) {
				int foundAt = xyz.indexOf(delm, index);
				String x = xyz.substring(index, foundAt);
				as.add(x);
				index = foundAt + 1;
			}
			as.add(xyz.substring(index));
		} else {
			while (index <= xyz.lastIndexOf(delim)) {
				int foundAt = xyz.indexOf(delim, index);
				String x = xyz.substring(index, foundAt);
				as.add(x);
				index = foundAt + delimLength;
			}
			as.add(xyz.substring(index));
		}
		return as;
	}
	
	public static List<String> getInBetweenFast(String str, String startPattern, String endPattern,
			boolean excludeStartEndPattern) {
		return Parser.getInBetweenFast(str, startPattern, endPattern, excludeStartEndPattern);
	}

	public static String findReplace(String content, String replaceStrings, String replaceWithStrings, String delim) {
		List<String> replaceStringArray = StringOps.fastSplit(replaceStrings, delim);
		List<String> replaceWithStringArray = StringOps.fastSplit(replaceWithStrings, delim);
		if (replaceStringArray.size() == replaceWithStringArray.size()) {
			if (content != null) {
				for (int i = 0; i < replaceStringArray.size(); i++) {
					if (!replaceWithStringArray.get(i).startsWith("-NOSUB-")) {
						content = content.replace(replaceStringArray.get(i), replaceWithStringArray.get(i));
					}
				}
				return content;
			} else
				return null;
		} else {
			if (Types.getInstance().getIsUI())
				DULogger.log(200,
						"ERROR: Number for values to replace and number of values to replace with aren't same...");
			else
				lo.error("ERROR: Number for values to replace and number of values to replace with aren't same...");

			return null;
		}
	}
}
package akhil.DataUnlimited.model;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;

public class SegmentRepetitionEval {
	private SegmentRepetitionEval() {
	}

	private static final Logger lo = LogManager.getLogger(SegmentRepetitionEval.class.getName());
	private static final String CONDITIONLAST = "last";
	private static final String CONDITIONNOTLAST = "!last";
	private static final String CONDITIONFIRST = "first";
	private static final String CONDITIONNOTFIRST = "!first";
	private static final String CONDITIONMULTIPLE = "/";
	private static final String CONDITIONNOTMULTIPLE = "!/";

	public static String[] getSegmentRepetitionConditions() {
		return new String[] { CONDITIONLAST + "  -> segment occurs only in last iteration of parent group",
				CONDITIONNOTLAST + "  -> segment occurs in all but last iteration of parent group",
				CONDITIONFIRST + "  -> segment occurs only in first iteration of parent group",
				CONDITIONNOTFIRST + "  -> segment occurs in all but first iteration of parent group",
				CONDITIONMULTIPLE + "X"
						+ "  -> segment occurs only in those iterations of parent group which are multiple of X",
				CONDITIONNOTMULTIPLE + "X"
						+ "  -> segment occurs in all except those iterations of parent group which are multiple of X" };
	}

	private static int checkRep(String s1) {
		try {
			return Integer.parseInt(s1);
		} catch (NumberFormatException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(300, "WARNING: NO NUMBER FOUND @ " + s1 + " ... defaulting to 1");
			else
				lo.warn("WARNING: NO NUMBER FOUND @ " + s1 + " ... defaulting to 1");
			return 1;
		}
	}

	public static boolean eval(int groupIteration, int totalGroupIterations, List<String> repetitionPattern) {
		boolean shouldEval = false;

		for (String s : repetitionPattern) {
			if (s.trim().equals(CONDITIONLAST)) {
				if (groupIteration == totalGroupIterations) {
					shouldEval = true; // if rep needs to happen in last and iteration number is last, not need to
										// check further
					break;
				}
			} else if (s.trim().equals(CONDITIONNOTLAST)) {
				if (groupIteration == totalGroupIterations) {
					shouldEval = false;
					break;
				} else
					shouldEval = true;
			} else if (s.trim().equals(CONDITIONFIRST)) {
				if (groupIteration == 1) {
					shouldEval = true;
					break;
				}
			} else if (s.trim().equals(CONDITIONNOTFIRST)) {
				if (groupIteration == 1) {
					shouldEval = false;
					break;
				} else
					shouldEval = true;
			} else if (s.trim().contains(CONDITIONMULTIPLE)) {
				try {
					if (s.trim().contains("!")) // not multiple condition (like not multiple of 5)
					{
						String s1 = s.trim().replace("!", "").replace("/", "");
						int rep = checkRep(s1);
						if (groupIteration % rep == 0) {
							shouldEval = false;
							break;
						} else
							shouldEval = true;
					} else // multiple condition (like multiple of 5)
					{
						String s1 = s.trim().replace("/", "");
						if (groupIteration % Integer.parseInt(s1) == 0) {
							shouldEval = true;
							break;
						} else
							shouldEval = false;
					}
				} catch (NumberFormatException e) {
					if (Types.getInstance().getIsUI())
						DULogger.log(200, "ERROR: X in /X or !/X should be replaced with number..." + s);
					else
						lo.error("ERROR: X in /X or !/X should be replaced with number..." + s);
				}
			} else {
				shouldEval = true;
			}
		}
		return shouldEval;
	}
}

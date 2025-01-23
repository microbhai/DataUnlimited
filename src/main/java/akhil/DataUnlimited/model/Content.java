package akhil.DataUnlimited.model;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.StringOps;

public class Content {
	private static final Logger lo = LogManager.getLogger(Content.class.getName());
	private Group group;
	private long repetitions;
	private List<String> repetitionPattern;
	private String repetitionParamName;
	private String type; // Segment or Group
	private List<DynamicContent> dc = new ArrayList<>(); // Dynamic Content

	private Group parentGroup;

	private void setGroupContent(String str) {
		Map<Integer, Integer> pairs = Parser.getStartEndPositionMap(str + Types.GROUPENDDELIMITER,
				Types.GROUPSTARTDELIMITER, Types.GROUPENDDELIMITER, parentGroup.getGroupsProcessor());
		pairs.remove(0);

		if (pairs.size() > 0) {
			this.group = new Group(str, 0, str.length(), pairs, parentGroup.getGroupsProcessor());
		} else {
			this.group = new Group(str, 0, str.length(), parentGroup.getGroupsProcessor());
		}
	}

	private String indexStartCheck(String data, int i)
	/* identify which parameter starts first {#show#{ or {#hide#{ or {#showlog#{ */
	{
		Map<Integer, String> indexPos = new TreeMap<>();

		int index;
		if ((index = data.indexOf(Types.PARAMSTARTSHOW, i)) >= 0) {
			indexPos.put(index, Types.PARAMSTARTSHOW);
		}

		if ((index = data.indexOf(Types.PARAMSTARTHIDE, i)) >= 0) {
			indexPos.put(index, Types.PARAMSTARTHIDE);
		}

		if ((index = data.indexOf(Types.PARAMSTARTSHOWLOG, i)) >= 0) {
			indexPos.put(index, Types.PARAMSTARTSHOWLOG);
		}

		if ((index = data.indexOf(Types.PARAMSTARTHIDELOG, i)) >= 0) {
			indexPos.put(index, Types.PARAMSTARTHIDELOG);
		}

		if (indexPos.size() > 0) {
			Entry<Integer, String> entry = indexPos.entrySet().iterator().next();
			return entry.getValue();
		} else
			return null;

	}

	private void setDynamicContent(String data) {
		int i = 0;

		while (i >= 0 && i <= data.length()) {
			int indexStart;
			int newParamStartCheck = -1;
			String check = indexStartCheck(data, i);

			if (check != null && check.equals(Types.PARAMSTARTSHOW)) {
				indexStart = data.indexOf(Types.PARAMSTARTSHOW, i);

				dc.add(new DynamicContent(data.substring(i, indexStart), Types.SEGMENTCONTENTCONSTANT, parentGroup,
						true, false));
				// int indexEnd = data.indexOf(Types.PARAMEND, i);
				int indexEnd = data.indexOf(Types.PARAMEND, indexStart);

				if (indexEnd != -1 && indexEnd > indexStart) {

					newParamStartCheck = newParamStartCheckFunc(data, indexStart, indexEnd);

					if (newParamStartCheck != -1) {
						logStartBeforeEndFound(data);
						break;
					} else
						dc.add(new DynamicContent(data.substring(indexStart + Types.PARAMSTARTSHOW.length(), indexEnd),
								Types.SEGMENTCONTENTPARAM, parentGroup, true, false));
				} else {
					logEndNotFound(data);
					break;
				}

				i = indexEnd + 2;
			} else if (check != null && check.equals(Types.PARAMSTARTHIDE)) {
				indexStart = data.indexOf(Types.PARAMSTARTHIDE, i);
				dc.add(new DynamicContent(data.substring(i, indexStart), Types.SEGMENTCONTENTCONSTANT, parentGroup,
						false, false));
				// int indexEnd = data.indexOf(Types.PARAMEND, i);
				int indexEnd = data.indexOf(Types.PARAMEND, indexStart);
				if (indexEnd != -1 && indexEnd > indexStart) {
					newParamStartCheck = newParamStartCheckFunc(data, indexStart, indexEnd);

					if (newParamStartCheck != -1) {
						logStartBeforeEndFound(data);
						break;
					} else
						dc.add(new DynamicContent(data.substring(indexStart + Types.PARAMSTARTHIDE.length(), indexEnd),
								Types.SEGMENTCONTENTPARAM, parentGroup, false, false));
				} else {
					logEndNotFound(data);
					break;
				}
				i = indexEnd + 2;
			} else if (check != null && check.equals(Types.PARAMSTARTSHOWLOG)) {
				indexStart = data.indexOf(Types.PARAMSTARTSHOWLOG, i);
				dc.add(new DynamicContent(data.substring(i, indexStart), Types.SEGMENTCONTENTCONSTANT, parentGroup,
						true, false));
				// int indexEnd = data.indexOf(Types.PARAMEND, i);
				int indexEnd = data.indexOf(Types.PARAMEND, indexStart);
				if (indexEnd != -1 && indexEnd > indexStart) {

					newParamStartCheck = newParamStartCheckFunc(data, indexStart, indexEnd);

					if (newParamStartCheck != -1) {
						logStartBeforeEndFound(data);
						break;
					} else
						dc.add(new DynamicContent(
								data.substring(indexStart + Types.PARAMSTARTSHOWLOG.length(), indexEnd),
								Types.SEGMENTCONTENTPARAM, parentGroup, true, true));
				} else {
					logEndNotFound(data);
					break;
				}
				i = indexEnd + 2;
			} else if (check != null && check.equals(Types.PARAMSTARTHIDELOG)) {
				indexStart = data.indexOf(Types.PARAMSTARTHIDELOG, i);
				dc.add(new DynamicContent(data.substring(i, indexStart), Types.SEGMENTCONTENTCONSTANT, parentGroup,
						false, false));
				// int indexEnd = data.indexOf(Types.PARAMEND, i);
				int indexEnd = data.indexOf(Types.PARAMEND, indexStart);
				if (indexEnd != -1 && indexEnd > indexStart) {

					newParamStartCheck = newParamStartCheckFunc(data, indexStart, indexEnd);

					if (newParamStartCheck != -1) {
						logStartBeforeEndFound(data);
						break;
					} else
						dc.add(new DynamicContent(
								data.substring(indexStart + Types.PARAMSTARTHIDELOG.length(), indexEnd),
								Types.SEGMENTCONTENTPARAM, parentGroup, false, true));
				} else {
					logEndNotFound(data);
					break;
				}
				i = indexEnd + 2;
			} else {
				dc.add(new DynamicContent(data.substring(i, data.length()), Types.SEGMENTCONTENTCONSTANT, parentGroup,
						true, false));
				i = data.length() + 1;
			}
		}
	}

	private void logStartBeforeEndFound(String data) {
		if (Types.getInstance().getIsUI()) {
			DULogger.log(200,
					"ERROR: Param start found before parameter end. Make sure every parameter definition ends with }}. Error @ \n "
							+ data);
		} else
			lo.error(
					"ERROR: Param start found before parameter end. Make sure every parameter definition ends with }}. Error @ \n "
							+ data);
	}

	private void logEndNotFound(String data) {
		if (Types.getInstance().getIsUI()) {
			DULogger.log(200,
					"ERROR: Param end was not found. Make sure every parameter definition ends with }}. For parameter starting @ \n "
							+ data);
		} else
			lo.error(
					"ERROR: Param end was not found. Make sure every parameter definition ends with }}. For parameter starting @ \n "
							+ data);
	}

	private int newParamStartCheckFunc(String data, int indexStart, int indexEnd) {
		int newParamStartCheck = -1;
		for (int z = 0; z < 4; z++) {
			data.indexOf(Types.PARAMSTARTSHOW, indexStart + 8);
			if (data.indexOf(Types.PARAMSTARTSHOW, indexStart + 8) != -1
					&& data.indexOf(Types.PARAMSTARTSHOW, indexStart + 8) < indexEnd)
				newParamStartCheck = data.indexOf(Types.PARAMSTARTSHOW, indexStart + 8);
			else if (data.indexOf(Types.PARAMSTARTHIDE, indexStart + 8) != -1
					&& data.indexOf(Types.PARAMSTARTHIDE, indexStart + 8) < indexEnd)
				newParamStartCheck = data.indexOf(Types.PARAMSTARTHIDE, indexStart + 8);
			else if (data.indexOf(Types.PARAMSTARTSHOWLOG, indexStart + 11) != -1
					&& data.indexOf(Types.PARAMSTARTSHOWLOG, indexStart + 11) < indexEnd)
				newParamStartCheck = data.indexOf(Types.PARAMSTARTHIDE, indexStart + 11);
			else if (data.indexOf(Types.PARAMSTARTHIDELOG, indexStart + 11) != -1
					&& data.indexOf(Types.PARAMSTARTHIDELOG, indexStart + 11) < indexEnd)
				newParamStartCheck = data.indexOf(Types.PARAMSTARTHIDELOG, indexStart + 11);
		}
		return newParamStartCheck;
	}

	public Content(String type, String data, Group parentGroup) {
		this.parentGroup = parentGroup;
		this.type = type;

		if (type.equals(Types.CONTENTTYPESEGMENT)) {
			String str = data.replaceAll(Types.SEGMENTSTARTDELIMITER, "").replaceAll(Types.SEGMENTENDDELIMITER, "");
			List<String> split = StringOps.fastSplit(str, "^^");
			try {
				repetitions = Long.parseLong(split.get(0));
			} catch (NumberFormatException e) {
				if (!(split.get(0).contains("{{") && split.get(0).contains("}}"))) {
					repetitions = 1;
					repetitionPattern = StringOps.fastSplit(split.get(0), ",");
					String msg = "WARNING: NO NUMBER FOUND @ " + split.get(0)
							+ "... in case of invalid condition, default value will be 1.";
					if (Types.getInstance().getIsUI())
						DULogger.log(300, msg);
					else
						lo.warn(msg);
					if (parentGroup.getGroupsProcessor().toLog)
						parentGroup.getGroupsProcessor().addLog(300, msg);

				} else {
					this.repetitionParamName = split.get(0).replace("{{", "").replace("}}", "");
					String msg = "WARNING: Segment repetitions are set based on a parameter : " + repetitionParamName;
					if (Types.getInstance().getIsUI())
						DULogger.log(300, msg);
					else
						lo.warn(msg);
					if (parentGroup.getGroupsProcessor().toLog)
						parentGroup.getGroupsProcessor().addLog(300, msg);
				}

			}
			String s = split.get(1);
			if (s.startsWith("\n")) {
				s = s.substring(1, s.length());
			}
			setDynamicContent(s);
		}
		if (type.equals(Types.CONTENTTYPEGROUP)) {
			setGroupContent(data);
		}
	}

	public String getValue(int iteration, int groupIteration, int totalGroupIterations) {
		if (type.equals(Types.CONTENTTYPESEGMENT)) {
			boolean shouldEval = true;
			if (repetitionPattern != null && !repetitionPattern.isEmpty())
				shouldEval = SegmentRepetitionEval.eval(groupIteration, totalGroupIterations, repetitionPattern);
			if (shouldEval) {
				StringBuilder sb = new StringBuilder();
				if (this.repetitionParamName != null) {
					try {
						this.repetitions = Long.parseLong(parentGroup.getGroupsProcessor().getParameterStore()
								.getParam(repetitionParamName).getCurrValue());
					} catch (NumberFormatException e) {
						String msg = "ERROR: Parameter value being used to determine repetitions of group/segment is not available yet or isn't a number... defaulting to 0."
								+ parentGroup.getGroupsProcessor().getParameterStore().getParam(repetitionParamName)
										.getCurrValue();
						if (Types.getInstance().getIsUI())
							DULogger.log(200, msg);
						else
							lo.error(msg);
						if (parentGroup.getGroupsProcessor().toLog)
							parentGroup.getGroupsProcessor().addLog(200, msg);
						repetitions = 0;
					}
				}
				for (int i = 0; i < repetitions; i++) {
					StringBuilder sbx = new StringBuilder();
					for (DynamicContent dcx : dc) {
						sbx.append(dcx.getDynamicContent(iteration));
					}
					String s = sbx.toString();
					if (s.length() > 0) {
						sb.append(s);
					} else
						sb.append("");
				}
				return sb.toString();
			} else
				return "";
		} else if (type.equals(Types.CONTENTTYPEGROUP)) {
			return group.getValue(iteration);
		} else
			return null;
	}

}

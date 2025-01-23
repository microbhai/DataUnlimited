package akhil.DataUnlimited.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.parameter.Parameter;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.LogStackTrace;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.StringOps;

public class Group {
	private static final Logger lo = LogManager.getLogger(Group.class.getName());
	private List<Content> contents = new ArrayList<>();
	private int repetitions;
	private int maxReps;
	private int minReps;
	private int compIteration = 0;
	private String name;
	private String repetitionParamName;
	private Processor p;

	public Processor getGroupsProcessor() {
		return p;
	}

	public int getGroupIteration() {
		return compIteration;
	}

	public String getGroupName() {
		return name;
	}

	public static List<Group> createGroups(String str, Map<Integer, Integer> pairs, Processor p) {
		List<Group> toReturn = new ArrayList<>();
		List<Integer> start = new ArrayList<>();
		for (Integer i : pairs.keySet()) {
			start.add(i);
		}

		for (Entry<Integer, Integer> i : pairs.entrySet()) {
			boolean isTopLevel = Parser.isTopLevel(i.getKey(), pairs);
			Map<Integer, Integer> internal = Parser.hasNesting(i.getKey(), pairs);
			// boolean hasNestedGroup === equal === (internal.size()==0) ? false : true
			boolean hasNestedGroup = !internal.isEmpty();

			if (isTopLevel && !hasNestedGroup) {
				toReturn.add(new Group(str, i.getKey(), i.getValue(), p));
			}
			if (isTopLevel && hasNestedGroup) {
				toReturn.add(new Group(str, i.getKey(), i.getValue(), internal, p));
			}
		}

		return toReturn;
	}

	public String getValue(int iteration) {
		StringBuilder sb = new StringBuilder();

		if (repetitionParamName != null) {
			try {
				this.repetitions = Integer.parseInt(p.getParameterStore().getParam(repetitionParamName).getCurrValue());
			} catch (NumberFormatException e) {
				String msg = "ERROR: Parameter value being used to determine repetitions of group/segment is not available yet... defaulting to 1.\n"
						+ LogStackTrace.get(e);
				if (Types.getInstance().getIsUI())
					DULogger.log(200, msg);
				else
					lo.error(msg);
				if (p.toLog)
					p.addLog(200, msg);
				this.repetitions = 1;
			}
			this.minReps = this.repetitions;
			this.maxReps = this.repetitions;

			List<Parameter> pl = p.getParameterStore().getParamsForGroup(this); // notifying all parameters for this
																				// group that group repetition has
																				// changed
			if (pl != null)
				for (Parameter p : pl)
					p.setGiveNewValue();
		}

		for (int i = 0; i < repetitions; i++) {
			StringBuilder sbx = new StringBuilder();
			compIteration = i + 1;

			String msgx = "DEBUG: Group Settings: " + this.name + ":GRP ITER:" + this.compIteration + ":REPS:"
					+ this.repetitions + ":ITER:" + iteration;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msgx);
			else
				lo.debug(msgx);

			for (Content c : contents) {

				sbx.append(c.getValue(iteration, compIteration, repetitions));
			}
			sb.append(sbx.toString());
		}
		repetitions++;
		if (repetitions > maxReps)
			repetitions = minReps;

		return sb.toString();
	}

	public Group(String data, int start, int end, Map<Integer, Integer> internal, Processor p) {
		this.p = p;
		String str = data.substring(start + Types.GROUPSTARTDELIMITER.length(), end);
		Map<Integer, Integer> pairs = Parser.getStartEndPositionMap(str, Types.GROUPSTARTDELIMITER,
				Types.GROUPENDDELIMITER, this.p);

		int x = str.indexOf("^^");

		List<String> nameReps = StringOps.fastSplit(str.substring(0, x), "#");

		if (nameReps.get(1).contains("-")) {
			List<String> split = StringOps.fastSplit(nameReps.get(1), "-");
			try {
				this.minReps = Integer.parseInt(split.get(0));
				this.maxReps = Integer.parseInt(split.get(1));
				if (this.minReps > this.maxReps) {
					this.repetitions = this.maxReps;
					this.maxReps = this.minReps;
					this.minReps = this.repetitions;
				} else
					this.repetitions = this.minReps;
			} catch (NumberFormatException e) {
				this.minReps = 1;
				this.maxReps = 1;
				this.repetitions = 1;
				String msg = "WARNING: NO NUMBER FOUND @ " + split.get(0) + " and " + split.get(1)
						+ " ... defaulting to 1.";
				if (Types.getInstance().getIsUI())
					DULogger.log(300, msg);
				else
					lo.warn(msg);
				if (p.toLog)
					p.addLog(300, msg);
			}
		} else if (nameReps.get(1).contains("{{") && nameReps.get(1).contains("}}")) {
			repetitionParamName = nameReps.get(1).replace("{{", "").replace("}}", "");
			String msg = "WARNING: Group repetitions are set based on a parameter : " + repetitionParamName;
			if (Types.getInstance().getIsUI())
				DULogger.log(300, msg);
			else
				lo.warn(msg);
			if (p.toLog)
				p.addLog(300, msg);
		} else {
			try {
				this.minReps = Integer.parseInt(nameReps.get(1));
				this.maxReps = Integer.parseInt(nameReps.get(1));
				this.repetitions = Integer.parseInt(nameReps.get(1));
			} catch (NumberFormatException e) {
				this.minReps = 1;
				this.maxReps = 1;
				this.repetitions = 1;
				String msg = "WARNING: NO NUMBER FOUND @ " + nameReps.get(1) + " ... defaulting to 1.";
				if (Types.getInstance().getIsUI())
					DULogger.log(300, msg);
				else
					lo.warn(msg);
				if (p.toLog)
					p.addLog(300, msg);
			}
		}
		this.name = nameReps.get(0);

		if (Parser.countSubstring(str, Types.SEGMENTSTARTDELIMITER) == Parser.countSubstring(str,
				Types.SEGMENTENDDELIMITER)) {
			Map<Integer, String> positions = Parser.getPositions(str, Types.SEGMENTSTARTDELIMITER,
					Types.CONTENTTYPESEGMENT);
			List<Integer> toRemove = new ArrayList<>();
			for (Integer i : positions.keySet()) {
				for (Entry<Integer, Integer> j : pairs.entrySet()) {
					if (i > j.getKey() && i < j.getValue()) {
						toRemove.add(i);
					}
				}
			}

			for (Integer i : toRemove) {
				positions.remove(i);
			}

			Map<Integer, Integer> hm = new TreeMap<>();
			int lastInt = 0;
			for (Entry<Integer, Integer> i : pairs.entrySet()) {
				if (Parser.isTopLevel(i.getKey(), pairs)) {
					hm.put(lastInt, i.getKey());
					lastInt = i.getValue() + 8;
				}
			}
			hm.put(lastInt, str.length());
			StringBuilder sb = new StringBuilder();
			for (Entry<Integer, Integer> i : hm.entrySet()) {
				sb.append(str.substring(i.getKey(), i.getValue()));
			}

			str = sb.toString();

			List<String> dynamicStrings = Parser.getInBetweenFast(str, Types.SEGMENTSTARTDELIMITER,
					Types.SEGMENTENDDELIMITER, false);

			for (Integer i : internal.keySet()) {
				if (Parser.isTopLevel(i, internal)) {
					positions.put(i - start - 7, Types.CONTENTTYPEGROUP);
				}
			}

			for (Entry<Integer, String> i : positions.entrySet()) {
				if (i.getValue().equals(Types.CONTENTTYPESEGMENT)) {
					contents.add(new Content(Types.CONTENTTYPESEGMENT, dynamicStrings.get(0), this));
					dynamicStrings.remove(0);
				}
				if (i.getValue().equals(Types.CONTENTTYPEGROUP)) {
					for (Entry<Integer, Integer> j : internal.entrySet()) {
						if (i.getKey() == j.getKey() - start - 7) {
							contents.add(new Content(Types.CONTENTTYPEGROUP, data.substring(j.getKey(), j.getValue()),
									this));
						}
					}
				}
			}
			String msg = "INFO: Group created: " + name + " @ " + new Date().toString();
			if (Types.getInstance().getIsUI())
				DULogger.log(400, msg);
			else
				lo.info(msg);
			if (p.toLog)
				p.addLog(400, msg);
		} else {
			String msg = "ERROR: Number of Segment starts are not equal to number of Segment endings. Error in DMS script syntax in Group "
					+ name;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (p.toLog)
				p.addLog(200, msg);
		}

	}

	public Group(String data, int start, int end, Processor p) {
		this.p = p;
		String str = data.substring(start + 7, end);
		int x = str.indexOf("^^");

		List<String> nameReps = StringOps.fastSplit(str.substring(0, x), "#");
		if (nameReps.get(1).contains("-")) {
			List<String> split = StringOps.fastSplit(nameReps.get(1), "-");
			try {
				this.minReps = Integer.parseInt(split.get(0));
				this.maxReps = Integer.parseInt(split.get(1));
				if (this.minReps > this.maxReps) {
					this.repetitions = this.maxReps;
					this.maxReps = this.minReps;
					this.minReps = this.repetitions;
				} else
					this.repetitions = this.minReps;
			} catch (NumberFormatException e) {
				this.minReps = 1;
				this.maxReps = 1;
				this.repetitions = 1;
				String msg = "WARNING: NO NUMBER FOUND @ " + split.get(0) + " and " + split.get(1)
						+ " ... defaulting to 1.";
				if (Types.getInstance().getIsUI())
					DULogger.log(300, msg);
				else
					lo.warn(msg);
				if (p.toLog)
					p.addLog(300, msg);
			}
		} else if (nameReps.get(1).contains("{{") && nameReps.get(1).contains("}}")) {
			repetitionParamName = nameReps.get(1).replace("{{", "").replace("}}", "");
			String msg = "WARNING: Group repetitions are set based on a parameter : " + repetitionParamName;
			if (Types.getInstance().getIsUI())
				DULogger.log(300, msg);
			else
				lo.warn(msg);
			if (p.toLog)
				p.addLog(300, msg);
		} else {
			try {
				this.minReps = Integer.parseInt(nameReps.get(1));
				this.maxReps = Integer.parseInt(nameReps.get(1));
				this.repetitions = Integer.parseInt(nameReps.get(1));
			} catch (NumberFormatException e) {
				this.minReps = 1;
				this.maxReps = 1;
				this.repetitions = 1;
				String msg = "WARNING: NO NUMBER FOUND @ " + nameReps.get(1) + " ... defaulting to 1.";
				if (Types.getInstance().getIsUI())
					DULogger.log(300, msg);
				else
					lo.warn(msg);
				if (p.toLog)
					p.addLog(300, msg);
			}
		}
		this.name = nameReps.get(0);

		if (Parser.countSubstring(str, Types.SEGMENTSTARTDELIMITER) == Parser.countSubstring(str,
				Types.SEGMENTENDDELIMITER)) {
			Map<Integer, String> positions = Parser.getPositions(str, Types.SEGMENTSTARTDELIMITER,
					Types.CONTENTTYPESEGMENT);
			List<String> dynamicStrings = Parser.getInBetweenFast(str, Types.SEGMENTSTARTDELIMITER,
					Types.SEGMENTENDDELIMITER, false);

			for (Entry<Integer, String> i : positions.entrySet()) {
				if (i.getValue().equals(Types.CONTENTTYPESEGMENT)) {
					contents.add(new Content(Types.CONTENTTYPESEGMENT, dynamicStrings.get(0), this));
					dynamicStrings.remove(0);
				}
			}
			String msg = "INFO: Group created: " + name + " @ " + new Date().toString();
			if (Types.getInstance().getIsUI())
				DULogger.log(400, msg);
			else
				lo.info(msg);
			if (p.toLog)
				p.addLog(400, msg);
		} else {
			String msg = "ERROR: Number of Segment starts are not equal to number of Segment endings. Error in DMS script syntax in Group "
					+ name;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (p.toLog)
				p.addLog(200, msg);
		}
	}

}

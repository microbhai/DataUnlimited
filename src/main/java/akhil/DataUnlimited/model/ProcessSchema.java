package akhil.DataUnlimited.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import akhil.DataUnlimited.model.types.Types;

public class ProcessSchema {
	HashMap<String, String> individuals = new HashMap<String, String>();
	List<String> combinations = new ArrayList<String>();
	HashMap<String, String[]> combinationMap = new HashMap<String, String[]>();
	List<String> origFile;

	public ProcessSchema(List<String> file) {
		this.combinations = separateCombinationsFromIndividuals(file);
		this.combinationMap = combinationMapping(this.combinations);
	}

	public ProcessSchema(List<String> file, List<String> origFile) {
		this.origFile = origFile;
		this.combinations = separateCombinationsFromIndividuals(file);
		this.combinationMap = combinationMapping(this.combinations);
	}

	public String getValue() {
		StringBuilder sb = new StringBuilder();

		for (String tag : combinationMap.keySet()) {
			if (isTopLevel(tag, combinationMap))
				sb.append(getTagValue(tag, combinationMap));
		}
		return sb.toString();
	}

	public boolean isTopLevel(String tag, HashMap<String, String[]> combinationMap) {
		boolean toReturn = true;
		for (String s : combinationMap.keySet()) {
			for (String str : combinationMap.get(s)) {
				if (str.equals(tag))
					toReturn = false;
			}
		}
		return toReturn;
	}

	public String getTagValue(String tag, HashMap<String, String[]> combinationMap) {
		StringBuilder sb = new StringBuilder();
		boolean inside = false;
		boolean hasStarted = false;
		sb.append(Types.GROUPSTARTDELIMITER + tag + "#1-1^^\n");

		for (String s : combinationMap.get(tag)) {
			if (individuals.containsKey(s)) {
				if (hasStarted && inside) {
					sb.append(individuals.get(s));
					sb.append("\n");
				} else if (!hasStarted && !inside) {
					sb.append(Types.SEGMENTSTARTDELIMITER + "1^^\n<" + tag + ">\n");
					hasStarted = true;
					inside = true;
					sb.append(individuals.get(s));
					sb.append("\n");
				} else if (hasStarted && !inside) {
					sb.append(Types.SEGMENTSTARTDELIMITER + "1^^\n");
					inside = true;
					sb.append(individuals.get(s));
					sb.append("\n");
				}
			} else {
				if (inside && hasStarted) {
					sb.append(Types.SEGMENTENDDELIMITER + "\n");
					inside = false;
					sb.append(getTagValue(s, combinationMap));
				} else if (!inside && hasStarted) {
					sb.append(getTagValue(s, combinationMap));
				} else if (!inside && !hasStarted) {
					sb.append(Types.SEGMENTSTARTDELIMITER + "1^^\n<" + tag + ">\n" + Types.SEGMENTENDDELIMITER + "\n");
					hasStarted = true;
					sb.append(getTagValue(s, combinationMap));
				}
			}
		}
		if (inside)
			sb.append("</" + tag + ">\n" + Types.SEGMENTENDDELIMITER + "\n" + Types.GROUPENDDELIMITER + "\n");
		else
			sb.append(
					// Types.STATICSTARTDELIMITER+"1^^\n</"+tag+">\n"+Types.STATICENDDELIMITER+"\n"+
					Types.GROUPENDDELIMITER + "\n");
		return sb.toString();
	}

	public HashMap<String, String[]> combinationMapping(List<String> combinations) {
		HashMap<String, String[]> toReturn = new HashMap<String, String[]>();
		for (String str : combinations) {
			String[] split = str.split(" ");
			String[] items = str.substring(str.indexOf("(") + 1, str.indexOf(")")).replace(" ", "").split(",");
			toReturn.put(split[1], items);
		}
		return toReturn;
	}

	public List<String> separateCombinationsFromIndividuals(List<String> s) {
		List<String> combinations = new ArrayList<String>();

		for (String str : s) {
			if (str.contains(Types.DTDSCHEMAELEMENT)) {
				if (str.contains(Types.DTDSCHEMAPCDATA)) {
					String[] split = str.split(" ");
					String value = "";
					if (origFile != null && origFile.size() > 0) {
						boolean isValueSet = false;
						for (String orig : origFile) {
							if (orig.indexOf("<" + split[1] + ">") != -1 && orig.indexOf("</" + split[1] + ">") != -1) {
								value = "<" + split[1] + ">"
										+ orig.substring(orig.indexOf("<" + split[1] + ">") + 2 + split[1].length(),
												orig.indexOf("</" + split[1] + ">"))
										+ "</" + split[1] + ">";
								isValueSet = true;
								break;
							}
						}
						if (!isValueSet) {
							value = "<" + split[1] + "></" + split[1] + ">";
						}
					} else {
						value = "<" + split[1] + "></" + split[1] + ">";
					}
					individuals.put(split[1], value);
				} else {
					for (String charToReplace : Types.getDTDSchemaCharStorePlacePlus())
						str = str.replace(charToReplace, "");
					combinations.add(str);
				}
			}
		}
		return combinations;
	}
}

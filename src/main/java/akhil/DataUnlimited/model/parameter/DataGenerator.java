package akhil.DataUnlimited.model.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import akhil.DataUnlimited.model.Parser;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.StringOps;

public class DataGenerator {

	private static DataGenerator dg = new DataGenerator();

	private HashMap<String, List<String>> data = new HashMap<>();
	private HashMap<String, Parameter> paramData = new HashMap<>();

	public HashMap<String, Parameter> getParamData() {
		return paramData;
	}

	private ParameterStore ps = new ParameterStore(null);

	public HashMap<String, List<String>> getData() {
		return data;
	}

	public static DataGenerator getInstance() {
		if (dg == null)
			dg = new DataGenerator();
		return dg;
	}

	public void ingestDataFromDat(String name, String values) {

		List<String> allVal = Parser.getInBetweenFast(values, "\"", "\"", true);
		for (String s : allVal) {

			if ((s.startsWith(Types.PARAMSTARTSHOW) || s.startsWith(Types.PARAMSTARTHIDE))
					&& s.endsWith(Types.PARAMEND)) {
				List<String> str = StringOps.fastSplit(s.substring(8, s.length() - 2), "^");
				paramData.put(name, new Parameter(str, ps));
			} else {
				if (data.containsKey(name))
					data.get(name).add(s);
				else {
					List<String> x = new ArrayList<>();
					x.add(s);
					data.put(name, x);
				}

			}

		}
	}

	public String getData(String type) {
		if (data.containsKey(type)) {
			return data.get(type).get(new Random().nextInt(data.get(type).size()));
		} else if (paramData.containsKey(type)) {
			return paramData.get(type).getValue(1, 1);
		} else
			return null;
	}
}

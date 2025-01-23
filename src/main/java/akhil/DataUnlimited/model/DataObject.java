package akhil.DataUnlimited.model;

import akhil.DataUnlimited.model.parameter.DataGenerator;

public class DataObject {

	private String type;
	private String value;

	public DataObject(String type, String value) {
		this.type = type;
		this.value = value;
	}

	public String getValue() {
		if (type.equals("nondata"))
			return value;
		else if (type.equals("data"))
			return DataGenerator.getInstance().getData(value);
		else
			return null;
	}

}

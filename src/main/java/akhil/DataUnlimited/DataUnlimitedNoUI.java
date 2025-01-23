package akhil.DataUnlimited;

import java.util.List;

import akhil.DataUnlimited.model.types.Types;

public class DataUnlimitedNoUI {
	public DataUnlimitedNoUI() {
		Types.getInstance().init("../datalib");
	}

	public void process(List<String> param) {
		new DataUnlimitedApi().generateData(param);
	}
}

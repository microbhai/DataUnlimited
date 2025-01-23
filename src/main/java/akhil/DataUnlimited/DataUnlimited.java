package akhil.DataUnlimited;

import akhil.DataUnlimited.ui.Logo;
import akhil.DataUnlimited.ui.UI;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataUnlimited {
	static boolean logoOn = false;

	public static void main(String[] args) {
		if (args.length == 0) {
			Logo x = new Logo();
			EventQueue.invokeLater(() -> {
				Types.getInstance().init("../datalib");
				UI ex = new UI();
				Types.getInstance().setUI(ex);
				ex.setVisible(true);
				logoOn = false;
			});
			logo(x);

		} else {
			List<String> param = new ArrayList<>(Arrays.asList(args));
			new DataUnlimitedNoUI().process(param);
		}
	}

	public static void logo(Logo x) {
		logoOn = true;
		x.setVisible(true);
		while (logoOn) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				DULogger.log(0, "Exception occured while starting DMS.");
				Thread.currentThread().interrupt();
			}
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			DULogger.log(0, "Exception occured while starting DMS.");
			Thread.currentThread().interrupt();
		}
		x.setVisible(false);
	}
}

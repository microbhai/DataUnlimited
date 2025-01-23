package akhil.DataUnlimited.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;

public class OSCommand {

	private static final Logger lo = LogManager.getLogger(OSCommand.class.getName());

	public String runCommand(String command) {
		StringBuilder output = new StringBuilder();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, "ERROR: While executing command " + command + "\n" + LogStackTrace.get(e));
			else
				lo.error("ERROR: While executing command " + command + "\n" + LogStackTrace.get(e));

		}

		return output.toString();
	}
}

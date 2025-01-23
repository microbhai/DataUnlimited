package akhil.DataUnlimited.util;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;

public class PayLoadExtractor {
	private static final Logger lo = LogManager.getLogger(PayLoadExtractor.class.getName());

	public void extract(File selectedDir, String fileSuffix, String start, String end) {
		for (File fileEntry : selectedDir.listFiles()) {
			if (!fileEntry.isDirectory()) {

				String fileContent = FileOperation.getFileContentAsString(fileEntry.getPath());
				int currPosition = 0;
				int count = 0;
				boolean flag = true;
				while (flag) {
					int startPayloadIndex = fileContent.indexOf(start, currPosition) + start.length();
					int endPayloadIndex = fileContent.indexOf(end, currPosition);
					if (startPayloadIndex != -1 && endPayloadIndex != -1) {
						if (fileSuffix != null && fileSuffix.length() > 0)
							FileOperation.writeFile("", fileEntry.getPath() + "_PL_" + count + "." + fileSuffix,
									fileContent.substring(startPayloadIndex, endPayloadIndex));
						else
							FileOperation.writeFile("", fileEntry.getPath() + "_PL_" + count + ".txt",
									fileContent.substring(startPayloadIndex, endPayloadIndex));

						if (Types.getInstance().getIsUI())
							DULogger.log(400, fileEntry.getPath() + "... extracted");
						else
							lo.info(fileEntry.getPath() + "... extracted");

						currPosition = endPayloadIndex + end.length();
						count++;
					} else {
						if (Types.getInstance().getIsUI())
							DULogger.log(200, "ERROR: Payload tags not found... \n Start tag:" + start + "\n End tag:"
									+ end + "\n" + "Index Position:" + currPosition + "\n");
						else
							lo.error("ERROR: Payload tags not found... \n Start tag:" + start + "\n End tag:" + end
									+ "\n" + "Index Position:" + currPosition + "\n");

						flag = false;
					}
					if (Types.getInstance().getIsUI())
						DULogger.log(400, "All Payloads Extracted...");
					else
						lo.info("All Payloads Extracted...");

				}
			}
		}

	}
}

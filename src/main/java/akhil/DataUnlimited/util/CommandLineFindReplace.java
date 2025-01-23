package akhil.DataUnlimited.util;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;

public class CommandLineFindReplace {

	private CommandLineFindReplace() {
	}

	private static final Logger lo = LogManager.getLogger(CommandLineFindReplace.class.getName());
	private static final String ERRORMSG = "ERROR:Command line parameter names starts and end in curly braces and should have atleast 1 character within... can't put blank space (for space type #DMSSPACE#)";

	public static String findReplace(String strToPrint, List<String> replacements) {
		for (String fr : replacements) {
			if (fr.contains("#regex#")) {
				String find = fr.substring(0, fr.indexOf(Types.DMSCMDLINEREGEXVALUESEPARATOR))
						.replace(Types.DMSSPACE, " ").replace(Types.DMSBACKSLASH, "\\");
				String replace = fr
						.substring(fr.indexOf(Types.DMSCMDLINEREGEXVALUESEPARATOR)
								+ Types.DMSCMDLINEREGEXVALUESEPARATOR.length(), fr.length())
						.replace(Types.DMSSPACE, " ").replace(Types.DMSBACKSLASH, "\\");
				if (find.startsWith("{") && find.endsWith("}") && find.length() >= 3)
					find = find.substring(1, find.length() - 1);
				else {
					String msg = ERRORMSG;
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					return null;
				}
				if (replace.startsWith("{") && replace.endsWith("}") && replace.length() >= 3)
					replace = replace.substring(1, replace.length() - 1);
				else {
					String msg = ERRORMSG;
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					return null;

				}
				if (replace.equals(Types.DMSNULL) || replace.equals(Types.DMSNULLLOWER)) {
					strToPrint = strToPrint.replaceAll(find, "");
				} else {
					strToPrint = strToPrint.replaceAll(find, replace);
				}
			} else {
				String find = fr.substring(0, fr.indexOf(Types.DMSCMDLINENAMEVALUESEPARATOR))
						.replace(Types.DMSSPACE, " ").replace(Types.DMSBACKSLASH, "\\");
				String replace = fr
						.substring(fr.indexOf(Types.DMSCMDLINENAMEVALUESEPARATOR)
								+ Types.DMSCMDLINENAMEVALUESEPARATOR.length(), fr.length())
						.replace(Types.DMSSPACE, " ").replace(Types.DMSBACKSLASH, "\\");
				if (find.startsWith("{") && find.endsWith("}") && find.length() >= 3)
					find = find.substring(1, find.length() - 1);
				else {
					String msg = ERRORMSG;
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					return null;

				}
				if (replace.startsWith("{") && replace.endsWith("}") && replace.length() >= 3)
					replace = replace.substring(1, replace.length() - 1);
				else {
					String msg = ERRORMSG;
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					return null;
				}
				if (replace.equals(Types.DMSNULL) || replace.equals(Types.DMSNULLLOWER)) {
					strToPrint = strToPrint.replace(find, "");
				} else {
					strToPrint = strToPrint.replace(find, replace);
				}

			}
		}
		return strToPrint;
	}

}

package akhil.DataUnlimited.model.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DBUtil;
import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.StorageAndRetrieval;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.StringOps;

public class DBQueries {
	private static final Logger lo = LogManager.getLogger(DBQueries.class.getName());
	private static final String DMSDELIM = "<DMSDELIM>";
	private Map<String, List<List<String>>> dbdata = new HashMap<>();
	private List<String> dataprint = new ArrayList<>();

	public void clearAll() {
		dbdata.clear();
		dataprint.clear();
	}

	public void setQuery(String filename, String filecontent, boolean printToFile, String findReplace,
			String outputFileName) {
		if (!dbdata.keySet().contains(filename)) {
			if (findReplace.length() > 0) {
				List<String> fr = StringOps.fastSplit(findReplace, DMSDELIM);
				if (fr.size() % 2 != 0) {
					if (Types.getInstance().getIsUI())
						DULogger.log(200,
								"ERROR: Improper find and replace values for DB param... number of find and replace should be even. Find Replace will be skipped...");
					else
						lo.error(
								"ERROR: Improper find and replace values for DB param... number of find and replace should be even. Find Replace will be skipped...");

				} else {
					int total = fr.size();
					List<String> find = new ArrayList<>();
					List<String> replace = new ArrayList<>();
					for (int i = 0; i < total; i++) {
						if (i % 2 == 0) {
							find.add(fr.get(i));
						} else {
							replace.add(fr.get(i));
						}
					}
					total = find.size();
					for (int i = 0; i < total; i++) {
						filecontent = filecontent.replace(find.get(i), replace.get(i));
					}
				}
			}
			List<String> split = StringOps.fastSplit(filecontent, "<DMSDBDELIM>");

			if (split.size() != 4) {
				if (Types.getInstance().getIsUI())
					DULogger.log(200,
							"ERROR: Improper DB Configuration found... Config details should have 4 values, connection string / url, user, password and query.");
				else
					lo.error(
							"ERROR: Improper DB Configuration found... Config details should have 4 values, connection string / url, user, password and query.");

			} else {
				if (Types.getInstance().getIsUI())
					DULogger.log(400, "INFO: Getting parameter values from DB... Running query.");
				else
					lo.info("INFO: Getting parameter values from DB... Running query.");

				List<String> result = DBUtil.getInstance().executeSQL(split.get(0).trim(), split.get(1).trim(),
						StorageAndRetrieval.toUse(split.get(2).trim()), split.get(3).trim());

				if (!result.isEmpty()) {
					int howmany = StringOps.fastSplit(result.get(0), DMSDELIM).size();
					List<List<String>> aas = new ArrayList<>();
					for (int i = 0; i < howmany; i++)
						aas.add(new ArrayList<String>());
					for (String s : result) {
						List<String> sa = StringOps.fastSplit(s, DMSDELIM);
						for (int x = 0; x < howmany; x++) {
							aas.get(x).add(sa.get(x));
						}
					}
					dbdata.put(filename, aas);

					if (printToFile && !dataprint.contains(filename)) {

						if (outputFileName.length() > 0)
							queryOutputToFile(filename, outputFileName);
						else
							queryOutputToFile(filename, filename + "_QUERY_RESULT.txt");
						dataprint.add(filename);
					}
				} else {
					if (Types.getInstance().getIsUI())
						DULogger.log(300, "WARNING: No results were returned during query execution.");
					else
						lo.warn("WARNING: No results were returned during query execution.");
				}
			}
		} else {
			if (Types.getInstance().getIsUI())
				DULogger.log(300, "WARNING: Query already registered");
			else
				lo.warn("WARNING: Query already registered");
		}
	}

	public void queryOutputToFile(String filename, String outputfilename) {
		FileOperation.deleteFile(outputfilename, "");
		List<List<String>> data = dbdata.get(filename);
		int columns = data.size();
		int rows = data.get(0).size();
		for (int i = 0; i < rows; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < columns; j++) {
				sb.append(data.get(j).get(i));
				if (j != columns - 1)
					sb.append(DMSDELIM);
			}
			if (Types.getInstance().getIsUI())
				DULogger.log(500, "DEBUG: DB Query Print: " + sb.toString());
			else
				lo.debug("DEBUG: DB Query Print: " + sb.toString());

			FileOperation.writeFile("", outputfilename, sb.toString() + "\n", true);
		}
	}

	public List<String> getData(String filename, int columnNumber) {
		return dbdata.get(filename).get(columnNumber);
	}
}

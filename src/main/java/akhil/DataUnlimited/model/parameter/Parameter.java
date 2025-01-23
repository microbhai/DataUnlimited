package akhil.DataUnlimited.model.parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.dataextractor.hierarchicaldoc.UtilityFunctions;
import akhil.DataUnlimited.model.DataObject;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.LogStackTrace;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.StringOps;

public class Parameter {
	private static final Logger lo = LogManager.getLogger(Parameter.class.getName());
	private String currValue;

	public void setCurrValue(String s) {
		this.currValue = s;
	}

	public String getCurrValue() {
		return this.currValue;
	}

	private String paramType; // file, randomString, randomNumber, db, time, uniqueNumber etc.
	private String paramName;
	private String embeddedString;
	private String decodeParamName;
	private String decodeDefaultValue;

	private Map<String, String> decodeMap;

	private static final String DMSDOUBLEQUOTE = "'<DMSDOUBLEQUOTE>'";

	public String getParamName() {
		return paramName;
	}

	public String getEmbeddedString() {
		return embeddedString;
	}

	public String getDecodeParamName() {
		return decodeParamName;
	}

	public String getDecodeDefaultValue() {
		return decodeDefaultValue;
	}

	public String getDecodeValue(String key) {
		String toReturn = null;
		if (decodeMap.keySet().contains(key)) {
			toReturn = decodeMap.get(key);
		} else {
			for (String s : decodeMap.keySet()) {
				if (ps.getParamNames().contains(s)) {
					if (ps.getParam(s).getCurrValue().equals(key))
						toReturn = decodeMap.get(s);
				}
			}
		}
		return toReturn;
	}

	private int width;
	private boolean isPrimaryTightFile;

	private boolean giveNewValue = false;

	private boolean hasDoubleQuote = false;
	private boolean setDefaultDecode = false;

	public boolean getDefaultDecode() {
		return setDefaultDecode;
	}

	public void setGiveNewValue() {
		giveNewValue = true;
	}

	public boolean isPrimaryTightFile() {
		return isPrimaryTightFile;
	}

	private int widthx = 0;
	private String dateType;

	private String dbUrl;
	private String query;
	private String lastQuery = "";

	public String getDbUrl() {
		return dbUrl;
	}

	public String getQuery() {
		return query;
	}

	public String getLastQuery() {
		return lastQuery;
	}

	public void setLastQuery(String lastQuery) {
		this.lastQuery = lastQuery;
	}

	public String getDateType() {
		return dateType;
	}

	private String dateTypex;
	private String paramSourceFile;

	public String getParamSourceFile() {
		return paramSourceFile;
	}

	public String getDateTypex() {
		return dateTypex;
	}

	private String format;

	public String getFormat() {
		return format;
	}

	private String decimalFormatString = "0.#";
	private int columnInSource;
	private List<String> paramData;
	private Map<String, List<String>> paramDataMap;

	public List<String> getParamData() {
		if (returnCountOnly) {
			List<String> as = new ArrayList<>();
			if (isFilterValueParam)
				as.add(String.valueOf(paramDataMap.get(lastFilterValue).size()));
			else
				as.add(String.valueOf(paramData.size()));
			return as;
		} else if (isFilterValueParam) {
			return paramDataMap.get(lastFilterValue);
		} else
			return paramData;
	}

	private int paramIndex = 0;
	private int recycleAfter = 0;
	private int startFrom = 0;

	private String indexParameterName = "";

	public String getIndexParameterName() {
		return indexParameterName;
	}

	public int getStartFrom() {
		return startFrom;
	}

	public int getRecycleAfterSetting() {
		return recycleAfter;
	}

	private boolean returnCountOnly = false;
	private boolean returnSorted = false;
	private boolean returnUnique = false;
	private boolean isFilterValueParam = false;
	private String lastFilterValue = "";
	private String filterValueParamName = "";

	public void setLastFilterValue(String lastFilterValue) {
		this.lastFilterValue = lastFilterValue;
	}

	public String getFilterValueParamName() {
		return filterValueParamName;
	}

	public String getLastFilterValue() {
		return lastFilterValue;
	}

	public boolean getIsFilterValueParam() {
		return isFilterValueParam;
	}

	public boolean getReturnCountOnly() {
		return returnCountOnly;
	}

	public int getRecycleAfter() {
		if (recycleAfter == 0) {
			if (isFilterValueParam)
				return paramDataMap.get(lastFilterValue).size();
			else
				return paramData.size();

		} else {
			return recycleAfter;
		}
	}

	public int getParamIndex() {
		return paramIndex;
	}

	public void setParamIndex(int paramIndex) {
		this.paramIndex = paramIndex;
	}

	private int iteration;
	private int groupIteration;

	public int getGroupIteration() {
		return groupIteration;
	}

	private String value;

	public String getValue() {
		return value;
	}

	private String paramIterationPolicy; // global eachOccurence group
	private boolean isZeroPadded = false;

	public boolean getZeroPadded() {
		return isZeroPadded;
	}

	private List<String> usedNumbers = new ArrayList<>();

	public List<String> getUsedNumbers() {
		return usedNumbers;
	}

	private boolean randomize = false;

	public boolean isRandom() {
		return randomize;
	}

	private boolean resetWithFile = false;
	private boolean iterationChanged = false;

	public boolean getResetWithFile() {
		return resetWithFile;
	}

	public boolean getIterationChanged() {
		return iterationChanged;
	}
	public String getDecimalFormatString()
	{
		return decimalFormatString;
	}

	private long upperLimit;
	private long lowerLimit;

	public long getUpperLimit() {
		return upperLimit;
	}

	public long getLowerLimit() {
		return lowerLimit;
	}

	private List<DataObject> dol;

	public List<DataObject> getDataObjects() {
		return dol;
	}

	private String caseChange = "asis";

	public String getCaseChange() {
		return caseChange;
	}

	private String evalExpression;

	public String getEvalExpression() {
		return evalExpression;
	}

	private ParameterStore ps;

	public ParameterStore getParameterStore() {
		return ps;
	}

	public int getWidth() {
		if (widthx == 0)
			return this.width;
		else {
			if (width < widthx)
				return ThreadLocalRandom.current().nextInt(width, widthx + 1);
			else
				return ThreadLocalRandom.current().nextInt(widthx, width + 1);
		}
	}

	public Parameter(List<String> paramDetails, ParameterStore ps) {
		this.iteration = 0;
		this.paramType = paramDetails.get(1);
		this.paramName = paramDetails.get(0);
		this.paramIterationPolicy = paramDetails.get(2);
		this.ps = ps;

		String msg = "INFO: Processing Parameter: " + this.paramName + "		Type: " + this.paramType;
		if (Types.getInstance().getIsUI())
			DULogger.log(400, msg);
		else
			lo.info(msg);
		if (ps.getProcessor() != null && ps.getProcessor().toLog)
			ps.getProcessor().addLog(400, msg);

		// array 0
		// db 1
		// file 2
		// groupIteration 3
		// numberSequence 4
		// randomAlphaNumericString 5
		// randomDateTime 6
		// randomLowerCaseString 7
		// randomMixedCaseString 8
		// randomNumber 9
		// randomUpperCaseString 10
		// rangeDateTime 11
		// rangeRandomNumber 12
		// relativeDateTime 13
		// uniqueNumber 14
		// customDataType 15
		// numberSequenceRange 16
		// tightfile 17
		// eval 18
		// virtualfile 19
		// multiParamFile 20
		// dbQuery 21
		// where 22
		// wherelike 23
		// uuid 24
		// dbStore 25
		// embeddedParamString 26
		// decode 27
		// mathCalc 28

		try {
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[18])) // eval type parameter
			{
				evalExpression = paramDetails.get(3);
				if (paramDetails.size() == 5)
					setCurrValue(paramDetails.get(4));
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[28])) // mathCalc type parameter
			{
				evalExpression = paramDetails.get(3);
				decimalFormatString = paramDetails.get(4);
				if (paramDetails.size() == 6)
					setCurrValue(paramDetails.get(5));
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[26])) { // embeddedParamString
				embeddedString = paramDetails.get(3);
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[27])) { // decode
				decodeParamName = paramDetails.get(3);
				String decodeString = paramDetails.get(4);

				if (decodeString.contains("\"")) {
					decodeString = decodeString.replace("\\\"", DMSDOUBLEQUOTE);
					hasDoubleQuote = true;
				}

				List<String> mapStrings = UtilityFunctions.getInBetweenFast(decodeString, "\"", "\"", true);

				if (mapStrings.size() % 2 != 0 && mapStrings.get(mapStrings.size() - 1).equalsIgnoreCase("default")) {
					setDefaultDecode = true;
					mapStrings.remove(mapStrings.size() - 1);
				}
				if (mapStrings.size() % 2 == 0 && mapStrings.get(mapStrings.size() - 2).equalsIgnoreCase("default")) {
					setDefaultDecode = true;
					decodeDefaultValue = mapStrings.get(mapStrings.size() - 1);
					mapStrings.remove(mapStrings.size() - 1);
					mapStrings.remove(mapStrings.size() - 1);
				}
				if (mapStrings.size() % 2 == 0) {
					decodeMap = new HashMap<>();
					for (int i = 0; i < mapStrings.size(); i++) {
						String key = null;
						String value = null;
						if (i % 2 == 0) {
							if (hasDoubleQuote) {
								key = mapStrings.get(i).replace(DMSDOUBLEQUOTE, "\"");
								value = mapStrings.get(i + 1).replace(DMSDOUBLEQUOTE, "\"");
							} else {
								key = mapStrings.get(i);
								value = mapStrings.get(i + 1);
							}
						}
						if (key != null && value != null) {
							decodeMap.put(key, value);
						}
					}

				} else {
					if (Types.getInstance().getIsUI())
						DULogger.log(200,
								"ERROR: Key/Values for decode parameter must be an even count (excluding defaults)... "
										+ decodeString);
					else
						lo.error("ERROR: Key/Values for decode parameter must be an even count (excluding defaults)... "
								+ decodeString);
				}

			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[25])) // dbStore type parameter
			{
				dbUrl = Types.DBURL;
				query = paramDetails.get(3);
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[21])) // dbQuery type parameter
			{
				dbUrl = Types.DBURL;
				query = paramDetails.get(3);
				try {
					this.startFrom = Integer.parseInt(paramDetails.get(4)) - 1;
				} catch (NumberFormatException e) {
					this.startFrom = 0;
					this.indexParameterName = paramDetails.get(4).trim();
				}
				this.recycleAfter = Integer.parseInt(paramDetails.get(5));
				this.resetWithFile = Boolean.parseBoolean(paramDetails.get(7));
				if (paramDetails.get(6).equals("true"))
					this.randomize = true;
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[17])) // tightfile type parameter
			{
				if (paramDetails.size() == 15)
					setTightFileParam(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
							paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
							paramDetails.get(10), paramDetails.get(11), ps, paramDetails.get(12), paramDetails.get(13),
							paramDetails.get(14));
				else if (paramDetails.size() == 14) {
					if (paramDetails.get(11).contains("true") || paramDetails.get(11).contains("false"))
						setTightFileParam(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
								paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
								paramDetails.get(10), null, ps, paramDetails.get(11), paramDetails.get(12),
								paramDetails.get(13));
					else
						setTightFileParam(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
								paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
								paramDetails.get(10), paramDetails.get(11), ps, "", paramDetails.get(12),
								paramDetails.get(13));
				} else
					setTightFileParam(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
							paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
							paramDetails.get(10), null, ps, "", paramDetails.get(11), paramDetails.get(12));
			}

			/*
			 * setParamSourceFile( paramSourceFile, columnInSource, startFrom, recycleAfter,
			 * delim, randomize, resetWithFile, rowMultiplier, isVirtualFile, sectionName,
			 * returnCountOnly, returnSorted, returnUnique)
			 * 
			 * parameterName 0 ^file 1 ^substitutionPolicy-global/eachOccurence/group 2
			 * ^filePath 3 ^columnNumber 4 ^startFrom(1 for starting from start) 5
			 * ^resetAfterNumber(0 for no limit) 6 ^delimiter e.g. , 7 ^randomize true/false
			 * 8 ^reset in each file - true/false 9 ^ (optional) row multiplier - format
			 * 1x2_3x4_6x3 etc 10 ^returnCountOnly(true/false) 11
			 */

			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[2])) // file type parameter
			{
				if (paramDetails.size() == 14)
					setParamSourceFile(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
							paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
							paramDetails.get(10), false, null, paramDetails.get(11), paramDetails.get(12),
							paramDetails.get(13));
				else if (paramDetails.size() == 13) {
					if (paramDetails.get(10).contains("true") || paramDetails.get(10).contains("false"))
						setParamSourceFile(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
								paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
								null, false, null, paramDetails.get(10), paramDetails.get(11), paramDetails.get(12));
					else
						setParamSourceFile(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
								paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
								paramDetails.get(10), false, null, "", paramDetails.get(11), paramDetails.get(12));
				} else
					setParamSourceFile(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
							paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), null,
							false, null, "", paramDetails.get(10), paramDetails.get(11));
			}

			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[22])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[23])) // where and wherelike file
																							// type parameter
			{
				boolean isVirtualFile = paramDetails.get(3).contains("virtual");
				/*
				 * 4- filepath/virtualFileName 5- sectionName 6- columnNumber^ 7- startFrom(1
				 * for starting from start) 8- ^resetAfterNumber(0 for no limit) 9- delimiter
				 * e.g. , 10- randomize true/false 11- reset in each file - true/false 12-
				 * filterColumnNumber 13- filterColumnValue
				 */
				if (paramDetails.size() == 18)
					setParamSourceFileWhereAndWhereLike(paramDetails.get(4), paramDetails.get(6), paramDetails.get(7),
							paramDetails.get(8), paramDetails.get(9), paramDetails.get(10), paramDetails.get(11),
							isVirtualFile, paramDetails.get(5), paramDetails.get(12), paramDetails.get(13),
							paramDetails.get(14), paramDetails.get(15), paramDetails.get(16), paramDetails.get(17));
				else
					/*
					 * 4- filepath/virtualFileName 5- columnNumber^ 6- startFrom(1 for starting from
					 * start) 7- ^resetAfterNumber(0 for no limit) 8- delimiter e.g. , 9- randomize
					 * true/false 10- reset in each file - true/false 11- filterColumnNumber 12-
					 * filterColumnValue
					 */
					setParamSourceFileWhereAndWhereLike(paramDetails.get(4), paramDetails.get(5), paramDetails.get(6),
							paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), paramDetails.get(10),
							isVirtualFile, null, paramDetails.get(11), paramDetails.get(12), paramDetails.get(13),
							paramDetails.get(14), paramDetails.get(15), paramDetails.get(16));

			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[19])) // virtualfile type parameter
			{
				if (paramDetails.size() == 14)
					setParamSourceFile(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
							paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
							paramDetails.get(10), true, null, paramDetails.get(11), paramDetails.get(12),
							paramDetails.get(13));
				else if (paramDetails.size() == 13) {
					if (paramDetails.get(10).contains("true") || paramDetails.get(10).contains("false"))
						setParamSourceFile(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
								paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
								null, true, null, paramDetails.get(10), paramDetails.get(11), paramDetails.get(12));
					else
						setParamSourceFile(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
								paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9),
								paramDetails.get(10), true, null, "", paramDetails.get(11), paramDetails.get(12));
				} else
					setParamSourceFile(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5),
							paramDetails.get(6), paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), null,
							true, null, "", paramDetails.get(10), paramDetails.get(11));
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[20])) // multiParamFile type parameter
			{
				if (paramDetails.size() == 15)
					setParamSourceFile(paramDetails.get(3), paramDetails.get(5), paramDetails.get(6),
							paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), paramDetails.get(10),
							paramDetails.get(11), false, paramDetails.get(4), paramDetails.get(12),
							paramDetails.get(13), paramDetails.get(14));
				else if (paramDetails.size() == 14) {
					if (paramDetails.get(11).contains("true") || paramDetails.get(11).contains("false"))
						setParamSourceFile(paramDetails.get(3), paramDetails.get(5), paramDetails.get(6),
								paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), paramDetails.get(10),
								null, false, paramDetails.get(4), paramDetails.get(11), paramDetails.get(12),
								paramDetails.get(13));
					else
						setParamSourceFile(paramDetails.get(3), paramDetails.get(5), paramDetails.get(6),
								paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), paramDetails.get(10),
								paramDetails.get(11), false, paramDetails.get(4), "", paramDetails.get(12),
								paramDetails.get(13));
				} else
					setParamSourceFile(paramDetails.get(3), paramDetails.get(5), paramDetails.get(6),
							paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), paramDetails.get(10), null,
							false, paramDetails.get(4), "", paramDetails.get(11), paramDetails.get(12));
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[1])) // db source parameter
			{
				// filename, columnNumber, startFrom, recycleAfter, randomize , resetWithFile
				if (paramDetails.size() == 12) {
					setDBSource(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5), paramDetails.get(6),
							paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), paramDetails.get(10),
							paramDetails.get(11), ps);
				} else if (paramDetails.size() == 11)
					setDBSource(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5), paramDetails.get(6),
							paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), paramDetails.get(10), "",
							ps);
				else
					setDBSource(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5), paramDetails.get(6),
							paramDetails.get(7), paramDetails.get(8), paramDetails.get(9), "", "", ps);

			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[0])) // array type parameter
			{
				setParamArray(paramDetails.get(3), paramDetails.get(4), paramDetails.get(5), paramDetails.get(6),
						paramDetails.get(7), paramDetails.get(8));
			}

			// number parameters - random, unique, numberSequence, group iteration,
			// rangeRandomNumber and rangeNumberSequence
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[9])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[3])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[4])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[14])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[12])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[16])) {

				if (paramDetails.get(3).contains("-")) {
					List<String> sa = StringOps.fastSplit(paramDetails.get(3), "-");

					this.width = Integer.parseInt(sa.get(0)); // range of digits, like 5 of 5-9 is stored here
					this.widthx = Integer.parseInt(sa.get(1)); // range of digits, like 9 of 5-9 is stored here
				} else {

					this.width = Integer.parseInt(paramDetails.get(3)); // if not range, single number is stored here
				}
				if (paramDetails.size() >= 5)
					this.isZeroPadded = Boolean.parseBoolean(paramDetails.get(4));
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[16])) {
				lowerLimit = Long.parseLong(paramDetails.get(6));
				upperLimit = Long.parseLong(paramDetails.get(7));
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[12])) {
				lowerLimit = Long.parseLong(paramDetails.get(5));
				upperLimit = Long.parseLong(paramDetails.get(6));
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[5])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[8])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[7])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[10])) {
				if (paramDetails.get(3).contains("-")) {
					List<String> sa = StringOps.fastSplit(paramDetails.get(3), "-");
					this.width = Integer.parseInt(sa.get(0));
					this.widthx = Integer.parseInt(sa.get(1));
				} else {
					this.width = Integer.parseInt(paramDetails.get(3));
				}
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[4])) // number sequence
			{
				this.resetWithFile = Boolean.parseBoolean(paramDetails.get(5));
			}
			// string parameters - upper case, mixed case, lower case, alpha numeric

			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[13])
					|| paramDetails.get(1).equals(Types.getInstance().getParamTypes()[6])) {
				this.format = paramDetails.get(3);
				this.dateType = paramDetails.get(4); // past/future/systime or number of seconds from current time for
														// relative and random date time.
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[11])) {
				this.format = paramDetails.get(3);
				SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Long d1 = format1.parse(paramDetails.get(4)).getTime();
				Long d2 = format2.parse(paramDetails.get(5)).getTime();
				this.dateType = d1.toString(); // lower limit of date stored as epoch string
				this.dateTypex = d2.toString(); // upper limit of date stored as epoch string
			}
			if (paramDetails.get(1).equals(Types.getInstance().getParamTypes()[15])) {
				if (paramDetails.size() == 5)
					setDataTypeParam(paramDetails.get(3), paramDetails.get(4));
				else
					setDataTypeParam(paramDetails.get(3), "asis");
			}
		} catch (ParseException e) {
			String msg1 = "ERROR: Date lower and upper limit format should be yyyy-MM-dd hh:mm:ss... please check the format, you can use 0's for time if time information is not important.\n"
					+ LogStackTrace.get(e);
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg1);
			else
				lo.error(msg1);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg1);
		} catch (Exception e) {
			String msg1 = "ERROR: IMPROPER PARAMETER DEFINITION for " + this.paramName
					+ ", please check parameter arguments...\n" + LogStackTrace.get(e);
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg1);
			else
				lo.error(msg1);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg1);
		}

	}

	private void setDataTypeParam(String format, String caseChange) {
		// we are parsing the format string and finding occurrences of the available
		// data types like NAME_FIRST etc.
		// then we are breaking the format String into data (NAME_FIRST, NAME_LAST etc)
		// and non-data (any other string in between and before/after data types)
		// these broken done pieces are stored in DataObject
		this.caseChange = caseChange;
		String[] datatypes = Types.getInstance().getDataTypeList();
		TreeMap<Integer, String> map = new TreeMap<>();
		for (String s : datatypes)
			if (format.indexOf(s) > -1)
				map.put(format.indexOf(s), s);
		dol = new ArrayList<>();
		Integer[] keys = map.keySet().toArray(new Integer[map.size()]);
		int index = 0;
		int i = 0;
		while (index < format.length() && i <= keys.length) {
			if (keys.length != i && index < keys[i]) {
				dol.add(new DataObject("nondata", format.substring(index, keys[i])));
			}
			if (keys.length == i) {
				dol.add(new DataObject("nondata", format.substring(index)));
			}
			if (keys.length != i) {
				dol.add(new DataObject("data", format.substring(keys[i], keys[i] + map.get(keys[i]).length())));
				index = keys[i] + map.get(keys[i]).length();
			}
			i++;
		}
	}

	private void setTightFileParam(String type, String paramSourceFile, String columnInSource, String startFrom,
			String recycleAfter, String delim, String randomize, String resetWithFile, String rowMultiplier,
			ParameterStore ps, String returnCountOnly, String returnSorted, String returnUnique) {
		this.paramSourceFile = paramSourceFile;
		if (returnCountOnly.equals("true"))
			this.returnCountOnly = true;
		if (returnSorted.equals("true"))
			this.returnSorted = true;
		if (returnUnique.equals("true"))
			this.returnUnique = true;
		if (type.equals("primary")) {
			this.isPrimaryTightFile = true;
			ps.addTightFileMap(paramSourceFile, this.paramName);
		} else
			this.isPrimaryTightFile = false;
		this.columnInSource = Integer.parseInt(columnInSource) - 1;
		this.startFrom = Integer.parseInt(startFrom) - 1;
		this.recycleAfter = Integer.parseInt(recycleAfter);
		this.resetWithFile = Boolean.parseBoolean(resetWithFile);
		if (randomize.equals("true"))
			this.randomize = true;
		if (this.startFrom < 0) {
			this.startFrom = 0;
			String msg = "ERROR: Start from value < 0... Using 0 to start at the beginning of parameter list... index 0..."
					+ this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

		}
		if (this.recycleAfter != 0 && this.recycleAfter <= this.startFrom) {
			String msg = "ERROR: Parameter values starts from position " + (this.startFrom + 1)
					+ " and values are reset after " + this.recycleAfter
					+ "... impossible condition... resetting reset after value to start From value..." + this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

			this.recycleAfter = this.startFrom + 1;
		}
		try {
			paramData = new ArrayList<>();
			List<String> fileContent = new ArrayList<>();
			fileContent.addAll(Files.readAllLines(Paths.get(paramSourceFile)));
			Map<Integer, Integer> rowMulMap = null;
			if (rowMultiplier != null) {
				rowMulMap = new HashMap<>();
				List<String> rowMul = StringOps.fastSplit(rowMultiplier, "_");
				for (String s : rowMul) {
					List<String> rm = StringOps.fastSplit(s, "x");
					rowMulMap.put(Integer.parseInt(rm.get(0)), Integer.parseInt(rm.get(1)));
				}
			}
			int rowCount = 1;
			for (String s : fileContent) {
				if (s.length() > 0) {
					List<String> split = StringOps.fastSplit(s, delim);
					if (this.columnInSource < split.size()) {

						if (rowMulMap != null && rowMulMap.containsKey(rowCount)) {
							int mul = rowMulMap.get(rowCount);
							for (int i = 0; i < mul; i++)
								paramData.add(split.get(this.columnInSource));
						} else
							paramData.add(split.get(this.columnInSource));
					} else {
						String msg = "ERROR: Number of columns in param file based on set delimiter < param column number..."
								+ this.paramName + "Line Value..." + s;
						if (Types.getInstance().getIsUI())
							DULogger.log(200, msg);
						else
							lo.error(msg);
						if (ps.getProcessor() != null && ps.getProcessor().toLog)
							ps.getProcessor().addLog(200, msg);
					}
				} else {
					String msg = "INFO: Parameter file has a blank line... skipping..." + this.paramName;
					if (Types.getInstance().getIsUI())
						DULogger.log(400, msg);
					else
						lo.info(msg);
					if (ps.getProcessor() != null && ps.getProcessor().toLog)
						ps.getProcessor().addLog(200, msg);
				}
				rowCount++;
			}

			paramData = paramData.subList(this.startFrom, getRecycleAfter());
			this.startFrom = 0;
			this.recycleAfter = 0;

			if (this.returnUnique)
				paramData = new ArrayList<String>(new HashSet<String>(paramData));

			if (this.returnSorted)
				Collections.sort(paramData);

			for (String xp : paramData) {
				String msg = "DEBUG: param data: " + xp;
				if (Types.getInstance().getIsUI())
					DULogger.log(500, msg);
				else {
					lo.debug(msg);
				}
				if (ps.getProcessor() != null && ps.getProcessor().toLog)
					ps.getProcessor().addLog(500, msg);
			}
		} catch (IOException e) {
			String msg = "ERROR: IO Exception reading param file:" + paramSourceFile + "..." + this.paramName + "\n"
					+ LogStackTrace.get(e);
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);
		}
	}

	private void setParamSourceFileWhereAndWhereLike(String paramSourceFile, String columnInSource, String startFrom,
			String recycleAfter, String delim, String randomize, String resetWithFile, boolean isVirtualFile,
			String sectionName, String filterColumnString, String filterValue, String isFilterValueParam,
			String returnCountOnly, String returnSorted, String returnUnique) {
		Integer filterColumn = Integer.parseInt(filterColumnString) - 1;
		this.columnInSource = Integer.parseInt(columnInSource) - 1;
		if (returnCountOnly.equals("true"))
			this.returnCountOnly = true;
		if (returnSorted.equals("true"))
			this.returnSorted = true;
		if (returnUnique.equals("true"))
			this.returnUnique = true;
		if (isFilterValueParam.equals("true"))
			this.isFilterValueParam = true;
		try {
			this.startFrom = Integer.parseInt(startFrom) - 1;
		} catch (NumberFormatException e) {
			this.startFrom = 0;
			this.indexParameterName = startFrom.trim();
		}
		this.recycleAfter = Integer.parseInt(recycleAfter);

		this.resetWithFile = Boolean.parseBoolean(resetWithFile);
		if (randomize.equals("true"))
			this.randomize = true;
		if (this.startFrom < 0) {
			this.startFrom = 0;
			String msg = "ERROR: Start from value < 0... Using 0 to start at the beginning of parameter list... index 0..."
					+ this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

		}
		if (this.recycleAfter != 0 && this.recycleAfter <= this.startFrom) {
			String msg = "ERROR: Parameter values starts from position " + (this.startFrom + 1)
					+ " and values are reset after " + this.recycleAfter
					+ "... impossible condition... resetting reset after value to start From value..." + this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

			this.recycleAfter = this.startFrom + 1;
		}

		paramData = new ArrayList<>();
		List<String> fileContent = new ArrayList<>();

		if (isVirtualFile) {
			try {
				if (ps.getVirtualFileParam().hasVF(paramSourceFile))
					fileContent.addAll(ps.getVirtualFileParam().getVirtualFiles(paramSourceFile));
				else {
					String msg = "ERROR: Virtual parameter file not found..." + paramSourceFile;
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					if (ps.getProcessor() != null && ps.getProcessor().toLog)
						ps.getProcessor().addLog(200, msg);
				}
			} catch (Exception e) {
				String msg = "ERROR: Exception in accessing virtual parameter file..." + e.getMessage();
				if (Types.getInstance().getIsUI())
					DULogger.log(200, msg);
				else
					lo.error(msg);
				if (ps.getProcessor() != null && ps.getProcessor().toLog)
					ps.getProcessor().addLog(200, msg);
			}
		} else {
			if (sectionName == null) {
				try {
					fileContent.addAll(Files.readAllLines(Paths.get(paramSourceFile)));
				} catch (IOException e) {
					String msg = "ERROR: IO Exception reading param file:" + paramSourceFile + "..." + this.paramName
							+ "\n" + LogStackTrace.get(e);
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					if (ps.getProcessor() != null && ps.getProcessor().toLog)
						ps.getProcessor().addLog(200, msg);
				}
			} else {
				String paramData = FileOperation.getFileContentAsString(paramSourceFile);
				String dmsvfs = "<dms-vf>";
				String dmsvfe = "</dms-vf>";
				String dmsvirtualfilestart = "<dms-virtualfile>";
				String dmsvirtualfileend = "</dms-virtualfile>";

				if ((paramData.toLowerCase().contains(dmsvirtualfilestart) || paramData.toLowerCase().contains(dmsvfs))
						&& (paramData.toLowerCase().contains(dmsvirtualfileend)
								|| paramData.toLowerCase().contains(dmsvfe))) {

					List<String> virtualfiledata;
					if (paramData.contains(dmsvfs))
						virtualfiledata = UtilityFunctions.getInBetweenFast(paramData, dmsvfs, dmsvfe, true);
					else
						virtualfiledata = UtilityFunctions.getInBetweenFast(paramData, dmsvirtualfilestart,
								dmsvirtualfileend, true);

					String name = "";
					String data = null;
					boolean found = false;
					for (String s : virtualfiledata) {

						if (s.contains("dms-vfname") && s.contains("dms-vfdata")) {
							name = UtilityFunctions.getInBetweenFast(s, "<dms-vfname>", "</dms-vfname>", true).get(0);
							if (name != null && name.equals(sectionName)) {
								data = UtilityFunctions.getInBetweenFast(s, "<dms-vfdata>", "</dms-vfdata>", true)
										.get(0);
								found = true;
								break;
							}

						} else {
							name = UtilityFunctions
									.getInBetweenFast(s, "<dms-virtualfilename>", "</dms-virtualfilename>", true)
									.get(0);
							if (name != null && name.equals(sectionName)) {
								data = UtilityFunctions
										.getInBetweenFast(s, "<dms-virtualfiledata>", "</dms-virtualfiledata>", true)
										.get(0);
								found = true;
								break;
							}

						}

					}
					if (found && data != null) {
						data = data.replace("\r", "");
						fileContent = StringOps.fastSplit(data, "\n");

					} else {
						String msg = "ERROR: Parameter section / virtual file section not found or doesn't have proper data ... for parameter: "
								+ this.paramName + " , for Virtual File Section :" + sectionName + '\n' + paramData
								+ '\n';
						if (Types.getInstance().getIsUI())
							DULogger.log(200, msg);
						else
							lo.error(msg);
						if (ps.getProcessor() != null && ps.getProcessor().toLog)
							ps.getProcessor().addLog(200, msg);
					}
				}
			}
		}

		for (String s : fileContent) {
			if (s.length() > 0) {
				List<String> split = StringOps.fastSplit(s, delim);
				if (this.columnInSource < split.size() && filterColumn < split.size()) {
					if (this.isFilterValueParam) {
						this.filterValueParamName = filterValue;
						if (paramDataMap == null)
							paramDataMap = new HashMap<>();

						if (this.paramType.equals("where") || this.paramType.equals("wherelike")) {
							if (paramDataMap.containsKey(split.get(filterColumn)))
								paramDataMap.get(split.get(filterColumn)).add(split.get(this.columnInSource));
							else {
								List<String> as = new ArrayList<>();
								as.add(split.get(this.columnInSource));
								paramDataMap.put(split.get(filterColumn), as);
							}
						} else {
						}
					} else {
						if (this.paramType.equals("where") && split.get(filterColumn).equals(filterValue))
							paramData.add(split.get(this.columnInSource));
						else if (this.paramType.equals("wherelike") && split.get(filterColumn).contains(filterValue))
							paramData.add(split.get(this.columnInSource));
						else {
						}
					}
				} else {
					String msg = "ERROR: Number of columns in param file based on set delimiter < specified column number in parameter..."
							+ this.paramName + "Line Value..." + s;
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					if (ps.getProcessor() != null && ps.getProcessor().toLog)
						ps.getProcessor().addLog(200, msg);
				}
			} else {
				String msg = "INFO: Parameter file has a blank line... skipping..." + this.paramName;
				if (Types.getInstance().getIsUI())
					DULogger.log(400, msg);
				else
					lo.info(msg);
				if (ps.getProcessor() != null && ps.getProcessor().toLog)
					ps.getProcessor().addLog(400, msg);

			}

		}

		if (this.returnUnique) {
			paramData = new ArrayList<String>(new HashSet<String>(paramData));

			if (paramDataMap != null) {
				Map<String, List<String>> paramDataMapX = new HashMap<>();
				for (Map.Entry<String, List<String>> entry : paramDataMap.entrySet()) {
					paramDataMapX.put(entry.getKey(), new ArrayList<String>(new HashSet<String>(entry.getValue())));
				}
				paramDataMap = paramDataMapX;
			}
		}

		if (this.returnSorted) {
			Collections.sort(paramData);
			if (paramDataMap != null) {
				for (Map.Entry<String, List<String>> entry : paramDataMap.entrySet()) {
					Collections.sort(entry.getValue());
				}
			}
		}

		for (String xp : paramData) {
			String msg = "DEBUG: param data: " + xp;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg);
			else
				lo.debug(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(500, msg);
		}

	}

	private void setParamSourceFile(String paramSourceFile, String columnInSource, String startFrom,
			String recycleAfter, String delim, String randomize, String resetWithFile, String rowMultiplier,
			boolean isVirtualFile, String sectionName, String returnCountOnly, String returnSorted,
			String returnUnique) {
		this.columnInSource = Integer.parseInt(columnInSource) - 1;
		if (returnCountOnly.equals("true"))
			this.returnCountOnly = true;
		if (returnSorted.equals("true"))
			this.returnSorted = true;
		if (returnUnique.equals("true"))
			this.returnUnique = true;
		try {
			this.startFrom = Integer.parseInt(startFrom) - 1;
		} catch (NumberFormatException e) {
			this.startFrom = 0;
			this.indexParameterName = startFrom.trim();
		}
		this.recycleAfter = Integer.parseInt(recycleAfter);

		this.resetWithFile = Boolean.parseBoolean(resetWithFile);
		if (randomize.equals("true"))
			this.randomize = true;
		if (this.startFrom < 0) {
			this.startFrom = 0;
			String msg = "ERROR: Start from value < 0... Using 0 to start at the beginning of parameter list... index 0..."
					+ this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

		}
		if (this.recycleAfter != 0 && this.recycleAfter <= this.startFrom) {
			String msg = "ERROR: Parameter values starts from position " + (this.startFrom + 1)
					+ " and values are reset after " + this.recycleAfter
					+ "... impossible condition... resetting reset after value to start From value..." + this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

			this.recycleAfter = this.startFrom + 1;
		}

		paramData = new ArrayList<>();
		List<String> fileContent = new ArrayList<>();

		if (isVirtualFile) {
			try {
				if (ps.getVirtualFileParam().hasVF(paramSourceFile))
					fileContent.addAll(ps.getVirtualFileParam().getVirtualFiles(paramSourceFile));
				else {
					String msg = "ERROR: Virtual parameter file not found..." + paramSourceFile;
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					if (ps.getProcessor() != null && ps.getProcessor().toLog)
						ps.getProcessor().addLog(200, msg);
				}
			} catch (Exception e) {
				String msg = "ERROR: Exception in accessing virtual parameter file..." + e.getMessage();
				if (Types.getInstance().getIsUI())
					DULogger.log(200, msg);
				else
					lo.error(msg);
				if (ps.getProcessor() != null && ps.getProcessor().toLog)
					ps.getProcessor().addLog(200, msg);
			}
		} else {
			if (sectionName == null) {
				try {
					fileContent.addAll(Files.readAllLines(Paths.get(paramSourceFile)));
				} catch (IOException e) {
					String msg = "ERROR: IO Exception reading param file:" + paramSourceFile + "..." + this.paramName
							+ "\n" + LogStackTrace.get(e);
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					if (ps.getProcessor() != null && ps.getProcessor().toLog)
						ps.getProcessor().addLog(200, msg);
				}
			} else {
				String paramData = FileOperation.getFileContentAsString(paramSourceFile);
				String dmsvfs = "<dms-vf>";
				String dmsvfe = "</dms-vf>";
				String dmsvirtualfilestart = "<dms-virtualfile>";
				String dmsvirtualfileend = "</dms-virtualfile>";

				if ((paramData.toLowerCase().contains(dmsvirtualfilestart) || paramData.toLowerCase().contains(dmsvfs))
						&& (paramData.toLowerCase().contains(dmsvirtualfileend)
								|| paramData.toLowerCase().contains(dmsvfe))) {

					List<String> virtualfiledata;
					if (paramData.contains(dmsvfs))
						virtualfiledata = UtilityFunctions.getInBetweenFast(paramData, dmsvfs, dmsvfe, true);
					else
						virtualfiledata = UtilityFunctions.getInBetweenFast(paramData, dmsvirtualfilestart,
								dmsvirtualfileend, true);

					String name = "";
					String data = null;
					boolean found = false;
					for (String s : virtualfiledata) {

						if (s.contains("dms-vfname") && s.contains("dms-vfdata")) {
							name = UtilityFunctions.getInBetweenFast(s, "<dms-vfname>", "</dms-vfname>", true).get(0);
							if (name != null && name.equals(sectionName)) {
								data = UtilityFunctions.getInBetweenFast(s, "<dms-vfdata>", "</dms-vfdata>", true)
										.get(0);
								found = true;
								break;
							}

						} else {
							name = UtilityFunctions
									.getInBetweenFast(s, "<dms-virtualfilename>", "</dms-virtualfilename>", true)
									.get(0);
							if (name != null && name.equals(sectionName)) {
								data = UtilityFunctions
										.getInBetweenFast(s, "<dms-virtualfiledata>", "</dms-virtualfiledata>", true)
										.get(0);
								found = true;
								break;
							}

						}

					}
					if (found && data != null) {
						data = data.replace("\r", "");
						fileContent = StringOps.fastSplit(data, "\n");

					} else {
						String msg = "ERROR: Parameter section / virtual file section not found or doesn't have proper data ... for parameter: "
								+ this.paramName + " , for Virtual File Section :" + sectionName + '\n' + paramData
								+ '\n';
						if (Types.getInstance().getIsUI())
							DULogger.log(200, msg);
						else
							lo.error(msg);
						if (ps.getProcessor() != null && ps.getProcessor().toLog)
							ps.getProcessor().addLog(200, msg);
					}
				}
			}
		}

		Map<Integer, Integer> rowMulMap = null;
		if (rowMultiplier != null) {
			rowMulMap = new HashMap<>();
			List<String> rowMul = StringOps.fastSplit(rowMultiplier, "_");
			for (String s : rowMul) {
				List<String> rm = StringOps.fastSplit(s, "x");
				rowMulMap.put(Integer.parseInt(rm.get(0)), Integer.parseInt(rm.get(1)));
			}
		}
		int rowCount = 1;
		for (String s : fileContent) {
			if (s.length() > 0) {
				List<String> split = StringOps.fastSplit(s, delim);
				if (this.columnInSource < split.size()) {
					if (rowMulMap != null && rowMulMap.containsKey(rowCount)) {
						int mul = rowMulMap.get(rowCount);
						for (int i = 0; i < mul; i++)
							paramData.add(split.get(this.columnInSource));
					} else
						paramData.add(split.get(this.columnInSource));
				} else {
					String msg = "ERROR: Number of columns in param file based on set delimiter < specified column number in parameter..."
							+ this.paramName + "Line Value..." + s;
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					if (ps.getProcessor() != null && ps.getProcessor().toLog)
						ps.getProcessor().addLog(200, msg);
				}
				rowCount++;
			} else {
				String msg = "INFO: Parameter file has a blank line... skipping..." + this.paramName;
				if (Types.getInstance().getIsUI())
					DULogger.log(400, msg);
				else
					lo.info(msg);
				if (ps.getProcessor() != null && ps.getProcessor().toLog)
					ps.getProcessor().addLog(400, msg);

			}

		}

		paramData = paramData.subList(this.startFrom, getRecycleAfter());
		this.startFrom = 0;
		this.recycleAfter = 0;

		if (this.returnUnique)
			paramData = new ArrayList<String>(new HashSet<String>(paramData));

		if (this.returnSorted)
			Collections.sort(paramData);

		for (String xp : paramData) {
			String msg = "DEBUG: param data: " + xp;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg);
			else
				lo.debug(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(500, msg);
		}

	}

	private void setParamArray(String csv, String startFrom, String recycleAfter, String delim, String randomize,
			String resetWithFile) {
		this.recycleAfter = Integer.parseInt(recycleAfter);
		try {
			this.startFrom = Integer.parseInt(startFrom) - 1;
		} catch (NumberFormatException e) {
			this.startFrom = 0;
			this.indexParameterName = startFrom.trim();
		}
		if (randomize.equals("true"))
			this.randomize = true;
		if (resetWithFile.equals("true"))
			this.resetWithFile = true;
		if (this.startFrom < 0) {
			String msg = "ERROR: Start from value < 0... Using 0 to start at the beginning of parameter list... index 0..."
					+ this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

			this.startFrom = 0;
		}
		if (this.recycleAfter != 0 && this.recycleAfter <= this.startFrom) {
			String msg = "ERROR: Parameter values starts from position " + (this.startFrom + 1)
					+ " and values are reset after " + this.recycleAfter
					+ "... impossible condition... resetting reset after value to start From value..." + this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

			this.recycleAfter = this.startFrom + 1;
		}
		paramData = new ArrayList<>();
		List<String> split = StringOps.fastSplit(csv, delim);
		for (String s : split)
			paramData.add(s);

		for (String xp : paramData) {
			String msg = "DEBUG: param data: " + xp;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg);
			else
				lo.debug(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(500, msg);
		}
	}

	private void setDBSource(String filename, String columnNumber, String startFrom, String recycleAfter,
			String randomize, String resetWithFile, String printToFile, String findReplace, String outputFileName,
			ParameterStore ps) {
		paramData = new ArrayList<>();
		this.columnInSource = Integer.parseInt(columnNumber) - 1;
		this.recycleAfter = Integer.parseInt(recycleAfter);
		try {
			this.startFrom = Integer.parseInt(startFrom) - 1;
		} catch (NumberFormatException e) {
			this.startFrom = 0;
			this.indexParameterName = startFrom.trim();
		}
		if (randomize.equals("true"))
			this.randomize = true;
		if (resetWithFile.equals("true"))
			this.resetWithFile = true;
		if (this.startFrom < 0) {
			String msg = "ERROR: Start from value < 0... Using 0 to start at the beginning of parameter list... index 0..."
					+ this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

			this.startFrom = 0;
		}
		if (this.recycleAfter != 0 && this.recycleAfter <= this.startFrom) {
			String msg = "ERROR: Parameter values starts from position " + (this.startFrom + 1)
					+ " and values are reset after " + this.recycleAfter
					+ "... impossible condition... resetting reset after value to start From value..." + this.paramName;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

			this.recycleAfter = this.startFrom + 1;
		}
		String filecontent = ps.getDBConf(filename);

		ps.getDBQueries().setQuery(filename, filecontent, printToFile.equals("true"), findReplace, outputFileName);
		paramData = ps.getDBQueries().getData(filename, columnInSource);

	}

	public String getValue(int iteration, int groupIteration) {
		String msgx = "Param Details:" + this.paramName + ":GRP ITER:" + this.groupIteration + ":NEW GRP ITER:"
				+ groupIteration + ":ITER:" + this.iteration + ":NEW ITER:" + iteration + ":GIVE NEW:" + giveNewValue;
		if (Types.getInstance().getIsUI())
			DULogger.log(500, msgx);
		else
			lo.debug(msgx);
		String toReturn = null;
		try {
			for (String s : Types.getParamFunctionMap().keySet()) {
				if (s.equals(paramType)) {
					if (paramIterationPolicy.equals(Types.ITERATIONPOLICYGLOBAL)) {
						if (this.iteration != iteration) {
							iterationChanged = true;
							value = Types.getParamFunctionMap().get(s).getValue(this);
							if (paramType.equals(Types.getInstance().getParamTypes()[0])
									|| paramType.equals(Types.getInstance().getParamTypes()[2]))
								usedNumbers.add(value);
							this.iteration = iteration;
							if (iterationChanged)
								iterationChanged = false;
						}
					} else if (paramIterationPolicy.equals(Types.ITERATIONPOLICYGROUP)) {
						if (this.groupIteration != groupIteration || this.iteration != iteration) {
							if (this.iteration != iteration) {
								iterationChanged = true;
							}

							value = Types.getParamFunctionMap().get(s).getValue(this);
							if (paramType.equals(Types.getInstance().getParamTypes()[0])
									|| paramType.equals(Types.getInstance().getParamTypes()[2]))
								usedNumbers.add(value);
							this.groupIteration = groupIteration;
							this.iteration = iteration;
							if (iterationChanged)
								iterationChanged = false;
						} else {
							if (giveNewValue) {
								value = Types.getParamFunctionMap().get(s).getValue(this);
							}
						}
						giveNewValue = false;
					} else {
						if (this.iteration != iteration) {
							iterationChanged = true;
						}

						this.groupIteration = groupIteration;
						this.iteration = iteration;
						value = Types.getParamFunctionMap().get(s).getValue(this);
						if (paramType.equals(Types.getInstance().getParamTypes()[0])
								|| paramType.equals(Types.getInstance().getParamTypes()[2]))
							usedNumbers.add(value);
						if (iterationChanged)
							iterationChanged = false;
					}
					toReturn = value;
				}
			}
			if (toReturn == null) {
				String msg = "ERROR: Param type not found in known param types/functions " + this.paramName;
				if (Types.getInstance().getIsUI())
					DULogger.log(200, msg);
				else
					lo.error(msg);
				if (ps.getProcessor() != null && ps.getProcessor().toLog)
					ps.getProcessor().addLog(200, msg);
			}

			String msg = "DEBUG: Parameter Values: " + this.paramName + ":" + toReturn + ":\n";
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg);
			else
				lo.debug(msg);

			return toReturn;
		} catch (Exception e) {
			String msg = "ERROR: Error getting value for parameter " + this.paramName + "\n" + e.getMessage() + "\n"
					+ LogStackTrace.get(e);
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (ps.getProcessor() != null && ps.getProcessor().toLog)
				ps.getProcessor().addLog(200, msg);

			return null;
		}
	}
};
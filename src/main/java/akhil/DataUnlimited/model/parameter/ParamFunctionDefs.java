package akhil.DataUnlimited.model.parameter;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.DataObject;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.LogStackTrace;

public class ParamFunctionDefs {

	private static final Logger lo = LogManager.getLogger(ParamFunctionDefs.class.getName());
	private Random rand;

	public ParamFunctionDefs() {
		try {
			rand = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
		}
	}

	public void defineParamFunctions(Map<String, ParamFunctions> paramFunctionMap) {
		paramFunctionMap.put("eval", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getEvalValue(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("mathCalc", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getMathCalcValue(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("uuid", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getUUID();
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("decode", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getDecodeValue(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("embeddedParamString", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getEmbeddedStringVal(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("dbQuery", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getQueryValue(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("dbStore", new ParamFunctions() {
			public String getValue(Parameter p) {
				insertUpdateDropQuery(p);
				p.setCurrValue("");
				return "";
			}
		});
		paramFunctionMap.put("array", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getValueFileDBArray(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("db", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getValueFileDBArray(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("file", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getValueFileDBArray(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("where", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = "dmsnull";
				try {
					toReturn = getValueFileDBArray(p);
				} catch (Exception e) {
					String msg = "DEBUG: Parameter " + p.getParamName() + "\n" + LogStackTrace.get(e);
					if (Types.getInstance().getIsUI())
						DULogger.log(300, msg);
					else
						lo.warn(msg);
					if (p.getParameterStore().getProcessor().toLog)
						p.getParameterStore().getProcessor().addLog(300, msg);
				}
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("wherelike", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = "dmsnull";
				try {
					toReturn = getValueFileDBArray(p);
				} catch (Exception e) {
				}
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("virtualfile", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getValueFileDBArray(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("multiParamFile", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getValueFileDBArray(p);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("tightfile", new ParamFunctions() {
			public String getValue(Parameter p) {

				if (p.isPrimaryTightFile()) {
					String toReturn = getValueFileDBArray(p);
					p.setCurrValue(toReturn);
					return toReturn;
				} else {
					int index = p.getParameterStore().getTightFileIndex(p.getParamSourceFile()) - 1;
					String toReturn = p.getParamData().get(index);
					p.setCurrValue(toReturn);
					return toReturn;
				}
			}
		});
		paramFunctionMap.put("randomNumber", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getRandomNumber(p.getWidth());
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("rangeRandomNumber", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = getRangeRandomNumber(p.getWidth(), p.getLowerLimit(), p.getUpperLimit(),
						p.getZeroPadded());
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("numberSequence", new ParamFunctions() {
			public String getValue(Parameter p) {
				if (p.getZeroPadded()) {
					if (p.getValue() == null || p.getValue().length() == 0
							|| p.getResetWithFile() && p.getIterationChanged()) {
						String fm = "%0" + p.getWidth() + "d";
						String toReturn = String.format(fm, Long.valueOf(1l));
						p.setCurrValue(toReturn);
						return toReturn;
					} else {
						String fm = "%0" + p.getWidth() + "d";
						String toReturn = String.format(fm, Long.valueOf(Long.parseLong(p.getValue()) + 1));
						p.setCurrValue(toReturn);
						return toReturn;
					}
				} else {
					if (p.getValue() == null || p.getValue().length() == 0
							|| p.getResetWithFile() && p.getIterationChanged()) {
						String toReturn = Long.toString(1l);
						p.setCurrValue(toReturn);
						return toReturn;
					} else {
						String toReturn = Long.toString(Long.parseLong(p.getValue()) + 1);
						p.setCurrValue(toReturn);
						return toReturn;
					}
				}
			}
		});
		paramFunctionMap.put("numberSequenceRange", new ParamFunctions() {
			public String getValue(Parameter p) {
				if (p.getZeroPadded()) {

					if (p.getValue() == null || p.getValue().length() == 0
							|| p.getResetWithFile() && p.getIterationChanged()
							|| p.getUpperLimit() != 0 && Long.valueOf(p.getValue()) >= p.getUpperLimit()) {
						String fm = "%0" + p.getWidth() + "d";
						String toReturn = String.format(fm, Long.valueOf(p.getLowerLimit()));
						p.setCurrValue(toReturn);
						return toReturn;
					} else {
						String fm = "%0" + p.getWidth() + "d";
						String toReturn = String.format(fm, Long.valueOf(Long.parseLong(p.getValue()) + 1));
						p.setCurrValue(toReturn);
						return toReturn;
					}
				} else {

					if (p.getValue() == null || p.getValue().length() == 0
							|| p.getResetWithFile() && p.getIterationChanged()
							|| p.getUpperLimit() != 0 && Long.valueOf(p.getValue()) >= p.getUpperLimit()) {
						String toReturn = Long.toString(p.getLowerLimit());
						p.setCurrValue(toReturn);
						return toReturn;
					} else {
						String toReturn = Long.toString(Long.parseLong(p.getValue()) + 1);
						p.setCurrValue(toReturn);
						return toReturn;
					}
				}
			}
		});
		paramFunctionMap.put("randomAlphaNumericString", new ParamFunctions() {
			public String getValue(Parameter p) {
				String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ1234567890";
				StringBuilder result = new StringBuilder();
				int x = p.getWidth();
				while (x > 0) {

					result.append(characters.charAt(rand.nextInt(characters.length())));
					x--;
				}
				String toReturn = result.toString();
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("randomUpperCaseString", new ParamFunctions() {
			public String getValue(Parameter p) {
				String characters = "ABCDEFGHIJLMNOPQRSTUVWXYZ";
				String toReturn = getRandomString(p.getWidth(), characters);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("randomLowerCaseString", new ParamFunctions() {
			public String getValue(Parameter p) {
				String characters = "abcdefghijklmnopqrstuvwxyz";
				String toReturn = getRandomString(p.getWidth(), characters);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("randomMixedCaseString", new ParamFunctions() {
			public String getValue(Parameter p) {
				String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJLMNOPQRSTUVWXYZ";
				String toReturn = getRandomString(p.getWidth(), characters);
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});

		paramFunctionMap.put("uniqueNumber", new ParamFunctions() {
			public String getValue(Parameter p) {
				boolean flag = true;
				String str = "";
				int count = 0;
				while (flag) {
					str = getRandomNumber(p.getWidth());
					if (!p.getUsedNumbers().contains(str)) {
						flag = false;
					}
					count++;
					if (count % 10 == 0) {
						String msg = "WARNING: DMS may be running out of Unique Number... this message prints every 10 attempts to check for unique numbers...";
						if (Types.getInstance().getIsUI())
							DULogger.log(300, msg);
						else
							lo.warn(msg);
						if (p.getParameterStore().getProcessor().toLog)
							p.getParameterStore().getProcessor().addLog(300, msg);

					}
				}
				p.setCurrValue(str);
				return str;
			}
		});
		paramFunctionMap.put("groupIteration", new ParamFunctions() {
			public String getValue(Parameter p) {
				if (p.getZeroPadded()) {
					String fm = "%0" + p.getWidth() + "d";
					String toReturn = String.format(fm, Integer.valueOf(p.getGroupIteration()));
					p.setCurrValue(toReturn);
					return toReturn;
				} else {
					String toReturn = Integer.toString(p.getGroupIteration());
					p.setCurrValue(toReturn);
					return toReturn;
				}
			}
		});
		paramFunctionMap.put("randomDateTime", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = null;
				Date d = new Date();
				if (p.getFormat().equals(Types.DATEFORMATEPOCH)) {
					if (p.getDateType().equals(Types.DATETYPEPAST)) {
						toReturn = Long.toString(d.getTime() - Long.valueOf(getRandomNumber(10)));
					} else if (p.getDateType().equals(Types.DATETYPEFUTURE)) {
						toReturn = Long.toString(d.getTime() + Long.valueOf(getRandomNumber(10)));
					} else {
						toReturn = Long.toString(d.getTime());
					}
				} else {
					if (p.getDateType().equals(Types.DATETYPEPAST)) {
						Date d1 = new Date(d.getTime() - Long.valueOf(getRandomNumber(10)));
						SimpleDateFormat f = new SimpleDateFormat(p.getFormat());
						toReturn = f.format(d1);
					} else if (p.getDateType().equals(Types.DATETYPEFUTURE)) {
						Date d1 = new Date(d.getTime() + Long.valueOf(getRandomNumber(10)));
						SimpleDateFormat f = new SimpleDateFormat(p.getFormat());
						toReturn = f.format(d1);
					} else {
						SimpleDateFormat f = new SimpleDateFormat(p.getFormat());
						toReturn = f.format(d);
					}
				}
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("relativeDateTime", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = null;
				Date d = new Date();
				Long d1 = (long) (Double.parseDouble(p.getDateType()) * 24 * 3600000);
				if (p.getFormat().equals(Types.DATEFORMATEPOCH)) {

					toReturn = Long.toString(d.getTime() + d1);
				} else {
					Date dt = new Date(d.getTime() + d1);
					SimpleDateFormat f = new SimpleDateFormat(p.getFormat());
					toReturn = f.format(dt);
				}
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("rangeDateTime", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = null;
				long lower = Long.parseLong(p.getDateType());
				long upper = Long.parseLong(p.getDateTypex());
				if (upper - lower > 0) {
					long randomDateTime = lower + (long) (Math.random() * (upper - lower));
					if (p.getFormat().equals(Types.DATEFORMATEPOCH)) {
						toReturn = Long.toString(randomDateTime);
					} else {
						Date d1 = new Date(randomDateTime);
						SimpleDateFormat f = new SimpleDateFormat(p.getFormat());
						toReturn = f.format(d1);
					}
				} else {
					String msg = "ERROR: Lower and upper limits of date range don't have sufficient difference..."
							+ Long.valueOf(upper - lower);
					if (Types.getInstance().getIsUI())
						DULogger.log(200, msg);
					else
						lo.error(msg);
					if (p.getParameterStore().getProcessor().toLog)
						p.getParameterStore().getProcessor().addLog(300, msg);
					return null;
				}
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
		paramFunctionMap.put("customDataType", new ParamFunctions() {
			public String getValue(Parameter p) {
				String toReturn = null;
				StringBuilder sb = new StringBuilder();
				for (DataObject dataobj : p.getDataObjects()) {
					sb.append(dataobj.getValue());
				}
				if (p.getCaseChange().equals("lower"))
					toReturn = sb.toString().toLowerCase();
				else if (p.getCaseChange().equals("upper"))
					toReturn = sb.toString().toUpperCase();
				else
					toReturn = sb.toString();
				p.setCurrValue(toReturn);
				return toReturn;
			}
		});
	}
}

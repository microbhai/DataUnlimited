package akhil.DataUnlimited.model.parameter;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mariuszgromada.math.mxparser.Expression;

import akhil.DataUnlimited.util.LogStackTrace;
import akhil.DataUnlimited.util.SQLite;
import akhil.DataUnlimited.util.StringOps;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.JSEngine;

import java.util.List;
import java.text.DecimalFormat;
import java.util.ArrayList;

public abstract class ParamFunctions {
	private static final Logger lo = LogManager.getLogger(ParamFunctions.class.getName());

	public abstract String getValue(Parameter p);

	public int getRandom(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public String getUUID() {
		return UUID.randomUUID().toString();
	}
	public String getDecodeValue(Parameter p)
	{
		String key = p.getParameterStore().getParam(p.getDecodeParamName()).getCurrValue();
		
		String toReturn = p.getDecodeValue(key);
		if (toReturn != null)
		{
			if (p.getParameterStore().getParamNames().contains(toReturn))
				toReturn = p.getParameterStore().getParam(toReturn).getCurrValue();
			return toReturn;
		}
		else {
			if (p.getDefaultDecode()) {
				if (p.getDecodeDefaultValue() == null)
					return key;
				else
					return p.getDecodeDefaultValue();
			}
			else
				return key;
		}
	}
	public String getEmbeddedStringVal(Parameter p) {
		String toReturn = p.getEmbeddedString();
		if (toReturn != null) {
			List<String> paramNames = p.getParameterStore().getParamNames();
			for (String paramName : paramNames) {
				if (toReturn.contains(paramName))
					toReturn = toReturn.replace(paramName, p.getParameterStore().getParam(paramName).getCurrValue());
			}
		} else
			toReturn = "not set";
		return toReturn;
	}
	
// Purpose of this function is to validate the occurrence of a parameter in eval expression
// To validate we make sure that the occurrence of parameter in the expression, doesn't start and end with a alphanumeric character
// If occurrence ends with another alpha numeric character, it may well be a substring inside another parameter name, rather than a real occurrence of parameters
// For example, when we check occurrence of parameter Customer in expression--> if ( 'BestCustomerInTheWorld' === 'dmsnull' ) 1; else 0;
// we will get a false positive if we simply check expression.contains("Customer"). 
// If we add additional check that characters just behind and just after Customer are not alphanumeric, then we can avoid the false positive
// 
	public static String getEvalExpression(String evalExpression, String paramName, String paramVal) {
		int found = 0;
		int index = 0;
		List<Integer> ls = new ArrayList<Integer>();
		while (found != -1) {
			found = evalExpression.indexOf(paramName, index);
			if (found > -1)
				ls.add(found);
			index = found + paramName.length();
		}

		List<Integer> fls = new ArrayList<Integer>();

		for (Integer i : ls) {
			int startIndex = 0;
			int endIndex = 0;

			if (i > 0)
				startIndex = i - 1;
			else
				startIndex = 0;

			if (i + paramName.length() < evalExpression.length())
				endIndex = i + paramName.length();
			else
				endIndex = paramName.length();

			boolean flag = false;
			if (startIndex == 0) {
				flag = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTWXYZ"
						.indexOf(evalExpression.charAt(endIndex)) == -1;
				if (flag)
					fls.add(i);
			}
			if (endIndex == paramName.length()) {
				flag = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTWXYZ"
						.indexOf(evalExpression.charAt(startIndex)) == -1;
				if (flag)
					fls.add(i);
			}
			if (startIndex != 0 && endIndex != paramName.length()) {
				flag = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTWXYZ"
						.indexOf(evalExpression.charAt(startIndex)) == -1
						&& "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTWXYZ"
								.indexOf(evalExpression.charAt(endIndex)) == -1;
				if (flag)
					fls.add(i);
			}
		}

		for (int z = fls.size(); z-- > 0;) {
			int si = fls.get(z);
			int ei = si + paramName.length();
			evalExpression = evalExpression.substring(0, si) + paramVal
					+ evalExpression.substring(ei, evalExpression.length());
		}

		return evalExpression;
	}
	
	public String getMathCalcValue(Parameter p)
	{
		String evalExpression = p.getEvalExpression().replace("[[", "").replace("]]", "");
		for (String s : p.getParameterStore().getParamNames()) {
			if (evalExpression.contains(s)) {
				evalExpression = getEvalExpression(evalExpression, s, p.getParameterStore().getParam(s).getCurrValue());
			}
		}
			String msg = "Math Calc Expression: " + evalExpression;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg);
			else
				lo.debug(msg);
			if (p.getParameterStore().getProcessor().toLog)
				p.getParameterStore().getProcessor().addLog(500, msg);

			evalExpression = evalExpression.replace("#exp#","^");
			Expression e = new Expression(evalExpression);
			return String.valueOf(new DecimalFormat(p.getDecimalFormatString()).format(e.calculate()));

	}

	public String getEvalValue(Parameter p) {
		String evalExpression = p.getEvalExpression().replace("[[", "").replace("]]", "");
		for (String s : p.getParameterStore().getParamNames()) {
			if (evalExpression.contains(s)) {
				evalExpression = getEvalExpression(evalExpression, s, p.getParameterStore().getParam(s).getCurrValue());
			}
		}
		try {
			String msg = "EVAL Expression: " + evalExpression;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg);
			else
				lo.debug(msg);
			if (p.getParameterStore().getProcessor().toLog)
				p.getParameterStore().getProcessor().addLog(500, msg);

			return JSEngine.eval("eval (\"" + evalExpression + "\")").toString();

		} catch (ScriptException e) {
			String msg = "EVAL Expression: " + evalExpression;
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (p.getParameterStore().getProcessor().toLog)
				p.getParameterStore().getProcessor().addLog(200, msg);

			msg = "ERROR: Eval expression couldn't be evaluated and converted to String.\n" + LogStackTrace.get(e);
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			if (p.getParameterStore().getProcessor().toLog)
				p.getParameterStore().getProcessor().addLog(200, msg);

			return null;
		}
	}

	public String getRandomString(int width, String characters) {
		StringBuilder result = new StringBuilder();
		Random rand = new Random();
		while (width > 0) {
			result.append(characters.charAt(rand.nextInt(characters.length())));
			width--;
		}
		return result.toString();
	}

	public void insertUpdateDropQuery(Parameter p) {
		String query = p.getQuery().trim();
		String dbUrl = p.getDbUrl();

		String msg2 = "DEBUG: DML Query " + query;

		if (query.startsWith("DROP TABLE") || query.startsWith("drop table")) {
			SQLite.ddlQuery(dbUrl, query);
			msg2 = "DEBUG: DML Query " + query;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg2);
			else
				lo.debug(msg2);
			if (p.getParameterStore().getProcessor().toLog)
				p.getParameterStore().getProcessor().addLog(500, msg2);
			return;
		}

		if (query.startsWith("INSERT INTO")) {
			String queryCreate = new String(
					StringOps.getInBetweenFast(p.getQuery().trim(), "INSERT ", " VALUES", true).get(0).trim());
			queryCreate = queryCreate.replace("INTO", "CREATE TABLE IF NOT EXISTS").replace(",", " TEXT,")
					.replace(")", " TEXT)").replace("(", "(id_ INTEGER PRIMARY KEY AUTOINCREMENT,")
					.replace(")", " ,t TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			SQLite.ddlQuery(dbUrl, queryCreate);
			msg2 = "DEBUG: DML Query " + queryCreate;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg2);
			else
				lo.debug(msg2);
			if (p.getParameterStore().getProcessor().toLog)
				p.getParameterStore().getProcessor().addLog(500, msg2);
		}
		if (query.startsWith("insert into")) {
			String queryCreate = new String(
					StringOps.getInBetweenFast(p.getQuery().trim(), "insert ", " values", true).get(0).trim());
			queryCreate = queryCreate.replace("into", "CREATE TABLE IF NOT EXISTS").replace(",", " TEXT,")
					.replace(")", " TEXT)").replace("(", "(id_ INTEGER PRIMARY KEY AUTOINCREMENT,")
					.replace(")", " ,t TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
			SQLite.ddlQuery(dbUrl, queryCreate);
			msg2 = "DEBUG: DML Query " + queryCreate;
			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg2);
			else
				lo.debug(msg2);
			if (p.getParameterStore().getProcessor().toLog)
				p.getParameterStore().getProcessor().addLog(500, msg2);
		}

		for (String s : p.getParameterStore().getParamNames()) {
			if (query.contains(s)) {
				query = getEvalExpression(query, s, p.getParameterStore().getParam(s).getCurrValue());
				msg2 = "DEBUG: DML Query Loop:" + query + ":" + s + ":"
						+ p.getParameterStore().getParam(s).getCurrValue();
				if (Types.getInstance().getIsUI())
					DULogger.log(500, msg2);
				else
					lo.debug(msg2);
				if (p.getParameterStore().getProcessor().toLog)
					p.getParameterStore().getProcessor().addLog(500, msg2);
			}
		}

		String msg1 = "DEBUG: DML Query " + query;
		if (Types.getInstance().getIsUI())
			DULogger.log(500, msg1);
		else
			lo.debug(msg1);
		if (p.getParameterStore().getProcessor().toLog)
			p.getParameterStore().getProcessor().addLog(500, msg1);

		SQLite.dmlQuery(dbUrl, query);
	}

	public String getQueryValue(Parameter p) {
		String query = p.getQuery();
		String dbUrl = p.getDbUrl();
		for (String s : p.getParameterStore().getParamNames()) {
			if (query.contains(s)) {
				query = getEvalExpression(query, s, p.getParameterStore().getParam(s).getCurrValue());
			}
		}
		String msg1 = "DEBUG: Query " + query;
		if (Types.getInstance().getIsUI())
			DULogger.log(500, msg1);
		else
			lo.debug(msg1);
		if (p.getParameterStore().getProcessor().toLog)
			p.getParameterStore().getProcessor().addLog(500, msg1);

		if (!p.getLastQuery().equals(query))
			p.setParamIndex(p.getStartFrom());

		p.setLastQuery(query);

		List<List<String>> queryResult = SQLite.selectQuery(dbUrl, query);

		if (p.getIndexParameterName().length() == 0) {
			int lastIndex = p.getParamIndex() - 1;
			if (p.getResetWithFile() && p.getIterationChanged()) {
				p.setParamIndex(p.getStartFrom());
			}
			if (p.getParamIndex() < p.getStartFrom())
				p.setParamIndex(p.getStartFrom());
			int recycleAfter = (p.getRecycleAfterSetting() == 0) ? queryResult.size() : p.getRecycleAfter();
			if (p.getParamIndex() == recycleAfter) {
				p.setParamIndex(p.getStartFrom());
			}
			if (p.isRandom()) {
				int i;
				int count = 0;
				do {
					i = getRandom(p.getStartFrom(), recycleAfter - 1);
					count++;
				} while (i == lastIndex && count < 5);
				p.setParamIndex(i);
				if (count == 5) {
					String msg = "WARNING: Parameter " + p.getParamName()
							+ " could be having issues with randomization of values. Please check the settings as random index is repeatedly equaling the last used index.";
					if (Types.getInstance().getIsUI())
						DULogger.log(300, msg);
					else
						lo.warn(msg);
					if (p.getParameterStore().getProcessor().toLog)
						p.getParameterStore().getProcessor().addLog(300, msg);

				}
			}
			String toReturn = queryResult.get(p.getParamIndex()).get(0);
			p.setParamIndex(p.getParamIndex() + 1);
			return toReturn;
		} else {
			return queryResult
					.get(Integer.parseInt(p.getParameterStore().getParam(p.getIndexParameterName()).getCurrValue()) - 1)
					.get(0);
		}
	}

	public String getValueFileDBArray(Parameter p) {
		if (p.getIndexParameterName().length() == 0) {
			int lastIndex = p.getParamIndex() - 1;

			if (p.getIsFilterValueParam()) {
				if (!p.getLastFilterValue()
						.equals(p.getParameterStore().getParam(p.getFilterValueParamName()).getCurrValue()))
					p.setParamIndex(p.getStartFrom());
				p.setLastFilterValue(p.getParameterStore().getParam(p.getFilterValueParamName()).getCurrValue());
			}

			String msg1 = "DEBUG: Parameter " + p.getParamName() + ": index : " + p.getParamIndex() + ": startFrom : "
					+ p.getStartFrom() + ": recycleAfter : " + p.getRecycleAfter();

			if (Types.getInstance().getIsUI())
				DULogger.log(500, msg1);
			else
				lo.warn(msg1);
			if (p.getParameterStore().getProcessor().toLog)
				p.getParameterStore().getProcessor().addLog(500, msg1);

			if (p.getResetWithFile() && p.getIterationChanged()) {
				p.setParamIndex(p.getStartFrom());
			}
			if (p.getParamIndex() < p.getStartFrom())
				p.setParamIndex(p.getStartFrom());
			if (p.getParamIndex() == p.getRecycleAfter()) {
				p.setParamIndex(p.getStartFrom());
			}
			if (p.getReturnCountOnly())
				p.setParamIndex(p.getStartFrom());

			if (p.isRandom()) {
				int i;
				int count = 0;
				do {
					i = getRandom(p.getStartFrom(), p.getRecycleAfter() - 1);
					count++;
				} while (i == lastIndex && count < 5);
				p.setParamIndex(i);
				if (count == 5) {
					String msg = "WARNING: Parameter " + p.getParamName()
							+ " could be having issues with randomization of values. Please check the settings as random index is repeatedly equaling the last used index.";
					if (Types.getInstance().getIsUI())
						DULogger.log(300, msg);
					else
						lo.warn(msg);
					if (p.getParameterStore().getProcessor().toLog)
						p.getParameterStore().getProcessor().addLog(300, msg);

				}
			}
			List<String> paramData = p.getParamData();
			int index = p.getParamIndex();
			String toReturn = paramData.get(index);

			p.setParamIndex(p.getParamIndex() + 1);

			return toReturn;
		} else {
			return p.getParamData().get(
					Integer.parseInt(p.getParameterStore().getParam(p.getIndexParameterName()).getCurrValue()) - 1);
		}
	}

	public String getRangeRandomNumber(int width, long lower, long upper, boolean zeroPadded) {
		long newLong = lower + (long) (Math.random() * (upper - lower + 1));
		String fm = "%0" + width + "d";
		if (width <= 19) {
			String str;
			if (zeroPadded)
				str = String.format(fm, newLong);
			else {
				str = Long.toString(newLong);
			}
			return str;
		} else {
			String str;
			if (zeroPadded)
				str = String.format(fm, newLong);
			else
				str = Long.toString(newLong);
			return str;
		}
	}

	public String getRandomNumber(int width) {
		return getRandomString(width, "0123456789");
	}
}

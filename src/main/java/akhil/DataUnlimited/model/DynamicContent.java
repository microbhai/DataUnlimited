package akhil.DataUnlimited.model;

import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.StringOps;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;

public class DynamicContent {
	private static final Logger lo = LogManager.getLogger(DynamicContent.class.getName());
	private String type; // either param or constant
	private String data; // use if constant
	private String paramName;
	private boolean show = true;
	private boolean log = false;
	private Group parentGroup;

	public DynamicContent(String data, String type, Group parentGroup, boolean show, boolean log) {
		this.type = type;
		this.show = show;
		this.log = log;
		this.parentGroup = parentGroup;
		if (type.equals(Types.SEGMENTCONTENTCONSTANT)) {
			this.data = data;
		} else if (type.equals(Types.SEGMENTCONTENTPARAM)) {
			List<String> split = StringOps.fastSplit(data, "^");
			parentGroup.getGroupsProcessor().getParameterStore().setParam(split, parentGroup);
			this.paramName = split.get(0);
		}
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getDynamicContent(int iteration) {
		if (type.equals(Types.SEGMENTCONTENTPARAM) && show) {
			String value = parentGroup.getGroupsProcessor().getParameterStore().getParamValue(iteration, paramName);
			if (log) {
				String msg = "LOG: " + paramName + ": " + value;
				if (Types.getInstance().getIsUI())
					DULogger.log(100, msg);
				else
					lo.fatal(msg);
				if (parentGroup.getGroupsProcessor().toLog)
					parentGroup.getGroupsProcessor().addLog(100, msg);
			}
			return value;
		} else if (type.equals(Types.SEGMENTCONTENTPARAM) && !show) {
			parentGroup.getGroupsProcessor().getParameterStore().getParamValue(iteration, paramName);
			return "";
		} else if (type.equals(Types.SEGMENTCONTENTCONSTANT)) {
			return data;
		} else
			return null;
	}
}

package akhil.DataUnlimited.model.parameter;

import akhil.DataUnlimited.util.DULogger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;

import akhil.DataUnlimited.model.Group;
import akhil.DataUnlimited.model.Processor;
import akhil.DataUnlimited.model.types.Types;

public class ParameterStore {
	private static final Logger lo = LogManager.getLogger(ParameterStore.class.getName());
	private Map<String, List<String>> log = new HashMap<>();
	private Map<String, Parameter> map;
	private Map<String, String> tightfilemap;
	private Map<String, String> dBConfig;
	private Map<String, Group> groupParameterMap = new HashMap<>();
	private DBQueries dbq;
	private HashMap<Group, List<Parameter>> grpParamMap = new HashMap<>(); // maintains mapping of group and its
																			// parameters

	public DBQueries getDBQueries() {
		return dbq;
	}

	private VirtualFileParam vfp = new VirtualFileParam();

	public VirtualFileParam getVirtualFileParam() {
		return vfp;
	}

	private Processor p;

	public Processor getProcessor() {
		return p;
	}

	public ParameterStore(VirtualFileParam vfpx, Processor p, Map<String, String> hm) {
		this(p, hm);
		if (vfpx != null) {
			this.vfp.mergeVirtualFileParam(vfpx);
		}
	}

	public ParameterStore(Processor p, Map<String, String> hm) {
		this(hm);
		this.p = p;
	}

	public ParameterStore(Map<String, String> hm) {
		dBConfig = hm;
		map = new HashMap<>();
		tightfilemap = new HashMap<>();
		dbq = new DBQueries();
		DULogger.log(400, "INFO: parameter store created");
		if (GlobalVirtualFileParameters.getInstance().getVFP() != null
				&& !GlobalVirtualFileParameters.getInstance().getVFP().getVirtualFiles().isEmpty()) {
			this.vfp.mergeVirtualFileParam(GlobalVirtualFileParameters.getInstance().getVFP());
			DULogger.log(400, "INFO: Merging global virtual file parameters");
		} else
			DULogger.log(400, "INFO: No Global Virtual File Parameters were found");
	}

	public String getDBConf(String name) {
		if (dBConfig != null)
			return dBConfig.get(name);
		else
			return null;
	}

	public void addTightFileMap(String paramSourceFile, String paramName) {
		tightfilemap.put(paramSourceFile, paramName);
	}

	public int getTightFileIndex(String paramSourceFile) {
		return map.get(tightfilemap.get(paramSourceFile)).getParamIndex();
	}

	public Parameter getParam(String paramname) {
		if (map.containsKey(paramname))
			return map.get(paramname);
		else {
			String msg = "ERROR: Parameter " + paramname + " is not registered... possible bug in the script";
			if (Types.getInstance().getIsUI())
				DULogger.log(200, msg);
			else
				lo.error(msg);
			return null;
		}
	}

	public List<String> getParamNames() {
		List<String> sortedList = new ArrayList<>(map.keySet());
		Collections.sort(sortedList);
		Collections.reverse(sortedList);
		return sortedList;
	}

	public void logParam(String param, String value) {
		if (log.containsKey(param))
			log.get(param).add(value);
		else {
			List<String> al = new ArrayList<>();
			al.add(value);
			log.put(param, al);
		}
	}

	public List<Parameter> getParamsForGroup(Group g) {
		return grpParamMap.get(g);
	}

	public void setParam(List<String> paramDetails, Group parentGroup) {
		if (!map.containsKey(paramDetails.get(0))) {

			if (paramDetails.size() >= 3) {

				Parameter p = new Parameter(paramDetails, this);
				map.put(paramDetails.get(0), p);
				groupParameterMap.put(paramDetails.get(0), parentGroup);

				if (grpParamMap.containsKey(parentGroup))
					grpParamMap.get(parentGroup).add(p);
				else {
					List<Parameter> ls = new ArrayList<>();
					ls.add(p);
					grpParamMap.put(parentGroup, ls);
				}
			} else {
				String msg = "ERROR: Improper parameter definition for new parameter: " + paramDetails.get(0);
				if (Types.getInstance().getIsUI())
					DULogger.log(200, msg);
				else
					lo.warn(msg);
				if (p.toLog)
					p.addLog(200, msg);
			}
		} else {
			String msg = "WARNING: Parameter already registered: " + paramDetails.get(0);
			if (Types.getInstance().getIsUI())
				DULogger.log(300, msg);
			else
				lo.warn(msg);
			if (p.toLog)
				p.addLog(300, msg);
		}
	}

	public String getParamValue(int iteration, String paramName) {
		Group g = groupParameterMap.get(paramName);
		String msgx = "Group Name:" + g.getGroupName();
		if (Types.getInstance().getIsUI())
			DULogger.log(500, msgx);
		else
			lo.debug(msgx);
		return map.get(paramName).getValue(iteration, g.getGroupIteration());
	}

}

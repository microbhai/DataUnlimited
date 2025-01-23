package akhil.DataUnlimited.model.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;

public class VirtualFileParam {

	public boolean mergeVirtualFileParam(VirtualFileParam vfp) {
		boolean flag = false;
		for (String name : vfp.getVirtualFileNames()) {
			List<String> ls = virtualFiles.put(name, new ArrayList<String>());
			if (ls != null) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(300, "WARNING: virtual file " + name + " is being overwritten with new data.");
				} else
					lo.warn("WARNING: virtual file " + name + " is being overwritten with new data.");
				flag = true;
			}
			virtualFiles.get(name).addAll(vfp.getVirtualFiles(name));
		}

		if (vfp.getLog() != null && log != null)
			log.addAll(vfp.getLog());
		return flag;
	}

	public Map<String, Integer> getIndexStore() {
		return indexStore;
	}

	public Map<String, List<String>> getVirtualFiles() {
		return virtualFiles;
	}

	public VirtualFileParam(List<String> logForApi, boolean toLog) {
		this.log = logForApi;
		this.toLog = toLog;
	}

	public List<String> getLog() {
		return log;
	}

	public boolean hasVF(String vfname) {
		return virtualFiles.containsKey(vfname);
	}

	public VirtualFileParam() {
	}

	public void incIndex(String node) {

		indexStore.remove(node);
		indexStore.put(node, Integer.valueOf(1));
	}

	public void setIndex(String node) {

		indexStore.computeIfAbsent(node, key -> Integer.valueOf(0));
	}

	public int getIndex(String node) {
		return indexStore.get(node);
	}

	public void removeIndex(String node) {
		indexStore.remove(node);
	}

	public List<String> getVirtualFiles(String name) {
		if (virtualFiles.get(name) != null)
			return virtualFiles.get(name);
		else {
			virtualFiles.put(name, new ArrayList<String>());
			return virtualFiles.get(name);
		}
	}

	public List<String> getVirtualFileNames() {
		return new ArrayList<>(virtualFiles.keySet());
	}

	public void remove(String identifier) {
		virtualFiles.remove(identifier);
	}

	private void printVf(String s) {
		StringBuilder sb = new StringBuilder();
		sb.append("============= File Name ============" + "\n");
		sb.append(s + "\n");
		sb.append("--------------------------" + "\n");
		for (String f : virtualFiles.get(s)) {
			sb.append(f + "\n");
		}
		sb.append("--------------------------" + "\n");
		if (Types.getInstance().getIsUI())
			DULogger.log(0, sb.toString());
		else
			lo.fatal(sb.toString());
		if (toLog && 0 <= DULogger.getLogLevel())
			log.add(sb.toString());
	}

	public void print() {
		if (!virtualFiles.isEmpty())
			virtualFiles.keySet().stream().sorted().forEach(this::printVf);
		else {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(300, "INFO: No virtual files found.");
			} else
				lo.warn("INFO: No virtual files found.");
		}

	}

	public void printToFile(String identifier, String paramDir) {

		if (identifier.contains(":"))
			identifier = identifier.substring(2, identifier.length());

		identifier = identifier.replace("\\", "_").replace("/", "_");
		if (identifier.length() > 0)
			identifier = identifier + "_";
		for (Map.Entry<String, List<String>> entry : virtualFiles.entrySet()) {
			StringBuilder sb = new StringBuilder();
			for (String f : entry.getValue()) {
				if (f.endsWith("<DMSDELIM>"))
					sb.append(f.substring(0, f.length() - 10).replaceAll("<DMSDELIM>", "||") + "\n");
				else
					sb.append(f + "\n");
			}
			String content = sb.toString();
			if (!content.trim().isEmpty())
				FileOperation.writeFile(paramDir, identifier + entry.getKey(), sb.toString());
		}
	}

	private Map<String, Integer> indexStore = new HashMap<>();
	private Map<String, List<String>> virtualFiles = new HashMap<>();
	private List<String> log;
	private boolean toLog = false;

	private static final Logger lo = LogManager.getLogger(VirtualFileParam.class.getName());
}

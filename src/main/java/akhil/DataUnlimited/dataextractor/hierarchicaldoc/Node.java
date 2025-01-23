package akhil.DataUnlimited.dataextractor.hierarchicaldoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.util.LogStackTrace;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.StringOps;

public class Node {
	private static final Logger lo = LogManager.getLogger(Node.class.getName());
	private static final String DMSNEWLINE = "<DMSNEWLINE>";
	private static final String DMSDELIM = "<DMSDELIM>";
	private static final String NODETXT = "_node.txt";
	private static final String PARAMTXT = "_param.txt";
	private static final String INFOMSG = "INFO: Extracted value : ";
	Map<String, String> hm = new LinkedHashMap<>();
	Map<String, List<Node>> nodes = new LinkedHashMap<>();
	List<String> seq;
	List<String> internalNodeData;
	private String name;
	private List<String> data;
	private HierarchyDelimitedFile hdf;

	public Node(String name, String data, List<String> seq, List<String> internalNodeData,
			List<List<String>> allSequence, HierarchyDelimitedFile hdf) {
		String msg = "INFO: Creating new node... " + name;
		if (Types.getInstance().getIsUI()) {
			DULogger.log(400, msg);
		} else
			lo.info(msg);
		if (hdf.isToLog())
			hdf.addLog(400, msg);
		this.name = name;
		this.data = StringOps.fastSplit(data, DMSNEWLINE);
		this.seq = seq;
		this.internalNodeData = internalNodeData;
		this.hdf = hdf;
		this.hdf.getVFP().setIndex(name);

		for (String extractInfo : seq) {
			List<String> extract = StringOps.fastSplit(extractInfo, DMSDELIM);
			boolean flag = false;
			for (String t : this.data) {
				String val = null;
				if (extract.get(3).equals("fixedlength")) {
					val = t.substring(Integer.parseInt(extract.get(4)), Integer.parseInt(extract.get(5)));
					if (extract.size() >= 7) {
						for (int j = 6; j < extract.size(); j++) {
							String operation = extract.get(j);
							val = UtilityFunctions.transform(val, operation);
						}
					}
					hm.put(extract.get(1), val);
					flag = true;
					msg = INFOMSG + extract.get(1) + "=" + val;
					if (Types.getInstance().getIsUI()) {
						DULogger.log(400, msg);
					} else
						lo.info(msg);
					if (hdf.isToLog())
						hdf.addLog(400, msg);

				}
				if (extract.get(3).equals("delimited") && t.contains(extract.get(4))) {

					try {
						val = StringOps.fastSplit(t, Pattern.quote(extract.get(5)))
								.get(Integer.parseInt(extract.get(6)) - 1);
					} catch (Exception e) {
						val = "null";
						msg = "ERROR: Delimiter based extraction encoutered error. Value will be set to null.\n"
								+ LogStackTrace.get(e);
						if (Types.getInstance().getIsUI()) {
							DULogger.log(200, msg);
						} else
							lo.error(msg);
						if (hdf.isToLog())
							hdf.addLog(400, msg);
					}
					if (extract.size() >= 8) {
						for (int j = 7; j < extract.size(); j++) {
							String operation = extract.get(j);
							val = UtilityFunctions.transform(val, operation);
						}
					}
					hm.put(extract.get(1), val);
					flag = true;
					msg = INFOMSG + extract.get(1) + "=" + val;
					if (Types.getInstance().getIsUI()) {
						DULogger.log(400, msg);
					} else
						lo.info(msg);
					if (hdf.isToLog())
						hdf.addLog(400, msg);

				}
				if (extract.get(3).equals("between") && t.contains(extract.get(4))) {

					List<String> values = UtilityFunctions.getInBetweenFast(t, extract.get(4), extract.get(5), true);
					if (!values.isEmpty()) {
						val = values.get(0);

						if (extract.size() >= 7) {
							for (int j = 6; j < extract.size(); j++) {
								String operation = extract.get(j);
								val = UtilityFunctions.transform(val, operation);
							}
						}
						hm.put(extract.get(1), val);
						flag = true;
						msg = INFOMSG + extract.get(1) + "=" + val;
						if (Types.getInstance().getIsUI()) {
							DULogger.log(400, msg);
						} else
							lo.info(msg);
						if (hdf.isToLog())
							hdf.addLog(400, msg);
					}

				}
				if (extract.get(3).equals("fieldname")) {

					if (t.contains("<" + extract.get(4) + ">")) {

						List<String> values = UtilityFunctions.getInBetweenFast(t, "<" + extract.get(4) + ">",
								"</" + extract.get(4) + ">", true);
						if (!values.isEmpty()) {
							val = values.get(0);

							if (extract.size() >= 6) {
								for (int j = 5; j < extract.size(); j++) {
									String operation = extract.get(j);
									val = UtilityFunctions.transform(val, operation);
								}
							}
							hm.put(extract.get(1), val);
							flag = true;
							msg = INFOMSG + extract.get(1) + "=" + val;
							if (Types.getInstance().getIsUI()) {
								DULogger.log(400, msg);
							} else
								lo.info(msg);
							if (hdf.isToLog())
								hdf.addLog(400, msg);
						} else {
							hm.put(extract.get(1), "");
							flag = true;
							msg = "WARNING: Extracted value : " + extract.get(1) + "=blank, no value found...";
							if (Types.getInstance().getIsUI()) {
								DULogger.log(300, msg);
							} else
								lo.warn(msg);
							if (hdf.isToLog())
								hdf.addLog(400, msg);
						}
					}
					if (t.contains("<" + extract.get(4) + "/>")) {

						hm.put(extract.get(1), "");
						flag = true;
						msg = "WARNING: Extracted value : " + extract.get(1) + "=blank, no value found...";
						if (Types.getInstance().getIsUI()) {
							DULogger.log(300, msg);
						} else
							lo.warn(msg);
						if (hdf.isToLog())
							hdf.addLog(400, msg);
					}

				}
			}

			if (!flag) {
				hm.put(extract.get(1), "dmsnull");
				msg = "WARNING: Extract field/boundaries not found : " + extract.get(1) + "=dmsnull, no value found...";
				if (Types.getInstance().getIsUI()) {
					DULogger.log(300, msg);
				} else
					lo.warn(msg);
				if (hdf.isToLog())
					hdf.addLog(400, msg);
			}

		}
		Map<String, String> childNodes = new LinkedHashMap<>();
		for (List<String> s : allSequence) {
			if (s.size() > 2 && s.get(0).equals("node") && s.get(2).equals(this.name)) {
				childNodes.put(s.get(1), s.get(4));
			}
		}

		for (Map.Entry<String, String> s : childNodes.entrySet()) {
			List<Node> ln = new ArrayList<>();
			for (String intData : internalNodeData) {
				List<String> extractSeq = new ArrayList<>();
				String datax = intData.replace("\n", DMSNEWLINE).replace("\r", "");
				List<String> internalNodeDatax = new ArrayList<>();
				if (datax.startsWith(s.getValue())) {
					for (List<String> t : allSequence) {
						if (t.get(0).equals("extract") && s.getKey().equals(t.get(2))) {
							extractSeq.add(String.join(DMSDELIM, t));
						}
						if (t.size() > 2 && t.get(0).equals("node") && s.getKey().equals(t.get(2))) {
							if (t.get(3).equals("singleline")) {
								internalNodeDatax.addAll(UtilityFunctions.getInternalNodeData(datax,
										StringOps.fastSplit(t.get(4), ","), Arrays.asList(DMSNEWLINE)));
								datax = UtilityFunctions.removeInternalNodeData(datax,
										StringOps.fastSplit(t.get(4), ","), Arrays.asList(DMSNEWLINE));
							} else {
								internalNodeDatax.addAll(UtilityFunctions.getInternalNodeData(datax,
										StringOps.fastSplit(t.get(4), ","), StringOps.fastSplit(t.get(5), ",")));
								datax = UtilityFunctions.removeInternalNodeData(datax,
										StringOps.fastSplit(t.get(4), ","), StringOps.fastSplit(t.get(5), ","));
							}
						}
					}
					ln.add(new Node(s.getKey(), datax, extractSeq, internalNodeDatax, allSequence, hdf));
				}
			}
			nodes.put(s.getKey(), ln);
		}

		printToVirtualFile();
	}

	public void printToVirtualFile() {
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder();
		if (hdf.getVFP().getIndex(this.name) == 0) {
			for (String s : nodes.keySet()) {
				sb.append("NodeCount_" + s + DMSDELIM);
			}
			for (String s : hm.keySet()) {
				sb1.append(s + DMSDELIM);
			}

			hdf.getVFP().getVirtualFiles(this.name + NODETXT).add(sb.toString());
			hdf.getVFP().getVirtualFiles(this.name + PARAMTXT).add(sb1.toString());
			sb = new StringBuilder();
			sb1 = new StringBuilder();

			hdf.getVFP().incIndex(this.name);
		}
		for (Map.Entry<String, List<Node>> s : nodes.entrySet()) {
			sb.append(s.getValue().size() + DMSDELIM);
		}
		for (Map.Entry<String, String> s : hm.entrySet()) {
			sb1.append(s.getValue() + DMSDELIM);
		}
		hdf.getVFP().getVirtualFiles(this.name + NODETXT).add(sb.toString());
		String msg = "Virtual File (node): " + this.name + NODETXT + "\n" + sb.toString() + "\n";
		if (Types.getInstance().getIsUI()) {
			DULogger.log(500, msg);
		} else
			lo.debug(msg);
		if (hdf.isToLog())
			hdf.addLog(500, msg);

		hdf.getVFP().getVirtualFiles(this.name + PARAMTXT).add(sb1.toString());
		msg = "Virtual File (param): " + this.name + PARAMTXT + "\n" + sb1.toString() + "\n";
		if (Types.getInstance().getIsUI()) {
			DULogger.log(500, msg);
		} else
			lo.debug(msg);
		if (hdf.isToLog())
			hdf.addLog(500, msg);
	}
}

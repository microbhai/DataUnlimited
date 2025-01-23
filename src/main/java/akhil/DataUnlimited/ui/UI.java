package akhil.DataUnlimited.ui;

import akhil.DataUnlimited.DataUnlimitedApi;
import akhil.DataUnlimited.dataextractor.hierarchicaldoc.UtilityFunctions;
import akhil.DataUnlimited.model.*;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.model.parameter.GlobalVirtualFileParameters;
import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.FormatConversion;
import akhil.DataUnlimited.util.GzipUtil;
import akhil.DataUnlimited.util.LogStackTrace;
import akhil.DataUnlimited.util.StringOps;
import akhil.DataUnlimited.util.DULogger;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

public class UI {

	private JFrame mainui = new JFrame();

	public JFrame getUI() {
		return mainui;
	}

	private String recentFiles = "../recent/r0c0x";
	private String dbconffilename = "../db/conf/c0n0x";
	private Object lock = new Object();

	public String getFileContent() {
		return fileContent.getText();
	}

	private JMenu recent = new JMenu("Open Recent Files");
	private Font  f1  = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	private JTextPane output;
	private JTextPane fileContent;
	private JTextPane findPane;
	private boolean fileContentChangedSinceLastSave;
	private JScrollPane cp = new JScrollPane(DockedControlPanel.getInstance());
	final StringBuilder saveFileLocationText = new StringBuilder();

	public void setVisible(boolean tf) {
		mainui.setVisible(tf);
	}

	private Map<String, String> examples;

	private int paramNameAutoIndex = 0;

	private int getIndex() {
		return paramNameAutoIndex++;
	}

	public void setCaretPosition(int pos) {
		fileContent.setCaretPosition(pos);
	}

	public JTextPane getFileContentPane() {
		return fileContent;
	}

	public int getCaretPosition() {
		return fileContent.getCaretPosition();
	}

	private JComboBox<String> datatypes = new JComboBox<>();
	ImageIcon img = new ImageIcon("../images/icon.jpg");

	public UI() {
		new Thread(() -> {
			initUI();
			while (true) {
				try {
					Thread.sleep(2000);
					DULogger.checkLoggerThread();
				} catch (Exception e) {
					DULogger.log(200, "ERROR: Thread checking logger got exception.\n" + LogStackTrace.get(e));
				}
			}
		}).start();
	}

	private void fixOutputTextColor() {
		int caretposition = output.getCaretPosition();
		UIUtil.setFontColor(output, Types.getOutputColors(), null);
		UIUtil.setFontColor(output, Color.BLUE);
		output.setCaretPosition(caretposition);
	}

	private void fixTextColor() {
		int x = UIUtil.undoCount;
		UIUtil.collect = true;
		int caretposition = fileContent.getCaretPosition();
		String origContent = Types.isWindows() ? fileContent.getText().replaceAll("\r", "") : fileContent.getText();
		fileContent.setText("");
		UIUtil.writeToPane(fileContent, origContent, Color.BLACK, Color.WHITE, false, false, false);
		UIUtil.setFontColor(fileContent, Types.getInstance().getKeywordsAndColors(), null);
		UIUtil.setFontColor(fileContent, Color.BLACK);
		try {
			fileContent.setCaretPosition(caretposition);
		} catch (IllegalArgumentException e) {
			fileContent.setCaretPosition(0);
		} catch (Exception e) {
			fileContent.setCaretPosition(0);
		}
		UIUtil.setUndoRedoCount(UIUtil.undoCount - x);
		UIUtil.collect = false;
	}

	public void logRefresh(List<String> log) {
		synchronized (lock) {
			Document blank = new DefaultStyledDocument();
			Document doc = output.getDocument();
			if (!log.isEmpty()) {

				try {
					doc.insertString(0,
							"\n...Starting log refresh... please be patient... you may not see any test in this area during the processing based on amount of data...\n\n\n\n\n",
							output.getInputAttributes());
				} catch (BadLocationException e) {
					DULogger.log(200, " ERROR: Bad Location exception while refreshing log");
				}

				try {
					for (String s : log) {
						blank.insertString(blank.getLength(), s, output.getInputAttributes());
					}

					output.setDocument(blank);
					output.setCaretPosition(output.getDocument().getLength());

				} catch (BadLocationException e) {
					DULogger.log(200, "ERROR: Bad location exception...\n" + LogStackTrace.get(e));
				}
			}
			fixOutputTextColor();
		}
	}

	public void writeToOutput(String toPrint) {
		synchronized (lock) {
			UIUtil.writeToPane(output, toPrint, Color.BLUE, Color.WHITE, true, true, false);
			fixOutputTextColor();
		}
	}

	private void checkSizeFunc(JButton checkSize, String extractInputDirLocation, JTextField sizeDisplay,
			String validationType, JCheckBox printOnCheck) {
		// this formats script in the scripting pane
		DULogger.log(400, "INFO: Validating script and checking size of the output file.");

		checkSize.setText("Processing...");
		checkSize.setEnabled(false);
		try {
			fixTextColor();
			UIUtil.setCaret(output, false);
			// this checks the size and print the output

			String str = fileContent.getText();
			String response = null;
			if (extractInputDirLocation.length() == 0) {
				if (str.contains(Types.DMSEXTRACTINPUTDATASTART) && str.contains(Types.DMSEXTRACTINPUTDATAEND)) {
					String data = UtilityFunctions.getInBetweenFast(fileContent.getText(),
							Types.DMSEXTRACTINPUTDATASTART, Types.DMSEXTRACTINPUTDATAEND, true).get(0);
					// if (DockedControlPanel.getIsJson())
					// data = FormatConversion.jsonToXML(data);
					response = new DataUnlimitedApi().getVirtualResponse(data, fileContent.getText(), "", true, true);
					sizeDisplay.setText(NumberFormat.getNumberInstance(Locale.US).format(response.length()) + " bytes");

				} else {
					response = new DataUnlimitedApi().checkSize(str, true);
					sizeDisplay.setText(
							NumberFormat.getNumberInstance(Locale.US).format(Integer.valueOf(response.length()))
									+ " bytes");
				}
			} else {
				File f = new File(extractInputDirLocation);
				if (f.isDirectory())
					DULogger.log(200,
							"ERROR: Select a file for extract input, Extract Input value should be a file name, not a directory.");
				else {
					response = new DataUnlimitedApi().getVirtualResponse(extractInputDirLocation, fileContent.getText(), "",
							false, true);
					sizeDisplay.setText(NumberFormat.getNumberInstance(Locale.US).format(response.length()) + " bytes");

				}
			}
			if (printOnCheck.isSelected()) {
				if (validationType.equals("none")) {
					DULogger.log(100, "SAMPLE DATA FROM CHECK SIZE ... \n------------------------------\n" + response
							+ "\n------------------------------\n ... SAMPLE DATA END...");
				} else if (validationType.equals("xml")) {
					DULogger.log(100,
							"SAMPLE DATA FROM CHECK SIZE ... \n------------------------------\n"
									+ FormatConversion.prettyPrint(response)
									+ "\n------------------------------\n ... SAMPLE DATA END...");
					DULogger.log(100,
							"\n------------------------------\nXML VALIDATION RESULT ... \n------------------------------\n");
					FormatConversion.isValidXML(response);
					DULogger.log(100, "\n------------------------------\n");
				} else if (printOnCheck.isSelected() && validationType.equals("json")) {
					DULogger.log(100,
							"SAMPLE DATA FROM CHECK SIZE ... \n------------------------------\n"
									+ FormatConversion.prettyPrint(response, true)
									+ "\n------------------------------\n ... SAMPLE DATA END...");
					DULogger.log(100,
							"\n------------------------------\nJSON VALIDATION RESULT ... \n------------------------------\n");
					FormatConversion.isJSONValid(response);
					DULogger.log(100, "\n------------------------------\n");
				} else
					DULogger.log(100,
							"SAMPLE DATA FROM CHECK SIZE ... \n------------------------------\nPrint Output is not selected. Not printing the output data.\nOnly displaying generated data file size: "
									+ NumberFormat.getNumberInstance(Locale.US).format(response.length()) + " bytes"
									+ "\n------------------------------\n ... SAMPLE DATA END...");

			}
			fixOutputTextColor();
		} catch (Exception e) {
			DULogger.log(200, "ERROR: Error occurred.\n" + LogStackTrace.get(e));

		}
		checkSize.setEnabled(true);
		checkSize.setText("Validate Script");

	}

	private void loadRecentFiles(JMenu recentFileMenu, String recentfilename) {

		try {
			List<String> files = FileOperation.getContentAsList(recentfilename, "utf8");
			if (files != null) {
				for (String file : files) {
					JMenuItem jmi = new JMenuItem(file);
					jmi.addActionListener(e -> loadRecent(jmi.getText()));
					recentFileMenu.add(jmi);
				}
			}
		} catch (Exception e) {
			DULogger.log(200, "ERROR: Recent files could not be loaded\n" + LogStackTrace.get(e));
		}
	}

	private void loadDBConf(String dbconffilename) {
		boolean dburl = false;
		boolean dbclass = false;
		boolean dbusr = false;
		boolean dbpwd = false;
		try {
			List<String> confstr = FileOperation.getContentAsList(dbconffilename, "utf8");
			if (confstr != null) {
				for (String str : confstr) {
					str = str.trim();
					if (!str.startsWith("#")) {

						if (str.startsWith("DBURL")) {
							Types.setDBURL(StringOps.fastSplit(str, "=").get(1).trim());
							dburl = true;
						}

						if (str.startsWith("DBCLASS")) {
							Types.setDBCLASS(StringOps.fastSplit(str, "=").get(1).trim());
							dbclass = true;
						}

						if (str.startsWith("USR")) {
							Types.setDBUSR(StringOps.fastSplit(str, "=").get(1).trim());
							dbusr = true;
						}

						if (str.startsWith("PWD")) {
							Types.setDBPWD(StringOps.fastSplit(str, "=").get(1).trim());
							dbpwd = true;
						}

					}

				}
				if (dburl && dbclass)
					DULogger.log(400, "INFO: DB Conf loaded from file\n");
				if (!dburl && dbclass)
					DULogger.log(300, "WARNING: DB URL not loaded from file, default jdbc:sqlite:../db/DMSV.db\n");
				if (!dbclass)
					DULogger.log(300, "WARNING: DB CLASS not loaded from file, default SQLite\n");
				if (!dbusr)
					DULogger.log(300, "WARNING: DB USR not loaded from file, default to be used\n");
				if (!dbpwd)
					DULogger.log(300, "WARNING: DB PWD not loaded from file, default to be used\n");

			}
		} catch (Exception e) {
			DULogger.log(200, "ERROR: DB Conf files could not be loaded\n" + LogStackTrace.get(e));
		}
	}

	private void loadRecent(String name) {
		try {
			fileContent.setText("");
			fileContent.setCaretPosition(0);
			String content = FileOperation.getContentAsString(name, "utf8");
			if (content != null) {
				if (saveFileLocationText.length() != 0) {
					saveFileLocationText.delete(0, saveFileLocationText.length());
				}
				saveFileLocationText.append(name);
				mainui.setTitle("Data Modeling And Service Virtualization - " + saveFileLocationText.toString());
				DockedControlPanel.setLastUsedDir(new File(name).getParent());
				UIUtil.writeToPane(fileContent, content, Color.BLACK, Color.WHITE, false, false, false);
				fixTextColor();
				fileContent.setCaretPosition(0);
			} else {
				DULogger.log(200, "ERROR: DMS script not found.");
			}
		} catch (Exception e) {
			DULogger.log(200, "ERROR: DMS script could not be loaded." + LogStackTrace.get(e));
		}
	}

	private void loadExampleDir(JMenu dataGenExample, String examppleDir) {

		try {
			List<String> files = FileOperation.getListofFiles(examppleDir, false, false);
			if (files != null) {
				for (String file : files) {

					String content = FileOperation.getContentAsString(file, "utf8");
					String name = UtilityFunctions
							.getInBetweenFast(content, Types.DMSEXAMPLENAMESTART, Types.DMSEXAMPLENAMEEND, true).get(0);
					String example = UtilityFunctions
							.getInBetweenFast(content, Types.DMSEXAMPLESTART, Types.DMSEXAMPLEEND, true).get(0);
					if (name != null && example != null)
						examples.put(name, example);
					JMenuItem jmi = new JMenuItem(name);

					jmi.addActionListener(e -> loadExample(jmi.getText()));
					dataGenExample.add(jmi);
				}
			}
		} catch (Exception e) {
			DULogger.log(200,
					"ERROR: Examples could not be loaded. Possible error in example file format. Tags <dms-example-name> and <dms-example> are mandatory.\n"
							+ LogStackTrace.get(e));
		}
	}

	private void loadExample(String name) {
		try {
			fileContent.setText("");
			fileContent.setCaretPosition(0);
			String example = examples.get(name);
			List<String> dmscripts = UtilityFunctions.getInBetweenFast(example, "<dms-dmsscript>", "</dms-dmsscript>",
					true);
			if (!dmscripts.isEmpty()) {
				String dmsscript = dmscripts.get(0);
				UIUtil.writeToPane(fileContent, dmsscript, Color.BLACK, Color.WHITE, false, false, false);
				fileContent.setCaretPosition(0);
				fixTextColor();
			} else {
				DULogger.log(200,
						"ERROR: DMS script not found in the loaded examples. <dms-dmsscript> and </dms-dmsscript> tags should be used to define DMS script in example.");
			}
		} catch (Exception e) {
			DULogger.log(200, "ERROR: Example could not be loaded." + LogStackTrace.get(e));
		}
	}

	protected void populateCustomParam() {
		String[] sa1 = new String[Types.getInstance().getDataTypeList().length];
		for (int i = 0; i < sa1.length; i++)
			sa1[i] = Types.getInstance().getDataTypeList()[i].replace(Types.CUSTOMPARAMNAMEENCLOSURE, "");
		Arrays.sort(sa1);
		datatypes.removeAllItems();
		for (String s : sa1)
			datatypes.addItem(s);
	}

	int currentFindPosition = 0;
	String currentFindString = "";

	public void find(String toFind) {
		if (toFind != null && toFind.length() > 0) {
			if (findPane == fileContent || findPane == output) {
				UIUtil.setCaret(output, false);
				int count = UIUtil.setFontBackground(findPane, toFind, Types.getInstance().getFindColor());// output
				writeToOutput("\nFound " + count + " occurences");
			} /*
				 * else { if (!currentFindString.equals(toFind)) { currentFindPosition = 0;
				 * currentFindString = toFind; } String str = output.getText(); int find =
				 * str.indexOf(toFind, currentFindPosition); if (find > 0) { currentFindPosition
				 * = find + toFind.length(); output.setCaretPosition(find);
				 * output.setSelectionStart(find); output.setSelectionEnd(find +
				 * toFind.length()); } else { currentFindPosition = 0;
				 * output.setCaretPosition(output.getDocument().getLength());
				 * writeToOutput("\nINFO: Text \"" + toFind + "\" not found in the output."); }
				 * }
				 */
		} else {
			DULogger.log(0, "ERROR: ISSUE WITH FIND: Please check Find or Script text values...");
			JOptionPane.showMessageDialog(null, "ISSUE WITH FIND: Please check Find or Script text values...", "ERROR",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}

	public void replace(String toFind, String replaceWith, boolean isRegex) {

		if (replaceWith == null || replaceWith.length() == 0)
			replaceWith = "";

		String str = fileContent.getText();
		if (findPane == output) {
			DULogger.log(0, "ERROR: ISSUE WITH REPLACE: Replace function not applicable to output pane...");
			JOptionPane.showMessageDialog(null,
					"ERROR: ISSUE WITH REPLACE: Replace function not applicable to output pane...", "ERROR",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			if (toFind != null && toFind.length() > 0 && str != null && str.length() > 0) {
				fileContent.setText("");
				if (replaceWith.length() > 0) {
					if (!isRegex)
						UIUtil.writeToPane(fileContent, str.replace(toFind, replaceWith).replaceAll("\r", ""),
								Color.BLACK, Color.WHITE, false, false, false);
					else
						UIUtil.writeToPane(fileContent, str.replaceAll(toFind, replaceWith).replaceAll("\r", ""),
								Color.BLACK, Color.WHITE, false, false, false);
				} else {
					DULogger.log(0, "WARNING: \"" + toFind + "\"" + " is being replaced with EMPTY string...");
					if (!isRegex)
						UIUtil.writeToPane(fileContent, str.replace(toFind, "").replaceAll("\r", ""), Color.BLACK,
								Color.WHITE, false, false, false);
					else
						UIUtil.writeToPane(fileContent, str.replaceAll(toFind, "").replaceAll("\r", ""), Color.BLACK,
								Color.WHITE, false, false, false);
				}
				UIUtil.setFontColor(fileContent, Types.getInstance().getKeywordsAndColors(), null);
				if (replaceWith.length() > 0) {
					int count = UIUtil.setFontBackground(fileContent, replaceWith, Types.getInstance().getFindColor());
					UIUtil.setCaret(output, false);
					writeToOutput(
							"\nReplaced " + count + " occurences of \"" + toFind + "\" with \"" + replaceWith + "\"");
				}
				fileContentChangedSinceLastSave = true;
			} else {
				DULogger.log(0, "ERROR: ISSUE WITH REPLACE: Find text can't be blank...");
				JOptionPane.showMessageDialog(null, "ERROR: ISSUE WITH REPLACE: Find text can't be blank...", "ERROR",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private void recentFileManage(String filename) {
		List<String> recentFileList = FileOperation.getContentAsList(recentFiles, "utf8");
		if (!recentFileList.contains(filename))
			recentFileList.add(filename);
		if (recentFileList.size() > 10)
			recentFileList.remove(0);
		FileOperation.writeFile("", recentFiles, recentFileList, true, false);
		loadRecentFilesToMenu();
	}

	private void loadRecentFilesToMenu() {
		recent.removeAll();
		loadRecentFiles(recent, recentFiles);
	}

	private void initUI() {

		DULogger.setLogLevel(400);
		examples = new HashMap<>();

		String title = "Data Modeling And Service Virtualization";

		mainui.setIconImage(img.getImage());
		JMenuBar menubar = new JMenuBar();
		JMenu filemenu = new JMenu("File");

		JMenuItem clearAll = new JMenuItem("New/Reset DMSV");
		JMenuItem open = new JMenuItem("Open File");
		JMenuItem save = new JMenuItem("Save File");
		JMenuItem saveAs = new JMenuItem("Save File As");
		JMenuItem openAsSchema = new JMenuItem("Open File as DTD Schema");
		JMenuItem exit = new JMenuItem("Exit");

		loadRecentFiles(recent, recentFiles);
		loadDBConf(dbconffilename);

		filemenu.add(clearAll);
		filemenu.add(open);
		filemenu.add(save);
		filemenu.add(saveAs);
		filemenu.add(openAsSchema);
		filemenu.add(recent);
		filemenu.add(exit);

		JMenu datautils = new JMenu("Utils");
		JMenuItem decode = new JMenuItem("Convert Encoding");

		datautils.add(decode);
		JMenuItem listFiles = new JMenuItem("List files in Directory/Folder");
		JMenuItem delimPrint = new JMenuItem(
				"Print config file delimiter for copy/paste in command config files/batch");

		datautils.add(listFiles);
		datautils.add(delimPrint);
		JMenu keys = new JMenu("Keys");
		JMenuItem clear = new JMenuItem("Clear Output");
		JMenuItem validateScript = new JMenuItem("Validate Script");
		JMenuItem extractmapper = new JMenuItem("Open Extract Map Designer");
		JMenuItem dockedcontrol = new JMenuItem("Open Docked Control Panel");
		JMenuItem fr = new JMenuItem("Find / Replace");
		JMenuItem pgvf = new JMenuItem("Print Global Virtual Files");

		JMenu help = new JMenu("Help");
		JMenuItem segmentRefHelp = new JMenuItem("Segment Repetition Reference");
		JMenuItem simpleDateFormat = new JMenuItem("Simple Date Format Reference");
		help.add(segmentRefHelp);
		help.add(simpleDateFormat);

		JMenu examplesMenu = new JMenu("Examples");
		JMenu dataGenExample = new JMenu("Data Generation");
		JMenu extractMapperExample = new JMenu("Extract Mapper");
		examplesMenu.add(dataGenExample);
		examplesMenu.add(extractMapperExample);

		keys.add(clear);
		keys.add(validateScript);
		keys.add(fr);
		keys.add(dockedcontrol);
		keys.add(extractmapper);
		keys.add(pgvf);

		JLabel element = new JLabel("Element:");
		JLabel parameter = new JLabel("Parameter:");
		JLabel format = new JLabel("CustomDataType Format:");
		JLabel size = new JLabel("Generated File Size:");
		JLabel log = new JLabel("Log Control");
		JLabel curPosLabel = new JLabel("Cursor Position:");
		// JLabel undoRedoCountLabel = new JLabel("Undo/Redo Event Count:");
		JCheckBox printOnCheck = new JCheckBox("Print Output (or check size only)?");
		printOnCheck.setSelected(true);
		JRadioButton asXML = new JRadioButton("As XML");
		JRadioButton asJson = new JRadioButton("As JSON");
		JRadioButton asNone = new JRadioButton("As None", true);
		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(asJson);
		bgroup.add(asXML);
		bgroup.add(asNone);

		JButton clearLog = new JButton("Clear Log");
		JButton insertParam = new JButton("Insert Param");
		JButton insertElement = new JButton("Insert Element");
		JButton checkSize = new JButton("Validate Script");
		// JButton undoRedoCountSet = new JButton("Set Undo Redo Count");
		JButton undoRedoCountReset = new JButton("Reset Undo Redo");

		JButton datatype = new JButton("Insert Format");

		if (!Types.isMacOS()) {
			open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			dockedcontrol.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
			fr.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
			saveAs.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
			openAsSchema.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
			clear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
			clearAll.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
			validateScript.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		} else {
			int commandKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			// Method for Java 10+ is provided below.
			// int commandKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
			open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, commandKey));
			save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, commandKey));
			dockedcontrol.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, commandKey));
			fr.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, commandKey));
			saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, commandKey | ActionEvent.SHIFT_MASK));
			openAsSchema.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, commandKey | ActionEvent.SHIFT_MASK));
			clear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, commandKey | ActionEvent.SHIFT_MASK));
			clearAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, commandKey | ActionEvent.SHIFT_MASK));
			validateScript.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, commandKey | ActionEvent.SHIFT_MASK));
		}

		menubar.add(filemenu);
		menubar.add(datautils);
		menubar.add(keys);
		menubar.add(examplesMenu);
		menubar.add(help);

		JComboBox<String> elements = new JComboBox<>(Types.getInstance().getElementTypes());
		JComboBox<String> logLevel = new JComboBox<>(new String[] { "Log Level", "0 - Off", "100 - Fatal",
				"200 - Error", "300 - Warn", "400 - Info", "500 - Debug", "600 - Trace" });

		String[] sa = Types.getInstance().getParamTypes().clone();
		Arrays.sort(sa);
		JComboBox<String> params = new JComboBox<>(sa);
		params.setPreferredSize(new Dimension(10, 10));

		JTextField sizeDisplay = new JTextField(10);// 20
		sizeDisplay.setMaximumSize(sizeDisplay.getPreferredSize());

		JTextField curPos = new JTextField(5);// 20
		curPos.setMaximumSize(curPos.getPreferredSize());

		output = new JTextPane();
		output.setFont(f1);
		output.setPreferredSize(new Dimension(40, 40));// 50,50
		UIUtil.setFontColor(output, Color.RED);
		JScrollPane scrollPaneOutput = new JScrollPane(output);
		scrollPaneOutput.setPreferredSize(new Dimension(45, 45));// 60,60

		fileContent = new JTextPane();
		fileContent.setFont(f1);
		UIUtil.setUndoCapability(fileContent);
		fileContent.setPreferredSize(new Dimension(40, 40));// 50,50
		JScrollPane scrollPaneFileContent = new JScrollPane(fileContent);
		scrollPaneFileContent.setPreferredSize(new Dimension(45, 45));// 60,60

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneFileContent, scrollPaneOutput);
		splitPane.setResizeWeight(0.7);

		loadExampleDir(dataGenExample, "../examples/datagen");
		loadExampleDir(extractMapperExample, "../examples/svextractmapper");

		// action listeners for buttons etc
		fileContent.addKeyListener(new KeyListener() {

			Set<Character> pressed = new HashSet<>();

			public void keyTyped(KeyEvent e) {
				fileContentChangedSinceLastSave = true;
				if (saveFileLocationText.length() > 0) {
					FileOperation.writeFile("", saveFileLocationText.toString(),
							fileContent.getText().replaceAll("\r", ""));
					fileContentChangedSinceLastSave = false;
				}
			}

			@Override
			public synchronized void keyPressed(KeyEvent e) {
				// Auto-generated method stub
				if (pressed.size() == 1) {
					int keyCode = e.getKeyCode();
					if (keyCode == KeyEvent.VK_RIGHT && fileContent.getSelectedText() != null) {

						fileContent.setCaretPosition(fileContent.getSelectionEnd());

					}
					if (keyCode == KeyEvent.VK_LEFT && fileContent.getSelectedText() != null) {

						fileContent.setCaretPosition(fileContent.getSelectionStart() + 2);

					}
					curPos.setText(Integer.toString(fileContent.getCaretPosition()));

				}
			}

			@Override
			public synchronized void keyReleased(KeyEvent e) {
				fileContentChangedSinceLastSave = true;
				if (saveFileLocationText.length() > 0) {
					FileOperation.writeFile("", saveFileLocationText.toString(),
							fileContent.getText().replaceAll("\r", ""));
					fileContentChangedSinceLastSave = false;
				}
				pressed.clear();
			}
		});

		fileContent.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				findPane = fileContent;
				curPos.setText(Integer.toString(fileContent.getCaretPosition()));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// Auto-generated method stub
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// Auto-generated method stub
			}
		});

		output.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				findPane = output;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// Auto-generated method stub

			}
		});

		exit.addActionListener(e -> {

			if (fileContentChangedSinceLastSave) {

				int jop = JOptionPane.showConfirmDialog(null, "Do you want to save you work?", "Choose Option",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (jop == JOptionPane.YES_OPTION) {

					if (saveFileLocationText.length() == 0) {
						JFileChooser fileChooser = new JFileChooser();
						if (DockedControlPanel.getLastUsedDir() == null
								|| DockedControlPanel.getLastUsedDir().length() == 0)
							fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")
									+ System.getProperty("file.separator") + "Desktop"));
						else
							fileChooser.setCurrentDirectory(new File(DockedControlPanel.getLastUsedDir()));
						if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							File selectedFile = fileChooser.getSelectedFile();
							saveFileLocationText.append(selectedFile.getPath());
							DockedControlPanel.setLastUsedDir(selectedFile.getParent());
						} else
							return;

					}
					String toSave = fileContent.getText().replaceAll("\r", "");

					DULogger.log(400, "<<<<<File Start>>>>>\n" + toSave + "\n<<<<<File End>>>>>");
					DULogger.log(0, "Saved");
					FileOperation.writeFile("", saveFileLocationText.toString(), toSave);
					System.exit(0);

				}
				if (jop == JOptionPane.NO_OPTION) {
					System.exit(0);

				}
			} else
				System.exit(0);

		});
		delimPrint.addActionListener(e -> {
			DULogger.log(0, "");
			DULogger.log(0, "<DMSCONFIGITEMDELIM>");
			DULogger.log(0, "");
		});

		undoRedoCountReset.addActionListener(e -> {
			UIUtil.resetURC();
		});
		fr.addActionListener(e -> FindReplace.getInstance(Types.getInstance().getUI().getUI()).start());
		extractmapper.addActionListener(e -> new ExtractMapper(Types.getInstance().getUI().getUI()).init());
		pgvf.addActionListener(e -> GlobalVirtualFileParameters.getInstance().getVFP().print());
		dockedcontrol.addActionListener(e -> {

			DockedControlPanel.getInstance().start(false, true, false);
			cp.setVisible(!cp.isVisible());
			Types.getInstance().getUI().getUI().pack();
			Types.getInstance().getUI().getUI().setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		});
		simpleDateFormat.addActionListener(e -> {
			JFrame f = new JFrame();
			JLabel l = new JLabel(new ImageIcon("../images/SimpleDateFormat.jpg"), SwingConstants.CENTER);
			f.add(l);
			f.setSize(810, 420);
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});

		segmentRefHelp.addActionListener(e -> {
			UIUtil.setCaret(output, false);
			DULogger.log(0, "---------------------------");
			DULogger.log(0, "Printing possible values for Segment Repetition Conditions...");
			DULogger.log(0,
					"These are to be used (comma separated if multiple conditions) here IN PLACE OF XXXXX...#SeGmEnT#XXXXX^^");
			DULogger.log(0,
					"E.g. #SeGmEnT#first,!last,/5,!7^^... which would mean that : \n 1. Segment will repeat in iteration 1 \n 2. Will not repeat in last iteration of parent group \n 3. Repeat in 5, 10, 15, 20, 25 iterations \n 4. Repeat in all iterations which are not multiples of 7 like 1, 2, 3, 4, 5, 6, 8, 9 etc");
			DULogger.log(0,
					"NOTE: Condition like /2,!/3 will conflict at iteration 6, 12, 18 etc. At those iterations of parent group, condition /2 would want to repeat segment, while !/3 would try to exclude it. In such scenario, conditions which allows repetition wins, implying that iteration 6, 12, 18 will be repeated and effect of !/3 will be negated.");

			DULogger.log(0, "---------------------------");
			for (String s : SegmentRepetitionEval.getSegmentRepetitionConditions())
				DULogger.log(0, s);
			DULogger.log(0, "---------------------------");
		});

		clearAll.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(null, "Are you sure? This will reset DMS and will clear all fields.",
					"Choose Option", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				fileContent.setText("");
				if (saveFileLocationText.length() > 0)
					saveFileLocationText.delete(0, saveFileLocationText.length());
				output.setText("");
				FindReplace.getInstance(Types.getInstance().getUI().getUI()).clear();
				DULogger.clear();
				sizeDisplay.setText("");
				asNone.setSelected(true);
				printOnCheck.setSelected(true);
				DockedControlPanel.getInstance().clearAll();
				fileContent.setBackground(Color.WHITE);
				UIUtil.setFontColor(fileContent, Color.BLACK);
				UIUtil.setFontBackground(fileContent, Color.WHITE);
				mainui.setTitle(title);
				GlobalVirtualFileParameters.getInstance().reset();
			}
		});

		// validate script using button
		checkSize.addActionListener(e -> {

			String inputdirloc = DockedControlPanel.getExtractInputDirLocation();
			if (asXML.isSelected())
				checkSizeFunc(checkSize, inputdirloc, sizeDisplay, "xml", printOnCheck);
			if (asJson.isSelected())
				checkSizeFunc(checkSize, inputdirloc, sizeDisplay, "json", printOnCheck);
			if (asNone.isSelected())
				checkSizeFunc(checkSize, inputdirloc, sizeDisplay, "none", printOnCheck);
			// DULogger.log(0, "UNDO Event:" + x + "," + UIUtil.undoCount +
			// ","+(UIUtil.undoCount-x));

		});
		// validate script using menu item under Keys
		validateScript.addActionListener(e -> {

			String inputdirloc = DockedControlPanel.getExtractInputDirLocation();
			if (asXML.isSelected())
				checkSizeFunc(checkSize, inputdirloc, sizeDisplay, "xml", printOnCheck);
			if (asJson.isSelected())
				checkSizeFunc(checkSize, inputdirloc, sizeDisplay, "json", printOnCheck);
			if (asNone.isSelected())
				checkSizeFunc(checkSize, inputdirloc, sizeDisplay, "none", printOnCheck);
			// DULogger.log(0, "UNDO Event:" + x + "," + UIUtil.undoCount +
			// ","+(UIUtil.undoCount-x));

		});
		clear.addActionListener(e -> {
			output.setText("");
			DULogger.clear();

		});
		clearLog.addActionListener(e -> {
			output.setText("");
			DULogger.clear();

		});

		logLevel.addActionListener(e -> DULogger.setLogLevel(Integer.parseInt(logLevel.getSelectedItem().toString()
				.replace("Log Level", "400").replace(" - Fatal", "").replace(" - Error", "").replace(" - Warn", "")
				.replace(" - Debug", "").replace(" - Off", "").replace(" - Trace", "").replace(" - Info", "").trim()))

		);

		save.addActionListener(e -> {
			if (saveFileLocationText.length() == 0) {
				JFileChooser fileChooser = new JFileChooser();
				DockedControlPanel.setDirForFileChooser(fileChooser);
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					saveFileLocationText.append(selectedFile.getPath());
					DockedControlPanel.setLastUsedDir(selectedFile.getParent());
				} else
					return;

			}
			String toSave = fileContent.getText().replaceAll("\r", "");

			DULogger.log(400, "<<<<<File Start>>>>>\n" + toSave + "\n<<<<<File End>>>>>");
			DULogger.log(0, "Saved");
			FileOperation.writeFile("", saveFileLocationText.toString(), toSave);
			fileContentChangedSinceLastSave = false;
			mainui.setTitle("Data Modeling And Service Virtualization - " + saveFileLocationText.toString());

		});
		saveAs.addActionListener(e -> {

			JFileChooser fileChooser = new JFileChooser();
			DockedControlPanel.setDirForFileChooser(fileChooser);
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();

				if (saveFileLocationText != null && saveFileLocationText.length() > 0)
					saveFileLocationText.delete(0, saveFileLocationText.length());

				saveFileLocationText.append(selectedFile.getPath());
				DockedControlPanel.setLastUsedDir(selectedFile.getParent());
				recentFileManage(selectedFile.getAbsolutePath());
				String toSave = fileContent.getText().replaceAll("\r", "");

				DULogger.log(400, "<<<<<File Start>>>>>\n" + toSave + "\n<<<<<File End>>>>>");
				DULogger.log(0, "Saved");
				FileOperation.writeFile("", saveFileLocationText.toString(), toSave);
				fileContentChangedSinceLastSave = false;
				mainui.setTitle("Data Modeling And Service Virtualization - " + saveFileLocationText.toString());
			}

		});

		openAsSchema.addActionListener(e -> {
			String origContent = fileContent.getText();
			String origfileLocation = saveFileLocationText.toString();
			fileContent.setText("");
			fileContent.setBackground(Color.BLACK);

			UIUtil.writeToPane(fileContent, Types.DTDSCHEMAMESSAGE, Color.GREEN, Color.BLACK, true, false, false);

			if (JOptionPane.showConfirmDialog(null,
					"Some information is provided in the script pane about using DTD schema.\nPlease read before proceeding.\nProceed?",
					"Choose Option", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				fileContent.setBackground(Color.WHITE);
				UIUtil.setFontColor(fileContent, Color.BLACK);
				UIUtil.setFontBackground(fileContent, Color.WHITE);

				JFileChooser fileChooser = new JFileChooser();
				DockedControlPanel.setDirForFileChooser(fileChooser);
				fileContent.setText("");
				if (saveFileLocationText.length() != 0)
					saveFileLocationText.delete(0, saveFileLocationText.length());
				fileContent.setBackground(Color.WHITE);
				String str = "";

				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					if (selectedFile.getPath().length() > 0) {
						List<String> file = FileOperation.getContentAsList(selectedFile.getPath(), "utf8");

						if (JOptionPane.showConfirmDialog(null, "Do you want to load original XML values?",
								"Choose Option", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							fileChooser = new JFileChooser();
							DockedControlPanel.setDirForFileChooser(fileChooser);
							if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
								File selectedFile1 = fileChooser.getSelectedFile();
								List<String> origFile = FileOperation.getContentAsList(selectedFile1.getPath(), "utf8");
								str = new ProcessSchema(file, origFile).getValue();
							}
						} else {
							str = new ProcessSchema(file).getValue();

						}
						saveFileLocationText.append(selectedFile.getPath() + ".dms");
						FileOperation.writeFile("", saveFileLocationText.toString(), str);
						mainui.setTitle(
								"Data Modeling And Service Virtualization - " + saveFileLocationText.toString());
						UIUtil.writeToPane(fileContent, str, Color.BLACK, Color.WHITE, false, false, false);
						UIUtil.setFontColor(fileContent, Types.getInstance().getKeywordsAndColors(), null);
					}
				} else {
					fileContent.setText("");
					UIUtil.writeToPane(fileContent, origContent, Color.BLACK, Color.WHITE, false, false, false);
					UIUtil.setFontColor(fileContent, Types.getInstance().getKeywordsAndColors(), null);
					saveFileLocationText.append(origfileLocation);
					mainui.setTitle("Data Modeling And Service Virtualization - " + saveFileLocationText.toString());
				}
			} else {
				fileContent.setText("");
				fileContent.setBackground(Color.WHITE);
				UIUtil.setFontColor(fileContent, Color.BLACK);
				UIUtil.setFontBackground(fileContent, Color.WHITE);
				UIUtil.writeToPane(fileContent, origContent, Color.BLACK, Color.WHITE, false, false, false);
				UIUtil.setFontColor(fileContent, Types.getInstance().getKeywordsAndColors(), null);
				mainui.setTitle("Data Modeling And Service Virtualization - " + saveFileLocationText.toString());

			}
			UIUtil.setFontColor(fileContent, Color.BLACK);
		}

		);

		open.addActionListener(e -> {
			String origContent = fileContent.getText();
			String origfileLocation = saveFileLocationText.toString();
			JFileChooser fileChooser = new JFileChooser();
			DockedControlPanel.setDirForFileChooser(fileChooser);
			fileContent.setText("");
			if (saveFileLocationText.length() != 0) {
				saveFileLocationText.delete(0, saveFileLocationText.length());
			}
			fileContent.setBackground(Color.WHITE);
			String str = "";

			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				if (saveFileLocationText != null && saveFileLocationText.length() > 0)
					saveFileLocationText.delete(0, saveFileLocationText.length());
				saveFileLocationText.append(selectedFile.getPath());
				if (saveFileLocationText.length() > 0) {
					str = FileOperation.getFileContentAsString(saveFileLocationText.toString());
					UIUtil.writeToPane(fileContent, str, Color.BLACK, Color.WHITE, false, false, false);
					UIUtil.setFontColor(fileContent, Types.getInstance().getKeywordsAndColors(), null);
					mainui.setTitle("Data Modeling And Service Virtualization - " + saveFileLocationText.toString());
				} else {
					fileContent.setText("");
					UIUtil.writeToPane(fileContent, origContent, Color.BLACK, Color.WHITE, false, false, false);
					UIUtil.setFontColor(fileContent, Types.getInstance().getKeywordsAndColors(), null);
				}
				DockedControlPanel.setLastUsedDir(selectedFile.getParent());
				recentFileManage(selectedFile.getAbsolutePath());
			} else {
				fileContent.setText("");
				UIUtil.writeToPane(fileContent, origContent, Color.BLACK, Color.WHITE, false, false, false);
				UIUtil.setFontColor(fileContent, Types.getInstance().getKeywordsAndColors(), null);
				saveFileLocationText.append(origfileLocation);
				recentFileManage(origfileLocation);
			}
			UIUtil.setFontColor(fileContent, Color.BLACK);
		});
		listFiles.addActionListener(e -> {
			boolean printDirNames = false;
			boolean recurseSubDir = false;
			if (JOptionPane.showConfirmDialog(listFiles, "Print Directory/Folder Names while preparing list?", "",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				printDirNames = true;

			}
			if (JOptionPane.showConfirmDialog(listFiles, "Recurse Sub Directories/Sub Folder?", "",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				recurseSubDir = true;
			}

			List<String> list = null;
			if (JOptionPane.showConfirmDialog(listFiles, "Select directory/folder for listing files...", "",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				JFileChooser fileChooser = new JFileChooser();
				DockedControlPanel.setDirForFileChooser(fileChooser);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File selectedDir = fileChooser.getSelectedFile();
					list = FileOperation.getListofFiles(selectedDir.getPath(), printDirNames, recurseSubDir);
				}

				if (JOptionPane.showConfirmDialog(listFiles,
						"Save list of files (or just print in the output logs pane?)", "",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					JFileChooser fileChooser1 = new JFileChooser();
					DockedControlPanel.setDirForFileChooser(fileChooser1);
					if (fileChooser1.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						File selectedFile1 = fileChooser1.getSelectedFile();
						FileOperation.writeFile(selectedFile1.getPath(), list, true);
						DULogger.log(0, "File names written to " + selectedFile1.getPath());
					} else {
						for (String s : list)
							DULogger.log(0, s);
					}
				} else {
					for (String s : list)
						DULogger.log(0, s);
				}
			} else
				DULogger.log(0,
						"ERROR: No directory was selected for listing files in directory. Operation Cancelled");

		});
		decode.addActionListener(e -> {
			String inputValue = JOptionPane
					.showInputDialog("Enter Comma Separated Source and Target charsets encoding");
			if (inputValue != null && inputValue.length() > 0 && inputValue.contains(",")) {
				JFileChooser fileChooser = new JFileChooser();
				DockedControlPanel.setDirForFileChooser(fileChooser);
				if (JOptionPane.showConfirmDialog(null,
						"Encoding has been specified for this file. Encoding should be in the format src,dest.\nYou will be asked to choose 2 directories for source and destination files, respectively. \nGenerated File Extn field will be used to create files in destination directory.\nIf anything doesn't look right, Cancel now. Do you want to proceed?",
						"Choose Option", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					File sourceDir = null;
					File destDir = null;
					if (JOptionPane.showConfirmDialog(null, "Choose directory with encoded files (source). Yes?",
							"Choose Option", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						DockedControlPanel.setDirForFileChooser(fileChooser);
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							sourceDir = fileChooser.getSelectedFile();
						}
					}
					if (JOptionPane.showConfirmDialog(null,
							"Choose directory to store converted files (destination). Yes?", "Choose Option",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						DockedControlPanel.setDirForFileChooser(fileChooser);
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							destDir = fileChooser.getSelectedFile();
						}
					}
					if (sourceDir != null && destDir != null) {
						String[] codes = inputValue.split(",");
						int i = 0;
						for (final File fileEntry : sourceDir.listFiles()) {
							if (!fileEntry.isDirectory()) {
								String content = FileOperation.getContentAsString(
										sourceDir.getPath() + File.separator + fileEntry.getName(), codes[0]);

								if (codes[1].contains("gzipbase64") || codes[1].contains("GZIPBASE64"))
									FileOperation.writeFile(destDir.getPath(),
											fileEntry.getName() + "to_" + codes[1] + "." + ".txt",
											GzipUtil.compressgzipbase64(content));
								else if (codes[1].contains("base64") || codes[1].contains("BASE64"))
									FileOperation.writeFile(destDir.getPath(),
											fileEntry.getName() + "to_" + codes[1] + "." + ".txt",
											GzipUtil.encodebase64(content));
								else
									FileOperation.writeFile(destDir.getPath(),
											fileEntry.getName() + "to_" + codes[1] + ".txt", content, codes[1]);

								if (i % 10 == 0)
									DULogger.log(400,
											"INFO: Printing info every 10 files... file writen to destination dir "
													+ fileEntry.getName() + "to_" + codes[1] + ".txt");
							} else
								DULogger.log(300,
										"WARNING: Skipping " + fileEntry.getName() + " as it is directory...");
							i++;
						}
						DULogger.log(0, "File connversion operation complete. Please check destination directory...");
						JOptionPane.showMessageDialog(null,
								"File connversion operation complete. Please check destination directory...", "INFO",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						DULogger.log(0, "ERROR: Either source or destination directory was not chosen...");
						JOptionPane.showMessageDialog(null, "Either source or destination directory was not chosen...",
								"ERROR", JOptionPane.INFORMATION_MESSAGE);
					}
				}

			} else
				DULogger.log(0, "ERROR: No encoding were properly entered...");

		});

		insertParam.addActionListener(e -> {
			int x = UIUtil.undoCount;
			UIUtil.collect = true;
			if (Types.getInstance().getParamTypeAndSampleMap().get(params.getSelectedItem()) != null) {
				String s = (String) params.getSelectedItem();
				if (s.equals("db")) {
					DBConfig dbc = new DBConfig(Types.getInstance().getUI().getUI());
					String file = dbc.getConfigFile();
					if (file.length() <= 0)
						return;
					else {
						UIUtil.writeToPane(fileContent,
								Types.getInstance().getParamTypeAndSampleMap().get(s)
										.replace("db config file path", file)
										.replace("parameterName", "parameterName" + getIndex()),
								Color.RED, Color.WHITE, false, false, false);
					}
				} else {
					UIUtil.writeToPane(fileContent, Types.getInstance().getParamTypeAndSampleMap().get(s)
							.replace("parameterName", "parameterName" + getIndex()), Color.RED, Color.WHITE, false,
							false, false);
				}
				fileContentChangedSinceLastSave = true;
			}
			UIUtil.setUndoRedoCount(UIUtil.undoCount - x);
			UIUtil.collect = false;
			fixTextColor();
			// DULogger.log(0, "UNDO Event:" + x + "," + UIUtil.undoCount +
			// ","+(UIUtil.undoCount-x));

		});
		datatype.addActionListener(e -> {
			int x = UIUtil.undoCount;
			UIUtil.collect = true;
			String s = "#" + (String) datatypes.getSelectedItem() + "#";
			UIUtil.writeToPane(fileContent, s, Types.getInstance().getKeywordsAndColors().get(s), Color.WHITE, false,
					false, false);
			fileContentChangedSinceLastSave = true;
			// DULogger.log(0, "UNDO Event:" + x + "," + UIUtil.undoCount +
			// ","+(UIUtil.undoCount-x));
			UIUtil.setUndoRedoCount(UIUtil.undoCount - x);
			UIUtil.collect = false;
		});
		insertElement.addActionListener(e -> {
			int x = UIUtil.undoCount;
			UIUtil.collect = true;
			if (Types.getInstance().getElementTypeAndSampleMap().get(elements.getSelectedItem()) != null) {
				String s = (String) elements.getSelectedItem();
				if (s.equals(Types.EID) || s.equals(Types.VFP) || s.equals(Types.GVFP) || s.equals(Types.EXTMAP))
					UIUtil.writeToPane(fileContent, Types.getInstance().getElementTypeAndSampleMap().get(s),
							Types.getInstance().getKeywordsAndColors().get(s), Color.WHITE, false, true, true);
				else
					UIUtil.writeToPane(fileContent, Types.getInstance().getElementTypeAndSampleMap().get(s),
							Types.getInstance().getKeywordsAndColors().get(s), Color.WHITE, false, false, false);
				fileContentChangedSinceLastSave = true;
			}

			// DULogger.log(0, "UNDO Event:" + x + "," + UIUtil.undoCount +
			// ","+(UIUtil.undoCount-x));
			UIUtil.setUndoRedoCount(UIUtil.undoCount - x);
			UIUtil.collect = false;
			fixTextColor();
		});
		JPanel scriptaidPanel = new JPanel();
		JPanel basecontrolPanel = new JPanel();

		scriptaidPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		basecontrolPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		createScriptAidPane(scriptaidPanel, element, parameter, format, size, elements, insertElement, params,
				insertParam, datatypes, datatype, checkSize, sizeDisplay, asJson, asXML, asNone, printOnCheck);
		createBasecontrolPane(basecontrolPanel, log, logLevel, clearLog, curPosLabel, curPos, undoRedoCountReset);

		JSplitPane splitPaneTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane, cp);
		splitPaneTop.setResizeWeight(0.5);

		JSplitPane splitPaneUpper = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scriptaidPanel, basecontrolPanel);
		splitPaneUpper.setResizeWeight(0.85);

		JSplitPane splitPaneMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneUpper, splitPaneTop);
		splitPaneMain.setResizeWeight(0.01);
		createLayout(splitPaneMain);

		mainui.setJMenuBar(menubar);
		mainui.setTitle(title);
		mainui.setSize(400, 500);
		DockedControlPanel.getInstance().start(true, false, false);
		cp.setVisible(false);
		mainui.pack();
		mainui.setExtendedState(JFrame.MAXIMIZED_BOTH);
		mainui.setLocationRelativeTo(null);
		mainui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		mainui.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (fileContentChangedSinceLastSave) {

					int jop = JOptionPane.showConfirmDialog(null, "Do you want to save you work?", "Choose Option",
							JOptionPane.YES_NO_CANCEL_OPTION);
					if (jop == JOptionPane.YES_OPTION) {

						if (saveFileLocationText.length() == 0) {
							JFileChooser fileChooser = new JFileChooser();
							if (DockedControlPanel.getLastUsedDir() == null
									|| DockedControlPanel.getLastUsedDir().length() == 0)
								fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")
										+ System.getProperty("file.separator") + "Desktop"));
							else
								fileChooser.setCurrentDirectory(new File(DockedControlPanel.getLastUsedDir()));
							if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
								File selectedFile = fileChooser.getSelectedFile();
								saveFileLocationText.append(selectedFile.getPath());
								DockedControlPanel.setLastUsedDir(selectedFile.getParent());
							} else
								return;

						}
						String toSave = fileContent.getText().replaceAll("\r", "");

						DULogger.log(400, "<<<<<File Start>>>>>\n" + toSave + "\n<<<<<File End>>>>>");
						DULogger.log(0, "Saved");
						FileOperation.writeFile("", saveFileLocationText.toString(), toSave);
						System.exit(0);

					}
					if (jop == JOptionPane.NO_OPTION) {
						System.exit(0);

					}
				} else
					System.exit(0);
			}
		});

		populateCustomParam();
		writeToOutput(
				"\nFor additional advanced options:\nPress Control + F for find/replace\nPress Control + P for opening/closing of docked control panel\nOR check \"Keys\" Menu for these options");
		DULogger.log(0,
				"\nFor additional advanced options:\nPress Control + F for find/replace\nPress Control + P for opening/closing of docked control panel\nOR check \"Keys\" Menu for these options");
	}

	// createScriptAidPane takes arguments (scriptaid, elements, insertElement,
	// params, insertParam,
	// datatypes, datatype, customDataDir, checkSize, sizeDisplay, asJson, asXML)
	private void createScriptAidPane(JPanel scriptaid, JLabel element, JLabel parameter, JLabel format, JLabel size,
			JComboBox<String> elements, JButton insertElement, JComboBox<String> params, JButton insertParam,
			JComboBox<String> datatypes, JButton datatype, JButton checkSize, JTextField sizeDisplay,
			JRadioButton asJson, JRadioButton asXML, JRadioButton asNone, JCheckBox printOnCheck) {
		sizeDisplay.setEditable(false);
		GroupLayout layout = new GroupLayout(scriptaid);
		scriptaid.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(element, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(elements, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(insertElement, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(parameter, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(params, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(insertParam, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(format, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(datatypes, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(datatype, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(size, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(sizeDisplay, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(asJson, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(asXML, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(asNone, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(printOnCheck, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(checkSize, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(element)
						.addComponent(elements).addComponent(insertElement))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(parameter)
						.addComponent(params).addComponent(insertParam))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(format)
						.addComponent(datatypes).addComponent(datatype))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(size)
						.addComponent(sizeDisplay).addComponent(asJson).addComponent(asXML).addComponent(asNone)
						.addComponent(printOnCheck).addComponent(checkSize)));

	}

	// createBasecontrolPane takes arguments (basecontrol, log,
	// logLevel,
	// clearLog, curPosLabel, curPos)
	private void createBasecontrolPane(JPanel basecontrol, JLabel log, JComboBox<String> logLevel, JButton clearLog,
			JLabel curPosLabel, JTextField curPos, JButton undoRedoCountReset) {
		curPos.setEditable(false);
		GroupLayout layout = new GroupLayout(basecontrol);
		basecontrol.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addComponent(log, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addComponent(logLevel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(curPosLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(curPos, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(undoRedoCountReset, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addComponent(clearLog, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(log))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(logLevel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(curPosLabel)
						.addComponent(curPos).addComponent(undoRedoCountReset))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(clearLog))

		);

	}

	private void createLayout(JComponent... arg) {
		JPanel pane = new JPanel();
		JScrollPane scrollPane = new JScrollPane(pane);
		mainui.add(scrollPane);
		GroupLayout layout = new GroupLayout(pane);
		pane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		/*
		 * createLayout function takes argument (scriptaidPanel 0, basecontrolPanel 1,
		 * splitPane 2)
		 */

		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup()
						.addComponent(arg[0], GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				// .addComponent(arg[1], GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
				// Short.MAX_VALUE)
				)
		// .addGroup(layout.createSequentialGroup().addComponent(arg[1],
		// GroupLayout.DEFAULT_SIZE,
		// GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))

		);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(arg[0])
				// .addComponent(arg[1])
				)
		// .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(arg[1]))

		);

	}
}

/*
 * To add action to text pane you can use this code
 * 
 * 
 * String keyStrokeAndKey = "control F";
 * 
 * KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
 * fileContent.getInputMap().put(keyStroke, keyStrokeAndKey);
 * fileContent.getActionMap().put(keyStrokeAndKey, new AbstractAction() {
 * private static final long serialVersionUID = 1L;
 * 
 * @Override public void actionPerformed(ActionEvent e) {
 * FindReplace.getInstance(Types.getInstance().getUI().getUI()).start(); } });
 * output.getInputMap().put(keyStroke, keyStrokeAndKey);
 * output.getActionMap().put(keyStrokeAndKey, new AbstractAction() { private
 * static final long serialVersionUID = 1L;
 * 
 * @Override public void actionPerformed(ActionEvent e) {
 * FindReplace.getInstance(Types.getInstance().getUI().getUI()).start(); } });
 * 
 * keyStrokeAndKey = "control P"; keyStroke =
 * KeyStroke.getKeyStroke(keyStrokeAndKey);
 * fileContent.getInputMap().put(keyStroke, keyStrokeAndKey);
 * fileContent.getActionMap().put(keyStrokeAndKey, new AbstractAction() {
 * private static final long serialVersionUID = 1L;
 * 
 * @Override public void actionPerformed(ActionEvent e) {
 * DockedControlPanel.getInstance().start(false, true, false);
 * cp.setVisible(!cp.isVisible());
 * 
 * Types.getInstance().getUI().getUI().pack();
 * Types.getInstance().getUI().getUI().setExtendedState(JFrame.MAXIMIZED_BOTH);
 * } }); output.getInputMap().put(keyStroke, keyStrokeAndKey);
 * output.getActionMap().put(keyStrokeAndKey, new AbstractAction() { private
 * static final long serialVersionUID = 1L;
 * 
 * @Override public void actionPerformed(ActionEvent e) {
 * DockedControlPanel.getInstance().start(true, false, false);
 * cp.setVisible(!cp.isVisible());
 * 
 * Types.getInstance().getUI().getUI().pack();
 * Types.getInstance().getUI().getUI().setExtendedState(JFrame.MAXIMIZED_BOTH);
 * } });
 */
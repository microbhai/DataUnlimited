package akhil.DataUnlimited.ui;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import akhil.DataUnlimited.DataUnlimitedApi;
import akhil.DataUnlimited.dataextractor.hierarchicaldoc.UtilityFunctions;
import akhil.DataUnlimited.model.parameter.GlobalVirtualFileParameters;
import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;
import akhil.DataUnlimited.util.FileOperation;
import akhil.DataUnlimited.util.OSCommand;

public class DockedControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static DockedControlPanel dgu;

	public static DockedControlPanel getInstance() {
		if (dgu == null)
			dgu = new DockedControlPanel();
		return dgu;
	}

	// static boolean getIsJson() {
	// return isJson.isSelected();
	// }
	static String getExtractInputDirLocation() {
		return extractInputDirLocation.getText();
	}

	static boolean getExtractIsFile() {
		return DockedControlPanel.extractIsFile.isSelected();
	}

	private static String lastUsedDir = null;
	final StringBuilder saveConfFileLocation = new StringBuilder();

	public static synchronized void setLastUsedDir(String x) {
		lastUsedDir = x;
	}

	public static synchronized String getLastUsedDir() {
		return lastUsedDir;
	}

	public static final JPanel dg = new JPanel();
	public static final JPanel em = new JPanel();
	public static final JPanel pc = new JPanel();

	static JLabel datagen = new JLabel("Data Generator");
	static JLabel fileExtnLabel = new JLabel("File Name / Suffix or Extsn (no dot)");
	static JLabel extmap = new JLabel("Extract Mapper");
	static JLabel numOfFileLabel = new JLabel("How many files to generate?");
	static JLabel loadCDTLabel = new JLabel("Load Custom Data Types (from .dat files):");
	static JLabel gvfLabel = new JLabel("Global Virtual File Controls");
	static JLabel batchFileLocationLabel = new JLabel("Current Batch File Location:");

	static JTextField generateFileLocation = new JTextField(20);
	static JTextField extractParamDirLocation = new JTextField(20);
	static JTextField extractInputDirLocation = new JTextField(20);
	static JTextField extractOutputDirLocation = new JTextField(20);
	static JTextField numberOfFiles = new JTextField(20);
	static JTextField fileSuffix = new JTextField(20);
	static JTextField batchFileLocation = new JTextField(20);

	static JCheckBox isRegex = new JCheckBox("Is Find/Replace Regex?");
	static JCheckBox extractIsFile = new JCheckBox("Is Input File? (or Folder)");

	static JButton generateIn = new JButton("Select Output Folder/Dir");
	static JButton generateData = new JButton("Generate Data");
	static JButton generateConfig = new JButton("Save as Batch");
	static JButton extractInputDir = new JButton("Select Input");
	static JButton extractOutputDir = new JButton("Select Output Folder");
	static JButton extractParamDirB = new JButton("Extract Parameters in Folder (optional)");
	static JButton extractVirtualFile = new JButton("Extract Virtual File");
	static JButton executeTransform = new JButton("Execute Transform");
	static JButton customDataDir = new JButton("Select Folder / Directory with .dat files");
	static JButton globalVirtualFiles = new JButton("Load Global Virtual Files");
	static JButton clearGlobalVirtualFiles = new JButton("Clear Global Virtual Files");
	static JButton printGlobalVirtualFiles = new JButton("Print Global Virtual Files");

	// static JCheckBox isJson = new JCheckBox("Is Json?");

	public void start(boolean pc, boolean dg, boolean em) {
		ispc.setSelected(pc);
		isdg.setSelected(dg);
		isem.setSelected(em);
		start();
	}

	public void start() {

		dg.setVisible(isdg.isSelected());
		em.setVisible(isem.isSelected());
		pc.setVisible(ispc.isSelected());
		dgu.setVisible(true);
	}

	JCheckBox isdg = new JCheckBox("Data Generator");
	JCheckBox isem = new JCheckBox("Extract Mapper");
	JCheckBox ispc = new JCheckBox("Advanced Parameter Controls");

	static JPanel main = new JPanel();

	public void clearAll() {
		clearDg();
		clearEm();
		isRegex.setSelected(false);
	}

	public void clearDg() {
		numberOfFiles.setText("");
		generateFileLocation.setText("");
		fileSuffix.setText("");
	}

	public void clearEm() {
		extractParamDirLocation.setText("");
		extractInputDirLocation.setText("");
		extractOutputDirLocation.setText("");
		extractIsFile.setSelected(false);
	}

	DockedControlPanel() {
		batchFileLocation.setEnabled(false);
		makeControlPanel();
		GroupLayout layout = new GroupLayout(main);
		main.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		JLabel head = new JLabel("Control Panel");
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(head, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(isdg, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(isem, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(ispc, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(dg, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(em, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(pc, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(
				layout.createSequentialGroup().addComponent(head)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(isdg)
								.addComponent(isem).addComponent(ispc))
						.addComponent(dg).addComponent(em).addComponent(pc));

		isdg.addActionListener(e ->

		dg.setVisible(isdg.isSelected())

		);
		isem.addActionListener(e -> em.setVisible(isem.isSelected()));
		ispc.addActionListener(e -> pc.setVisible(ispc.isSelected()));

		this.add(main);
		this.setVisible(false);
	}

	public static void setDirForFileChooser(JFileChooser fileChooser) {
		if (DockedControlPanel.getLastUsedDir() == null || getLastUsedDir().length() == 0) {
			fileChooser.setCurrentDirectory(
					new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
		} else
			fileChooser.setCurrentDirectory(new File(getLastUsedDir()));
	}

	private void makeControlPanel() {

		dg.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		em.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		pc.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		GroupLayout layoute = new GroupLayout(em);
		em.setLayout(layoute);
		layoute.setAutoCreateGaps(true);
		layoute.setAutoCreateContainerGaps(true);

		layoute.setHorizontalGroup(layoute.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoute.createSequentialGroup().addComponent(extmap, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layoute.createSequentialGroup()
						.addComponent(extractIsFile, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(extractInputDir, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(extractInputDirLocation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				// .addComponent(isJson, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
				// Short.MAX_VALUE)
				.addGroup(layoute.createSequentialGroup()
						.addComponent(extractParamDirB, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(extractParamDirLocation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGroup(layoute.createSequentialGroup()
						.addComponent(extractOutputDir, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(extractOutputDirLocation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGroup(layoute.createSequentialGroup()
						.addComponent(executeTransform, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(extractVirtualFile, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)));

		layoute.setVerticalGroup(layoute.createSequentialGroup()
				.addGroup(layoute.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(extmap))
				.addGroup(layoute.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(extractIsFile)
						.addComponent(extractInputDir).addComponent(extractInputDirLocation))
				// .addComponent(isJson)
				.addGroup(layoute.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(extractParamDirB)
						.addComponent(extractParamDirLocation))
				.addGroup(layoute.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(extractOutputDir)
						.addComponent(extractOutputDirLocation))
				.addGroup(layoute.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(executeTransform)
						.addComponent(extractVirtualFile)));

		GroupLayout layoutd = new GroupLayout(dg);
		dg.setLayout(layoutd);
		layoutd.setAutoCreateGaps(true);
		layoutd.setAutoCreateContainerGaps(true);

		layoutd.setHorizontalGroup(
				layoutd.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layoutd.createSequentialGroup().addComponent(datagen, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layoutd.createSequentialGroup()
								.addComponent(generateIn, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(generateFileLocation, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layoutd.createSequentialGroup()
								.addComponent(fileExtnLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(fileSuffix, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layoutd.createSequentialGroup()
								.addComponent(numOfFileLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(numberOfFiles, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layoutd.createSequentialGroup()
								.addComponent(generateData, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(generateConfig, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addComponent(batchFileLocationLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(batchFileLocation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

		);

		layoutd.setVerticalGroup(layoutd.createSequentialGroup()
				.addGroup(layoutd.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(datagen))
				.addGroup(layoutd.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(generateIn)
						.addComponent(generateFileLocation))
				.addGroup(layoutd.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(fileExtnLabel)
						.addComponent(fileSuffix))
				.addGroup(layoutd.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(numOfFileLabel)
						.addComponent(numberOfFiles))
				.addGroup(layoutd.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(generateData)
						.addComponent(generateConfig))
				.addComponent(batchFileLocationLabel).addComponent(batchFileLocation));

		GroupLayout layoutp = new GroupLayout(pc);
		pc.setLayout(layoutp);
		layoutp.setAutoCreateGaps(true);
		layoutp.setAutoCreateContainerGaps(true);
		layoutp.setHorizontalGroup(layoutp.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(loadCDTLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(customDataDir, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(gvfLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(layoutp.createSequentialGroup()
						.addComponent(globalVirtualFiles, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(printGlobalVirtualFiles, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)

				).addComponent(clearGlobalVirtualFiles, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE));

		layoutp.setVerticalGroup(layoutp.createSequentialGroup().addComponent(loadCDTLabel).addComponent(customDataDir)
				.addComponent(gvfLabel).addGroup(layoutp.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(globalVirtualFiles).addComponent(printGlobalVirtualFiles))
				.addComponent(clearGlobalVirtualFiles));

		globalVirtualFiles.addActionListener(e -> {
			DULogger.log(400, "INFO: "
					+ new DataUnlimitedApi().createGlobalVirtualFileParam(Types.getInstance().getUI().getFileContent(), true));
		});
		clearGlobalVirtualFiles.addActionListener(e -> {
			GlobalVirtualFileParameters.getInstance().reset();
			DULogger.log(400, "INFO: Global Virtual Files were cleared");
		});
		printGlobalVirtualFiles.addActionListener(e -> GlobalVirtualFileParameters.getInstance().getVFP().print());

		customDataDir.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			setDirForFileChooser(fileChooser);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File selectedDir = fileChooser.getSelectedFile();
				Types.getInstance().init(selectedDir.getAbsolutePath());
				Types.getInstance().getUI().populateCustomParam();
				setLastUsedDir(selectedDir.getAbsolutePath());
			}

		});

		executeTransform.addActionListener(e -> {
			String extractParamDirVal = "";
			DULogger.log(400, "INFO: Extract and Generate function will be invoked...");
			if (extractOutputDirLocation.getText().length() == 0) {
				DULogger.log(200, "ERROR: Please choose Output Folder. It can't be blank.");
				JOptionPane.showMessageDialog(null, "ERROR: Please choose Output Folder. It can't be blank.", "ERROR",
						JOptionPane.INFORMATION_MESSAGE);
			}
			if (extractInputDirLocation.getText().length() == 0) {
				DULogger.log(200, "ERROR: Please choose Input File/Folder. It can't be blank.");
				JOptionPane.showMessageDialog(null, "ERROR: Please choose Input File/Folder. It can't be blank.",
						"ERROR", JOptionPane.INFORMATION_MESSAGE);
			}
			if (extractParamDirLocation.getText().length() != 0) {
				extractParamDirVal = extractParamDirLocation.getText();
			}

			if (Types.getInstance().getUI().getFileContent().length() == 0) {
				DULogger.log(200, "ERROR: DMS Script needs to be chosen. Script field is blank.");
				JOptionPane.showMessageDialog(null, "ERROR: DMS Script needs to be chosen. Script field is blank.",
						"ERROR", JOptionPane.INFORMATION_MESSAGE);
			}
			if (extractOutputDirLocation.getText().length() != 0
					&& Types.getInstance().getUI().getFileContent().length() != 0
					&& extractInputDirLocation.getText().length() != 0)
				new DataUnlimitedApi().processExtractTransform(extractOutputDirLocation.getText(),
						extractInputDirLocation.getText(), Types.getInstance().getUI().getFileContent(),
						extractParamDirVal, "", "txt", true);

		});

		extractVirtualFile.addActionListener(e -> {
			String extractParamDir = "";
			if (extractParamDirLocation.getText().length() != 0) {
				extractParamDir = extractParamDirLocation.getText();
				DULogger.log(400,
						"INFO: Extracted parameter will be written to files in directory/folder " + extractParamDir);
			}

			if (Types.getInstance().getUI().getFileContent().length() > 0) {
				if (extractInputDirLocation.getText().length() > 0)
					new DataUnlimitedApi().extractWithMapper(extractInputDirLocation.getText(),
							Types.getInstance().getUI().getFileContent(), extractParamDir, null, false, false);
				else {
					if (Types.getInstance().getUI().getFileContent().contains(Types.DMSEXTRACTINPUTDATASTART)
							&& Types.getInstance().getUI().getFileContent().contains(Types.DMSEXTRACTINPUTDATAEND)) {
						String data = UtilityFunctions.getInBetweenFast(Types.getInstance().getUI().getFileContent(),
								Types.DMSEXTRACTINPUTDATASTART, Types.DMSEXTRACTINPUTDATAEND, true).get(0);
						// if (isJson.isSelected())
						// data = FormatConversion.jsonToXML(data);
						new DataUnlimitedApi().extractWithMapper(data, Types.getInstance().getUI().getFileContent(),
								extractParamDir, null, true, false);
					} else
						DULogger.log(200,
								"ERROR: Extract input data is not provided either by selecting the file/folder or through DMS script content.");
				}
			} else
				DULogger.log(200, "ERROR: DMS script content can't be blank. Please open or create DMS script.");

		});

		extractInputDir.addActionListener(e -> {

			JFileChooser fileChooser = new JFileChooser();
			setDirForFileChooser(fileChooser);
			if (!extractIsFile.isSelected())
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File selectedDir = fileChooser.getSelectedFile();
				extractInputDirLocation.setText(selectedDir.getAbsolutePath());
				setLastUsedDir(selectedDir.getAbsolutePath());
			}
		});

		extractOutputDir.addActionListener(e -> {

			JFileChooser fileChooser = new JFileChooser();
			setDirForFileChooser(fileChooser);

			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File selectedDir = fileChooser.getSelectedFile();
				extractOutputDirLocation.setText(selectedDir.getAbsolutePath());
				setLastUsedDir(selectedDir.getAbsolutePath());
			}
		});

		extractParamDirB.addActionListener(e -> {

			JFileChooser fileChooser = new JFileChooser();
			if (lastUsedDir == null || lastUsedDir.length() == 0)
				fileChooser.setCurrentDirectory(
						new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
			else
				fileChooser.setCurrentDirectory(new File(lastUsedDir));
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File selectedDir = fileChooser.getSelectedFile();
				extractParamDirLocation.setText(selectedDir.getAbsolutePath());
				setLastUsedDir(selectedDir.getAbsolutePath());
			}
		});

		generateIn.addActionListener(e -> {

			JFileChooser fileChooser = new JFileChooser();
			setDirForFileChooser(fileChooser);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File selectedDir = fileChooser.getSelectedFile();
				generateFileLocation.setText(selectedDir.getAbsolutePath());
				setLastUsedDir(selectedDir.getAbsolutePath());
			}

		});

		generateData.addActionListener(e -> new Thread(() -> {
			generateData.setText("Processing...");
			generateData.setEnabled(false);
			Types.getInstance().getUI();
			String str = Types.getInstance().getUI().getFileContent();
			String fileExtension = fileSuffix.getText();
			if (fileExtension.length() <= 0)
				fileExtension = "txt";

			String path = generateFileLocation.getText();

			if (path.length() != 0) {
				new DataUnlimitedApi().generateData(str, numberOfFiles.getText(), path, fileExtension);
			} else {
				DULogger.log(200,
						"ERROR: Generate In field can't be left blank... please specify a directory in the text field...");
				JOptionPane.showMessageDialog(null,
						"Generate In field can't be left blank... please specify a directory in the text field...",
						"ERROR", JOptionPane.INFORMATION_MESSAGE);
			}
			generateData.setEnabled(true);
			generateData.setText("Generate Data");
		}).start());

		generateConfig.addActionListener(e -> {

			// fileSuffix
			if (JOptionPane.showConfirmDialog(null,
					"Two different files (.batch and .bat/.sh) will be created. If files move, \ncontent of the files will need modification to work (or regeneration). Proceed? ",
					"Choose Option", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				String configStr = "";
				if (numberOfFiles.getText().length() == 0 || generateFileLocation.getText().length() == 0
						|| Types.getInstance().getUI().saveFileLocationText.length() == 0) {
					DULogger.log(200,
							"ERROR: Generate File Location, Selected and Active DMS scripts and number of filed are necessary parameters. They can't be missing.");
					return;
				} else {
					if (fileSuffix.getText().length() > 0)
						configStr = Types.getInstance().getUI().saveFileLocationText.toString() + "\n" + Types.DMSDELIM
								+ generateFileLocation.getText() + "\n" + Types.DMSDELIM + numberOfFiles.getText()
								+ Types.DMSDELIM + fileSuffix.getText() + "\n\n" + Types.DMSCONFIGCOMMENTSTART
								+ " Runtime replacements (parameters or otherwise) below " + Types.DMSCONFIGCOMMENTEND
								+ "\n" + "\n" + Types.DMSDELIM + "xyz" + "\n" + Types.DMSSUBDELIM + "abc\n" + "\n"
								+ Types.DMSDELIM + "xyz" + "\n" + Types.DMSSUBDELIM + "abc\n" + "\n"
								+ Types.DMSCONFIGITEMDELIM + "\n\n";
					else
						configStr = Types.getInstance().getUI().saveFileLocationText.toString() + "\n" + Types.DMSDELIM
								+ generateFileLocation.getText() + "\n" + Types.DMSDELIM + numberOfFiles.getText()
								+ Types.DMSDELIM + "txt\n\n" + Types.DMSCONFIGCOMMENTSTART
								+ " Runtime replacements (parameters or otherwise) below " + Types.DMSCONFIGCOMMENTEND
								+ "\n" + "\n" + Types.DMSDELIM + "xyz" + "\n" + Types.DMSSUBDELIM + "abc\n" + "\n"
								+ Types.DMSDELIM + "xyz" + "\n" + Types.DMSSUBDELIM + "abc\n" + "\n"
								+ Types.DMSCONFIGITEMDELIM + "\n\n";

					DULogger.log(400, configStr);

					if (saveConfFileLocation.toString().length() == 0) {
						JFileChooser fileChooser = new JFileChooser();
						setDirForFileChooser(fileChooser);
						if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							File selectedFile = fileChooser.getSelectedFile();
							saveConfFileLocation.append(selectedFile.getPath());
							FileOperation.writeFile("", saveConfFileLocation.toString() + "_config.batch", configStr);
							DULogger.log(0, "Config file has been created...\n" + saveConfFileLocation.toString()
									+ "_config.batch");
						} else
							return;
					} else {
						if (JOptionPane.showConfirmDialog(null,
								"Append to current file?... " + saveConfFileLocation.toString() + "_config.batch",
								"Choose Option", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							FileOperation.writeFile("", saveConfFileLocation.toString() + "_config.batch", configStr,
									true);
							DULogger.log(0, "Config file has been updated...\n" + saveConfFileLocation.toString()
									+ "_config.batch");
						} else {
							JFileChooser fileChooser = new JFileChooser();
							setDirForFileChooser(fileChooser);
							if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
								File selectedFile = fileChooser.getSelectedFile();
								saveConfFileLocation.append(selectedFile.getPath());
								setLastUsedDir(selectedFile.getParent());
								FileOperation.writeFile("", saveConfFileLocation.toString() + "_config.batch",
										configStr);
								DULogger.log(0, "Config file has been created...\n" + saveConfFileLocation.toString()
										+ "_config.batch");
								saveConfFileLocation.delete(0, saveConfFileLocation.length());
							} else
								return;
						}
					}

					String workingDir = System.getProperty("user.dir");
					if (Types.isWindows()) {
						String[] drive = workingDir.split(":");
						String command = windowsCommand(drive[0], workingDir, saveConfFileLocation.toString());
						FileOperation.writeFile("", saveConfFileLocation.toString() + "_command.bat", command);
						DULogger.log(0,
								"Batch file have been created...\n" + saveConfFileLocation.toString() + "_command.bat");
					} else {
						String command = unixCommand(workingDir, saveConfFileLocation.toString());
						FileOperation.writeFile("", saveConfFileLocation.toString() + "_command.sh", command);
						DULogger.log(0,
								"Batch files have been created...\n" + saveConfFileLocation.toString() + "_command.sh");
						new OSCommand().runCommand("chmod 744 " + saveConfFileLocation.toString() + "_command.sh");
					}

				}
			}
			batchFileLocation.setText(saveConfFileLocation.toString() + "_config.batch");
		});

	}

	private String windowsCommand(String drive, String workDir, String saveConfFileLoc) {
		return "echo off\nset param=\nfor %%x in (%*) do call set \"param=%%param%% %%x\"\n" + "cd " + drive + ":\n"
				+ "cd " + workDir + "\n" + "cd ..\n" + "runDMS.bat -f " + saveConfFileLoc + "_config.batch %param%"
				+ "\n";
	}

	private String unixCommand(String workDir, String saveConfFileLoc) {
		return "currdir=`pwd`\nparam=\"\"\nfor i in $*\ndo\nparam=$param\" \"$i\ndone\necho \"Command line parameter list...\"\necho $param"
				+ "\ncd " + workDir + "\n" + "cd ..\n" + "./runDMS.sh -f " + saveConfFileLoc + "_config.batch $param"
				+ "\ncd $currdir\n";
	}
}

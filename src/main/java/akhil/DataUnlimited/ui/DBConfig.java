package akhil.DataUnlimited.ui;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.StorageAndRetrieval;
import akhil.DataUnlimited.util.DULogger;

public class DBConfig extends JDialog {

	private static final long serialVersionUID = 1L;
	private final StringBuilder filePath = new StringBuilder();
	private static final String DMSDBDELIM = "<DMSDBDELIM>";

	public String getConfigFile() {
		return filePath.toString();
	}

	private JCheckBox ikfn = new JCheckBox("Yes?    If not, proceeed below to create one.");
	private JTextField usr = new JTextField(40);
	private JPasswordField pwd = new JPasswordField(40);
	private JTextField srvc = new JTextField(40);
	private JTextPane qry = new JTextPane();

	public void clearAll() {
		usr.setText("");
		pwd.setText("");
		srvc.setText("");
		qry.setText("");
	}

	DBConfig(JFrame f) {
		super(f, "DB Parameter Configuration", true);

		Types.getInstance().setDBConfig(this);
		JPanel pane = new JPanel();
		this.add(pane);

		GroupLayout layout = new GroupLayout(pane);
		pane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);

		JLabel dykfn = new JLabel("Do you have existing DB config or config file for this query?", SwingConstants.LEFT);
		dykfn.setMinimumSize(new Dimension(80, 10));

		JLabel connString = new JLabel("Connection String/URL", SwingConstants.LEFT);
		connString.setMinimumSize(new Dimension(80, 10));
		JLabel connStringLabel = new JLabel("Connection String/URL Sample", SwingConstants.LEFT);
		JLabel connStringSample = new JLabel("[jdbc:dbname:server:port/service or SID or jdbc:sqlite:filename)]",
				SwingConstants.LEFT);
		JLabel user = new JLabel("User", SwingConstants.LEFT);
		user.setMinimumSize(new Dimension(80, 10));
		JLabel password = new JLabel("Password", SwingConstants.LEFT);
		password.setMinimumSize(new Dimension(80, 10));
		JLabel query = new JLabel("Query", SwingConstants.LEFT);
		query.setMinimumSize(new Dimension(80, 10));
		JButton submit = new JButton("Submit");
		qry.setMinimumSize(new Dimension(440, 50));

		JScrollPane scrollPaneOutput = new JScrollPane(qry);
		scrollPaneOutput.setPreferredSize(new Dimension(60, 60));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(dykfn, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(ikfn, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(connString, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(srvc, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(connStringLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(connStringSample, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(user, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(usr, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(password, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(pwd, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup()
						.addComponent(query, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(qry, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addComponent(submit, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(dykfn)
						.addComponent(ikfn))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(connString)
						.addComponent(srvc))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(connStringLabel)
						.addComponent(connStringSample))
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(user).addComponent(usr))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(password)
						.addComponent(pwd))

				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(query)
						.addComponent(qry))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(submit)));

		ikfn.addActionListener(e -> {
			if (ikfn.isSelected()) {
				// if (std.isSelected()) {
				String name = JOptionPane.showInputDialog(f,
						"Provide the name for DB config used for this parameter...");
				if (name != null && name.length() > 0) {
					filePath.append(name);
				} else if (Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: No DB config was selected for this parameter.");

				/*
				 * } else { JFileChooser fileChooser = new JFileChooser();
				 * fileChooser.setCurrentDirectory(new File( System.getProperty("user.home") +
				 * System.getProperty("file.separator") + "Desktop")); if
				 * (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { File
				 * selectedFile = fileChooser.getSelectedFile(); String filename =
				 * selectedFile.getPath(); if (filename != null && filename.length() > 0) {
				 * filePath.append(filename); }
				 * 
				 * } else if (Types.getInstance().getIsUI()) DULogger.log(200,
				 * "ERROR: No DB config was selected for this parameter."); }
				 */
				Types.getInstance().getDBConfig().setVisible(false);
				Types.getInstance().getDBConfig().clearAll();
				Types.getInstance().setDBConfig(null);
			}

		});

		submit.addActionListener(e -> {
			if (usr.getText().length() > 0 && new String(pwd.getPassword()).length() > 0 && srvc.getText().length() > 0
					&& qry.getText().length() > 0) {
				String configStr = "";

				configStr = srvc.getText() + DMSDBDELIM + usr.getText() + DMSDBDELIM
						+ StorageAndRetrieval.toKeep(new String(pwd.getPassword())) + DMSDBDELIM + qry.getText();

				String name = JOptionPane.showInputDialog(f, "Give a name to your DB config...");
				if (name != null && name.length() > 0) {
					filePath.append(name);
					String conf = "\n\n\n<dms-dbconf>\n<dms-confname>" + name
							+ "</dms-confname>\n<dms-comment>\nConnection String or URL<DMSDBDELIM>user<DMSDBDELIM>pwd<DMSDBDELIM>query\n</dms-comment>\n<dms-conf>\n"
							+ configStr + "\n</dms-conf>\n</dms-dbconf>";
					int pos = Types.getInstance().getUI().getCaretPosition();
					UIUtil.writeToPane(Types.getInstance().getUI().getFileContentPane(), conf, Color.BLACK, Color.WHITE,
							true, true, false);
					UIUtil.setFontColor(Types.getInstance().getUI().getFileContentPane(),
							Types.getInstance().getKeywordsAndColors(), null);
					Types.getInstance().getUI().setCaretPosition(pos);
				} else if (Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: No name given to DB config. Nothing will be saved in the DMS script.");

				Types.getInstance().getDBConfig().setVisible(false);
				Types.getInstance().getDBConfig().clearAll();
				Types.getInstance().setDBConfig(null);
			} else {
				if (Types.getInstance().getIsUI())
					DULogger.log(200,
							"ERROR: All fields required for DB configuration are not provided. Please provide all fields. User, password, Connection String/URL and query can't be blank.");

			}
		});

		this.setLocation(100, 100);
		this.setSize(800, 500);
		this.setVisible(true);

	}

}

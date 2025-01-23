package akhil.DataUnlimited.ui;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import akhil.DataUnlimited.model.types.Types;

public class FindReplace {

	private JDialog jd;
	private static FindReplace frinstance;

	public static FindReplace getInstance(JFrame f) {
		if (frinstance == null)
			frinstance = new FindReplace(f);
		return frinstance;
	}

	static final JPanel fr = new JPanel();

	static JLabel findReplaceLabel = new JLabel("Find / Replace");

	static JTextField findMe = new JTextField(20);
	static JTextField replaceWith = new JTextField(20);
	static JCheckBox isRegex = new JCheckBox("Is Find/Replace Regex?");

	static JButton find = new JButton("Find");
	static JButton replace = new JButton("Replace");

	public void clear() {
		isRegex.setSelected(false);
		findMe.setText("");
		replaceWith.setText("");
	}

	public void start() {
		fr.setVisible(true);
		jd.pack();
		jd.setVisible(true);
	}

	JPanel main = new JPanel();

	FindReplace(JFrame f) {
		jd = new JDialog(f, "Find / Replace", false);

		makeControlPanel();
		GroupLayout layout = new GroupLayout(main);
		main.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup()
						.addComponent(fr, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(fr)));

		jd.add(main);
		jd.setLocationRelativeTo(null);
		jd.pack();
	}

	private void makeControlPanel() {

		fr.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		GroupLayout layoutf = new GroupLayout(fr);
		fr.setLayout(layoutf);
		layoutf.setAutoCreateGaps(true);
		layoutf.setAutoCreateContainerGaps(true);

		layoutf.setHorizontalGroup(layoutf.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutf.createSequentialGroup().addComponent(findReplaceLabel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layoutf.createSequentialGroup()
						.addComponent(findMe, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(find, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layoutf.createSequentialGroup()
						.addComponent(replaceWith, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(replace, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layoutf.createSequentialGroup().addComponent(isRegex, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

		layoutf.setVerticalGroup(layoutf.createSequentialGroup()
				.addGroup(layoutf.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(findReplaceLabel))
				.addGroup(layoutf.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(findMe)
						.addComponent(find))
				.addGroup(layoutf.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(replaceWith)
						.addComponent(replace))
				.addGroup(layoutf.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(isRegex)));

		find.addActionListener(e -> Types.getInstance().getUI().find(findMe.getText()));

		replace.addActionListener(e -> Types.getInstance().getUI().replace(findMe.getText(), replaceWith.getText(),
				isRegex.isSelected()));
	}
}

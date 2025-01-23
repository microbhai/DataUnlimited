package akhil.DataUnlimited.ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;

import javax.swing.JComboBox;
import javax.swing.JDialog;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import akhil.DataUnlimited.model.types.Types;
import akhil.DataUnlimited.util.DULogger;

public class ExtractMapper extends JDialog {

	private static final long serialVersionUID = 1L;
	private final StringBuilder filePath = new StringBuilder();
	private static final String DMSDELIM = "<DMSDELIM>";

	public String getConfigFile() {
		return filePath.toString();
	}

	private StringBuilder transformSB = new StringBuilder();
	private StringBuilder mapper = new StringBuilder();

	private JLabel mapType = new JLabel("Map Type : ", SwingConstants.LEFT); // extract or node
	private JComboBox<String> mapTypeValues = new JComboBox<>();

	private JLabel info1 = new JLabel("How to use me?", SwingConstants.LEFT); // name the mapping
	private JLabel info2 = new JLabel(
			"			Selection of various items in dropdowns below, results in enabling and disabling of different text fields.",
			SwingConstants.LEFT);
	private JLabel info3 = new JLabel(
			"			All the fields enabled with a particular choice are required fields for that mapper creation. If some field is disabled, you can't/shouldn't enter that value.",
			SwingConstants.LEFT);
	private JLabel info4 = new JLabel(
			"			Use Add Mapper button to create mapper entry, and use Send to DMS Script button to send it to DMS scripting pane.",
			SwingConstants.LEFT);

	private JLabel name = new JLabel("Map Name : ", SwingConstants.LEFT); // name the mapping
	private JTextField nameValue = new JTextField(40);

	private JLabel parentName = new JLabel("Where can you find it - Parent Node Name: ", SwingConstants.LEFT); // parent
																												// node
	private JTextField parentNameValue = new JTextField(40);

	private JLabel type = new JLabel("Type : ", SwingConstants.LEFT); // single/multi or between/delimited/fixedlength
	private JComboBox<String> typeValue = new JComboBox<>();

	private JLabel startText = new JLabel("Start Text : ", SwingConstants.LEFT);
	private JTextField startValue = new JTextField(40);

	private JLabel endText = new JLabel("End Text : ", SwingConstants.LEFT);
	private JTextField endValue = new JTextField(40);

	private JLabel delimiter = new JLabel("Delimiter : ", SwingConstants.LEFT);
	private JTextField delimValue = new JTextField(40);

	private JLabel columnNumber = new JLabel("Column Number : ", SwingConstants.LEFT);
	private JTextField columnValue = new JTextField(40);

	private JLabel transform = new JLabel("Transform Functions : ", SwingConstants.LEFT);
	private JComboBox<String> transformFunc = new JComboBox<>();
	private JTextField transformValue = new JTextField(40);
	private JTextField transformText = new JTextField(200);

	JButton addT = new JButton("Add Transform");
	JButton addM = new JButton("Add Mapper");
	JButton clearT = new JButton("Clear Transform");
	JButton clearM = new JButton("Clear Mapper");
	JButton send = new JButton("Send to DMS Script");

	private JTextPane mapperText = new JTextPane();
	private JScrollPane scrollPaneOutput = new JScrollPane(mapperText);

	private void setLabelPosition(boolean b) {
		if (b) {
			endText.setText("End Position (number, end position of text capture) : ");
			startText.setText("Start Position (number, start position of text capture) : ");
		} else {
			endText.setText("End Text (text before this will be captured) : ");
			startText.setText("Start Text (text after this will be captured) : ");
		}
	}

	private void enableMain(boolean t) {
		if (t) {
			parentNameValue.setEnabled(true);
			startValue.setEnabled(true);
			nameValue.setEnabled(true);
		} else {
			parentNameValue.setEnabled(false);
			startValue.setEnabled(false);
			nameValue.setEnabled(false);
		}
	}

	private void enableTransformFunc(boolean b) {
		if (b) {
			addT.setEnabled(true);
			clearT.setEnabled(true);
			transformFunc.setEnabled(true);
			transformValue.setEnabled(true);
			transformText.setEnabled(true);
		} else {
			addT.setEnabled(false);
			clearT.setEnabled(false);
			transformFunc.setEnabled(false);
			transformValue.setEnabled(false);
			transformText.setEnabled(false);
		}
	}

	private void clear() {
		nameValue.setText("");
		parentNameValue.setText("");
		startValue.setText("");
		transformSB = new StringBuilder();

		if (endValue.isEnabled()) {
			endValue.setText("");
		}
		if (delimValue.isEnabled()) {
			delimValue.setText("");
		}
		if (columnValue.isEnabled()) {
			columnValue.setText("");
		}
		if (transformText.isEnabled()) {
			transformText.setText("");
		}
	}

	private void createLayout(GroupLayout layout) {
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addComponent(info1, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addComponent(info2, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addComponent(info3, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addComponent(info4, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(mapType, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(mapTypeValues, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(type, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(typeValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(name, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(nameValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(parentName, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(parentNameValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(startText, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(startValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(endText, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(endValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(delimiter, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(delimValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(columnNumber, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(columnValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(transform, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(transformFunc, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(transformValue, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(addT, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(clearT, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addComponent(transformText, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(
								layout.createSequentialGroup().addComponent(scrollPaneOutput, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(addM, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(clearM, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(send, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(info1))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(info2))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(info3))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(info4))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(mapType)
						.addComponent(mapTypeValues))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(type)
						.addComponent(typeValue))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(name)
						.addComponent(nameValue))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(parentName)
						.addComponent(parentNameValue))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(startText)
						.addComponent(startValue))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(endText)
						.addComponent(endValue))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(delimiter)
						.addComponent(delimValue))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(columnNumber)
						.addComponent(columnValue))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(transform)
						.addComponent(transformFunc))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(transformValue)
						.addComponent(addT).addComponent(clearT))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(transformText))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(scrollPaneOutput))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(addM)
						.addComponent(clearM).addComponent(send)

				));

	}

	ExtractMapper(JFrame f) {
		super(f, "Extract Mapper Designer", false);

		mapTypeValues.addItem("");
		mapTypeValues.addItem("node");
		mapTypeValues.addItem("extract");
		JPanel pane = new JPanel();
		this.add(pane);

		GroupLayout layout = new GroupLayout(pane);
		pane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		name.setMinimumSize(new Dimension(80, 10));
		mapType.setMinimumSize(new Dimension(80, 10));
		parentName.setMinimumSize(new Dimension(80, 10));
		type.setMinimumSize(new Dimension(80, 10));
		columnNumber.setMinimumSize(new Dimension(80, 10));
		transform.setMinimumSize(new Dimension(80, 10));
		delimiter.setMinimumSize(new Dimension(80, 10));
		endText.setMinimumSize(new Dimension(80, 10));
		startText.setMinimumSize(new Dimension(80, 10));
		transformText.setMinimumSize(new Dimension(160, 10));

		mapperText.setMinimumSize(new Dimension(500, 200));

		transformFunc.addItem("");
		transformFunc.addItem("trim()");
		transformFunc.addItem("padzero(\"width\")");
		transformFunc.addItem(
				"substring(\"integer or string or len()-x\", \"integer or string or len()-y\", \"true/false (optional - exclude start string)\")");
		transformFunc.addItem("split(\"delimited\", \"index\")");
		transformFunc.addItem("replace(\"to be replaced\", \"replace with\")");
		transformFunc.addItem("regexReplace(\"regex\", \"replace with\")");
		transformFunc.addItem(
				"decode(\"data1\", \"value1\",\"data2\", \"value2\",\"data3\", \"value3\", (optional)\"default\", (optional)\"default value\")");
		transformFunc.addItem("multiply(\"1000\",\"000.00\")");
		transformFunc.addItem("date(\"yyMMdd HHmmss\",\"yyyy-MM-dd HH:mm:ss\")");
		transformFunc.addItem("lowerCase()");
		transformFunc.addItem("upperCase()");
		transformFunc.addItem("gzipbase64()");

		scrollPaneOutput.setPreferredSize(new Dimension(60, 60));
		createLayout(layout);

		mapTypeValues.addActionListener(e -> {
			String maptype = "";
			clear();
			if (mapTypeValues.getSelectedItem() != null)
				maptype = (String) mapTypeValues.getSelectedItem();
			if (maptype.equals("node")) {
				name.setText("Give it a name - Node Name: ");
				typeValue.removeAllItems();
				typeValue.addItem("");
				typeValue.addItem("top");
				typeValue.addItem("multiline");
				typeValue.addItem("singleline");
				setLabelPosition(false);
				endValue.setEnabled(true);
				enableTransformFunc(false);
				columnValue.setEnabled(false);
				delimValue.setEnabled(false);
				enableMain(true);
			} else if (maptype.equals("extract")) {
				name.setText("Give it a name - Parameter Name: ");
				typeValue.removeAllItems();
				typeValue.addItem("");
				typeValue.addItem("between");
				typeValue.addItem("delimited");
				typeValue.addItem("fixedlength");
				typeValue.addItem("fieldname");
				setLabelPosition(false);
				enableTransformFunc(true);
				columnValue.setEnabled(true);
				delimValue.setEnabled(true);
				endValue.setEnabled(true);
				enableMain(true);
			} else {
				typeValue.removeAllItems();
				enableTransformFunc(true);
				columnValue.setEnabled(true);
				delimValue.setEnabled(true);
				enableMain(true);
				name.setText("Map Name: ");
			}

		});

		typeValue.addActionListener(e -> {
			String typevalue = "";
			if (typeValue.getSelectedItem() != null)
				typevalue = (String) typeValue.getSelectedItem();
			if (typevalue.equals("top")) {
				enableMain(false);
				endValue.setEnabled(false);
				setLabelPosition(false);
				enableTransformFunc(false);
				delimValue.setEnabled(false);
				columnValue.setEnabled(false);
			} else if (typevalue.equals("singleline")) {
				endValue.setEnabled(false);
				setLabelPosition(false);
				enableTransformFunc(false);
				enableMain(true);
			} else if (typevalue.equals("multiline")) {
				endValue.setEnabled(true);
				setLabelPosition(false);
				columnValue.setEnabled(false);
				delimValue.setEnabled(false);
				enableTransformFunc(false);
				enableMain(true);
			} else if (typevalue.equals("between")) {
				delimValue.setEnabled(false);
				columnValue.setEnabled(false);
				setLabelPosition(false);
				endValue.setEnabled(true);
				enableTransformFunc(true);
				enableMain(true);
			} else if (typevalue.equals("delimited")) {
				endValue.setEnabled(false);
				delimValue.setEnabled(true);
				columnValue.setEnabled(true);
				enableTransformFunc(true);
				setLabelPosition(false);
				enableMain(true);
			} else if (typevalue.equals("fixedlength")) {
				endValue.setEnabled(true);
				setLabelPosition(true);

				delimValue.setEnabled(false);
				columnValue.setEnabled(false);
				enableTransformFunc(true);
				enableMain(true);
			} else if (typevalue.equals("fieldname")) {
				endValue.setEnabled(false);
				startText.setText("Field Name (say you are using JSON/XML): ");

				delimValue.setEnabled(false);
				columnValue.setEnabled(false);
				enableTransformFunc(true);
				enableMain(true);
			} else {
				endValue.setEnabled(true);
				delimValue.setEnabled(true);
				columnValue.setEnabled(true);
				enableTransformFunc(true);
				setLabelPosition(false);
				enableMain(true);
			}

		});

		transformFunc.addActionListener(e -> transformValue.setText((String) transformFunc.getSelectedItem()));

		addT.addActionListener(e -> {
			transformSB.append(transformValue.getText() + DMSDELIM);
			transformText.setText(transformSB.toString());
		}

		);

		clearT.addActionListener(e -> {
			transformSB = new StringBuilder();
			transformText.setText("");
			transformValue.setText("");
		}

		);
		clearM.addActionListener(e -> {
			mapper = new StringBuilder();
			mapperText.setText("");
		}

		);

		addM.addActionListener(e -> {
			if (mapTypeValues.getSelectedItem() != null && mapTypeValues.getSelectedItem().toString().length() > 0) {
				mapper.append(mapTypeValues.getSelectedItem().toString() + DMSDELIM);
			} else {
				if (!typeValue.getSelectedItem().toString().equals("top") && Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: Improper value for map type");
			}
			if (nameValue.getText() != null && nameValue.getText().length() > 0) {
				mapper.append(nameValue.getText() + DMSDELIM);
			} else {
				if (!typeValue.getSelectedItem().toString().equals("top") && Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: Improper value for map name");

			}
			if (parentNameValue.getText() != null && parentNameValue.getText().length() > 0) {
				mapper.append(parentNameValue.getText() + DMSDELIM);
			} else {
				if (Types.getInstance().getIsUI() && !typeValue.getSelectedItem().toString().equals("top"))
					DULogger.log(200, "ERROR: Improper value for parent name");

			}
			if (typeValue.getSelectedItem() != null && typeValue.getSelectedItem().toString().length() > 0) {
				mapper.append(typeValue.getSelectedItem().toString() + DMSDELIM);
			} else {
				if (Types.getInstance().getIsUI())
					DULogger.log(200, "ERROR: Improper value for type");

			}
			if (startValue.getText() != null && startValue.getText().length() > 0) {
				mapper.append(startValue.getText() + DMSDELIM);
			} else {
				if (Types.getInstance().getIsUI() && !typeValue.getSelectedItem().toString().equals("top"))
					DULogger.log(200, "ERROR: Improper value for start value");

			}
			if (endValue.isEnabled() && endValue.getText() != null && endValue.getText().length() > 0) {
				mapper.append(endValue.getText() + DMSDELIM);
			} else {
				if (Types.getInstance().getIsUI())
					DULogger.log(400, "INFO: Not using end value");
			}
			if (delimValue.isEnabled() && delimValue.getText() != null && delimValue.getText().length() > 0) {
				mapper.append(delimValue.getText() + DMSDELIM);
			} else {
				if (Types.getInstance().getIsUI())
					DULogger.log(400, "INFO: Not using value for delimiter");

			}
			if (columnValue.isEnabled() && columnValue.getText() != null && columnValue.getText().length() > 0) {
				mapper.append(columnValue.getText() + DMSDELIM);
			} else {
				if (Types.getInstance().getIsUI())
					DULogger.log(400, "INFO: Not using value for column number");

			}
			if (transformText.isEnabled() && transformText.getText() != null && transformText.getText().length() > 0) {
				mapper.append(transformText.getText().substring(0, transformText.getText().length() - 10));
			} else {
				if (Types.getInstance().getIsUI())
					DULogger.log(400, "INFO: Not using value for transform");
			}
			mapper.append("\n");
			mapperText.setText(mapper.toString());

		});

		send.addActionListener(e -> {
			String mapperT = Types.DMSEXTRACTMAPPERSTART + "\n" + mapperText.getText() + "\n"
					+ Types.DMSEXTRACTMAPPEREND + "\n";
			UIUtil.writeToPane(Types.getInstance().getUI().getFileContentPane(), mapperT, Color.BLACK, Color.WHITE,
					false, true, true);
			UIUtil.setFontColor(Types.getInstance().getUI().getFileContentPane(),
					Types.getInstance().getKeywordsAndColors(), null);
		}

		);
	}

	public void init() {
		this.setLocation(20, 50);
		this.setSize(980, 700);
		this.setVisible(true);
	}
}

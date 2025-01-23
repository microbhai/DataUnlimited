package akhil.DataUnlimited.ui;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Logo extends JFrame {

	private static final long serialVersionUID = 1L;

	public Logo() {
		showLogo();
	}

	private void showLogo() {
		ImageIcon img = new ImageIcon("../images/icon.jpg");
		JLabel logo = new JLabel(img, SwingConstants.CENTER);
		this.add(logo);
		this.setSize(598, 405);
		this.setLocationRelativeTo(null);
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		this.setVisible(true);

	}

}

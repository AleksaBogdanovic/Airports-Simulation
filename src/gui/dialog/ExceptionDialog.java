package gui.dialog;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ExceptionDialog {

	public static void show(Frame parent, String poruka) {
		Dialog dialog = new Dialog(parent, "Error", true); // true = modalan
		dialog.setLayout(new java.awt.BorderLayout());

		Label label = new Label(poruka);
		Panel panel = new Panel();
		panel.add(label);
		dialog.add(panel, java.awt.BorderLayout.CENTER);

		Button ok = new Button("OK");
		ActionListener close = e -> dialog.dispose();
		ok.addActionListener(close);

		Panel dugmePanel = new Panel();
		dugmePanel.add(ok);
		dialog.add(dugmePanel, java.awt.BorderLayout.SOUTH);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dialog.dispose();
			}
		});

		dialog.setSize(400, 150);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
}
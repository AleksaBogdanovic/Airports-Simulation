package gui.dialog;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;

import gui.MyWindow;

public class ShutdownDialog extends Dialog{

	
    private final Label countdownLabel;
    private final MyWindow owner;

	
	public ShutdownDialog(MyWindow parent, String msg, int secondsLeft) {
		super(parent, msg);
		this.owner = parent;

	this.setLayout(new FlowLayout());

	countdownLabel = new Label("Closing in " + secondsLeft + "s");
	Label prompt = new Label("Do you want to continue with using the program?");
	Button continueButton = new Button("Continue");
	this.add(countdownLabel);
	this.add(prompt);
	this.add(continueButton);

	this.setSize(300, 150);
	this.setModal(false);
	this.setLocationRelativeTo(parent);

	continueButton.addActionListener(e -> {
		owner.resetTimer();
		this.dispose();
	});

	}
	
	  public void updateCountdown(int secondsLeft) {
	        countdownLabel.setText("Closing in " + secondsLeft + "s");
	   }
}

package gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import file.CSVParser;
import file.JSONParser;
import exceptions.DuplicateAirportCodeException;
import exceptions.FileReadException;
import exceptions.FileWriteException;
import exceptions.MyException;
import exceptions.ParsingException;
import exceptions.UnsupportedFileFormatException;
import project.Airport;
import project.Flight;
import systemdata.Simulation;
import systemdata.SystemData;

public class MyWindow extends Frame {

	private TextArea textArea;
	private volatile int timeLeft = 60;
	private Thread timer;
	private AirportListPanel listPanel;
	private MapPanel mapPanel;
	private Simulation simulation = null;

	private void startTimer() {
		timer = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}

				 boolean simActive = simulation != null && simulation.isRunning() && !simulation.isPaused();
				if (!mapPanel.isAirportSelected() && !simActive) { // samo ako nije selektovan ili ako ne traje simulacija
					timeLeft--;
				}

				if (timeLeft == 5) {
					startShutdownDialog();
				}

				if (timeLeft <= 0) {
					dispose(); // shutdown
					break;
				}
			}
		});
		timer.start();
	}

	private void startShutdownDialog() {
		Dialog dialog = new Dialog(this, "Warning"); // inactivity dialog
		dialog.setLayout(new FlowLayout());

		Label label0 = new Label("Closing in " + timeLeft + "s");
		Label label1 = new Label("Do you want to continue with using the program?");
		Button continueButton = new Button("Continue");
		dialog.add(label0);
		dialog.add(label1);
		dialog.add(continueButton);

		dialog.setSize(300, 150);
		dialog.setModal(false);
		dialog.setLocationRelativeTo(this);

		continueButton.addActionListener(e -> {
			timeLeft = 60;
			dialog.dispose();
		});

		Thread dialogThread = new Thread(() -> {
			while (timeLeft > 0 && timeLeft <= 5) {
				label0.setText("Closing in " + timeLeft + "s");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
			dialog.dispose();
		});
		dialogThread.start();

		dialog.setVisible(true);

	}

	private void disposeThreads() {
		timer.interrupt();
	}

	private void refreshDisplay() {
		StringBuilder sb = new StringBuilder();

		sb.append("=== AIRPORTS ===\n");
		for (Airport a : SystemData.airports) {
			sb.append(a.toString()).append("\n");
		}

		sb.append("\n=== FLIGHTS ===\n");
		for (Flight f : SystemData.flights) {
			sb.append(f.toString()).append("\n");
		}

		textArea.setText(sb.toString());
	}

	public MyWindow() {
		super("MAIN");
		this.setSize(700, 1000);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				MyWindow.this.disposeThreads();
				mapPanel.disposeThreads();
				if (simulation != null) {
				    simulation.reset(); // gasi sve niti u simulaciji
				}
				dispose();
			}
		});

		this.startTimer(); // timer for inactivity

		MenuBar menuBar = new MenuBar();
		this.setMenuBar(menuBar);
		Menu addFile = new Menu("File");
		MenuItem open = new MenuItem("Open...");

		addFile.add(open);
		menuBar.add(addFile);

		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileDialog dialog = new FileDialog(MyWindow.this, "Choose file...", FileDialog.LOAD);
				dialog.setVisible(true);

				String dir = dialog.getDirectory();
				String filename = dialog.getFile();

				if (filename == null) {
					return;
				}

				String path = dir + filename;

				int dotIndex = filename.lastIndexOf('.') + 1;
				String extension = filename.substring(dotIndex).toLowerCase();

				try {
					if (extension.equals("csv")) {
						CSVParser parser = new CSVParser();
						SystemData.store(parser.load(path));

					} else if (extension.equals("json")) {
						JSONParser parser = new JSONParser();
						SystemData.store(parser.load(path));

					} else {
						throw new UnsupportedFileFormatException(extension);
					}
				} catch (MyException ex) 
				{
					ExceptionDialog.show(MyWindow.this, ex.getMessage());
					return; // ne osvezavaj prikaz ako ucitavanje nije uspelo
				}

				refreshDisplay();
				listPanel.refresh();
				mapPanel.repaint();
			}
		});

		Menu simMenu = new Menu("Simulation");

		MenuItem startItem = new MenuItem("Start");
		MenuItem pauseItem = new MenuItem("Pause");
		MenuItem resetItem = new MenuItem("Reset");

		simMenu.add(startItem);
		simMenu.add(pauseItem);
		simMenu.add(resetItem);
		menuBar.add(simMenu);

		
		startItem.addActionListener(e -> {
		    if (simulation != null && simulation.isFinished()) {
		        textArea.append("\nSimulation is finished - use Reset before restarting simulation.\n");
		        return;
		    }
		    if (simulation != null) {
		        simulation.start();
		    } else {
		        simulation = new Simulation(mapPanel);
		        simulation.setOnFinishCallback(() -> {
		            pauseItem.setLabel("Pause");
		            pauseItem.setEnabled(false);
		            textArea.append("\n==== SIMULATION FINISHED ===\n");
		        });
		        simulation.start();
		    }
		});

		pauseItem.addActionListener(e -> {
			if (simulation == null) {
				return;
			}
			if (simulation.isPaused()) {
				simulation.resume();
				pauseItem.setLabel("Pause");
			} else {
				simulation.pause();
				pauseItem.setLabel("Resume");
			}
		});

		resetItem.addActionListener(e -> {
			if (simulation != null) {
				simulation.reset();
				pauseItem.setLabel("Pause");
			}
		});
		
		MenuItem saveCsv = new MenuItem("Save as CSV...");
		MenuItem saveJson = new MenuItem("Save as JSON...");
		addFile.add(saveCsv);
		addFile.add(saveJson);

		saveCsv.addActionListener(e -> {
		    FileDialog dialog = new FileDialog(MyWindow.this, "Save as CSV", FileDialog.SAVE);
		    dialog.setFile("data.csv");
		    dialog.setVisible(true);

		    String dir = dialog.getDirectory();
		    String filename = dialog.getFile();
		    if (filename == null) {
		        return;
		    }

		    CSVParser parser = new CSVParser();
		    try {
		        parser.save(dir + filename);
		    } catch (FileWriteException ex) {
		        ExceptionDialog.show(MyWindow.this, ex.getMessage());
		    }
		});

		saveJson.addActionListener(e -> {
		    FileDialog dialog = new FileDialog(MyWindow.this, "Save as JSON", FileDialog.SAVE);
		    dialog.setFile("data.json");
		    dialog.setVisible(true);

		    String dir = dialog.getDirectory();
		    String filename = dialog.getFile();
		    if (filename == null) {
		        return;
		    }

		    JSONParser parser = new JSONParser();
		    try {
		        parser.save(dir + filename);
		    } catch (FileWriteException ex) {
		        ExceptionDialog.show(MyWindow.this, ex.getMessage());
		    }
		});

		// ---------------------------------------------------------------------------------------------------------------------------------------------------

		textArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY); // ispis rezultata na izlaz
		textArea.setEditable(false);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

		add(textArea, BorderLayout.SOUTH);

		listPanel = new AirportListPanel(); // dodaj listu aerodroma za cekiranje
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(listPanel);
		add(scrollPane, BorderLayout.WEST);

		mapPanel = new MapPanel();
		add(mapPanel, BorderLayout.CENTER);
		listPanel.setOnChangeCallback(() -> mapPanel.repaint());
		
		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
		    if (event instanceof MouseEvent) {
		        MouseEvent me = (MouseEvent) event;
		        if (me.getID() == MouseEvent.MOUSE_CLICKED) {
		            timeLeft = 60;
		        }
		    }
		}, AWTEvent.MOUSE_EVENT_MASK);

		this.setVisible(true);
	}

}
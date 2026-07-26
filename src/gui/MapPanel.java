package gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import project.Airport;
import project.Flight;
import systemdata.SystemData;

public class MapPanel extends Canvas {

	private static final double X_MIN = -180;
	private static final double X_MAX = 180;
	private static final double Y_MIN = -90;
	private static final double Y_MAX = 90;

	private Airport selectedAirport = null;
	private boolean selectedRed = true;
	Thread blinkingSelected;

	public int scaleX(int x) {
		return (int) ((x - MapPanel.X_MIN) / (MapPanel.X_MAX - MapPanel.X_MIN) * this.getWidth());
	}

	public int scaleY(int y) {
		return (int) ((-y - MapPanel.Y_MIN) / (MapPanel.Y_MAX - MapPanel.Y_MIN) * this.getHeight());
	}

	public boolean isAirportSelected() {
		return this.selectedAirport != null;
	}

	private Airport findAirportOn(int mx, int my) {
		for (Airport a : SystemData.getCheckedAirports()) {
			int x = scaleX(a.getX());
			int y = scaleY(a.getY());

			if (mx >= x && mx <= x + 10 && my >= y && my <= y + 10) {
				return a;
			}
		}
		return null;
	}

	private void startBlinking() {
		this.selectedRed = true;
		blinkingSelected = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					break;
				}
				selectedRed = !selectedRed;
				repaint();
			}
		});
		blinkingSelected.start();
	}

	private void stopBlinking() {
		if (this.blinkingSelected != null) {
			this.blinkingSelected.interrupt();
			this.blinkingSelected = null;
		}

		this.selectedAirport = null;
		this.selectedRed = false;
	}

	public void disposeThreads() {
		stopBlinking();
	}
	
	public MapPanel() {
		setBackground(new Color(176, 221, 255)); // lightblue
		
		

		this.addMouseListener(new MouseAdapter() { // listener za selektovanje treptanja aerodroma
			@Override
			public void mouseClicked(MouseEvent e) {
				Airport target = findAirportOn(e.getX(), e.getY());

				if (target == null) {
					return;
				}
				if (target == MapPanel.this.selectedAirport) {
					MapPanel.this.stopBlinking();
				} else {
					MapPanel.this.stopBlinking();
					MapPanel.this.selectedAirport = target;
					MapPanel.this.startBlinking();
				}
			}

		});

		setVisible(true);
	}

	@Override
	public void paint(Graphics g) {
		System.out.println("paint pozvan, letova u vazduhu: " + SystemData.activeFlightsSize());

		
		for (Airport a : SystemData.getCheckedAirports()) {
			int x = this.scaleX(a.getX());
			int y = this.scaleY(a.getY());

			g.setColor(Color.GRAY);
			g.drawRect(x, y, 10, 10);

			if (a == this.selectedAirport && this.selectedRed) {
				g.setColor(Color.RED);
				g.fillRect(x, y, 10, 10);
			} else {
				g.fillRect(x, y, 10, 10);
			}

			g.setColor(Color.BLACK);
			g.drawString(a.getCode(), x + 12, y + 10);
		}
		
		for (Flight f : SystemData.getInFlight()) {
			f.draw(g, this);
		}
	}
}

package gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import project.Airport;
import systemdata.FlightTask;
import systemdata.Simulation;

public class MapPanel extends Canvas {

	private static final double X_MIN = Airport.X_MIN;
	private static final double X_MAX = Airport.X_MAX;
	private static final double Y_MIN = Airport.Y_MIN;
	private static final double Y_MAX = Airport.Y_MAX;
	private static final int AIRPORT_MARKER_SIZE = AirportRenderer.AIRPORT_MARKER_SIZE;
	private static final int AIRPLANE_RADIUS = FlightRenderer.AIRPLANE_RADIUS;

	private Airport selectedAirport = null;
	private boolean selectedRed = true;
	Thread blinkingSelected;
	private Image offscreen;

	private AirportListPanel listPanel;
	public void setListPanel(AirportListPanel p) {
		this.listPanel = p;
	}
	
	private Simulation simulation;
	public void setSimulation(Simulation s) { this.simulation = s; }
	
	private final AirportRenderer airportRenderer = new AirportRenderer();
	private final FlightRenderer flightRenderer = new FlightRenderer();

//-----------------------------TREPERENJE-AERODROMA-----------------------------------------------------------
	public boolean isAirportSelected() {
		return this.selectedAirport != null;
	}

	private Airport findAirportOn(int mx, int my) {

		CoordinateMapper mapper = new CoordinateMapper(this.getWidth(), this.getHeight());

		for (Airport a : listPanel.getCheckedAirports()) {
			int x = mapper.scaleX(a.getX());
			int y = mapper.scaleY(a.getY());

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
	// ===========================================================================================================================================================

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
	
	public void update(Graphics g) {
	    paint(g); 
	}

	@Override
	public void paint(Graphics g) {

		int w = getWidth(), h = getHeight();

		if (offscreen == null || offscreen.getWidth(null) != w || offscreen.getHeight(null) != h) {
			offscreen = createImage(w, h);
		}
		Graphics og = offscreen.getGraphics();
		og.setColor(getBackground());
		og.fillRect(0, 0, w, h);

		CoordinateMapper mapper = new CoordinateMapper(w, h);

		// crtaj aerdodrome sa treperenjem ako treba
		for (Airport a : listPanel.getCheckedAirports()) {
				boolean isSelectedNow = (a == selectedAirport && selectedRed);
				airportRenderer.draw(og, a, mapper, isSelectedNow);
		}

		// crtaj avione koje lete
		if (simulation != null) {
			int t = simulation.getSimulatedTime();
			for (FlightTask task : simulation.inFlightSnapshot()) {
				flightRenderer.draw(og, task.currentPosition(t), mapper);
			}
		}

		og.dispose();
		g.drawImage(offscreen, 0, 0, this);
	}
}

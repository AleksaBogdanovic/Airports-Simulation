package gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import project.Airport;
import systemdata.FlightPosition;
import systemdata.FlightTask;
import systemdata.Simulation;

public class MapPanel extends Canvas {

	private static final double X_MIN = Airport.X_MIN;
	private static final double X_MAX = Airport.X_MAX;
	private static final double Y_MIN = Airport.Y_MIN;
	private static final double Y_MAX = Airport.Y_MAX;
	private static final int AIRPORT_MARKER_SIZE = AirportRenderer.AIRPORT_MARKER_SIZE;
	private static final int AIRPLANE_RADIUS = FlightRenderer.AIRPLANE_RADIUS;

	private FlightTask selectedFlight = null;
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
	//=======================================================================================================================================
	
	private FlightTask findFlightOn(int mx, int my) {
	    if (simulation == null) {
	        return null;
	    }
	    CoordinateMapper mapper = new CoordinateMapper(getWidth(), getHeight());
	    int t = simulation.getSimulatedTime();

	    for (FlightTask task : simulation.inFlightSnapshot()) {
	        FlightPosition pos = task.currentPosition(t);
	        int px = mapper.scaleX(pos.getX());
	        int py = mapper.scaleY(pos.getY());

	        int dx = mx - px;
	        int dy = my - py;
	        int r = AIRPLANE_RADIUS + 3;              // +3 px tolerancija
	        if (dx * dx + dy * dy <= r * r) {
	            return task;
	        }
	    }
	    return null;
	}
	
	
	
	// ===========================================================================================================================================================

	public MapPanel() {
		setBackground(new Color(176, 221, 255)); // lightblue

		this.addMouseListener(new MouseAdapter() { // listener za selektovanje treptanja aerodroma
			@Override
			public void mouseClicked(MouseEvent e) {	
				 // 1) Ako je avion vec selektovan -> klik dodaje waypoint tom letu
				if (selectedFlight != null) {
				    // klik ponovo na isti avion -> deselektuj, prestani da dodajes tacke
				    if (findFlightOn(e.getX(), e.getY()) == selectedFlight) {
				        selectedFlight = null;
				        repaint();
				        return;
				    }
				    // inace: dodaj waypoint
				    CoordinateMapper mapper = new CoordinateMapper(getWidth(), getHeight());
				    selectedFlight.addWaypoint(
				        new FlightPosition(mapper.unscaleX(e.getX()), mapper.unscaleY(e.getY())));
				    repaint();
				    return;
				}

			    // 2) Da li je klik pogodio neki avion u vazduhu?
			    FlightTask hitFlight = findFlightOn(e.getX(), e.getY());
			    if (hitFlight != null) {
			        MapPanel.this.stopBlinking();      // otkaci eventualni aerodrom
			        selectedFlight = hitFlight;
			        repaint();
			        return;
			    }	
				
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
			List<FlightTask> inAir = simulation.inFlightSnapshot();

			// ako selektovani avion vise nije u vazduhu (sleteo ili reset) -> deselektuj
			if (selectedFlight != null && !inAir.contains(selectedFlight)) {
				selectedFlight = null;
			}
			
			int t = simulation.getSimulatedTime();
			for (FlightTask task : inAir) {
				flightRenderer.draw(og, task.currentPosition(t), mapper);
			}
		}

		og.dispose();
		g.drawImage(offscreen, 0, 0, this);
	}
}


/*
 if (e.getButton() == MouseEvent.BUTTON3) {          // desni klik = blokiraj/odblokiraj
    Airport target = findAirportOn(e.getX(), e.getY());
    if (target != null) {
        target.setBlocked(!target.isBlocked());     // toggle
        repaint();
    }
    return;                                          // desni klik NE radi nista drugo
}
 
 private volatile boolean blocked = false;

public boolean isBlocked() { return blocked; }
public void setBlocked(boolean b) { this.blocked = b; } 
  
 */


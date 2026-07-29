package systemdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import project.Airport;
import project.Flight;

public class Simulation implements Runnable {

	private static final int TICK_MINUTES = 2;
	private static final int TICK_MILLIS = 200;
	private static final int MS_PER_SIM_MINUTE = 100; 							// 1 min simulirano = 100ms realno

	// trenutno simulirano vreme od 00:00(u minutima realnog vremena)
	private int simulatedTime = 0;
	private long startSimulationClockTime; 										// realno vreme starta
	private final Object clockLock = new Object();

	// --------------------------------------------------------------------------------
	private volatile boolean paused = false;
	private volatile boolean finished = false;
	private volatile boolean running = false;
	private final Object pauseLock = new Object();

	// --------------------------------------------------------------------------------
	private final List<Flight> scheduled; 										// svi koje treba odsimulirati (filtrirano)
	private final Map<Airport, Runway> runways = new HashMap<>(); 				// koristi se za proveru slobodnih termina i njihovu
																				// rezervaciju
	private final List<FlightTask> inFlight = new ArrayList<>();
	private final List<Thread> flightThreads = new ArrayList<>();
	AtomicInteger completed = new AtomicInteger(0);								// brojac zavrshenig flight taskova

	private Thread thread;

	private final List<SimulationListener> listeners = new CopyOnWriteArrayList<>();

	public Simulation(List<Flight> flightsToSimulate) {
		this.scheduled = flightsToSimulate;
	}

	@Override
	public void run() {
		startSimulationClockTime = System.currentTimeMillis();
		long pausedMillis = 0;

		try {
			for (Flight f : scheduled) {										// pokrecu se svi zakazani flight taskovi odmah, svaki task zna koliko treba da
																				// saceka
				Thread t = new Thread(new FlightTask(f, this));
				flightThreads.add(t);
				t.start();
			}

			if (scheduled.isEmpty()) {
				finish();
				return;
			}

			while (!Thread.interrupted()) {
				long pauseStartTime = 0;
				synchronized (pauseLock) {
					if (paused)
						pauseStartTime = System.currentTimeMillis();			 // zapamti kada je pocela pauza
					while (paused) {
						pauseLock.wait();
					}
				}
				if (pauseStartTime != 0) {
					pausedMillis += System.currentTimeMillis() - pauseStartTime;
				}

				long elapsed = System.currentTimeMillis() - this.startSimulationClockTime - pausedMillis;
				int newTime = (int) elapsed / MS_PER_SIM_MINUTE;

				synchronized (clockLock) {
					simulatedTime = newTime;
					clockLock.notifyAll(); 										// probudi svaki task koji ce za sebe proveriti da li je dodao njegov
																				// trenutak
				}

				fireTick();														// signal za gui

				if (completed.get() >= scheduled.size()) {
					finish();
					break;
				}

				Thread.sleep(TICK_MILLIS);
			}

		} catch (InterruptedException e) {

		}
	}

	// -------------------------------------------------------------------------------------

	public synchronized Runway runwayOf(Airport a) {
		Runway r = runways.get(a);
		if (r == null) {
			r = new Runway();
			runways.put(a, r);
		}
		return r;
	}

	public void waitSimulatedTime(int target) throws InterruptedException {
		synchronized (clockLock) {
			while (simulatedTime < target) {
				if (finished || !running) {
					return; 													// simulacija stala
				}
				clockLock.wait();
			}
		}
	}

	public void taskCompleted() {
		completed.incrementAndGet();
	}

	private void completedReset() {
		completed.set(0);
	}

	// --------------------------------INAIR FLIGHTS------------------------------------------------------------

	void addInFlight(FlightTask task) {
		synchronized (inFlight) {
			inFlight.add(task);
		}
	}

	void removeInFlight(FlightTask task) {
		synchronized (inFlight) {
			inFlight.remove(task);
		}
	}

	public List<FlightTask> inFlightSnapshot() {
		synchronized (inFlight) {
			return new ArrayList<>(inFlight);
		}
	}

	// -----------------------------------SIMULATION CONTROLS----------------------------------------------
	public void start() {
		if (running) {
			return;
		}

		if (thread == null || !thread.isAlive()) {
			thread = new Thread(this);
			running = true;
			thread.start();
		}
	}

	private void finish() {
		running = false;
		finished = true;
		synchronized (clockLock) {
			clockLock.notifyAll(); // za svaki slucaj ako se neko nije probudio
		}
		fireFinished();
	}

	public void pause() {
		if (running && !paused) {
			paused = true;
		}
	}

	public void resume() {
		if (running && paused) {
			synchronized (pauseLock) {
				paused = false;
				pauseLock.notifyAll(); // probudi tick thread
			}
		}
	}

	public void reset() {

		if (thread != null && thread.isAlive()) {
			thread.interrupt();
			try {
				thread.join(1000); // sacekaj malo da se nit zavrsi
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		for (Thread t : flightThreads) { // prekini SVE letove one u vazduhu i one koji tek čekaju termin

			t.interrupt();
		}

		synchronized (clockLock) {
			clockLock.notifyAll(); // budi sve niti da vide da su prekinute
		}
		for (Thread t : flightThreads) { // za svaki slucaj
			try {
				t.join(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		flightThreads.clear();
		synchronized (inFlight) {
			inFlight.clear();
		}
		runways.clear();
		completedReset();

		simulatedTime = 0;
		paused = false;
		running = false;
		finished = false;
		thread = null;

		fireTick(); // signal for map refresh
	}
	// ------------------------------------------------------------------------------

	// -----------------------LISTENERS------------------------------------------------

	public void addListener(SimulationListener l) {
		listeners.add(l); // dodaj u listu prijavljenih
	}

	private void fireTick() {
		for (SimulationListener l : listeners) {
			l.onTick(simulatedTime);
		}
	}

	void fireDeparted(Flight f) {
		for (SimulationListener l : listeners) {
			l.onFlightDeparted(f);
		}
	}

	void fireArrived(Flight f) {
		for (SimulationListener l : listeners) {
			l.onFlightArrived(f);
		}
	}

	private void fireFinished() {
		for (SimulationListener l : listeners) {
			l.onSimulationFinished();
		}
	}

	void fireError(Exception e) {
		for (SimulationListener l : listeners) {
			l.onSimulationError(e);
		}
	}
	
	
	//-------------------------------------------------------------------------------------------

	public int getSimulatedTime() {
		return simulatedTime;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isFinished() {
		return finished;
	}
}

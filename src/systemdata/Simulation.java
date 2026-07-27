package systemdata;

import java.util.List;

import gui.MapPanel;
import project.Flight;



public class Simulation implements Runnable {

	// trenutno simulirano vreme od 00:00
	private int simulatedTime = 0;
	private MapPanel map;

	private static final int TICK_MINUTES = 2;
	private static final int TICK_MILLIS = 200;

	private int nextFlightIndex = 0;

	private Thread thread;

	private volatile boolean paused = false;

	private final Object pauseLock = new Object();

	private volatile boolean finished = false;
	
	private volatile boolean running = false;

	private List<Flight> activeFlights; // hronoloski sortirana lista iz SystemData
	
	private Runnable onFinishCallback;

	public void setOnFinishCallback(Runnable callback) {
	    this.onFinishCallback = callback;
	}

	public Simulation(MapPanel map) {
		this.activeFlights = SystemData.sortByDepartureTime(SystemData.getActiveFlights()); //na pocetku svi checked ulaze u simulaciju
		this.map =  map;
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {

				synchronized (pauseLock) {
					while (paused) {
						pauseLock.wait();
					}
				}

				simulatedTime += TICK_MINUTES;
				System.out.println("tick: simulatedTime=" + simulatedTime + " activeFlights.size=" + activeFlights.size());


				while (nextFlightIndex < activeFlights.size()
						&& activeFlights.get(nextFlightIndex).getDepartureTime() <= simulatedTime) {
					Flight f = activeFlights.get(nextFlightIndex);
					f.startFlight(this); 
					nextFlightIndex++;
				}

				if (map != null) {
					map.repaint();
				}
				
				if (nextFlightIndex >= activeFlights.size() && SystemData.isInFlightEmpty()) {
				    running = false;
				    finished = true;
				    if (onFinishCallback != null) {
				        onFinishCallback.run();
				    }
				    break;
				}

				Thread.sleep(TICK_MILLIS);
			}
		} catch (InterruptedException e) {
			// nit je prekinuta - izlazak iz petlje je ocekivano ponasanje
		}
	}
	
	  public void start() {
	        if (running) {
	            return;
	        }
	       
	        if (thread == null || !thread.isAlive()) {
	            thread = new Thread(this);
	            thread.start();
	            running = true;
	        }
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
	                pauseLock.notifyAll(); 
	            }
	        }
	    }

	    public void reset() {
	        if (thread != null && thread.isAlive()) {
	            thread.interrupt();
	            try {
	                thread.join(1000); // sacekaj da nit zavrsi
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	        }

	        for (Flight f : SystemData.getInFlight()) {
	            f.stopFlight();
	        }

	        SystemData.clearInFlight();
	        SystemData.resetAirportsBusy();

	        simulatedTime = 0;
	        nextFlightIndex = 0;
	        paused = false;
	        running = false;
	        finished = false;   
	        thread = null;

	        activeFlights = SystemData.sortByDepartureTime(SystemData.getActiveFlights());

	        // refresh map
	        if (map != null) {
	            map.repaint();
	        }
	    }


	public boolean isRunning() {
		return running;
	}

	public boolean isPaused() {
		return paused;
	}

	public int getSimulatedTime() {
		return simulatedTime;
	}
	
	public boolean isFinished() {
		return this.finished;
	}
	
	public void onFlightFinished(Flight f) {
		SystemData.removeInFlight(f);
	}
}

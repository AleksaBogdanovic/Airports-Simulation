package project;

import java.awt.Color;
import java.awt.Graphics;

import gui.MapPanel;
import systemdata.Simulation;
import systemdata.SystemData;

public class Flight {

	private Airport from, to;
	private int depHour, depMinute;
	private int duration;

	private static final int WAIT_TIME = 1000; // 1000ms = 10min simulacije
	private Simulation simulation; // instanca trenutne simulacije
	private int departureSimTime; // stvarno vreme polaska (ako je bilo guzve na aerodromu)
	private volatile Thread flightThread;

	public Flight(Airport from, Airport to, int departureHour, int departureMinute, int duration) {
		this.from = from;
		this.to = to;
		this.depHour = departureHour;
		this.depMinute = departureMinute;
		this.duration = duration;
	}

	public String toString() {
		return String.format("%-5s -> %5s  %02d:%02d   %5d min", from.getCode(), to.getCode(), depHour, depMinute,
				duration);
	}

	private void simulateFlight() {
		System.out.println("simulateFlight start: " + this);

		double durationMS = this.duration * 100; // podeli sa deset minuta i pomnozi sa 1000ms

		int currentTime = 0;

		try {
			synchronized (this.from) {
				while (this.from.airportBusy()) {
					this.from.wait();
				}
				this.from.setBusy(true);
				System.out.println("from busy postavljen, kreće let: " + this);

			}
			this.departureSimTime = simulation.getSimulatedTime(); // stvarni trenutak poletanja
			SystemData.addInFlight(this);
			System.out.println("dodat u inFlight, trenutno u vazduhu: " + SystemData.getInFlight().size());

			boolean waitTimePassed = false;
			while (!Thread.interrupted() && currentTime < durationMS) {

				Thread.sleep(200);

				currentTime += 200;
				if (currentTime >= Flight.WAIT_TIME && !waitTimePassed) {
					synchronized (this.from) {
						this.from.setBusy(false);
						this.from.notifyAll();
					}
					waitTimePassed = true;
				}

				if (currentTime >= durationMS) {
					simulation.onFlightFinished(this);
				}

			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	public void startFlight(Simulation sim) {
		System.out.println("startFlight pozvan za let: " + this);
		this.simulation = sim;
		this.flightThread = new Thread(() -> {
			simulateFlight();
		});
		this.flightThread.start();
	}
	
	public void stopFlight() {
	    if (this.flightThread != null) {
	        this.flightThread.interrupt();
	    }
	}

	public void draw(Graphics g, MapPanel panel) {
		int now = simulation.getSimulatedTime();
		int elapsed = Math.min(now - this.departureSimTime, this.duration);// proteklo simulirano vreme (u minutima), za
																			// slucaj da je proslo vise nego sto let
																			// traje

		double xRaw = from.getX() + this.displacementX() * elapsed;
		double yRaw = from.getY() + this.displacementY() * elapsed;

		int x = panel.scaleX((int) xRaw);
		int y = panel.scaleY((int) yRaw);

		g.setColor(Color.BLUE);
		g.fillOval(x - 5, y - 5, 10, 10);
	}

	private double displacementX() {
		double xStart = this.from.getX();
		double xEnd = this.to.getX();
		double time = this.duration;

		return (xEnd - xStart) / time; // Vx = x/t, posto vazi 100ms realnog vremena = 1min simuliranog, a

	}

	private double displacementY() { // put koji predje za jedan minut
		double yStart = this.from.getY();
		double yEnd = this.to.getY();
		double time = this.duration;

		return (yEnd - yStart) / time; // Vy = y/t, posto vazi 100ms realnog vremena = 1min simuliranog, a

	}

	public int getDepHour() {
		return this.depHour;
	}

	public int getDepMinute() {
		return this.depMinute;
	}

	public int getDepartureTime() {
		return this.depHour * 60 + this.depMinute;
	}

	public Airport getFrom() {
		return this.from;
	}
	
	public Airport getTo() {
		return this.to;
	}
	
	public int getDuration() {
		return this.duration;
	}
	
	public String serialize() {
	    return from.getCode() + "," + to.getCode() + ","
	            + String.format("%02d:%02d", depHour, depMinute) + "," + duration;
	}
	

}

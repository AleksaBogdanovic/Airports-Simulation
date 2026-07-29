package systemdata;

import project.Flight;

public class FlightTask implements Runnable {

	private final Flight flight;
	private final Simulation sim;
	private volatile int actualDeparture = -1;

	public FlightTask(Flight flight, Simulation sim) {
		this.flight = flight;
		this.sim = sim;
	}

	@Override
	public void run() {

		try {
			Runway runway = sim.runwayOf(flight.getFrom());
			actualDeparture = runway.reserve(flight.getDepartureTime());

			sim.waitSimulatedTime(actualDeparture); //waitSim ceka da prodje simulirano vreme u zagradi da bi nastavio(Poredi sa Simulation.simulatedTime)

			sim.addInFlight(this); // poletanje
			sim.fireDeparted(flight);

			sim.waitSimulatedTime(actualDeparture + flight.getDuration()); // sacekaj da se let zavrsi

			sim.removeInFlight(this); // sletanje
			sim.fireArrived(flight);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); //resetovasno, znaci samo izadji
		} catch (RuntimeException e) {
			sim.fireError(e);
		} finally {
			sim.removeInFlight(this); // garantovano skini iz vazduha
			sim.taskCompleted();
		}
	}
	
	public FlightPosition currentPosition(int simulatedTime) {
        int elapsed = normalizeElapsed(simulatedTime);
        double t = (double) elapsed / flight.getDuration();
        double x = flight.getFrom().getX()
                 + (flight.getTo().getX() - flight.getFrom().getX()) * t;
        double y = flight.getFrom().getY()
                 + (flight.getTo().getY() - flight.getFrom().getY()) * t;
        return new FlightPosition(x, y);
    }

    private int normalizeElapsed(int simulatedTime) {
        if (actualDeparture < 0) return 0;
        int elapsed = simulatedTime - actualDeparture;
        if (elapsed < 0) return 0;
        if (elapsed > flight.getDuration()) return flight.getDuration();
        return elapsed;
    }
	
    public Flight getFlight() { return flight; }
}

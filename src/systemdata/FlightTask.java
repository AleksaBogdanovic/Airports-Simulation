package systemdata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import project.Airport;
import project.Flight;

public class FlightTask implements Runnable {

	private final Flight flight;
	private final Simulation sim;
	private volatile int actualDeparture = -1;
	
	private volatile boolean rerouted = false;

	private double speed;
	private final List<FlightPosition> waypoints = new CopyOnWriteArrayList<>();
	
	public FlightTask(Flight flight, Simulation sim) {
		this.flight = flight;
		this.sim = sim;
		this.speed = Airport.distance(this.flight.getFrom(), this.flight.getTo())/this.flight.getDuration();
	}

	@Override
	public void run() {

		try {
			Runway runway = sim.runwayOf(flight.getFrom());
			actualDeparture = runway.reserve(flight.getDepartureTime());

			sim.waitSimulatedTime(actualDeparture); // waitSim ceka da prodje simulirano vreme u zagradi da bi
													// nastavio(Poredi sa Simulation.simulatedTime)

			sim.addInFlight(this); // poletanje
			sim.fireDeparted(flight);

		//sim.waitSimulatedTime(actualDeparture + this.flight.getDuration()); // sacekaj da se let zavrsi
			while (true) {
			    if (sim.isFinished() || !sim.isRunning()) break;   // simulacija stala spolja
			    double covered = (sim.getSimulatedTime() - actualDeparture) * speed;
			    if (covered >= totalDistance()) break;             // stigao, sa aktuelnim tackama
			    sim.awaitTick();                                    // sacekaj sledeci tick pa ponovi
			}
			    
			sim.removeInFlight(this); // sletanje
			sim.fireArrived(flight);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // resetovasno, znaci samo izadji
		} catch (RuntimeException e) {
			sim.fireError(e);
		} finally {
			sim.removeInFlight(this); // garantovano skini iz vazduha
			sim.taskCompleted();
		}
	}

	public FlightPosition currentPosition(int simulatedTime) {
//        int elapsed = normalizeElapsed(simulatedTime);
//        double t = (double) elapsed / flight.getDuration();
//        double x = flight.getFrom().getX()
//                 + (flight.getTo().getX() - flight.getFrom().getX()) * t;
//        double y = flight.getFrom().getY()
//                 + (flight.getTo().getY() - flight.getFrom().getY()) * t;
//        return new FlightPosition(x, y);
		ArrayList<FlightPosition> path = new ArrayList<>(waypoints);
		path.add(0, new FlightPosition(this.flight.getFrom().getX(), this.flight.getFrom().getY()));
		path.add(new FlightPosition(this.flight.getTo().getX(), this.flight.getTo().getY()));

		double t = simulatedTime - this.actualDeparture;
		double distanceCovered = t * this.speed;
		if (distanceCovered <= 0)
			return path.get(0);

		for (int seg = 0; seg < path.size() - 1; seg++) {
			FlightPosition a = path.get(seg);
			FlightPosition b = path.get(seg + 1);
			double d = FlightPosition.distance(a, b);

			if (d == 0)
				continue;

			if (distanceCovered < d) {
				double frac = distanceCovered / d;
				double x = a.getX() + (b.getX() - a.getX()) * frac;
				double y = a.getY() + (b.getY() - a.getY()) * frac;
				return new FlightPosition(x, y);
			}

			distanceCovered -= d;
		}

		return path.get(path.size() - 1); // presao ceo put -> odrediste
	}

	private int normalizeElapsed(int simulatedTime) {
		if (actualDeparture < 0)
			return 0;
		int elapsed = simulatedTime - actualDeparture;
		if (elapsed < 0)
			return 0;
		if (elapsed > flight.getDuration())
			return flight.getDuration();
		return elapsed;
	}

	private double totalDistance() {
		if (this.waypoints.isEmpty()) {
			return Airport.distance(this.flight.getFrom(), this.flight.getTo());
		}
		double distance = 0;

		FlightPosition position0 = new FlightPosition(this.flight.getFrom().getX(), this.flight.getFrom().getY());
		FlightPosition position1;

		for (FlightPosition p : waypoints) {
			position1 = p;
			distance += FlightPosition.distance(position0, position1);
			position0 = position1;
		}

		position1 = new FlightPosition(this.flight.getTo().getX(), this.flight.getTo().getY());
		distance += FlightPosition.distance(position0, position1);

		return distance;
	}

	public void addWaypoint(FlightPosition p) {
		if(!rerouted) {
			waypoints.add(this.currentPosition(sim.getSimulatedTime()));
			this.rerouted = true;
		}
	    waypoints.add(p);
	}
	
	public Flight getFlight() {
		return flight;
	}
}

/*
 * sim.waitSimulatedTime(actualDeparture);

// otkazivanje: ako je from ili to blokiran, let se ne izvrsava
if (flight.getFrom().isBlocked() || flight.getTo().isBlocked()) {
    return;                 // finally ce svejedno pozvati taskCompleted()
}

sim.addInFlight(this); // poletanje
sim.fireDeparted(flight);
*/

//=====================================================================================
	// ===  PREUSMERAVANJE LETA KAD SE ODREDISNI AERODROM BLOKIRA  (opciono - za odbranu)  ===
	// =====================================================================================
	//
	// ------ POLJA (ici uz ostala polja klase) ------
	// private volatile Airport currentTarget;      // promenljivo odrediste; init u konstruktoru: this.currentTarget = flight.getTo();
	// private volatile boolean rerouted = false;   // preusmerenje se desava tacno jednom
	//
	//
	// ------ IZMENA 3: provera na pocetku while petlje u run(), odmah posle
	//         if (sim.isFinished() || !sim.isRunning()) break; ------
	//
	//     // ako je trenutno odrediste blokirano i jos nismo preusmereni -> preusmeri
	//     if (currentTarget.isBlocked() && !rerouted) {
	//         Airport alt = AirportRegistry.nearestUnblocked(currentTarget);
	//         if (alt != null) {
	//             redirectTo(alt);
	//         }
	//     }
	//
	//
	// ------ IZMENA 4: u currentPosition(), umesto path.add(... flight.getTo() ...) ------
	//
	//     path.add(new FlightPosition(this.currentTarget.getX(), this.currentTarget.getY()));
	//
	//
	// ------ IZMENA 5: u totalDistance(), oba mesta gde stoji flight.getTo() ------
	//
	//     // (a) u grani waypoints.isEmpty():
	//     return Airport.distance(this.flight.getFrom(), this.currentTarget);
	//
	//     // (b) poslednja tacka pre zbira:
	//     position1 = new FlightPosition(this.currentTarget.getX(), this.currentTarget.getY());
	//
	//
	// ------ IZMENA 6: nova metoda (uz ostale metode klase) ------
	//
	// private void redirectTo(Airport newTarget) {
	//     waypoints.add(currentPosition(sim.getSimulatedTime()));
	//     currentTarget = newTarget;   // dalje leti ka novom cilju, istom brzinom
	//     rerouted = true;             // tacno jedno preusmerenje
	// }
	//
	// ------ USLOV: AirportRegistry.nearestUnblocked(...) mora postojati (vidi AirportRegistry) ------
	// =====================================================================================

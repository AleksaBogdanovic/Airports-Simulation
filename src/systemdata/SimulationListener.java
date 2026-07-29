package systemdata;

import project.Flight;

public interface SimulationListener {
    void onTick(int simulatedMinutes);
    void onFlightDeparted(Flight f);
    void onFlightArrived(Flight f);
    void onSimulationFinished();
    void onSimulationError(Exception e);
}
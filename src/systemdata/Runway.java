package systemdata;

//klasa je odgovorna za zakazivanje letova, odnosno rezervisanje termina poletanja
//sa jednog aerodroma u slucaju da postoji vise aerodroma koji polecu u prozoru od 10 minuta(prema specifikaciji projekta)
public class Runway {

	 private static final int SLOT_MINUTES = 10;
	    private int lastDeparture = Integer.MIN_VALUE;

	    public synchronized int reserve(int requestedTime) {
	        int actual = requestedTime;
	        if (actual < lastDeparture + SLOT_MINUTES) {
	            actual = lastDeparture + SLOT_MINUTES;
	        }
	        lastDeparture = actual;
	        return actual;
	    }

	    public synchronized void reset() {
	        lastDeparture = Integer.MIN_VALUE;
	    }
}

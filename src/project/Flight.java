package project;

public class Flight {

	private Airport from, to;
	private int depHour, depMinute;
	private int duration;

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
		return from.getCode() + "," + to.getCode() + "," + String.format("%02d:%02d", depHour, depMinute) + ","
				+ duration;
	}
}

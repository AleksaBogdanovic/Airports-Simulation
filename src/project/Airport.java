package project;

import exceptions.InvalidAirportCodeException;
import exceptions.InvalidCoordinateException;

public class Airport {
	
	public static final double X_MIN = -180;
	public static final double X_MAX = 180;
	public static final double Y_MIN = -90;
	public static final double Y_MAX = 90;
	
	
	private String CODE;
	private String name;
	private int x, y;
	
	private volatile boolean busy = false; //used for flight simulation
	
	
	public Airport(String code, String name, int x, int y) throws InvalidAirportCodeException, InvalidCoordinateException {
	
		 if (code == null || !code.matches("[A-Z]{3}")) {
		        throw new InvalidAirportCodeException(code, name);
		    }
		    if (x < X_MIN || x > X_MAX) {
		        throw new InvalidCoordinateException("X", x, X_MIN, X_MAX, name);
		    }
		    if (y < Y_MIN || y > Y_MAX) {
		        throw new InvalidCoordinateException("Y", y, Y_MIN, Y_MAX, name);
		    }

		    this.CODE = code;
		    this.name = name;
		    this.x = x;
		    this.y = y;
		}
	
	public static double distance(Airport a, Airport b) {
	    return Math.hypot((double) a.x - b.x, (double) b.y - a.y);
	}

	public String getCode() {
		return CODE;
	}

	public String getName() {
		return name;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public boolean airportBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	@Override
	public String toString() {
	    return String.format("%-5s  %-30s %6d %6d", CODE, name, x, y);
	}

	
	public String serialize() {
	    return CODE + "," + name + "," + x + "," + y;
	}
	

}

package project;

import java.util.HashMap;
import java.util.Map;

public class Airport {
	
	private String CODE;
	private String name;
	private int x, y;
	
	private volatile boolean busy = false; //used for flight simulation
	
	
	public Airport(String code, String name, int x, int y) {
	
		
		this.CODE = code;
		this.name = name;
		this.x = x;
		this.y = y;
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

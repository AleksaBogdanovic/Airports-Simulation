package gui;

import java.awt.Color;
import java.awt.Graphics;

import project.Airport;
import systemdata.FlightPosition;

public class FlightRenderer {
	public static final int AIRPLANE_RADIUS = 5;	

	public void draw(Graphics og, FlightPosition pos, CoordinateMapper mapper) {
		int x = mapper.scaleX(pos.getX());
		int y = mapper.scaleY(pos.getY());
		og.setColor(Color.BLUE);
		og.fillOval(x - AIRPLANE_RADIUS, y - AIRPLANE_RADIUS, AIRPLANE_RADIUS * 2, AIRPLANE_RADIUS * 2);
	}
}

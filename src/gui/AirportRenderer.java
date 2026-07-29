package gui;

import java.awt.Color;
import java.awt.Graphics;

import project.Airport;

public class AirportRenderer {
	
	public static final int AIRPORT_MARKER_SIZE = 10;


	public void draw(Graphics og, Airport a, CoordinateMapper mapper, boolean isSelectedNow) {
		int x = mapper.scaleX(a.getX());
		int y = mapper.scaleY(a.getY());

		og.setColor(isSelectedNow ? Color.RED : Color.GRAY);
		og.fillRect(x, y, AIRPORT_MARKER_SIZE, AIRPORT_MARKER_SIZE);

		og.setColor(Color.BLACK);
		og.drawString(a.getCode(), x + AIRPORT_MARKER_SIZE + 2, y + AIRPORT_MARKER_SIZE);
	}
}

package gui;

import project.Airport;

public class CoordinateMapper {

	static final double X_MIN = Airport.X_MIN;
	static final double X_MAX = Airport.X_MAX;
	static final double Y_MIN = Airport.Y_MIN;
	static final double Y_MAX = Airport.Y_MAX;

	private final int width, height;

	public CoordinateMapper(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int scaleX(double x) {
		return (int) ((x - CoordinateMapper.X_MIN) / (CoordinateMapper.X_MAX - CoordinateMapper.X_MIN)
				* width);
	}

	public int scaleY(double y) {
		return (int) ((-y - CoordinateMapper.Y_MIN) / (CoordinateMapper.Y_MAX - CoordinateMapper.Y_MIN)
				* height);
	}
}



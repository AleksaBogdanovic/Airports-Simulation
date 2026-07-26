package exceptions;

public class UnsupportedFileFormatException extends Exception {
	public UnsupportedFileFormatException(String extension) {
		super("Unsupported file format \".." + extension + "\". Please choose a .csv or .json file.");
	}
}

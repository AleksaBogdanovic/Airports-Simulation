package exceptions;

public class UnsupportedFileFormatException extends MyException {
	public UnsupportedFileFormatException(String extension) {
		super("Unsupported file format \".." + extension + "\". Please choose a .csv or .json file.");
	}
}

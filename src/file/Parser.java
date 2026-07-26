package file;

import java.util.ArrayList;

import exceptions.FileReadException;
import exceptions.FileWriteException;
import exceptions.ParsingException;

public interface Parser {
	
	ArrayList<Object> load(String path) throws FileReadException, ParsingException;
	void save(String path) throws FileWriteException;

}

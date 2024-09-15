package net.bopsys.ai;

/**
 * @author Marco Ruiz
 */
public class JsonNotClosedException extends Exception {

	public JsonNotClosedException() {
		super("The JSON object is missing its closing curly brace");
	}
}

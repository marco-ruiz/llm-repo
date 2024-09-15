package net.bopsys.ai.examples.books;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * @author Marco Ruiz
 */
@Data
public class Writer {

	private final Innovator writer;
	private final List<Book> books = new ArrayList<>();

	public void addBook(Book book) {
		books.add(book);
	}
}

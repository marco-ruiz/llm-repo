package net.bopsys.ai.examples.books;

import net.bopsys.ai.AiAnswer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Marco Ruiz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book implements AiAnswer<Book> {

	private String bookTitle;
	private String isbn;
	private int publicationYear;

	@Override
	public Book buildAnswerExample() {
		return new Book("1984", "978-0451524935", 1949);
	}
}

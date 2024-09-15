package net.bopsys.ai.examples.books;

import net.bopsys.ai.ChatQueryRunner;
import net.bopsys.ai.examples.MatchesList;

import java.time.Duration;
import java.util.List;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

/**
 * @author Marco Ruiz
 */
public class App {

	public static final ChatLanguageModel MODEL = OllamaChatModel.builder()
			.baseUrl("http://localhost:11434")
			.timeout(Duration.ofMinutes(30))
			.modelName("llama2")
//			.modelName("mistral")
//			.modelName("tinyllama")
//			.modelName("phi")
//			.temperature(0.1)
//			.format("json")
			.build();

	public static void main(String[] args) {
		ChatQueryRunner queryRunner = new ChatQueryRunner(MODEL, 5);

		List<Writer> writers = queryRunner.runQuery(MatchesList.class, "List of 2 famous writers").getMatches().stream()
				.map(r -> queryRunner.runQuery(Innovator.class, "Details of writer " + r + " and the top 2 books he/she wrote"))
				.map(Writer::new)
				.peek(w -> w.getWriter().getCreations().forEach(b -> w.addBook(queryRunner.runQuery(Book.class, buildBookQuery(b, w.getWriter())))))
				.toList();

		writers.forEach(System.out::println);
	}

	private static String buildBookQuery(String bookTitle, Innovator writer) {
		return String.format("details of book titled '%s' written by '%s %s'", bookTitle, writer.getFirstName(), writer.getLastName());
	}
}

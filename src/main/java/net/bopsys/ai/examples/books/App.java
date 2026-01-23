/*
 * Copyright 2026 Bopsys LLC & Marco Ruiz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

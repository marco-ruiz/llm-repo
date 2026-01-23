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

package net.bopsys.ai;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.function.Predicate;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;

import static net.bopsys.ai.JsonUtils.createBeanFromEmbeddedJson;

/**
 * @author Marco Ruiz
 */
public class ChatQueryRunner {

	private static String buildFullUserPrompt(String userPrompt, String prevErrorMsg) {
		return userPrompt + ((prevErrorMsg == null) ? "" : ANSWER_ERROR_MESSAGE + "\n" + prevErrorMsg);
	}

	public static final String ANSWER_ERROR_MESSAGE = """
			The JSON object you provided in the previous answer is not valid.
			Please try again with a valid JSON object that conforms to the required schema.
			The specific error with the previous JSON object was the following:
			""";

 	private final ChatLanguageModel languageModel;
	private final int maxRetries;
	private final ChatQueryProfileRepo queryProfileRepo = new ChatQueryProfileRepo();

	public ChatQueryRunner(ChatLanguageModel languageModel, int maxRetries) {
		this.languageModel = languageModel;
		this.maxRetries = maxRetries;
	}

	public <T> T runQuery(Class<T> answerModelClass, String userPrompt) {
		return runQuery(answerModelClass, userPrompt, r -> true);
	}

	public <T> T runQuery(Class<T> answerModelClass, String userPrompt, Predicate<T> resultTester) {
		@SuppressWarnings("unchecked")
		ChatQueryProfile<T> queryProfile = (ChatQueryProfile<T>) queryProfileRepo.get(answerModelClass);
		return (queryProfile == null) ? null : runQuery(queryProfile, userPrompt, resultTester);
	}

	public <T> T runQuery(ChatQueryProfile<T> queryProfile, String userPrompt) {
		return runQuery(queryProfile, userPrompt, r -> true);
	}

	public <T> T runQuery(ChatQueryProfile<T> queryProfile, String userPrompt, Predicate<T> resultTester) {
		queryProfileRepo.register(queryProfile);

		String prevErrorMsg = null;
		for (int i = 0; i < maxRetries; i++) {
			try {
				String fullUserPrompt = buildFullUserPrompt(userPrompt, prevErrorMsg);
				List<ChatMessage> messages = queryProfile.createQueryMessages(fullUserPrompt);
				String answer = languageModel.generate(messages).content().text();
				T result = createBeanFromEmbeddedJson(queryProfile.getAnswerModelClass(), answer);
				if (resultTester.test(result))
					return result;

				prevErrorMsg = "answer missing information or not as expected";
			} catch (JsonProcessingException | JsonNotClosedException e) {
				prevErrorMsg = e.getMessage();
			}
		}
		return null;
	}
}

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

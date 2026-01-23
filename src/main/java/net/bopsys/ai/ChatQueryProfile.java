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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.Getter;

import static net.bopsys.ai.JsonUtils.toJsonSchema;
import static net.bopsys.ai.ReflectionUtils.buildFieldDescription;
import static net.bopsys.ai.ReflectionUtils.buildPredefinedExample;

/**
 * @author Marco Ruiz
 */
@Getter
public class ChatQueryProfile<T> {

	public static final String SYSTEM_MESSAGE = """
			You are an assistant who strictly answers EVERY question with a single JSON object. 
			This means that the first character in your answer must be the first character of the JSON object ("{")
			and the last character in your answer must be the last character of the JSON object ("}").
			
			You are forbidden to output	anything else in your answer. 
			You are unable to output anything else in your answer.
			In fact, your answer will be considered AUTOMATICALLY WRONG, if it includes anything other than a
			JSON object that is a syntactically VALID.
			
			""";

	private static <T> String buildJsonSchemaDescription(Class<T> beanClass) {
		try {
			return buildJsonSchemaDescription(beanClass, toJsonSchema(beanClass, true));
		} catch (Exception e) {
			return "";
		}
	}

	private static <T> String buildJsonSchemaDescription(Class<T> beanClass, JsonNode jsonSchema) {
		StringBuilder explanation = new StringBuilder();
		jsonSchema.fieldNames().forEachRemaining(e -> explanation.append(buildFieldDescription(beanClass, e)).append("\n"));
		return String.format(
				"The answer MUST be a JSON object that PRECISELY conforms with the following schema:\n%s\n\nWhere:\n%s",
				jsonSchema.toPrettyString(),
				explanation
		);
	}

	private final Class<T> answerModelClass;
	private final String exampleAiAcceptableJsonSchema;
	private final List<ChatMessage> setupMessages = new ArrayList<>();

	public ChatQueryProfile(Class<T> answerModelClass) {
		this(answerModelClass, buildPredefinedExample(answerModelClass));
	}

	public ChatQueryProfile(Class<T> answerModelClass, AssistantExample<T>... examples) {
		this.answerModelClass = answerModelClass;
		this.exampleAiAcceptableJsonSchema = buildJsonSchemaDescription(answerModelClass);
		addSystemMessage(SystemMessage.from(SYSTEM_MESSAGE + exampleAiAcceptableJsonSchema));

		if (examples != null)
			Arrays.stream(examples).filter(Objects::nonNull).forEach(e -> setupMessages.addAll(e.toChatMessages()));
	}

	public ChatQueryProfile<T> addSystemMessage(String msg) {
		return addSystemMessage(SystemMessage.from(msg));
	}

	public ChatQueryProfile<T> addSystemMessage(SystemMessage msg) {
		setupMessages.add(0, msg);
		return this;
	}

	public List<ChatMessage> createQueryMessages(String userPrompt) {
		List<ChatMessage> result = new ArrayList<>(setupMessages);
		result.add(UserMessage.from(userPrompt));
		return result;
	}
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;

import static net.bopsys.ai.JsonUtils.objToJson;
import static net.bopsys.ai.ReflectionUtils.toReadableForm;

/**
 * @author Marco Ruiz
 */
public class AssistantExample<T> {

	private final UserMessage userRequest;
	private final List<T> acceptableAnswers;

	public AssistantExample(T answerExample) {
		this(
				"details of any fictitious or real " + toReadableForm(answerExample.getClass(), false),
				answerExample
		);
	}

	public AssistantExample(String userPrompt, T firstAcceptableAnswer, T... otherAcceptableAnswers) {
		this.userRequest = UserMessage.from(userPrompt);
		this.acceptableAnswers = new ArrayList<>();
		this.acceptableAnswers.add(firstAcceptableAnswer);
		this.acceptableAnswers.addAll(Arrays.stream(otherAcceptableAnswers).toList());
	}

	public List<ChatMessage> toChatMessages() {
		return acceptableAnswers.stream().flatMap(this::buildExampleChatMessages).toList();
	}

	private Stream<ChatMessage> buildExampleChatMessages(T acceptableValue) {
		return Stream.of(userRequest, AiMessage.from(objToJson(acceptableValue)));
	}
}

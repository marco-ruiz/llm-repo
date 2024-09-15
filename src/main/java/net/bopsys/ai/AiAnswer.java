package net.bopsys.ai;

import static net.bopsys.ai.ReflectionUtils.buildRandomModel;

/**
 * @author Marco Ruiz
 */
public interface AiAnswer<T extends AiAnswer<T>> {

	default T buildAnswerExample() {
		return (T) buildRandomModel(this.getClass());
	}

	default AssistantExample<T> buildExample() {
		return new AssistantExample<>(buildAnswerExample());
	}
}

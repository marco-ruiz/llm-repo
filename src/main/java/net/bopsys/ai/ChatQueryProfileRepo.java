package net.bopsys.ai;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco Ruiz
 */
public class ChatQueryProfileRepo {

	private final Map<Class<?>, ChatQueryProfile<?>> repo = new HashMap<>();

	public ChatQueryProfileRepo(ChatQueryProfile<?>... queryProfiles) {
		Arrays.stream(queryProfiles).forEach(this::register);
	}

	public void register(ChatQueryProfile<?> profile) {
		repo.putIfAbsent(profile.getAnswerModelClass(), profile);
	}

	public ChatQueryProfile<?> get(Class<?> answerModelClass) {
		return repo.computeIfAbsent(answerModelClass, ChatQueryProfile::new);
	}

	public Collection<ChatQueryProfile<?>> getRegistered() {
		return repo.values();
	}
}

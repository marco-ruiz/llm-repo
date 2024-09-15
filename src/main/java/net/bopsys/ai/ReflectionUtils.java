package net.bopsys.ai;

import net.bopsys.ai.examples.books.Innovator;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Marco Ruiz
 */
public class ReflectionUtils {

	private static EasyRandom MODEL_RANDOM_GENERATOR = null;

	public static <T> AssistantExample<T> buildPredefinedExample(Class<T> answerModelClass) {
		try {
			T answerExample = buildRandomModel(answerModelClass);
			return AiAnswer.class.isAssignableFrom(answerModelClass) ?
					((AiAnswer) answerExample).buildExample() :
					new AssistantExample<>(answerExample);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T buildRandomModel(Class<T> answerModelClass) {
		if (MODEL_RANDOM_GENERATOR == null) {
			EasyRandomParameters parameters = new EasyRandomParameters();
			parameters.stringLengthRange(8, 20);
			parameters.collectionSizeRange(3, 5);

			MODEL_RANDOM_GENERATOR = new EasyRandom(parameters);
		}

		return MODEL_RANDOM_GENERATOR.nextObject(answerModelClass);
	}

	public static String buildFieldDescription(Class<?> targetClass, String fieldName) {
		try {
			return buildFieldDescription(targetClass, targetClass.getDeclaredField(fieldName));
		} catch (NoSuchFieldException e) {
			return "";
		}
	}

	private static String buildFieldDescription(Class<?> targetClass, Field field) {
		boolean isIterable = Iterable.class.isAssignableFrom(field.getType());
		String elementDesc = String.format("%s representing the %s of the '%s' json object",
				buildFieldDescription(field, isIterable),
				toReadableForm(field.getType(), isIterable),
				toReadableForm(targetClass, false)
		);
		return String.format("* '%s' is a %s", field.getName(), isIterable ? "list of " + elementDesc : elementDesc);
	}

	private static String buildFieldDescription(Field field, boolean isIterable) {
		Class<?> elementClass = isIterable ? getGenericParameterType(field, 0) : field.getType();
		return toReadableForm(elementClass, isIterable);
	}

	private static Class<?> getGenericParameterType(Field field, int genericParameterIndex) {
		ParameterizedType genericType = (ParameterizedType) field.getGenericType();
		return (Class<?>) genericType.getActualTypeArguments()[genericParameterIndex];
	}

	public static String toReadableForm(Class<?> targetClass, boolean pluralize) {
		String readableForm = Arrays.stream(targetClass.getSimpleName().split("(?=[A-Z])")) // Split on uppercase letters
				.map(String::toLowerCase) // Convert each word to lowercase
				.collect(Collectors.joining(" "));
		return (pluralize && !readableForm.endsWith("s")) ? readableForm + "s" : readableForm;
	}

	public static void main(String[] args) {
		System.out.println(buildFieldDescription(Innovator.class, "firstName"));
		System.out.println(buildFieldDescription(Innovator.class, "lastName"));
		System.out.println(buildFieldDescription(Innovator.class, "creations"));
	}
}

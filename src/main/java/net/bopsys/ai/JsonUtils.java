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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import net.bopsys.ai.examples.books.Innovator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Marco Ruiz
 */
public class JsonUtils {

	public static final String EMPTY_JSON = "{}";
	public static final ObjectMapper OBJ_MAPPER = createObjectMapper();

	//==================
	// OBJECT MAPPER
	//==================

	public static ObjectMapper createObjectMapper() {
		ObjectMapper result = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(String.class, new JsonDeserializer<>() {
			public String deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
				return jsonParser.getValueAsString().replace('\"', '\'');
			}
		});

		// Configure to ignore unknown properties
		result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// Configure to tolerate missing values (setting default values for missing fields)
		result.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
		result.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
		result.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		result.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
		result.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false);

		result.registerModule(module);
		return result;
	}

	//===============
	// READING JSON
	//===============

	public static <T> T createBeanFromEmbeddedJson(Class<T> beanClass, String text) throws JsonProcessingException, JsonNotClosedException {
		String json = trimToTopMostJson(text);
		if (json != null)
			return OBJ_MAPPER.readValue(json, beanClass);

		json = trimToTopMostJson(text + "}");
		if (json == null)
			throw new JsonNotClosedException();

		try {
			return OBJ_MAPPER.readValue(json, beanClass);
		} catch(JsonProcessingException e) {
			throw new JsonNotClosedException();
		}
	}

	public static JsonNode extractEmbeddedJson(String text) {
		try {
			return OBJ_MAPPER.readTree(trimToTopMostJson(text));
		} catch (JsonProcessingException e) {
			return OBJ_MAPPER.createObjectNode();
		}
	}

	private static String trimToTopMostJson(String text) {
		return trimToTopMost(text, '{', '}');
	}

	public static String trimToTopMost(String input, char startChar, char endChar) {
		int startIndex = input.indexOf(startChar);
		int endIndex = findEndCharIndex(input, startChar, endChar, startIndex);
		return (startIndex < 0 || endIndex < 0) ? null : input.substring(startIndex, endIndex + 1);
	}

	public static int findEndCharIndex(String input, char startChar, char endChar, int startIndex) {
		int length = input.length();
		int currIndex = startIndex + 1;
		int numOpenedBlocks = 1;
		while (currIndex < length) {
			numOpenedBlocks += getMatchingIncrement(input.charAt(currIndex), startChar, endChar);
			if (numOpenedBlocks == 0)
				return currIndex;
			currIndex++;
		}
		return -1;
	}

	private static int getMatchingIncrement(char c, char plusOneChar, char minusOneChar) {
		return (c == plusOneChar) ? 1 : (c == minusOneChar) ? -1 : 0;
	}

	//================
	// WRITING JSON
	//================

	public static String toJson(String fieldName, List<String> values) {
		return toJson(Map.of(fieldName, values));
	}

	public static String toJson(Map<String, ?> values) {
		try {
			return OBJ_MAPPER.writeValueAsString(values);
		} catch (Exception e) {
			return EMPTY_JSON;
		}
	}

	public static String objToJson(Object valueBean) {
		try {
			return OBJ_MAPPER.writeValueAsString(valueBean);
		} catch (Exception e) {
			return EMPTY_JSON;
		}
	}

	//=====================
	// WRITING JSON SCHEMA
	//======================

	public static JsonNode toJsonSchema(Class<?> clazz, boolean propertiesNodeOnly) throws JsonProcessingException {
		JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(OBJ_MAPPER);
		JsonSchema schema = schemaGen.generateSchema(clazz);
		String schemaJson = OBJ_MAPPER.writeValueAsString(schema);
		JsonNode result = OBJ_MAPPER.readTree(schemaJson);
		return propertiesNodeOnly ? result.get("properties") : result;
	}

	public static void main(String[] args) throws JsonProcessingException {
		Class<Innovator> type = Innovator.class;
		toJsonSchema(type, true);
	}

}

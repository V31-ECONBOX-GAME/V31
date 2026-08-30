/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.v31bank.build.properties;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.v31bank.build.util.JsonFiles;

/**
 * One {@code spring-configuration-metadata.json}, read as the structure it was written as
 * so that its order and its duplicates can be checked.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
final class ConfigurationMetadata {

	/**
	 * The kinds of element the file is made of, in the order it has to list them in.
	 */
	private static final List<String> ELEMENT_TYPES = List.of("groups", "properties", "hints");

	private final File file;

	private final Map<String, Object> json;

	private ConfigurationMetadata(File file, Map<String, Object> json) {
		this.file = file;
		this.json = json;
	}

	/**
	 * Read as a plain object: what the sections hold is settled one section at a time.
	 * @param file the file to read
	 * @return the result
	 */
	static ConfigurationMetadata of(File file) {
		return new ConfigurationMetadata(file, JsonFiles.readObject(file));
	}

	/**
	 * Only the section asked for is looked at. The format has sections this does not
	 * read, {@code ignored} among them, and reading one is no reason to fail on another.
	 * @param elementType the section to read
	 * @return its elements, empty when the file declares no such section
	 */
	List<Map<String, Object>> elements(String elementType) {
		Object elements = this.json.get(elementType);
		if (elements == null) {
			return List.of();
		}
		if (!(elements instanceof List<?> array)) {
			throw new IllegalArgumentException("'%s' in %s is not a JSON array".formatted(elementType, this.file));
		}
		return array.stream().map((element) -> object(element, elementType)).toList();
	}

	/**
	 * Element by element rather than one cast over the list: only the erased shape
	 * reaches runtime, so a cast to the whole list would prove nothing.
	 * @param element one element of the section
	 * @param elementType the section it came from, for the message
	 * @return the element as an object
	 */
	private Map<String, Object> object(Object element, String elementType) {
		if (!(element instanceof Map<?, ?> map)) {
			throw new IllegalArgumentException(
					"'%s' in %s holds something that is not a JSON object".formatted(elementType, this.file));
		}
		Map<String, Object> object = new LinkedHashMap<>();
		map.forEach((key, value) -> object.put(String.valueOf(key), value));
		return object;
	}

	/**
	 * Walks every kind of element the file holds. The shape of the file is this class's
	 * business, so a caller that only wants the names does not have to know it.
	 * @param action what to do with each kind and the names it lists
	 */
	void forEachElementType(BiConsumer<String, List<String>> action) {
		for (String elementType : ELEMENT_TYPES) {
			action.accept(elementType, names(elementType));
		}
	}

	List<String> names(String elementType) {
		return elements(elementType).stream().map((element) -> (String) element.get("name")).toList();
	}

}

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
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.v31bank.build.util.JsonFiles;

/**
 * One configuration metadata file, read in the order it was written.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
final class ConfigurationMetadata {

	private static final List<String> ELEMENT_TYPES = List.of("groups", "properties", "hints");

	private final File file;

	private final Map<String, Object> json;

	private ConfigurationMetadata(File file, Map<String, Object> json) {
		this.file = file;
		this.json = json;
	}

	static ConfigurationMetadata of(File file) {
		return new ConfigurationMetadata(file, JsonFiles.readObject(file));
	}

	@SuppressWarnings("unchecked")
	List<Map<String, Object>> array(String name) {
		Object section = this.json.getOrDefault(name, List.of());
		if (!(section instanceof List<?> array)) {
			throw new IllegalArgumentException("'%s' in %s is not a JSON array".formatted(name, this.file));
		}
		return (List<Map<String, Object>>) array;
	}

	@SuppressWarnings("unchecked")
	Map<String, Object> object(String name) {
		Object section = this.json.getOrDefault(name, Map.of());
		if (!(section instanceof Map<?, ?> object)) {
			throw new IllegalArgumentException("'%s' in %s is not a JSON object".formatted(name, this.file));
		}
		return (Map<String, Object>) object;
	}

	void forEachElementType(BiConsumer<String, List<String>> action) {
		for (String elementType : ELEMENT_TYPES) {
			action.accept(elementType, names(elementType));
		}
	}

	List<String> names(String elementType) {
		return array(elementType).stream().map((element) -> (String) element.get("name")).toList();
	}

}

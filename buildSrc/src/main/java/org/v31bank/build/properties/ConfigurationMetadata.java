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

import tools.jackson.core.type.TypeReference;

import org.v31bank.build.util.JsonFiles;

/**
 * One {@code spring-configuration-metadata.json}, read as the structure it was written as
 * so that its order and its duplicates can be checked.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
final class ConfigurationMetadata {

	static final List<String> ELEMENT_TYPES = List.of("groups", "properties", "hints");

	private static final TypeReference<Map<String, Object>> TYPE = new TypeReference<>() {
	};

	private final Map<String, Object> json;

	private ConfigurationMetadata(Map<String, Object> json) {
		this.json = json;
	}

	/**
	 * The shape is declared rather than cast to, so a file that does not have it fails
	 * here rather than wherever the first surprising value is read.
	 * @param file the file to read
	 * @return the result
	 */
	static ConfigurationMetadata of(File file) {
		return new ConfigurationMetadata(JsonFiles.read(file, TYPE));
	}

	@SuppressWarnings("unchecked")
	List<Map<String, Object>> elements(String elementType) {
		return (List<Map<String, Object>>) this.json.getOrDefault(elementType, List.of());
	}

	List<String> names(String elementType) {
		return elements(elementType).stream().map((element) -> (String) element.get("name")).toList();
	}

}

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

package org.v31bank.build.util;

import java.io.File;
import java.util.Map;

import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * A JSON file read as a plain object.
 *
 * @author Xander Wang
 */
public final class JsonFiles {

	private static final JsonMapper MAPPER = JsonMapper.builder()
		.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
		.build();

	private static final TypeReference<Map<String, Object>> OBJECT = new TypeReference<>() {
	};

	private JsonFiles() {
	}

	public static Map<String, Object> readObject(File file) {
		try {
			return MAPPER.readValue(file, OBJECT);
		}
		catch (JacksonException ex) {
			throw new IllegalArgumentException("Failed to read %s".formatted(file), ex);
		}
	}

}

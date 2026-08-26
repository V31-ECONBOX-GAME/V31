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
 * A JSON file read as the object it is, with every value left as written.
 * <p>
 * Nothing beyond "the top level is an object" is asked of the file, so a section this
 * build does not read is a section it cannot fail on. Narrowing a value it does read is
 * the caller's business.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class JsonFiles {

	private static final JsonMapper MAPPER = JsonMapper.builder()
		.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
		.build();

	private static final TypeReference<Map<String, Object>> OBJECT = new TypeReference<>() {
	};

	private JsonFiles() {
	}

	/**
	 * Reads the file's top-level object, keeping the order it was written in.
	 * @param file the file to read
	 * @return what it holds
	 */
	public static Map<String, Object> readObject(File file) {
		try {
			return MAPPER.readValue(file, OBJECT);
		}
		catch (JacksonException ex) {
			throw new IllegalArgumentException("Failed to read %s".formatted(file), ex);
		}
	}

}

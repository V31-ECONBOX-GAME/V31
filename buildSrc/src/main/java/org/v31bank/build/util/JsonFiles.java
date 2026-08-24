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

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * A JSON file read as a shape the caller declares.
 * <p>
 * The shape is a type argument rather than a cast, so it survives to runtime and the file
 * is held to it as it is read:
 *
 * <pre class="code">
 * private static final TypeReference&lt;Map&lt;String, String&gt;&gt; TYPE = new TypeReference&lt;&gt;() {
 * };
 *
 * Map&lt;String, String&gt; json = JsonFiles.read(file, TYPE);
 * </pre>
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class JsonFiles {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private JsonFiles() {
	}

	/**
	 * Reads the file, and fails naming it: Jackson keeps the source out of its own
	 * message, so the file is named here or nowhere.
	 * @param <T> the shape to read the file as
	 * @param file the file to read
	 * @param type the shape to read it as
	 * @return the file, read as that shape
	 */
	public static <T> T read(File file, TypeReference<T> type) {
		try {
			return MAPPER.readValue(file, type);
		}
		catch (JacksonException ex) {
			throw new IllegalArgumentException("Failed to read %s".formatted(file), ex);
		}
	}

}

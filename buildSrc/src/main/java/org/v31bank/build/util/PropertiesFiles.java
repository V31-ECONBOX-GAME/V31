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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Properties rendered as a stable file.
 *
 * @author Xander Wang
 */
public final class PropertiesFiles {

	private PropertiesFiles() {
	}

	public static byte[] render(Properties properties) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		properties.store(buffer, null);
		String content = buffer.toString(StandardCharsets.ISO_8859_1)
			.lines()
			.filter((line) -> !line.startsWith("#"))
			.sorted()
			.collect(Collectors.joining("\n", "", "\n"));
		return content.getBytes(StandardCharsets.ISO_8859_1);
	}

}

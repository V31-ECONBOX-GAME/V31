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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link ConfigurationMetadata}.
 *
 * @author Xander Wang
 */
class ConfigurationMetadataTests {

	@TempDir
	private Path directory;

	@Test
	void readsASectionWrittenAsAnArray() {
		ConfigurationMetadata metadata = write("""
				{"properties": [{"name": "v31.b"}, {"name": "v31.a"}]}""");
		assertThat(metadata.names("properties")).containsExactly("v31.b", "v31.a");
	}

	@Test
	void readsNothingFromASectionTheFileDoesNotDeclare() {
		ConfigurationMetadata metadata = write("""
				{"hints": [], "ignored": {"properties": []}}""");
		assertThat(metadata.array("properties")).isEmpty();
	}

	@Test
	void refusesASectionAskedForAsAnArrayThatIsWrittenAsAnObject() {
		ConfigurationMetadata metadata = write("""
				{"properties": {"v31.a": {}}}""");
		assertThatIllegalArgumentException().isThrownBy(() -> metadata.array("properties"))
			.withMessageContaining("'properties'")
			.withMessageContaining("is not a JSON array")
			.withMessageContaining(this.directory.toString());
	}

	@Test
	void refusesASectionAskedForAsAnObjectThatIsWrittenAsAnArray() {
		ConfigurationMetadata metadata = write("""
				{"ignored": []}""");
		assertThatIllegalArgumentException().isThrownBy(() -> metadata.object("ignored"))
			.withMessageContaining("'ignored'")
			.withMessageContaining("is not a JSON object")
			.withMessageContaining(this.directory.toString());
	}

	@Test
	void readsEverySectionOfAFileThatDeclaresNone() {
		ConfigurationMetadata metadata = write("{}");
		metadata.forEachElementType((elementType, names) -> assertThat(names).isEmpty());
	}

	@Test
	void readsASectionWrittenAsAnObject() {
		ConfigurationMetadata metadata = write("""
				{"hints": [], "ignored": {"properties": []}}""");
		assertThat(metadata.object("ignored")).containsKey("properties");
	}

	@Test
	void readsNothingFromAnObjectSectionTheFileDoesNotDeclare() {
		ConfigurationMetadata metadata = write("{}");
		assertThat(metadata.object("ignored")).isEmpty();
	}

	@Test
	void readsAModulesMetadata() {
		ConfigurationMetadata metadata = ConfigurationMetadata
			.of(MetadataFiles.realMetadata(this.directory.resolve("spring-configuration-metadata.json")).toFile());
		assertThat(metadata.names("groups")).hasSize(6).contains("v31.grpc.client.deadline");
		assertThat(metadata.names("properties")).hasSize(5).contains("v31.grpc.propagation.headers");
		assertThat(metadata.names("hints")).isEmpty();
		assertThat(metadata.object("ignored")).containsKey("properties");
	}

	private ConfigurationMetadata write(String json) {
		Path file = this.directory.resolve("spring-configuration-metadata.json");
		try {
			Files.writeString(file, json);
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return ConfigurationMetadata.of(file.toFile());
	}

}

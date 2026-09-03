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

package org.v31bank.build.constant;

/**
 * Build locations.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Locations {

	public static final String CONFIGURATION_METADATA_FILE = "META-INF/spring-configuration-metadata.json";

	public static final String ADDITIONAL_CONFIGURATION_METADATA_FILE = "META-INF/additional-spring-configuration-metadata.json";

	public static final String AUTO_CONFIGURATION_IMPORTS_FILE = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

	public static final String AUTO_CONFIGURATION_IMPORTS_FILE_NAME = AUTO_CONFIGURATION_IMPORTS_FILE
		.substring(AUTO_CONFIGURATION_IMPORTS_FILE.lastIndexOf('/') + 1);

	public static final String AUTO_CONFIGURATION_METADATA_FILE = "auto-configuration-metadata.properties";

	public static final String STARTER_METADATA_FILE = "starter-metadata.properties";

	public static final String FAILURE_REPORT_FILE = "failure-report.txt";

	public static final String MIGRATIONS_DIRECTORY = "db/migration";

	public static final String GENERATED_SOURCES_DIRECTORY = "src/main/generated/sources";

	public static final String PROTO_TOOLCHAIN_DIRECTORY = "buf";

	public static final String BUF_EXECUTABLE = "buf";

	public static final String PROTOC_EXECUTABLE = "protoc";

	public static final String GRPC_JAVA_GENERATOR_EXECUTABLE = "protoc-gen-grpc-java";

	private Locations() {
	}

}

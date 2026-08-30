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
 * The files and directories this build's own logic addresses by path.
 * <p>
 * Each of these is written by one class and read by another, so the path is agreed here
 * rather than spelled out at both ends.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Locations {

	/**
	 * Where the configuration processor writes what it generated, relative to the root of
	 * a jar or of a classes directory.
	 */
	public static final String CONFIGURATION_METADATA_FILE = "META-INF/spring-configuration-metadata.json";

	/**
	 * Where whatever the processor cannot work out is written by hand.
	 */
	public static final String ADDITIONAL_CONFIGURATION_METADATA_FILE = "META-INF/additional-spring-configuration-metadata.json";

	/**
	 * Where Spring Boot looks for the file a module registers its auto-configurations in.
	 */
	public static final String AUTO_CONFIGURATION_IMPORTS_FILE = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

	/**
	 * {@link #AUTO_CONFIGURATION_IMPORTS_FILE} without the directories it sits in.
	 */
	public static final String AUTO_CONFIGURATION_IMPORTS_FILE_NAME = AUTO_CONFIGURATION_IMPORTS_FILE
		.substring(AUTO_CONFIGURATION_IMPORTS_FILE.lastIndexOf('/') + 1);

	/**
	 * What the auto-configuration metadata task writes.
	 */
	public static final String AUTO_CONFIGURATION_METADATA_FILE = "auto-configuration-metadata.properties";

	/**
	 * What the starter metadata task writes.
	 */
	public static final String STARTER_METADATA_FILE = "starter-metadata.properties";

	/**
	 * The source root a generator owns, kept apart from handwritten code so that the
	 * checks and the formatter can skip it by path.
	 */
	public static final String GENERATED_SOURCES_DIRECTORY = "src/main/generated/sources";

	/**
	 * Where buf and the generators are installed, beneath the root project's build
	 * directory.
	 */
	public static final String PROTO_TOOLCHAIN_DIRECTORY = "buf";

	/**
	 * buf as it is named once installed. The project that installs it writes this name
	 * and the projects that run it read it.
	 */
	public static final String BUF_EXECUTABLE = "buf";

	/**
	 * protoc as it is named once installed.
	 */
	public static final String PROTOC_EXECUTABLE = "protoc";

	/**
	 * The gRPC generator as it is named once installed.
	 */
	public static final String GRPC_JAVA_GENERATOR_EXECUTABLE = "protoc-gen-grpc-java";

	private Locations() {
	}

}

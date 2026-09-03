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
 * Dependency coordinates.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Coordinates {

	public static final String JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher";

	public static final String PROTOBUF_JAVA = "com.google.protobuf:protobuf-java";

	public static final String GRPC_PROTOBUF = "io.grpc:grpc-protobuf";

	public static final String GRPC_STUB = "io.grpc:grpc-stub";

	public static final String BUF = "build.buf:buf";

	public static final String PROTOC = "com.google.protobuf:protoc";

	public static final String GRPC_JAVA_GENERATOR = "io.grpc:protoc-gen-grpc-java";

	public static final String CONFIGURATION_PROCESSOR = "org.springframework.boot:spring-boot-configuration-processor";

	public static final String AUTO_CONFIGURATION_PROCESSOR = "org.springframework.boot:spring-boot-autoconfigure-processor";

	public static final String FLYWAY = "org.springframework.boot:spring-boot-flyway";

	public static final String FLYWAY_POSTGRESQL = "org.flywaydb:flyway-database-postgresql";

	public static final String POSTGRESQL = "org.postgresql:postgresql";

	private Coordinates() {
	}

}

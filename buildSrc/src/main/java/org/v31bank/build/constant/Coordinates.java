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
 * The external modules this build's own logic names.
 * <p>
 * A version is settled by the platform, so what is named here is the module alone.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Coordinates {

	/**
	 * Finds and runs what a JUnit Platform engine discovers, so every source set that
	 * runs tests needs it at runtime.
	 */
	public static final String JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher";

	/**
	 * The protobuf runtime the generated types are compiled against, and so the version
	 * {@link #PROTOC} is fetched at.
	 */
	public static final String PROTOBUF_JAVA = "com.google.protobuf:protobuf-java";

	/**
	 * The gRPC runtime the generated stubs are compiled against, and so the version
	 * {@link #GRPC_JAVA_GENERATOR} is fetched at.
	 */
	public static final String GRPC_PROTOBUF = "io.grpc:grpc-protobuf";

	/**
	 * The base class every generated stub extends.
	 */
	public static final String GRPC_STUB = "io.grpc:grpc-stub";

	/**
	 * buf, published as an executable rather than as a jar.
	 */
	public static final String BUF = "build.buf:buf";

	/**
	 * protoc, published as an executable rather than as a jar.
	 */
	public static final String PROTOC = "com.google.protobuf:protoc";

	/**
	 * The protoc plugin that turns a service into Java stubs.
	 */
	public static final String GRPC_JAVA_GENERATOR = "io.grpc:protoc-gen-grpc-java";

	/**
	 * Generates the metadata describing a module's {@code @ConfigurationProperties}.
	 */
	public static final String CONFIGURATION_PROCESSOR = "org.springframework.boot:spring-boot-configuration-processor";

	/**
	 * Generates the metadata describing a module's auto-configurations.
	 */
	public static final String AUTO_CONFIGURATION_PROCESSOR = "org.springframework.boot:spring-boot-autoconfigure-processor";

	/**
	 * What a service whose schema Flyway owns needs on its runtime classpath.
	 */
	public static final String FLYWAY = "org.springframework.boot:spring-boot-flyway";

	/**
	 * Teaches Flyway the dialect the migrations are written in.
	 */
	public static final String FLYWAY_POSTGRESQL = "org.flywaydb:flyway-database-postgresql";

	/**
	 * The driver those migrations run over.
	 */
	public static final String POSTGRESQL = "org.postgresql:postgresql";

	private Coordinates() {
	}

}

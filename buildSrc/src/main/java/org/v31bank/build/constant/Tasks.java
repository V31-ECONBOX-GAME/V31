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
 * The tasks this build's own logic registers or looks up by name.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Tasks {

	/**
	 * Fetches buf and the generators it runs, once for the whole build.
	 */
	public static final String INSTALL_PROTO_TOOLCHAIN = "installProtoToolchain";

	/**
	 * Generates one API project's sources from its {@code .proto}.
	 */
	public static final String GENERATE_PROTO_SOURCES = "generateProtoSources";

	/**
	 * Checks one API against the rules {@code buf.yaml} names.
	 */
	public static final String LINT_PROTO = "lintProto";

	/**
	 * Runs the tests that need more than the project they are in. Names the source set
	 * they live in as well.
	 */
	public static final String INT_TEST = "intTest";

	/**
	 * Checks migration file names before a bad one is ever applied.
	 */
	public static final String VALIDATE_MIGRATION_NAMES = "validateMigrationNames";

	/**
	 * Checks the metadata the configuration processor generated.
	 */
	public static final String CHECK_CONFIGURATION_METADATA = "checkSpringConfigurationMetadata";

	/**
	 * Checks the metadata written by hand beside it.
	 */
	public static final String CHECK_ADDITIONAL_CONFIGURATION_METADATA = "checkAdditionalSpringConfigurationMetadata";

	/**
	 * Checks the metadata of a module that has nothing to generate it from.
	 */
	public static final String CHECK_MANUAL_CONFIGURATION_METADATA = "checkManualSpringConfigurationMetadata";

	/**
	 * Checks the imports file a module registers its auto-configurations in.
	 */
	public static final String CHECK_AUTO_CONFIGURATION_IMPORTS = "checkAutoConfigurationImports";

	/**
	 * Checks those classes against what the imports file claims.
	 */
	public static final String CHECK_AUTO_CONFIGURATION_CLASSES = "checkAutoConfigurationClasses";

	private Tasks() {
	}

}

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
 * Build task names.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class Tasks {

	public static final String INSTALL_PROTO_TOOLCHAIN = "installProtoToolchain";

	public static final String GENERATE_PROTO_SOURCES = "generateProtoSources";

	public static final String LINT_PROTO = "lintProto";

	public static final String INT_TEST = "intTest";

	public static final String VALIDATE_MIGRATION_NAMES = "validateMigrationNames";

	public static final String CHECK_CONFIGURATION_METADATA = "checkSpringConfigurationMetadata";

	public static final String CHECK_ADDITIONAL_CONFIGURATION_METADATA = "checkAdditionalSpringConfigurationMetadata";

	public static final String CHECK_MANUAL_CONFIGURATION_METADATA = "checkManualSpringConfigurationMetadata";

	public static final String CHECK_AUTO_CONFIGURATION_IMPORTS = "checkAutoConfigurationImports";

	public static final String CHECK_AUTO_CONFIGURATION_CLASSES = "checkAutoConfigurationClasses";

	private Tasks() {
	}

}

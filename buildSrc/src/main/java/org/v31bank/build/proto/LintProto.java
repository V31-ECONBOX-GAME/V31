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

package org.v31bank.build.proto;

import java.io.IOException;
import java.nio.file.Files;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * Checks one API against the rules {@code buf.yaml} names.
 * <p>
 * The rules catch what the compiler cannot: a package that does not match its directory,
 * a response reused across RPCs so that neither can grow a field, a name that reads
 * unlike every other name in the API. One API per task, so a failure says which one.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public abstract class LintProto extends BufTask {

	/**
	 * A marker written when the API lints clean, declared only so the task can be up to
	 * date: Gradle re-runs a task that has no output on every build.
	 * @return the marker file
	 */
	@OutputFile
	public abstract RegularFileProperty getReport();

	@TaskAction
	void lint() throws IOException {
		buf("lint", "--path", getApi().get());
		Files.writeString(getReport().get().getAsFile().toPath(),
				getApi().get() + " lints clean" + System.lineSeparator());
	}

}

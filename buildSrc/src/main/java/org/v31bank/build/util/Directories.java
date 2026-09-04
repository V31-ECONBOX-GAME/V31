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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;

/**
 * Directory work {@link Files} leaves out.
 *
 * @author Xander Wang
 */
public final class Directories {

	private Directories() {
	}

	public static Directory rootDirOf(Project project) {
		return IsolatedProjects.rootOf(project).getProjectDirectory();
	}

	public static void deleteRecursively(Path root) {
		if (root == null || !Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(root)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(path);
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to clear " + root, ex);
		}
	}

}

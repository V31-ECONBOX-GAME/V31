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

package org.v31bank.data.valkey.util;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Builds the keys this application writes, all under one prefix. Valkey has one flat
 * keyspace, so two services on the same instance collide the moment both cache something
 * they call {@code customer:1}.
 * <p>
 * A segment may not contain the separator: segments are built from caller-supplied
 * values, and one containing a colon would address a key outside its namespace — a
 * customer identified as {@code 7:session:admin} reaching a key it may not read.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class ValkeyKeys {

	/**
	 * What separates the parts of a key. Colon by convention, and what every Valkey and
	 * Redis browser expects when it renders keys as a tree.
	 */
	public static final String SEPARATOR = ":";

	private final String prefix;

	/**
	 * Create a builder that puts everything under the given prefix.
	 * @param prefix the prefix every key begins with, typically the service name
	 */
	public ValkeyKeys(String prefix) {
		this.prefix = requireSegment(prefix, "prefix");
	}

	/**
	 * Build a key under this application's prefix — {@code keys.of("customer", id)} gives
	 * {@code v31:customer:0199-7a}.
	 * @param segments the parts of the key, in order, none of them empty or containing
	 * the separator
	 * @return the key
	 */
	public String of(String... segments) {
		Objects.requireNonNull(segments, "segments must not be null");
		if (segments.length == 0) {
			throw new IllegalArgumentException("A key needs at least one segment beyond the prefix");
		}
		StringJoiner key = new StringJoiner(SEPARATOR);
		key.add(this.prefix);
		for (String segment : segments) {
			key.add(requireSegment(segment, "segment"));
		}
		return key.toString();
	}

	private static String requireSegment(String segment, String name) {
		Objects.requireNonNull(segment, name + " must not be null");
		if (segment.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		if (segment.contains(SEPARATOR)) {
			throw new IllegalArgumentException(
					"A key " + name + " must not contain '" + SEPARATOR + "', but was '" + segment + "'");
		}
		return segment;
	}

}

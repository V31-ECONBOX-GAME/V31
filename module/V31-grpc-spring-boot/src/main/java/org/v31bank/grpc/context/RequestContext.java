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

package org.v31bank.grpc.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import io.grpc.Context;

/**
 * What travels with a request wherever it goes next — which trace it belongs to, which
 * tenant asked — so the meaning does not stop at the first hop.
 * <p>
 * Two carriers, because there are two kinds of thread. A servlet request stays on its
 * thread, so a {@link ThreadLocal} holds it; a gRPC interceptor and handler run on
 * different threads, so {@link Context} holds it there. {@link #current()} reads the gRPC
 * context first and falls back to the thread.
 * <p>
 * <strong>Nothing secret goes in here.</strong> Everything placed here is copied onto
 * every outgoing call, to every service reached, and reaches any of them that logs its
 * metadata. Credentials travel deliberately, per call.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class RequestContext {

	/**
	 * Where the values live while a gRPC call is being served.
	 */
	public static final Context.Key<Map<String, String>> CONTEXT_KEY = Context.key("v31-request-context");

	/**
	 * What a value must look like to be carried: printable ASCII and bounded. These end
	 * up in metadata, headers and log lines, where a newline forges a log entry and an
	 * unbounded length makes every downstream call expensive.
	 */
	private static final Pattern ACCEPTED_VALUE = Pattern.compile("[\\x20-\\x7E]{1,256}");

	private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();

	private RequestContext() {
	}

	/**
	 * Return what this request is carrying.
	 * @return the values, empty when there is nothing to carry
	 */
	public static Map<String, String> current() {
		Map<String, String> fromContext = CONTEXT_KEY.get();
		if (fromContext != null) {
			return fromContext;
		}
		Map<String, String> fromThread = THREAD_LOCAL.get();
		return (fromThread != null) ? fromThread : Map.of();
	}

	/**
	 * Return one value this request is carrying.
	 * @param name the name it travels under, lower case
	 * @return the value, or {@code null} when the request is not carrying it
	 */
	public static String get(String name) {
		return current().get(name);
	}

	/**
	 * Attach values to the current thread until the returned scope is closed. Returns a
	 * scope rather than a pair of methods because values left on a pooled thread would be
	 * attributed to whatever it served next.
	 * @param values what the request is carrying
	 * @return the scope to close when the request is done with
	 */
	public static Scope attach(Map<String, String> values) {
		Objects.requireNonNull(values, "values must not be null");
		Map<String, String> previous = THREAD_LOCAL.get();
		THREAD_LOCAL.set(Map.copyOf(values));
		return () -> {
			if (previous != null) {
				THREAD_LOCAL.set(previous);
			}
			else {
				THREAD_LOCAL.remove();
			}
		};
	}

	/**
	 * Add a value to a set being built, if it is one that may be carried.
	 * @param values the set being built
	 * @param name the name to carry it under, lower case
	 * @param value the value, ignored when absent or not safe to carry
	 */
	public static void put(Map<String, String> values, String name, String value) {
		if (value != null && ACCEPTED_VALUE.matcher(value).matches()) {
			values.put(name, value);
		}
	}

	/**
	 * Return an empty set to build up.
	 * @return the set
	 */
	public static Map<String, String> newValues() {
		return new LinkedHashMap<>();
	}

	/**
	 * What {@link #attach} returns: closing it puts the thread back as it was.
	 */
	@FunctionalInterface
	public interface Scope extends AutoCloseable {

		@Override
		void close();

	}

}

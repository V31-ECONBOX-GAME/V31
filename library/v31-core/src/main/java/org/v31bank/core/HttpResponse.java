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

package org.v31bank.core;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * An endpoint's response body.
 *
 * @param code the HTTP status
 * @param message what to tell the caller
 * @param data the payload, absent on failure
 * @param total records across all pages
 * @param <T> the payload type
 * @author Xander Wang
 * @since 0.2.0
 */
public record HttpResponse<T>(int code, String message, T data, Long total) {

	/**
	 * The status a success carries.
	 */
	public static final int SUCCESS = 200;

	private static final String SUCCESS_MESSAGE = "Success";

	public HttpResponse(int code, String message, T data) {
		this(code, message, data, null);
	}

	public boolean succeeded() {
		return this.code == SUCCESS;
	}

	public static HttpResponse<Void> ok() {
		return ok(null);
	}

	public static <T> HttpResponse<T> ok(T data) {
		return ok(data, SUCCESS_MESSAGE);
	}

	public static <T> HttpResponse<T> ok(T data, String message) {
		return new HttpResponse<>(SUCCESS, message, data);
	}

	public static <T> HttpResponse<List<T>> page(List<T> records, long total) {
		Objects.requireNonNull(records, "records must not be null");
		return new HttpResponse<>(SUCCESS, SUCCESS_MESSAGE, List.copyOf(records), total);
	}

	public static <T> HttpResponse<T> error(int code, String message) {
		return new HttpResponse<>(code, message, null);
	}

	public <R> HttpResponse<R> map(Function<? super T, ? extends R> converter) {
		Objects.requireNonNull(converter, "converter must not be null");
		R converted = (this.data != null) ? converter.apply(this.data) : null;
		return new HttpResponse<>(this.code, this.message, converted, this.total);
	}

}

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

package org.v31bank.grpc;

import java.util.Objects;
import java.util.function.Supplier;

import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Turns a failed call back into the exception the far side threw.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class GrpcErrors {

	private GrpcErrors() {
	}

	public static <T> T call(Supplier<T> call) {
		Objects.requireNonNull(call, "call must not be null");
		try {
			return call.get();
		}
		catch (StatusRuntimeException ex) {
			throw asResponseStatusException(ex);
		}
	}

	public static ResponseStatusException asResponseStatusException(StatusRuntimeException exception) {
		Objects.requireNonNull(exception, "exception must not be null");
		return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), exception);
	}

}

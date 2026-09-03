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

package org.v31bank.grpc.client;

import java.util.Objects;
import java.util.function.Supplier;

import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Turns a failed call into the same {@link ResponseStatusException} the far side threw,
 * so a caller does not reason about {@link StatusRuntimeException} in its application
 * layer.
 * <p>
 * Not an interceptor: one can change the status a call fails with but not the exception
 * type the generated stub raises. Translating at the call site also keeps it visible.
 *
 * <pre class="code">
 * CustomerResponse response = GrpcErrors.call(() -&gt; this.customerStub.get(request));
 * </pre>
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class GrpcErrors {

	private GrpcErrors() {
	}

	/**
	 * Make a call, reporting a failure as an {@link ResponseStatusException}.
	 * @param call the call to make
	 * @param <T> what the call returns
	 * @return what the call returned
	 * @throws ResponseStatusException if the call failed
	 */
	public static <T> T call(Supplier<T> call) {
		Objects.requireNonNull(call, "call must not be null");
		try {
			return call.get();
		}
		catch (StatusRuntimeException ex) {
			throw asResponseStatusException(ex);
		}
	}

	/**
	 * Translate a failed call. Every one is reported as {@code 500}: gRPC's status says
	 * what the transport saw, not what an HTTP caller should do about it, and the
	 * description names hosts and methods. Both stay on the cause, for the log.
	 * @param exception the failure to translate
	 * @return the exception to raise in its place
	 */
	public static ResponseStatusException asResponseStatusException(StatusRuntimeException exception) {
		Objects.requireNonNull(exception, "exception must not be null");
		return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), exception);
	}

}

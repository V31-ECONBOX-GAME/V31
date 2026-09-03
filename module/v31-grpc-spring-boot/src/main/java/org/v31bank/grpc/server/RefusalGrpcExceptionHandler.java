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

package org.v31bank.grpc.server;

import io.grpc.Status;
import io.grpc.StatusException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

/**
 * Reports a refused request over gRPC the way the REST layer reports it over HTTP: a
 * status the transport understands and the wording its author chose. Spring gRPC asks
 * each {@link GrpcExceptionHandler} in order, so this returns {@code null} for what it
 * does not own.
 * <p>
 * The message is passed through because a {@link ResponseStatusException} carries one
 * written for the caller — not true of anything else, hence
 * {@link UnexpectedExceptionGrpcExceptionHandler}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class RefusalGrpcExceptionHandler implements GrpcExceptionHandler {

	@Override
	public StatusException handleException(Throwable exception) {
		if (!(exception instanceof ResponseStatusException refusal)) {
			return null;
		}
		return Status.INTERNAL.withDescription(refusal.getReason()).asException();
	}

}

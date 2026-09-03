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

package org.v31bank.grpc.autoconfigure;

import io.grpc.ServerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;

import org.v31bank.grpc.server.HeaderPropagationServerInterceptor;
import org.v31bank.grpc.server.RefusalGrpcExceptionHandler;
import org.v31bank.grpc.server.UnexpectedExceptionGrpcExceptionHandler;

/**
 * {@link AutoConfiguration Auto-configuration} for the calls a V31 service serves:
 * refusals reported with the platform's error codes, an unexpected failure's detail kept
 * off the wire, an identifier on every call.
 * <p>
 * Spring gRPC gathers every {@link GrpcExceptionHandler} bean and asks them in order, so
 * this contributes handlers rather than an interceptor — a service adds its own the same
 * way.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@AutoConfiguration(before = GrpcServerAutoConfiguration.class)
@ConditionalOnClass({ ServerInterceptor.class, GrpcExceptionHandler.class })
@EnableConfigurationProperties(V31GrpcProperties.class)
public class V31GrpcServerAutoConfiguration {

	/**
	 * Translates a refusal the caller is meant to see.
	 * @return the handler
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBooleanProperty(name = "v31.grpc.server.exception-handling.enabled", matchIfMissing = true)
	public RefusalGrpcExceptionHandler refusalGrpcExceptionHandler() {
		return new RefusalGrpcExceptionHandler();
	}

	/**
	 * Answers everything else, last, without disclosing why.
	 * @return the handler
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBooleanProperty(name = "v31.grpc.server.exception-handling.enabled", matchIfMissing = true)
	public UnexpectedExceptionGrpcExceptionHandler unexpectedExceptionGrpcExceptionHandler() {
		return new UnexpectedExceptionGrpcExceptionHandler();
	}

	/**
	 * Makes what a call arrived carrying reachable for the rest of it, including the
	 * calls made onward.
	 * @param properties the header names to carry
	 * @return the interceptor
	 */
	@Bean
	@GlobalServerInterceptor
	@ConditionalOnMissingBean
	@ConditionalOnBooleanProperty(name = "v31.grpc.propagation.enabled", matchIfMissing = true)
	public HeaderPropagationServerInterceptor headerPropagationServerInterceptor(V31GrpcProperties properties) {
		return new HeaderPropagationServerInterceptor(properties.getPropagation().getHeaders());
	}

}

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

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import org.v31bank.grpc.web.HeaderPropagationFilter;

/**
 * {@link AutoConfiguration Auto-configuration} for where a request's context enters the
 * platform. The interceptors carry values between services, but the first hop is usually
 * an HTTP call from outside and something has to put them there to begin with.
 * <p>
 * Servlet applications only: a service speaking gRPC alone has no HTTP entry point.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ Filter.class, HeaderPropagationFilter.class })
@ConditionalOnBooleanProperty(name = "v31.grpc.propagation.enabled", matchIfMissing = true)
@EnableConfigurationProperties(V31GrpcProperties.class)
public class V31GrpcWebAutoConfiguration {

	/**
	 * Reads the request's context off the incoming HTTP request.
	 * @param properties the header names to carry
	 * @return the filter
	 */
	@Bean
	@ConditionalOnMissingBean
	public HeaderPropagationFilter headerPropagationFilter(V31GrpcProperties properties) {
		return new HeaderPropagationFilter(properties.getPropagation().getHeaders());
	}

}

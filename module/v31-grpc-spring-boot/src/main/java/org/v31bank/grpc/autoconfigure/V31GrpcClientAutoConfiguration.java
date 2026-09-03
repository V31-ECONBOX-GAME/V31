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

import java.time.Duration;

import io.grpc.ClientInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.GlobalClientInterceptor;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.client.interceptor.DefaultDeadlineSetupClientInterceptor;

import org.v31bank.grpc.client.HeaderPropagationClientInterceptor;

/**
 * {@link AutoConfiguration Auto-configuration} for the calls a V31 service makes: every
 * one gets a deadline, and the headers listed for propagation are carried onward.
 * <p>
 * gRPC imposes no deadline, so a call to a service answering nothing waits until the
 * connection breaks — under load, how one unhealthy service exhausts the pools in front
 * of it while all of them look healthy. Spring gRPC ships the interceptor but does not
 * install it; here it is installed and removing it is the deliberate act.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@AutoConfiguration(before = GrpcClientAutoConfiguration.class)
@ConditionalOnClass({ ClientInterceptor.class, GrpcChannelFactory.class })
@EnableConfigurationProperties(V31GrpcProperties.class)
public class V31GrpcClientAutoConfiguration {

	/**
	 * Applies the default deadline to any call that did not set one. Set
	 * {@code v31.grpc.client.deadline.enabled} to {@code false} to leave calls unbounded.
	 * @param properties the deadline to apply
	 * @return the interceptor
	 * @throws IllegalStateException if the configured duration is not positive, since a
	 * deadline of zero expires every call the moment it leaves
	 */
	@Bean
	@GlobalClientInterceptor
	@ConditionalOnMissingBean
	@ConditionalOnBooleanProperty(name = "v31.grpc.client.deadline.enabled", matchIfMissing = true)
	public DefaultDeadlineSetupClientInterceptor defaultDeadlineSetupClientInterceptor(V31GrpcProperties properties) {
		Duration duration = properties.getClient().getDeadline().getDuration();
		if (duration == null || duration.isZero() || duration.isNegative()) {
			throw new IllegalStateException("v31.grpc.client.deadline.duration must be positive, but was " + duration
					+ "; set v31.grpc.client.deadline.enabled to false to leave calls without a deadline");
		}
		return new DefaultDeadlineSetupClientInterceptor(duration);
	}

	/**
	 * Sends onward whatever the request being served arrived carrying.
	 * @return the interceptor
	 */
	@Bean
	@GlobalClientInterceptor
	@ConditionalOnMissingBean
	@ConditionalOnBooleanProperty(name = "v31.grpc.propagation.enabled", matchIfMissing = true)
	public HeaderPropagationClientInterceptor headerPropagationClientInterceptor() {
		return new HeaderPropagationClientInterceptor();
	}

}

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
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for V31 gRPC.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@ConfigurationProperties("v31.grpc")
public class V31GrpcProperties {

	private final Propagation propagation = new Propagation();

	private final Server server = new Server();

	private final Client client = new Client();

	public Propagation getPropagation() {
		return this.propagation;
	}

	public Server getServer() {
		return this.server;
	}

	public Client getClient() {
		return this.client;
	}

	/**
	 * What travels with a request from one service to the next.
	 */
	public static class Propagation {

		/**
		 * Whether to carry a request's context onward, into the calls made while serving
		 * it.
		 */
		private boolean enabled = true;

		/**
		 * Header names carried onward, lower case as gRPC requires. Nothing secret
		 * belongs here: every name listed is copied onto every outgoing call, to every
		 * service reached.
		 */
		private List<String> headers = new ArrayList<>();

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public List<String> getHeaders() {
			return this.headers;
		}

		public void setHeaders(List<String> headers) {
			this.headers = headers;
		}

	}

	/**
	 * Properties for calls this service serves.
	 */
	public static class Server {

		private final ExceptionHandling exceptionHandling = new ExceptionHandling();

		public ExceptionHandling getExceptionHandling() {
			return this.exceptionHandling;
		}

		/**
		 * Exception handling properties.
		 */
		public static class ExceptionHandling {

			/**
			 * Whether to report a refused call with the platform's error code, and keep
			 * an unexpected failure's message off the wire.
			 */
			private boolean enabled = true;

			public boolean isEnabled() {
				return this.enabled;
			}

			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}

		}

	}

	/**
	 * Properties for calls this service makes.
	 */
	public static class Client {

		private final Deadline deadline = new Deadline();

		public Deadline getDeadline() {
			return this.deadline;
		}

	}

	/**
	 * Deadline properties.
	 */
	public static class Deadline {

		/**
		 * Whether a call that set no deadline is given one. Turning it off lets a call
		 * wait until the connection breaks, which should be a deliberate decision.
		 */
		private boolean enabled = true;

		/**
		 * How long a call may run before it is given up on. gRPC applies none of its own,
		 * so a call to a service answering nothing holds the caller's thread until the
		 * connection breaks — under load, how one unhealthy service exhausts the pools in
		 * front of it. Must be positive, checked at startup.
		 */
		private Duration duration = Duration.ofSeconds(5);

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Duration getDuration() {
			return this.duration;
		}

		public void setDuration(Duration duration) {
			this.duration = duration;
		}

	}

}

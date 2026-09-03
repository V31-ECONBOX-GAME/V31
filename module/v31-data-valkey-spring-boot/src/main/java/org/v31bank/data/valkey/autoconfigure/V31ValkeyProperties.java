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

package org.v31bank.data.valkey.autoconfigure;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for V31 Data Valkey.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@ConfigurationProperties("v31.data.valkey")
public class V31ValkeyProperties {

	private final Serialization serialization = new Serialization();

	private final Cache cache = new Cache();

	/**
	 * Prefix every key this application writes begins with, keeping one service's keys
	 * apart from another's on a shared instance.
	 */
	private String keyPrefix = "v31";

	public Serialization getSerialization() {
		return this.serialization;
	}

	public Cache getCache() {
		return this.cache;
	}

	public String getKeyPrefix() {
		return this.keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	/**
	 * Serialization properties.
	 */
	public static class Serialization {

		/**
		 * Package prefixes a stored value may name as its own type. Anything else is
		 * refused on read, which stops a tampered entry naming a class whose construction
		 * does something. Widening this to {@code java} gives up the protection entirely.
		 */
		private List<String> trustedPackages = List.of("org.v31bank", "java.util", "java.time", "java.math");

		public List<String> getTrustedPackages() {
			return this.trustedPackages;
		}

		public void setTrustedPackages(List<String> trustedPackages) {
			this.trustedPackages = trustedPackages;
		}

	}

	/**
	 * Cache properties.
	 */
	public static class Cache {

		/**
		 * Whether to configure Spring's cache abstraction against Valkey.
		 */
		private boolean enabled = true;

		/**
		 * How long an entry lives when its cache is not named below.
		 */
		private Duration defaultTtl = Duration.ofMinutes(10);

		/**
		 * How long an entry lives, per cache name. A cache not named here uses the
		 * default.
		 */
		private Map<String, Duration> ttls = new LinkedHashMap<>();

		/**
		 * Whether a lookup that found nothing may be cached. On by default: without it,
		 * repeated lookups of something absent reach the database every time.
		 */
		private boolean allowNullValues = true;

		/**
		 * Whether a failed cache read or write fails the call. Off by default, so an
		 * unreachable Valkey costs latency rather than availability. A failed eviction is
		 * always allowed out regardless.
		 */
		private boolean failFast;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Duration getDefaultTtl() {
			return this.defaultTtl;
		}

		public void setDefaultTtl(Duration defaultTtl) {
			this.defaultTtl = defaultTtl;
		}

		public Map<String, Duration> getTtls() {
			return this.ttls;
		}

		public void setTtls(Map<String, Duration> ttls) {
			this.ttls = ttls;
		}

		public boolean isAllowNullValues() {
			return this.allowNullValues;
		}

		public void setAllowNullValues(boolean allowNullValues) {
			this.allowNullValues = allowNullValues;
		}

		public boolean isFailFast() {
			return this.failFast;
		}

		public void setFailFast(boolean failFast) {
			this.failFast = failFast;
		}

	}

}

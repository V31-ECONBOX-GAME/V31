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

package org.v31bank.smoketest.datavalkey;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import org.v31bank.data.valkey.ValkeyKeys;
import org.v31bank.data.valkey.ValkeyLock;
import org.v31bank.data.valkey.autoconfigure.V31ValkeyProperties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class V31DataValkeySmokeTests {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private V31ValkeyProperties properties;

	@Test
	void theStarterAloneContributesTheKeysAndTheLock() {
		assertThat(this.context.getBeansOfType(ValkeyKeys.class)).hasSize(1);
		assertThat(this.context.getBeansOfType(ValkeyLock.class)).hasSize(1);
	}

	@Test
	void theStarterAloneContributesTheCacheConfiguration() {
		assertThat(this.context.getBeansOfType(RedisCacheConfiguration.class)).hasSize(1);
	}

	@Test
	void applicationPropertiesAreBound() {
		assertThat(this.properties.getKeyPrefix()).isEqualTo("smoke");
		assertThat(this.properties.getCache().getDefaultTtl()).isEqualTo(Duration.ofSeconds(42));
	}

}

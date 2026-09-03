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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import org.v31bank.data.valkey.lock.ValkeyLock;
import org.v31bank.data.valkey.util.ValkeyKeys;

/**
 * {@link AutoConfiguration Auto-configuration} for V31 Data Valkey: a template storing
 * JSON under readable keys, plus the key builder and the lock built on it.
 * <p>
 * It <em>takes the name</em> of Spring Boot's {@code redisTemplate} rather than adding a
 * second bean. Boot's serialises with JDK serialization, which turns a Valkey entry an
 * attacker could write into remote code execution on read; leaving it as the obvious bean
 * to inject would be leaving a trap.
 * <p>
 * JSON cannot say which class an entry comes back as, so the serializer records the type
 * — and honouring an unchecked type is the same problem one format removed, which is why
 * Spring Data calls its switch {@code enableUnsafeDefaultTyping}. An entry may only name
 * a type under {@code v31.data.valkey.serialization.trusted-packages}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@AutoConfiguration(before = DataRedisAutoConfiguration.class)
@ConditionalOnClass({ RedisConnectionFactory.class, GenericJacksonJsonRedisSerializer.class })
@EnableConfigurationProperties(V31ValkeyProperties.class)
public class V31ValkeyAutoConfiguration {

	/**
	 * The serializer used for values everywhere — by the template below and by the cache
	 * configuration — so an entry written through one is readable through the other.
	 * @param properties the types an entry is allowed to name
	 * @return the serializer
	 */
	@Bean
	@ConditionalOnMissingBean(name = "valkeyValueSerializer")
	public RedisSerializer<Object> valkeyValueSerializer(V31ValkeyProperties properties) {
		return GenericJacksonJsonRedisSerializer.builder()
			.enableDefaultTyping(trustedTypes(properties))
			.enableSpringCacheNullValueSupport()
			.build();
	}

	/**
	 * Takes the name Spring Boot's template would have used, so code injecting
	 * {@code RedisTemplate} gets the safe one. Boot's backs off because this registers
	 * first, which {@code before = DataRedisAutoConfiguration.class} arranges.
	 * @param connectionFactory the connection to Valkey
	 * @param valkeyValueSerializer the serializer for values
	 * @return the template
	 */
	@Bean
	@ConditionalOnMissingBean(name = "redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
			@Qualifier("valkeyValueSerializer") RedisSerializer<Object> valkeyValueSerializer) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		template.setValueSerializer(valkeyValueSerializer);
		template.setHashValueSerializer(valkeyValueSerializer);
		return template;
	}

	@Bean
	@ConditionalOnMissingBean
	public ValkeyKeys valkeyKeys(V31ValkeyProperties properties) {
		return new ValkeyKeys(properties.getKeyPrefix());
	}

	@Bean
	@ConditionalOnMissingBean
	public ValkeyLock valkeyLock(StringRedisTemplate stringRedisTemplate) {
		return new ValkeyLock(stringRedisTemplate);
	}

	/**
	 * Build the check applied to the type an entry names on read. Matching is by package
	 * prefix, so listing {@code java} would undo the point of checking at all.
	 * @param properties the trusted package prefixes
	 * @return the validator
	 */
	private static PolymorphicTypeValidator trustedTypes(V31ValkeyProperties properties) {
		BasicPolymorphicTypeValidator.Builder validator = BasicPolymorphicTypeValidator.builder();
		for (String trustedPackage : properties.getSerialization().getTrustedPackages()) {
			validator.allowIfSubType(trustedPackage);
		}
		return validator.build();
	}

}

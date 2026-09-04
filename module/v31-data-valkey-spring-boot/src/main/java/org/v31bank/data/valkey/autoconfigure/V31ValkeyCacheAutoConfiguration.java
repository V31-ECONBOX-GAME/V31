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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import org.v31bank.data.valkey.ValkeyCacheErrorHandler;
import org.v31bank.data.valkey.ValkeyCachingConfigurer;

/**
 * {@link AutoConfiguration Auto-configuration} for caching through Valkey.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@AutoConfiguration(before = CacheAutoConfiguration.class, after = V31ValkeyAutoConfiguration.class)
@ConditionalOnClass({ RedisCacheManager.class, RedisCacheManagerBuilderCustomizer.class })
@ConditionalOnBooleanProperty(name = "v31.data.valkey.cache.enabled", matchIfMissing = true)
@EnableConfigurationProperties(V31ValkeyProperties.class)
public class V31ValkeyCacheAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RedisCacheConfiguration valkeyCacheConfiguration(V31ValkeyProperties properties,
			@Qualifier("valkeyValueSerializer") RedisSerializer<Object> valkeyValueSerializer) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
			.prefixCacheNameWith(properties.getKeyPrefix() + ":cache:")
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valkeyValueSerializer));
		configuration = applyTtl(configuration, properties.getCache().getDefaultTtl());
		return properties.getCache().isAllowNullValues() ? configuration : configuration.disableCachingNullValues();
	}

	@Bean
	public RedisCacheManagerBuilderCustomizer valkeyCacheTtlCustomizer(V31ValkeyProperties properties,
			RedisCacheConfiguration valkeyCacheConfiguration) {
		return (builder) -> properties.getCache()
			.getTtls()
			.forEach((cacheName, ttl) -> builder.withCacheConfiguration(cacheName,
					applyTtl(valkeyCacheConfiguration, ttl)));
	}

	@Bean
	@ConditionalOnMissingBean(CachingConfigurer.class)
	public CachingConfigurer valkeyCachingConfigurer(V31ValkeyProperties properties) {
		return new ValkeyCachingConfigurer(new ValkeyCacheErrorHandler(properties.getCache().isFailFast()));
	}

	private static RedisCacheConfiguration applyTtl(RedisCacheConfiguration configuration, Duration ttl) {
		return (ttl != null && !ttl.isNegative() && !ttl.isZero()) ? configuration.entryTtl(ttl) : configuration;
	}

}

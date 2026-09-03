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

import org.v31bank.data.valkey.cache.ValkeyCacheErrorHandler;
import org.v31bank.data.valkey.cache.ValkeyCachingConfigurer;

/**
 * {@link AutoConfiguration Auto-configuration} for caching through Valkey: the same JSON
 * serialization the template uses, every cache under this application's key prefix, and
 * every cache given an expiry.
 * <p>
 * Spring's default is no expiry at all, so a missed eviction is stale data that never
 * heals. {@code v31.data.valkey.cache.ttls} sets it per cache where ten minutes is wrong.
 * <p>
 * Takes effect only where the application switched caching on with
 * {@code @EnableCaching}.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@AutoConfiguration(before = CacheAutoConfiguration.class, after = V31ValkeyAutoConfiguration.class)
@ConditionalOnClass({ RedisCacheManager.class, RedisCacheManagerBuilderCustomizer.class })
@ConditionalOnBooleanProperty(name = "v31.data.valkey.cache.enabled", matchIfMissing = true)
@EnableConfigurationProperties(V31ValkeyProperties.class)
public class V31ValkeyCacheAutoConfiguration {

	/**
	 * The configuration every cache starts from. Spring Boot picks this up and builds the
	 * cache manager on it.
	 * @param properties the prefix, the default expiry, and whether misses are cached
	 * @param valkeyValueSerializer the serializer values are written with
	 * @return the default cache configuration
	 */
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

	/**
	 * Apply the expiry configured for individual caches, on top of the defaults above.
	 * @param properties the per-cache expiries
	 * @param valkeyCacheConfiguration the configuration to vary
	 * @return the customizer
	 */
	@Bean
	public RedisCacheManagerBuilderCustomizer valkeyCacheTtlCustomizer(V31ValkeyProperties properties,
			RedisCacheConfiguration valkeyCacheConfiguration) {
		return (builder) -> properties.getCache()
			.getTtls()
			.forEach((cacheName, ttl) -> builder.withCacheConfiguration(cacheName,
					applyTtl(valkeyCacheConfiguration, ttl)));
	}

	/**
	 * Keep an unreachable Valkey from failing every cached call. Backs off when the
	 * application supplies its own {@link CachingConfigurer}, since Spring accepts one.
	 * @param properties whether failures are allowed out
	 * @return the configurer carrying the error handler
	 */
	@Bean
	@ConditionalOnMissingBean(CachingConfigurer.class)
	public CachingConfigurer valkeyCachingConfigurer(V31ValkeyProperties properties) {
		return new ValkeyCachingConfigurer(new ValkeyCacheErrorHandler(properties.getCache().isFailFast()));
	}

	/**
	 * Set an expiry, leaving the configuration alone when the value is not positive — a
	 * zero or negative expiry is read as a deliberate request for entries that never
	 * expire rather than silently corrected.
	 * @param configuration the configuration to vary
	 * @param ttl how long an entry lives
	 * @return the configuration, with the expiry applied where there was one
	 */
	private static RedisCacheConfiguration applyTtl(RedisCacheConfiguration configuration, Duration ttl) {
		return (ttl != null && !ttl.isNegative() && !ttl.isZero()) ? configuration.entryTtl(ttl) : configuration;
	}

}

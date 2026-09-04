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

package org.v31bank.data.valkey;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import org.v31bank.core.Uuids;

/**
 * A lock held in Valkey, with a holder token and an expiry.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class ValkeyLock {

	/**
	 * Deletes the key only if it still carries the token the caller was given. Returns
	 * the number of keys deleted, so nothing is 0 and success is 1.
	 */
	private static final RedisScript<Long> RELEASE = new DefaultRedisScript<>("""
			if redis.call('get', KEYS[1]) == ARGV[1] then
			    return redis.call('del', KEYS[1])
			else
			    return 0
			end
			""", Long.class);

	/**
	 * Pushes the expiry out, again only if the key still carries the caller's token.
	 */
	private static final RedisScript<Long> EXTEND = new DefaultRedisScript<>("""
			if redis.call('get', KEYS[1]) == ARGV[1] then
			    return redis.call('pexpire', KEYS[1], ARGV[2])
			else
			    return 0
			end
			""", Long.class);

	private final StringRedisTemplate template;

	public ValkeyLock(StringRedisTemplate template) {
		this.template = Objects.requireNonNull(template, "template must not be null");
	}

	public Optional<String> acquire(String key, Duration lease) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(lease, "lease must not be null");
		if (lease.isNegative() || lease.isZero()) {
			throw new IllegalArgumentException("A lease must be positive, or the lock expires before it is used");
		}
		String token = Uuids.timeOrdered().toString();
		return Boolean.TRUE.equals(this.template.opsForValue().setIfAbsent(key, token, lease)) ? Optional.of(token)
				: Optional.empty();
	}

	public boolean release(String key, String token) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(token, "token must not be null");
		Long released = this.template.execute(RELEASE, List.of(key), token);
		return released != null && released > 0;
	}

	public boolean extend(String key, String token, Duration lease) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(token, "token must not be null");
		Objects.requireNonNull(lease, "lease must not be null");
		if (lease.isNegative() || lease.isZero()) {
			throw new IllegalArgumentException("A lease must be positive, or the lock expires before it is used");
		}
		Long extended = this.template.execute(EXTEND, List.of(key), token, Long.toString(lease.toMillis()));
		return extended != null && extended > 0;
	}

	public <T> Optional<T> runExclusively(String key, Duration lease, Supplier<T> action) {
		Objects.requireNonNull(action, "action must not be null");
		Optional<String> token = acquire(key, lease);
		if (token.isEmpty()) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(action.get());
		}
		finally {
			release(key, token.get());
		}
	}

}

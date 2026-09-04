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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * What happens when Valkey cannot be reached mid-call.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class ValkeyCacheErrorHandler implements CacheErrorHandler {

	private static final Log logger = LogFactory.getLog(ValkeyCacheErrorHandler.class);

	private final boolean failFast;

	public ValkeyCacheErrorHandler(boolean failFast) {
		this.failFast = failFast;
	}

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		degrade("read from", cache, exception);
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		degrade("write to", cache, exception);
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		logger.error(
				"Could not evict from cache '" + cache.getName() + "'; it may now be serving a value known to be stale",
				exception);
		throw exception;
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		logger.error("Could not clear cache '" + cache.getName() + "'; it may now be serving values known to be stale",
				exception);
		throw exception;
	}

	private void degrade(String operation, Cache cache, RuntimeException exception) {
		if (this.failFast) {
			throw exception;
		}
		logger.warn("Could not " + operation + " cache '" + cache.getName() + "', continuing without it: "
				+ exception.getMessage());
		if (logger.isDebugEnabled()) {
			logger.debug("Cache failure detail for '" + cache.getName() + "'", exception);
		}
	}

}

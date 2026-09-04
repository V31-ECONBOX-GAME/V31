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

package org.v31bank.grpc;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Puts an HTTP request's context into the platform.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HeaderPropagationFilter extends OncePerRequestFilter {

	private final List<String> propagated;

	public HeaderPropagationFilter(Collection<String> propagatedHeaders) {
		this.propagated = List.copyOf(propagatedHeaders);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		Map<String, String> values = RequestContext.newValues();
		for (String name : this.propagated) {
			RequestContext.put(values, name, request.getHeader(name));
		}
		try (RequestContext.Scope scope = RequestContext.attach(values)) {
			chain.doFilter(request, response);
		}
	}

}

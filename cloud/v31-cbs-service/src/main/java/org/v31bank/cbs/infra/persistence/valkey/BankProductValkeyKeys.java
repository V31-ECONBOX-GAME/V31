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

package org.v31bank.cbs.infra.persistence.valkey;

import java.util.UUID;

import org.springframework.stereotype.Component;

import org.v31bank.cbs.domain.constant.BankProductCategory;
import org.v31bank.cbs.domain.constant.BankProductStatus;
import org.v31bank.data.valkey.ValkeyKeys;

/**
 * Every key the bank product catalogue occupies, in one place.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@Component
public class BankProductValkeyKeys {

	private static final String SERVICE = "cbs";

	private static final String ENTITY = "product";

	private final ValkeyKeys keys;

	public BankProductValkeyKeys(ValkeyKeys keys) {
		this.keys = keys;
	}

	public String product(UUID id) {
		return this.keys.of(SERVICE, ENTITY, id.toString());
	}

	public String code(String code) {
		return this.keys.of(SERVICE, ENTITY, "code", code);
	}

	public String index() {
		return this.keys.of(SERVICE, ENTITY, "index");
	}

	public String categoryIndex(BankProductCategory category) {
		return this.keys.of(SERVICE, ENTITY, "index", "category", category.name());
	}

	public String statusIndex(BankProductStatus status) {
		return this.keys.of(SERVICE, ENTITY, "index", "status", status.name());
	}

	public String intersection(String token) {
		return this.keys.of(SERVICE, ENTITY, "index", "intersection", token);
	}

}

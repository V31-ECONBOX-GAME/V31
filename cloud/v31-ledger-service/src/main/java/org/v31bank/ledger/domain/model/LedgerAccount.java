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

package org.v31bank.ledger.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.v31bank.data.jpa.Audited;
import org.v31bank.ledger.domain.constant.LedgerAccountStatus;
import org.v31bank.ledger.domain.constant.LedgerAccountType;

@Entity
@Table(name = "ledger_account",
		uniqueConstraints = @UniqueConstraint(name = "uk_ledger_account_code", columnNames = "code"))
public class LedgerAccount extends Audited {

	public static final int CODE_MAX_LENGTH = 32;

	public static final int NAME_MAX_LENGTH = 100;

	@Column(name = "code", length = CODE_MAX_LENGTH, nullable = false)
	private String code;

	@Column(name = "name", length = NAME_MAX_LENGTH, nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", length = 20, nullable = false)
	private LedgerAccountType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private LedgerAccountStatus status = LedgerAccountStatus.ACTIVE;

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LedgerAccountType getType() {
		return this.type;
	}

	public void setType(LedgerAccountType type) {
		this.type = type;
	}

	public LedgerAccountStatus getStatus() {
		return this.status;
	}

	public void setStatus(LedgerAccountStatus status) {
		this.status = status;
	}

}

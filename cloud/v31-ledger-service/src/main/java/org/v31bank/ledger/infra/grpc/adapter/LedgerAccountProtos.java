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

package org.v31bank.ledger.infra.grpc.adapter;

import java.time.Instant;
import java.util.UUID;

import com.google.protobuf.Timestamp;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.v31bank.ledger.domain.constant.LedgerAccountStatus;
import org.v31bank.ledger.domain.constant.LedgerAccountType;
import org.v31bank.ledger.domain.model.LedgerAccount;

/**
 * Where the wire contract and the domain model meet.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public final class LedgerAccountProtos {

	private LedgerAccountProtos() {
	}

	public static org.v31bank.ledger.api.v1.LedgerAccount toProto(LedgerAccount account) {
		org.v31bank.ledger.api.v1.LedgerAccount.Builder builder = org.v31bank.ledger.api.v1.LedgerAccount.newBuilder()
			.setId(account.getId().toString())
			.setCode(account.getCode())
			.setName(account.getName())
			.setType(toProto(account.getType()))
			.setStatus(toProto(account.getStatus()));
		if (account.getCreatedDate() != null) {
			builder.setCreatedDate(toProto(account.getCreatedDate()));
		}
		if (account.getLastModifiedDate() != null) {
			builder.setLastModifiedDate(toProto(account.getLastModifiedDate()));
		}
		return builder.build();
	}

	public static org.v31bank.ledger.api.v1.LedgerAccountType toProto(LedgerAccountType type) {
		return switch (type) {
			case ASSET -> org.v31bank.ledger.api.v1.LedgerAccountType.LEDGER_ACCOUNT_TYPE_ASSET;
			case LIABILITY -> org.v31bank.ledger.api.v1.LedgerAccountType.LEDGER_ACCOUNT_TYPE_LIABILITY;
			case EQUITY -> org.v31bank.ledger.api.v1.LedgerAccountType.LEDGER_ACCOUNT_TYPE_EQUITY;
			case REVENUE -> org.v31bank.ledger.api.v1.LedgerAccountType.LEDGER_ACCOUNT_TYPE_REVENUE;
			case EXPENSE -> org.v31bank.ledger.api.v1.LedgerAccountType.LEDGER_ACCOUNT_TYPE_EXPENSE;
		};
	}

	public static org.v31bank.ledger.api.v1.LedgerAccountStatus toProto(LedgerAccountStatus status) {
		return switch (status) {
			case ACTIVE -> org.v31bank.ledger.api.v1.LedgerAccountStatus.LEDGER_ACCOUNT_STATUS_ACTIVE;
			case CLOSED -> org.v31bank.ledger.api.v1.LedgerAccountStatus.LEDGER_ACCOUNT_STATUS_CLOSED;
		};
	}

	public static LedgerAccountType fromProto(org.v31bank.ledger.api.v1.LedgerAccountType type) {
		return switch (type) {
			case LEDGER_ACCOUNT_TYPE_ASSET -> LedgerAccountType.ASSET;
			case LEDGER_ACCOUNT_TYPE_LIABILITY -> LedgerAccountType.LIABILITY;
			case LEDGER_ACCOUNT_TYPE_EQUITY -> LedgerAccountType.EQUITY;
			case LEDGER_ACCOUNT_TYPE_REVENUE -> LedgerAccountType.REVENUE;
			case LEDGER_ACCOUNT_TYPE_EXPENSE -> LedgerAccountType.EXPENSE;
			default -> null;
		};
	}

	public static LedgerAccountStatus fromProto(org.v31bank.ledger.api.v1.LedgerAccountStatus status) {
		return switch (status) {
			case LEDGER_ACCOUNT_STATUS_ACTIVE -> LedgerAccountStatus.ACTIVE;
			case LEDGER_ACCOUNT_STATUS_CLOSED -> LedgerAccountStatus.CLOSED;
			default -> null;
		};
	}

	public static UUID toUuid(String id) {
		try {
			return UUID.fromString(id);
		}
		catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'" + id + "' is not an identifier", ex);
		}
	}

	private static Timestamp toProto(Instant instant) {
		return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
	}

}

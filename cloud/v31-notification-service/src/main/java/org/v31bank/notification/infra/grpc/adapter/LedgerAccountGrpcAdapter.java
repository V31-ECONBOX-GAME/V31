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

package org.v31bank.notification.infra.grpc.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import org.v31bank.core.HttpResponse;
import org.v31bank.grpc.GrpcErrors;
import org.v31bank.ledger.api.v1.CreateLedgerAccountRequest;
import org.v31bank.ledger.api.v1.DeleteLedgerAccountRequest;
import org.v31bank.ledger.api.v1.GetLedgerAccountRequest;
import org.v31bank.ledger.api.v1.LedgerAccountServiceGrpc;
import org.v31bank.ledger.api.v1.LedgerAccountStatus;
import org.v31bank.ledger.api.v1.LedgerAccountType;
import org.v31bank.ledger.api.v1.ListLedgerAccountsRequest;
import org.v31bank.ledger.api.v1.ListLedgerAccountsResponse;
import org.v31bank.ledger.api.v1.UpdateLedgerAccountRequest;
import org.v31bank.notification.application.dto.LedgerAccountSummary;
import org.v31bank.notification.application.port.out.LedgerAccountPort;

/**
 * {@link LedgerAccountPort} adapter backed by a gRPC call to the ledger service.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
@Component
public class LedgerAccountGrpcAdapter implements LedgerAccountPort {

	private final LedgerAccountServiceGrpc.LedgerAccountServiceBlockingStub ledger;

	public LedgerAccountGrpcAdapter(LedgerAccountServiceGrpc.LedgerAccountServiceBlockingStub ledger) {
		this.ledger = ledger;
	}

	@Override
	public LedgerAccountSummary create(String code, String name, String type) {
		return toSummary(GrpcErrors.call(() -> this.ledger.createLedgerAccount(CreateLedgerAccountRequest.newBuilder()
			.setCode(nullToEmpty(code))
			.setName(nullToEmpty(name))
			.setType(toType(type))
			.build())).getLedgerAccount());
	}

	@Override
	public Optional<LedgerAccountSummary> findById(UUID id) {
		try {
			return Optional.of(toSummary(
					this.ledger.getLedgerAccount(GetLedgerAccountRequest.newBuilder().setId(id.toString()).build())
						.getLedgerAccount()));
		}
		catch (StatusRuntimeException ex) {
			if (ex.getStatus().getCode() == Status.Code.NOT_FOUND) {
				return Optional.empty();
			}
			throw GrpcErrors.asResponseStatusException(ex);
		}
	}

	@Override
	public HttpResponse<List<LedgerAccountSummary>> findPage(int pageNumber, int pageSize, String code) {
		ListLedgerAccountsRequest.Builder request = ListLedgerAccountsRequest.newBuilder()
			.setPageNumber(pageNumber)
			.setPageSize(pageSize);
		if (StringUtils.hasText(code)) {
			request.setCode(code);
		}
		ListLedgerAccountsResponse response = GrpcErrors.call(() -> this.ledger.listLedgerAccounts(request.build()));
		List<LedgerAccountSummary> records = response.getRecordsList()
			.stream()
			.map(LedgerAccountGrpcAdapter::toSummary)
			.toList();
		return HttpResponse.page(records, response.getTotal());
	}

	@Override
	public LedgerAccountSummary update(UUID id, String code, String name, String type, String status) {
		return toSummary(GrpcErrors.call(() -> this.ledger.updateLedgerAccount(UpdateLedgerAccountRequest.newBuilder()
			.setId(id.toString())
			.setCode(nullToEmpty(code))
			.setName(nullToEmpty(name))
			.setType(toType(type))
			.setStatus(toStatus(status))
			.build())).getLedgerAccount());
	}

	@Override
	public LedgerAccountSummary delete(UUID id) {
		return toSummary(GrpcErrors
			.call(() -> this.ledger
				.deleteLedgerAccount(DeleteLedgerAccountRequest.newBuilder().setId(id.toString()).build()))
			.getLedgerAccount());
	}

	private static LedgerAccountSummary toSummary(org.v31bank.ledger.api.v1.LedgerAccount account) {
		return new LedgerAccountSummary(UUID.fromString(account.getId()), account.getCode(), account.getName(),
				strip(account.getType().name(), "LEDGER_ACCOUNT_TYPE_"),
				strip(account.getStatus().name(), "LEDGER_ACCOUNT_STATUS_"), toInstant(account.getCreatedDate()),
				toInstant(account.getLastModifiedDate()));
	}

	private static LedgerAccountType toType(String type) {
		if (!StringUtils.hasText(type)) {
			return LedgerAccountType.LEDGER_ACCOUNT_TYPE_UNSPECIFIED;
		}
		LedgerAccountType value = LedgerAccountType.valueOf("LEDGER_ACCOUNT_TYPE_" + type);
		return (value != LedgerAccountType.UNRECOGNIZED) ? value : LedgerAccountType.LEDGER_ACCOUNT_TYPE_UNSPECIFIED;
	}

	private static LedgerAccountStatus toStatus(String status) {
		if (!StringUtils.hasText(status)) {
			return LedgerAccountStatus.LEDGER_ACCOUNT_STATUS_UNSPECIFIED;
		}
		LedgerAccountStatus value = LedgerAccountStatus.valueOf("LEDGER_ACCOUNT_STATUS_" + status);
		return (value != LedgerAccountStatus.UNRECOGNIZED) ? value
				: LedgerAccountStatus.LEDGER_ACCOUNT_STATUS_UNSPECIFIED;
	}

	private static String strip(String name, String prefix) {
		return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
	}

	private static Instant toInstant(Timestamp timestamp) {
		return (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0) ? null
				: Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
	}

	private static String nullToEmpty(String value) {
		return (value != null) ? value : "";
	}

}

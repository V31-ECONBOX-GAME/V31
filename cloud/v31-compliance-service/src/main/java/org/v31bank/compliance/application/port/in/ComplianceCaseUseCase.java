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

package org.v31bank.compliance.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.v31bank.compliance.application.dto.ComplianceCasePageQuery;
import org.v31bank.compliance.domain.constant.ComplianceCaseStatus;
import org.v31bank.compliance.domain.constant.ComplianceCaseType;
import org.v31bank.compliance.domain.model.ComplianceCase;
import org.v31bank.core.HttpResponse;

/**
 * Use cases for managing compliance cases.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public interface ComplianceCaseUseCase {

	HttpResponse<ComplianceCase> create(String caseNumber, UUID customerId, ComplianceCaseType type, String summary);

	Optional<ComplianceCase> get(UUID id);

	HttpResponse<List<ComplianceCase>> page(ComplianceCasePageQuery query);

	HttpResponse<ComplianceCase> update(UUID id, String caseNumber, UUID customerId, ComplianceCaseType type,
			ComplianceCaseStatus status, String summary);

	HttpResponse<ComplianceCase> delete(UUID id);

}

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

package org.v31bank.compliance.application.dto;

import java.util.UUID;

import org.v31bank.compliance.domain.constant.ComplianceCaseStatus;
import org.v31bank.compliance.domain.constant.ComplianceCaseType;
import org.v31bank.core.PageQuery;

/**
 * Paginated compliance case query with optional filters, using one-based page numbering.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public class ComplianceCasePageQuery extends PageQuery {

	private String caseNumber;

	private UUID customerId;

	private ComplianceCaseType type;

	private ComplianceCaseStatus status;

	public String getCaseNumber() {
		return this.caseNumber;
	}

	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}

	public UUID getCustomerId() {
		return this.customerId;
	}

	public void setCustomerId(UUID customerId) {
		this.customerId = customerId;
	}

	public ComplianceCaseType getType() {
		return this.type;
	}

	public void setType(ComplianceCaseType type) {
		this.type = type;
	}

	public ComplianceCaseStatus getStatus() {
		return this.status;
	}

	public void setStatus(ComplianceCaseStatus status) {
		this.status = status;
	}

}

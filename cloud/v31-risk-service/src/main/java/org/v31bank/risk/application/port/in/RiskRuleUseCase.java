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

package org.v31bank.risk.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.v31bank.core.HttpResponse;
import org.v31bank.risk.application.dto.RiskRulePageQuery;
import org.v31bank.risk.domain.constant.RiskRuleStatus;
import org.v31bank.risk.domain.constant.RiskSeverity;
import org.v31bank.risk.domain.model.RiskRule;

/**
 * Use cases for managing risk rules.
 *
 * @author Xander Wang
 * @since 0.2.0
 */
public interface RiskRuleUseCase {

	HttpResponse<RiskRule> create(String code, String name, RiskSeverity severity);

	Optional<RiskRule> get(UUID id);

	HttpResponse<List<RiskRule>> page(RiskRulePageQuery query);

	HttpResponse<RiskRule> update(UUID id, String code, String name, RiskSeverity severity, RiskRuleStatus status);

	HttpResponse<RiskRule> delete(UUID id);

}

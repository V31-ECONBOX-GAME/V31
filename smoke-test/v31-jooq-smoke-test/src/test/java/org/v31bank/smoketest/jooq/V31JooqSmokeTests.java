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

package org.v31bank.smoketest.jooq;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.v31bank.jooq.audit.AuditRecordListener;
import org.v31bank.jooq.audit.AuditorSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class V31JooqSmokeTests {

	@Autowired
	private DSLContext dslContext;

	@Autowired
	private AuditorSupplier auditorSupplier;

	@Test
	void theStarterAloneContributesTheAuditor() {
		assertThat(this.auditorSupplier.currentAuditor()).contains("smoke-test");
	}

	@Test
	void theAuditListenerIsAttachedToTheRealDslContext() {
		assertThat(this.dslContext.configuration().recordListenerProviders()).isNotEmpty();
		assertThat(this.dslContext.configuration().recordListenerProviders()[0].provide())
			.isInstanceOf(AuditRecordListener.class);
	}

}

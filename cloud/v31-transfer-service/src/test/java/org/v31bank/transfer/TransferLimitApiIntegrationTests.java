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

package org.v31bank.transfer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;

import org.v31bank.transfer.infra.persistence.jpa.JpaTransferLimitRepository;
import org.v31bank.transfer.presentation.controller.v1.TransferLimitController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TransferLimitController}.
 *
 * @author Xander Wang
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferLimitApiIntegrationTests {

	private static final String PATH = "/api/v1/transfer-limits";

	private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT = new ParameterizedTypeReference<>() {
	};

	private static final UUID ABSENT_ID = UUID.fromString("00000000-0000-7000-8000-000000000000");

	@LocalServerPort
	private int port;

	@Autowired
	private JpaTransferLimitRepository records;

	private RestTestClient client;

	@BeforeEach
	void setUp() {
		this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + this.port).build();
		this.records.deleteAll();
	}

	@Test
	void createsARecordAndSaysWhereItWent() {
		Map<String, Object> body = this.client.post()
			.uri(PATH)
			.body(request("LIM-0001", "First"))
			.exchange()
			.expectStatus()
			.isCreated()
			.expectBody(JSON_OBJECT)
			.returnResult()
			.getResponseBody();
		assertThat(body).containsEntry("code", 200);
		assertThat(data(body)).containsEntry("code", "LIM-0001")
			.containsEntry("name", "First")
			.containsEntry("status", "ACTIVE");
	}

	@Test
	void issuesATimeOrderedIdentifierAndStampsTheAuditFields() {
		Map<String, Object> created = create("LIM-0001", "First");
		assertThat(UUID.fromString((String) created.get("id")).version()).isEqualTo(7);
		assertThat(created.get("createdDate")).asString().endsWith("Z");
		assertThat(created).containsEntry("createdDate", created.get("lastModifiedDate"));
	}

	@Test
	void findsARecordItJustCreated() {
		String id = (String) create("LIM-0001", "First").get("id");
		assertThat(data(get(PATH + "/" + id))).containsEntry("code", "LIM-0001");
	}

	@Test
	void refusesADuplicate() {
		create("LIM-0001", "First");
		Map<String, Object> body = this.client.post()
			.uri(PATH)
			.body(request("LIM-0001", "Duplicate"))
			.exchange()
			.expectStatus()
			.isEqualTo(org.springframework.http.HttpStatus.CONFLICT)
			.expectBody(JSON_OBJECT)
			.returnResult()
			.getResponseBody();
		assertThat(body).containsEntry("code", 409);
		assertThat(this.records.count()).isEqualTo(1);
	}

	@Test
	void reportsAnAbsentRecordAsNotFound() {
		Map<String, Object> body = this.client.get()
			.uri(PATH + "/" + ABSENT_ID)
			.exchange()
			.expectStatus()
			.isNotFound()
			.expectBody(JSON_OBJECT)
			.returnResult()
			.getResponseBody();
		assertThat(body).containsEntry("code", 404);
	}

	@Test
	void pagesNewestFirstAndCountsThemAll() {
		createMany(25);
		Map<String, Object> page = get(PATH + "?pageNumber=1&pageSize=10");
		assertThat(page).containsEntry("total", 25);
		assertThat(records(page)).hasSize(10);
	}

	@Test
	void reportsTheLastPageAsTheLastOne() {
		createMany(25);
		Map<String, Object> page = get(PATH + "?pageNumber=3&pageSize=10");
		assertThat(records(page)).hasSize(5);
	}

	@Test
	void doesNotRepeatOrDropARecordAcrossPages() {
		createMany(25);
		Set<Object> seen = new HashSet<>();
		for (int page = 1; page <= 3; page++) {
			records(get(PATH + "?pageNumber=" + page + "&pageSize=10")).forEach((record) -> seen.add(record.get("id")));
		}
		assertThat(seen).hasSize(25);
	}

	@Test
	void updatesARecord() {
		String id = (String) create("LIM-0001", "First").get("id");
		this.client.put().uri(PATH + "/" + id).body(update("LIM-0001", "Renamed")).exchange().expectStatus().isOk();
		assertThat(data(get(PATH + "/" + id))).containsEntry("name", "Renamed").containsEntry("status", "SUSPENDED");
	}

	@Test
	void reportsAnUpdateToAnAbsentRecordAsNotFound() {
		this.client.put()
			.uri(PATH + "/" + ABSENT_ID)
			.body(update("LIM-0001", "Renamed"))
			.exchange()
			.expectStatus()
			.isNotFound();
	}

	@Test
	void deletesARecord() {
		String id = (String) create("LIM-0001", "First").get("id");
		this.client.delete().uri(PATH + "/" + id).exchange().expectStatus().isOk();
		this.client.get().uri(PATH + "/" + id).exchange().expectStatus().isNotFound();
		assertThat(this.records.count()).isZero();
	}

	@Test
	void reportsADeleteOfAnAbsentRecordAsNotFound() {
		this.client.delete().uri(PATH + "/" + ABSENT_ID).exchange().expectStatus().isNotFound();
	}

	@Test
	void refusesAnIdentifierThatIsNotOne() {
		this.client.get().uri(PATH + "/not-a-uuid").exchange().expectStatus().isBadRequest();
	}

	private static Map<String, Object> request(String code, String name) {
		return Map.of("code", code, "name", name, "dailyMax", "1000.500000000000000000");
	}

	private static Map<String, Object> update(String code, String name) {
		return Map.of("code", code, "name", name, "dailyMax", "1000.500000000000000000", "status", "SUSPENDED");
	}

	private Map<String, Object> create(String code, String name) {
		return data(this.client.post()
			.uri(PATH)
			.body(request(code, name))
			.exchange()
			.expectStatus()
			.isCreated()
			.expectBody(JSON_OBJECT)
			.returnResult()
			.getResponseBody());
	}

	private void createMany(int count) {
		for (int i = 1; i <= count; i++) {
			create("LIM-%04d".formatted(i), "Record " + i);
		}
	}

	private Map<String, Object> get(String uri) {
		return this.client.get()
			.uri(uri)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(JSON_OBJECT)
			.returnResult()
			.getResponseBody();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> data(Map<String, ?> envelope) {
		return (Map<String, Object>) envelope.get("data");
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> records(Map<String, ?> envelope) {
		return (List<Map<String, Object>>) envelope.get("data");
	}

	@Test
	void rejectsAnEmptyBody() {
		Map<String, Object> body = this.client.post()
			.uri(PATH)
			.body(Map.of())
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody(JSON_OBJECT)
			.returnResult()
			.getResponseBody();
		assertThat(body).containsEntry("code", 400);
	}

	@Test
	void rejectsAValueLongerThanTheColumnHolds() {
		Map<String, Object> body = this.client.post()
			.uri(PATH)
			.body(Map.of("code", "LIM-1", "name", TOO_LONG, "dailyMax", "1000.5"))
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody(JSON_OBJECT)
			.returnResult()
			.getResponseBody();
		assertThat(body).containsEntry("code", 400);
	}

	@Test
	void reportsAFrameworkRejectionInTheSameEnvelope() {
		Map<String, Object> body = this.client.post()
			.uri(PATH)
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
			.body("not json at all")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody(JSON_OBJECT)
			.returnResult()
			.getResponseBody();
		assertThat(body).containsEntry("code", 400)
			.doesNotContainKeys("succeeded", "violations", "timestamp", "traceId");
	}

	private static final String TOO_LONG = "x".repeat(100 + 1);

}

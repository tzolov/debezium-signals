/*
 * Copyright 2023-2023 the original author or authors.
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

package org.spring.boot.extension.autoconfigure.debezium.signals;

import org.junit.jupiter.api.Test;
import org.spring.boot.extension.autoconfigure.debezium.signals.DebeziumSignalsAutoConfiguration.DebeziumContextHolder;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DebeziumSignalsAutoConfiguration}.
 *
 * @author Christian Tzolov
 */
public class DebeziumSignalsAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(DebeziumSignalsAutoConfiguration.class));

	@Test
	void neitherNotificationNorSignalProperty() {
		this.contextRunner.withPropertyValues("debezium.properties.connector.class=Dummy")
				.run((context) -> {
					assertThat(DebeziumContextHolder.applicationContext()).isNull();
				});
	}

	@Test
	void withSpringNotificationChannel() {
		this.contextRunner
				.withPropertyValues("debezium.properties.notification.enabled.channels=SpringNotificationChannel")
				.run((context) -> {
					assertThat(DebeziumContextHolder.applicationContext()).isNotNull();
				});
	}

	@Test
	void withSpringSignalChannelReader() {
		this.contextRunner
				.withPropertyValues("debezium.properties.signal.enabled.channels=source,SpringSignalChannelReader")
				.run((context) -> {
					assertThat(DebeziumContextHolder.applicationContext()).isNotNull();
				});
	}

	@Test
	void withMetadataStoreOffsetBackingStore() {
		this.contextRunner
				.withPropertyValues(
						"debezium.properties.offset.storage=org.spring.boot.extension.autoconfigure.debezium.signals.MetadataStoreOffsetBackingStore")
				.run((context) -> {
					assertThat(DebeziumContextHolder.applicationContext()).isNotNull();
				});
	}

	@Test
	void withMetadataStoreOffsetBackingStoreButNoMetaStoreClass() {
		this.contextRunner
				.withClassLoader(new FilteredClassLoader("org.springframework.integration.metadata"))
				.withPropertyValues(
						"debezium.properties.offset.storage=org.spring.boot.extension.autoconfigure.debezium.signals.MetadataStoreOffsetBackingStore")
				.run((context) -> {
					assertThat(DebeziumContextHolder.applicationContext()).isNull();
				});
	}
}

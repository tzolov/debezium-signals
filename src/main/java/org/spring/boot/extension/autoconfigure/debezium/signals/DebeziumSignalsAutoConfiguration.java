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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.integration.metadata.MetadataStore;

/**
 *
 * @author Christian Tzolov
 */
@AutoConfiguration(beforeName = "org.springframework.cloud.fn.common.debezium.DebeziumEngineBuilderAutoConfiguration")
@Conditional(DebeziumSignalsAutoConfiguration.OnDebeziumSignalOrNotificationCondition.class)
public class DebeziumSignalsAutoConfiguration {

	@Bean
	public DebeziumContextHolder debeziumSignalsContext() {
		return new DebeziumContextHolder();
	}

	/**
	 * Determine if Debezium's signals contains 'SpringSignalChannelReader' or notifications contains
	 * 'SpringNotificationChannel'.
	 */
	@Order(Ordered.LOWEST_PRECEDENCE)
	static class OnDebeziumSignalOrNotificationCondition extends AnyNestedCondition {

		OnDebeziumSignalOrNotificationCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnExpression("'${debezium.properties.notification.enabled.channels}'.contains('SpringNotificationChannel')")
		static class HasSpringNotificationChannelNotification {

		}

		@ConditionalOnExpression("'${debezium.properties.signal.enabled.channels}'.contains('SpringSignalChannelReader')")
		static class HasSpringSignalChannelReaderChannel {

		}

		@ConditionalOnExpression("'${debezium.properties.offset.storage}'.equalsIgnoreCase('org.spring.boot.extension.autoconfigure.debezium.signals.MetadataStoreOffsetBackingStore')")
		@ConditionalOnClass(MetadataStore.class)
		static class HasMetadataStore {

		}
	}

}

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

import io.debezium.config.CommonConnectorConfig;
import io.debezium.function.BlockingConsumer;
import io.debezium.pipeline.notification.Notification;
import io.debezium.pipeline.notification.channels.ConnectChannel;
import io.debezium.pipeline.notification.channels.NotificationChannel;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Offsets;
import io.debezium.pipeline.spi.Partition;
import io.debezium.schema.SchemaFactory;
import org.apache.kafka.connect.source.SourceRecord;
import org.spring.boot.extension.autoconfigure.debezium.signals.DebeziumSignalsAutoConfiguration.DebeziumContextHolder;

import org.springframework.context.ApplicationContext;

/**
 *
 * @author Christian Tzolov
 */
public class SpringNotificationChannel implements NotificationChannel, ConnectChannel {

	private ApplicationContext context;

	@Override
	public void init(CommonConnectorConfig config) {
		this.context = DebeziumContextHolder.applicationContext();
	}

	@Override
	public String name() {
		return "SpringNotificationChannel";
	}

	@Override
	public void send(Notification notification) {
		this.context.publishEvent(notification);
	}

	@Override
	public void close() {
	}

	@Override
	public void initConnectChannel(SchemaFactory schemaFactory, BlockingConsumer<SourceRecord> consumer) {
	}

	@Override
	public <P extends Partition, O extends OffsetContext> void send(Notification notification, Offsets<P, O> offsets) {
		this.context.publishEvent(notification);
	}

}

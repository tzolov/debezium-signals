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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.pipeline.signal.SignalRecord;
import io.debezium.pipeline.signal.channels.SignalChannelReader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author Christian Tzolov
 */
public class SpringSignalChannelReader implements SignalChannelReader {

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private Lock writeLock = lock.writeLock();
	private Lock readLock = lock.readLock();

	private List<SignalRecord> signals = new ArrayList<>();

	@Override
	public String name() {
		return "SpringSignalChannelReader";
	}

	@Override
	public void init(CommonConnectorConfig connectorConfig) {
		ApplicationContext context = DebeziumContextHolder.applicationContext();
		((ConfigurableApplicationContext) context)
				.addApplicationListener(new ApplicationListener<PayloadApplicationEvent<SignalRecord>>() {
					@Override
					public void onApplicationEvent(PayloadApplicationEvent<SignalRecord> event) {
						try {
							writeLock.lock();
							SignalRecord signalRecord = event.getPayload();
							signals.add(signalRecord);
							System.out.println("ADD SIGNAL: " + signalRecord);
						}
						finally {
							writeLock.unlock();
						}
					}
				});
	}

	@Override
	public List<SignalRecord> read() {
		try {
			readLock.lock();
			List<SignalRecord> copy = new ArrayList<>(signals);
			signals.clear();

			if (!CollectionUtils.isEmpty(copy)) {
				System.out.println("READ SIGNALS: " + copy.toString());
			}
			return copy;
		}
		finally {
			readLock.unlock();
		}
	}

	@Override
	public void close() {
	}

}

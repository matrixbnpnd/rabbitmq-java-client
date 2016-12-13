// Copyright (c) 2007-Present Pivotal Software, Inc.  All rights reserved.
//
// This software, the RabbitMQ Java client library, is triple-licensed under the
// Mozilla Public License 1.1 ("MPL"), the GNU General Public License version 2
// ("GPL") and the Apache License version 2 ("ASL"). For the MPL, please see
// LICENSE-MPL-RabbitMQ. For the GPL, please see LICENSE-GPL2.  For the ASL,
// please see LICENSE-APACHE2.
//
// This software is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
// either express or implied. See the LICENSE file for specific language governing
// rights and limitations of this software.
//
// If you have any questions regarding licensing, please contact us at
// info@rabbitmq.com.

package com.rabbitmq.examples.perf;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.*;

public class MulticastParams {
    private long confirm = -1;
    private long latencyLimitation = 0;
    private int consumerCount = 1;
    private int producerCount = 1;
    private int consumerTxSize = 0;
    private int producerTxSize = 0;
    private int channelPrefetch = 0;
    private int consumerPrefetch = 0;
    private int minMsgSize = 0;

    private int timeLimit = 0;
    private float producerRateLimit = 0;
    private float consumerRateLimit = 0;
    private int producerMsgCount = 0;
    private int consumerMsgCount = 0;

    private String exchangeName = "direct";
    private String exchangeType = "direct";
    private String queueName = "";
    private String routingKey = null;
    private String headerMatchMode = null;
    private boolean randomRoutingKey = false;

    private List<?> flags = new ArrayList<Object>();

    private int multiAckEvery = 0;
    private boolean autoAck = true;
    private boolean autoDelete = false;
    private boolean explicitAck = true;

    private boolean predeclared;
    private Map<String, Object> headerSettings = null;

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public void setHeaderMatchMode(String headerMatchMode) {
        this.headerMatchMode = headerMatchMode;
    }

    public void setRandomRoutingKey(boolean randomRoutingKey) {
        this.randomRoutingKey = randomRoutingKey;
    }

    public void setProducerRateLimit(float producerRateLimit) {
        this.producerRateLimit = producerRateLimit;
    }

    public void setProducerCount(int producerCount) {
        this.producerCount = producerCount;
    }

    public void setConsumerRateLimit(float consumerRateLimit) {
        this.consumerRateLimit = consumerRateLimit;
    }

    public void setConsumerCount(int consumerCount) {
        this.consumerCount = consumerCount;
    }

    public void setProducerTxSize(int producerTxSize) {
        this.producerTxSize = producerTxSize;
    }

    public void setConsumerTxSize(int consumerTxSize) {
        this.consumerTxSize = consumerTxSize;
    }

    public void setConfirm(long confirm) {
        this.confirm = confirm;
    }

    public void setLatencyLimitation(long latencyLimitation) { this.latencyLimitation = latencyLimitation; }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }

    public void setExplicitAck(boolean explicitAck) { 
        this.explicitAck = explicitAck; 
    }

    public void setMultiAckEvery(int multiAckEvery) {
        this.multiAckEvery = multiAckEvery;
    }

    public void setChannelPrefetch(int channelPrefetch) {
        this.channelPrefetch = channelPrefetch;
    }

    public void setConsumerPrefetch(int consumerPrefetch) {
        this.consumerPrefetch = consumerPrefetch;
    }

    public void setMinMsgSize(int minMsgSize) {
        this.minMsgSize = minMsgSize;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public void setProducerMsgCount(int producerMsgCount) {
        this.producerMsgCount = producerMsgCount;
    }

    public void setConsumerMsgCount(int consumerMsgCount) {
        this.consumerMsgCount = consumerMsgCount;
    }

    public void setMsgCount(int msgCount) {
        setProducerMsgCount(msgCount);
        setConsumerMsgCount(msgCount);
    }

    public void setFlags(List<?> flags) {
        this.flags = flags;
    }

    public void setHeaderKeyPairs(List<?> headerKeyPairs) {
        this.headerSettings = new HashMap<String, Object>();
        headerSettings.put("x-match", headerMatchMode);
        if (!headerKeyPairs.isEmpty()) {
            Object headerKeyPair = headerKeyPairs.get(0);
            for (String keyPair : headerKeyPair.toString().split(",")) {
                String[] s = keyPair.split("=");
                System.out.println("s0" + s[0] + "s1" + s[1]);
                headerSettings.put(s[0], s[1]);
            }
        }
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public void setPredeclared(boolean predeclared) {
        this.predeclared = predeclared;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public int getProducerCount() {
        return producerCount;
    }

    public int getMinMsgSize() {
        return minMsgSize;
    }

    public long getLatencyLimitation() { return latencyLimitation; }

    public String getRoutingKey() {
        return routingKey;
    }

    public boolean getRandomRoutingKey() {
        return randomRoutingKey;
    }

    public Producer createProducer(Connection connection, Stats stats, String id) throws IOException {
        Channel channel = connection.createChannel();
        if (producerTxSize > 0) channel.txSelect();
        if (confirm >= 0) channel.confirmSelect();
        if (!predeclared || !exchangeExists(connection, exchangeName)) {
            channel.exchangeDeclare(exchangeName, exchangeType);
        }

        AMQP.BasicProperties prop = new AMQP.BasicProperties(null, null, headerSettings,
                flags.contains("persistent") ? 2 : null, null, null, null, null, null, null, null, null, null, null);

        final Producer producer = new Producer(channel, exchangeName, id,
                                               randomRoutingKey, flags, producerTxSize,
                                               producerRateLimit, producerMsgCount,
                                               minMsgSize, timeLimit,
                                               confirm, stats, prop);
        channel.addReturnListener(producer);
        channel.addConfirmListener(producer);
        return producer;
    }

    public Consumer createConsumer(Connection connection, Stats stats, String id) throws IOException {
        Channel channel = connection.createChannel();
        if (consumerTxSize > 0) channel.txSelect();
        String qName = configureQueue(connection, id);
        if (consumerPrefetch > 0) channel.basicQos(consumerPrefetch);
        if (channelPrefetch > 0) channel.basicQos(channelPrefetch, true);
        return new Consumer(channel, id, qName,
                                         consumerTxSize, autoAck, explicitAck, multiAckEvery,
                                         stats, consumerRateLimit, consumerMsgCount, timeLimit);
    }

    public boolean shouldConfigureQueue() {
        return consumerCount == 0 && !queueName.equals("");
    }

    public String configureQueue(Connection connection, String id) throws IOException {
        Channel channel = connection.createChannel();
        if (!predeclared || !exchangeExists(connection, exchangeName)) {
            channel.exchangeDeclare(exchangeName, exchangeType);
        }
        String qName = queueName;
        if (!predeclared || !queueExists(connection, queueName)) {
            qName = channel.queueDeclare(queueName,
                                         flags.contains("persistent"),
                                         false, autoDelete,
                                         null).getQueue();
        }
        if (exchangeType.equals("headers")) {
            channel.queueBind(qName, exchangeName, "", headerSettings);
        } else {
            channel.queueBind(qName, exchangeName, id);
        }
        channel.abort();
        return qName;
    }

    private static boolean exchangeExists(Connection connection, final String exchangeName) throws IOException {
        return exists(connection, new Checker() {
            public void check(Channel ch) throws IOException {
                ch.exchangeDeclarePassive(exchangeName);
            }
        });
    }

    private static boolean queueExists(Connection connection, final String queueName) throws IOException {
        return queueName != null && exists(connection, new Checker() {
            public void check(Channel ch) throws IOException {
                ch.queueDeclarePassive(queueName);
            }
        });
    }

    private static interface Checker {
        public void check(Channel ch) throws IOException;
    }

    private static boolean exists(Connection connection, Checker checker) throws IOException {
        try {
            Channel ch = connection.createChannel();
            checker.check(ch);
            ch.abort();
            return true;
        }
        catch (IOException e) {
            ShutdownSignalException sse = (ShutdownSignalException) e.getCause();
            if (!sse.isHardError()) {
                AMQP.Channel.Close closeMethod = (AMQP.Channel.Close) sse.getReason();
                if (closeMethod.getReplyCode() == AMQP.NOT_FOUND) {
                    return false;
                }
            }
            throw e;
        }
    }
}

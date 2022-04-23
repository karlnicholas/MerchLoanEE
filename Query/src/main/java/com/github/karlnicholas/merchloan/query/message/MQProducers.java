package com.github.karlnicholas.merchloan.query.message;

import com.github.karlnicholas.merchloan.jms.ReplyWaiting;
import com.github.karlnicholas.merchloan.jms.config.MQQueueNames;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@Slf4j
public class MQProducers {
    private final MQQueueNames MQQueueNames;
    private final Channel querySendQueue;
    private final Map<String, ReplyWaiting> repliesWaiting;

    public MQProducers(Connection connection, MQQueueNames MQQueueNames) throws IOException {
        this.MQQueueNames = MQQueueNames;
        repliesWaiting = new TreeMap<>();
        querySendQueue = connection.createChannel();

        Channel queryReplyQueue = connection.createChannel();
        queryReplyQueue.exchangeDeclare(MQQueueNames.getExchange(), BuiltinExchangeType.DIRECT, false, true, null);

        queryReplyQueue.queueDeclare(MQQueueNames.getQueryReplyQueue(), false, true, true, null);
        queryReplyQueue.queueBind(MQQueueNames.getQueryReplyQueue(), MQQueueNames.getExchange(), MQQueueNames.getQueryReplyQueue());
        queryReplyQueue.basicConsume(MQQueueNames.getQueryReplyQueue(), true, this::handleReplies, consumerTag -> {});
    }

    public Object queryServiceRequest(UUID id) {
        log.debug("queryServiceRequest: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServicerequestQueryIdQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty() ) {
                    repliesWaiting.wait(ReplyWaiting.responseTimeout);
                    if ( System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                        log.error("queryServiceRequest reply timeout{}, ", id);
                        break;
                    }
                }
                return repliesWaiting.remove(responseKey).getReply();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryServiceRequest", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryAccount(UUID id) {
        log.debug("queryAccount: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountQueryAccountIdQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty() ) {
                    repliesWaiting.wait(ReplyWaiting.responseTimeout);
                    if ( System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                        log.error("queryServiceRequest reply timeout{}, ", id);
                        break;
                    }
                }
                return repliesWaiting.remove(responseKey).getReply();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryAccount", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public Object queryLoan(UUID id) {
        log.debug("queryLoan: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getAccountQueryLoanIdQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty() ) {
                    repliesWaiting.wait(ReplyWaiting.responseTimeout);
                    if ( System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                        log.error("queryServiceRequest reply timeout{}, ", id);
                        break;
                    }
                }
                return repliesWaiting.remove(responseKey).getReply();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryLoan", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryStatement(UUID id) {
        log.debug("queryStatement: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getStatementQueryStatementQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty() ) {
                    repliesWaiting.wait(ReplyWaiting.responseTimeout);
                    if ( System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                        log.error("queryServiceRequest reply timeout{}, ", id);
                        break;
                    }
                }
                return repliesWaiting.remove(responseKey).getReply();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryStatement", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryStatements(UUID id) {
        log.debug("queryStatements: {}", id);
        String responseKey = id.toString();
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getStatementQueryStatementsQueue(), props, SerializationUtils.serialize(id));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty() ) {
                    repliesWaiting.wait(ReplyWaiting.responseTimeout);
                    if ( System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                        log.error("queryServiceRequest reply timeout{}, ", id);
                        break;
                    }
                }
                return repliesWaiting.remove(responseKey).getReply();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryStatements", e);
            throw new RuntimeException(e);
        }
    }

    public Object queryCheckRequest() {
        log.debug("queryCheckRequest:");
        String responseKey = UUID.randomUUID().toString();
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(responseKey).replyTo(MQQueueNames.getQueryReplyQueue()).build();
        try {
            querySendQueue.basicPublish(MQQueueNames.getExchange(), MQQueueNames.getServiceRequestCheckRequestQueue(), props, SerializationUtils.serialize(new byte[0]));
            synchronized (repliesWaiting) {
                while ( repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty() ) {
                    repliesWaiting.wait(ReplyWaiting.responseTimeout);
                    if (  System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                        log.error("queryCheckRequest reply timeout");
                        break;
                    }
                }
                return repliesWaiting.remove(responseKey).getReply();
            }
        } catch (IOException | InterruptedException e) {
            log.error("queryCheckRequest", e);
            throw new RuntimeException(e);
        }
    }

    private void handleReplies(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.get(corrId).setReply(SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }

}

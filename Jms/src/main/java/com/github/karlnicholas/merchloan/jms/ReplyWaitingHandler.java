package com.github.karlnicholas.merchloan.jms;

import com.rabbitmq.client.Delivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.SerializationUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ReplyWaitingHandler {
    public static final int RESPONSE_TIMEOUT = 3000;
    public static final long TIMEOUT_MAX = 9_000_000_000L;
    private final ConcurrentMap<String, ReplyWaiting> repliesWaiting;

    public ReplyWaitingHandler() {
        repliesWaiting = new ConcurrentHashMap<>();
    }

    public void put(String responseKey) {
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nanoTime(System.nanoTime()).reply(null).build());
    }

    public Object getReply(String responseKey) throws InterruptedException {
        synchronized (repliesWaiting) {
            while (repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty()) {
                repliesWaiting.wait(RESPONSE_TIMEOUT);
                if (System.nanoTime() - repliesWaiting.get(responseKey).getNanoTime() > TIMEOUT_MAX) {
                    log.error("getReply timeout");
                    break;
                }
            }
        }
        return repliesWaiting.remove(responseKey).getReply();
    }

    public void handleReplies(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.get(corrId).setReply(SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }
}

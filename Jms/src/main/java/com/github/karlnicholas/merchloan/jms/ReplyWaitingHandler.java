package com.github.karlnicholas.merchloan.jms;

import com.rabbitmq.client.Delivery;
import org.springframework.util.SerializationUtils;

import java.util.Map;
import java.util.TreeMap;

public class ReplyWaitingHandler {
    private final Map<String, ReplyWaiting> repliesWaiting;

    public ReplyWaitingHandler() {
        repliesWaiting = new TreeMap<>();
    }

    public void put(String responseKey) {
        repliesWaiting.put(responseKey, ReplyWaiting.builder().nonoTime(System.nanoTime()).reply(null).build());
    }

    public Object getReply(String responseKey) throws InterruptedException {
        synchronized (repliesWaiting) {
            while (repliesWaiting.containsKey(responseKey) && repliesWaiting.get(responseKey).checkReply().isEmpty()) {
                repliesWaiting.wait(ReplyWaiting.responseTimeout);
                if (System.nanoTime() - repliesWaiting.get(responseKey).getNonoTime() > ReplyWaiting.timeoutMax) {
                    throw new IllegalStateException("getReply timeout");
                }
            }
            return repliesWaiting.remove(responseKey).getReply();
        }
    }

    public synchronized void handleReplies(String consumerTag, Delivery delivery) {
        synchronized (repliesWaiting) {
            String corrId = delivery.getProperties().getCorrelationId();
            repliesWaiting.get(corrId).setReply(SerializationUtils.deserialize(delivery.getBody()));
            repliesWaiting.notifyAll();
        }
    }
}

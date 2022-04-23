package com.github.karlnicholas.merchloan.jms;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class ReplyWaiting {
    public static final int responseTimeout = 3000;
    public static final long timeoutMax = 9_000_000_000L;
    private Object reply;
    private long nonoTime;
    public Optional<Object> checkReply() {
        return Optional.ofNullable(reply);
    }
}

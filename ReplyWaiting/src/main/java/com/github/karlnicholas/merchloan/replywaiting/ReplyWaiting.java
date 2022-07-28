package com.github.karlnicholas.merchloan.replywaiting;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
public class ReplyWaiting {
    private Object reply;
    private long nanoTime;
    public Optional<Object> checkReply() {
        return Optional.ofNullable(reply);
    }
}

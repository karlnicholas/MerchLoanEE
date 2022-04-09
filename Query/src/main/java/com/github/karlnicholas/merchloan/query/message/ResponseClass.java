package com.github.karlnicholas.merchloan.query.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseClass {
    private Object response;
    private String type;
    private String thread;
}

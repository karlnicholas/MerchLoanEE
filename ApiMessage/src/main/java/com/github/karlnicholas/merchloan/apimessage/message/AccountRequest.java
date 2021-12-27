package com.github.karlnicholas.merchloan.apimessage.message;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountRequest implements ServiceRequestMessage {
    private String customer;
}

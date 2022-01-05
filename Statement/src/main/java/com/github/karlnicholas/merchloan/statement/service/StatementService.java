package com.github.karlnicholas.merchloan.statement.service;

import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.statement.repository.StatementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StatementService {
    private final RabbitMqSender rabbitMqSender;
    private final StatementRepository statementRepository;

    public StatementService(RabbitMqSender rabbitMqSender, StatementRepository statementRepository) {
        this.rabbitMqSender = rabbitMqSender;
        this.statementRepository = statementRepository;
    }
}

package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.AccountComponent;
import com.github.karlnicholas.merchloan.client.component.LoanComponent;
import com.github.karlnicholas.merchloan.client.component.LoanStatusComponent;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import org.springframework.context.ApplicationListener;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class LoanCycle implements ApplicationListener<BusinessDateEvent> {
    enum LOAN_STATES {OPEN, CLOSED}
    private LoanDto loanDto;
    private LoanStateHandler loanStateHandler;
    private LocalDate eventDate;
    private String clientName;

    public LoanCycle(AccountComponent accountComponent, LoanComponent loanComponent, LoanStatusComponent loanStatusComponent, RestTemplate restTemplate, String clientName) {
        this.clientName = clientName;
        eventDate = LocalDate.now();
        loanStateHandler = new LoanStateNewHandler(accountComponent, loanComponent, loanStatusComponent);
    }
    @Override
    public void onApplicationEvent(BusinessDateEvent event) {
        System.out.println("Received spring custom event - " + event.getMessage());
        if ( event.getMessage().isEqual(eventDate)) {
            Optional<LoanDto> loanStatusOpt = loanStateHandler.progressState(clientName, new BigDecimal(10000.00), "FUNDING", "PAYMENT", "CLOSE");
        }
    }
}

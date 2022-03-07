package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.client.component.*;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import org.springframework.context.ApplicationListener;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class LoanCycle implements ApplicationListener<BusinessDateEvent> {
    enum LOAN_STATES {NEW, OPEN, CLOSED}

    private final LoanStatusComponent loanStatusComponent;
    private final RequestStatusComponent requestStatusComponent;
    private final CloseComponent closeComponent;
    private final RestTemplate restTemplate;
    private LoanStateHandler loanStateHandler;
    private LocalDate eventDate;
    private LOAN_STATES loanState;
    private Object[] args;

    public LoanCycle(AccountComponent accountComponent, LoanComponent loanComponent, CloseComponent closeComponent, RequestStatusComponent requestStatusComponent, LoanStatusComponent loanStatusComponent, RestTemplate restTemplate, String clientName) {
        this.requestStatusComponent = requestStatusComponent;
        this.closeComponent = closeComponent;
        this.loanStatusComponent = loanStatusComponent;
        this.restTemplate = restTemplate;
        eventDate = LocalDate.now();
        loanStateHandler = new LoanStateNewHandler(accountComponent, loanComponent, loanStatusComponent, this.requestStatusComponent);
        loanState = LOAN_STATES.NEW;
        args = new Object[]{clientName, new BigDecimal(10000.00), "FUNDING", "PAYMENT", "CLOSE"};
    }

    @Override
    public void onApplicationEvent(BusinessDateEvent event) {
        System.out.println("Received spring custom event - " + event.getMessage());
        if (event.getMessage().isEqual(eventDate)) {
            Optional<LoanDto> loanStatusOpt = loanStateHandler.progressState(args);
            if (loanStatusOpt.isPresent()) {
                switch (loanState) {
                    case NEW:
                        loanStateHandler = new LoanStateOpenHandler(loanStatusComponent, restTemplate);
                        eventDate = loanStatusOpt.get().getStartDate().plusMonths(1);
                        loanState = LOAN_STATES.OPEN;
                        break;
                    case OPEN:
                        eventDate = loanStatusOpt.get().getLastStatementDate().plusMonths(1);
                        if (eventDate.isEqual(loanStatusOpt.get().getStartDate().plusYears(1))) {
                            loanStateHandler = new LoanStateCloseHandler(closeComponent, restTemplate);
                            loanState = LOAN_STATES.CLOSED;
                        }
                        break;
                }
            }
        }
    }
}

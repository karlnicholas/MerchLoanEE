package com.github.karlnicholas.merchloan.client.process;

public interface LoanProcessHandler {
    boolean progressState(LoanData loanData);
}

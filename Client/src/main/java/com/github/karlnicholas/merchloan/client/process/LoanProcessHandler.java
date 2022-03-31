package com.github.karlnicholas.merchloan.client.process;

import java.util.concurrent.ExecutionException;

public interface LoanProcessHandler {
    boolean progressState(LoanData loanData) throws ExecutionException, InterruptedException;
}

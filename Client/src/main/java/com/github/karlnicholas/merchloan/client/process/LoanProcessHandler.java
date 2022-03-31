package com.github.karlnicholas.merchloan.client.process;

import org.apache.http.HttpException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface LoanProcessHandler {
    boolean progressState(LoanData loanData) throws ExecutionException, InterruptedException, HttpException, IOException;
}

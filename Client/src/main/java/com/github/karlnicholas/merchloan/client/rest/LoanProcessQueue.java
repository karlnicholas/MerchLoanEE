package com.github.karlnicholas.merchloan.client.rest;

import com.github.karlnicholas.merchloan.client.process.LoanData;
import com.github.karlnicholas.merchloan.client.process.LoanProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class LoanProcessQueue {
    private final ExecutorService executorService;
    private final List<Future<?>> tasks;

    public LoanProcessQueue() {
        executorService = Executors.newFixedThreadPool(200);
        tasks = new ArrayList<>();
    }

    public Future<Boolean> process(LoanProcessHandler loanProcessHandler, LoanData loanData) {
        Future<Boolean> task = executorService.submit(() -> loanProcessHandler.progressState(loanData));
        tasks.add(task);
        return task;
    }

    public boolean checkWorking() {
        Iterator<Future<?>> tit = tasks.iterator();
        boolean working = false;
        while (tit.hasNext()) {
            Future<?> task = tit.next();
            boolean done = task.isDone();
            if (done) tit.remove();
            else working = true;
        }
        return working;
    }
    public ExecutorService getExecutorService() {
        return executorService;
    }
}

package com.github.karlnicholas.merchloan.client.rest;

import com.github.karlnicholas.merchloan.client.process.LoanData;
import com.github.karlnicholas.merchloan.client.process.LoanProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class LoanProcessQueue {
    private final ExecutorService executorService;
    private final List<Future<?>> tasks;

    public LoanProcessQueue() {
        executorService = Executors.newFixedThreadPool(100);
        tasks = new ArrayList<>();
    }

    public synchronized Future<Boolean> process(LoanProcessHandler loanProcessHandler, LoanData loanData) throws ExecutionException, InterruptedException {
        Future<Boolean> task = executorService.submit(() -> loanProcessHandler.progressState(loanData));
        tasks.add(task);
        return task;
    }

    private void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
            log.error("Sleep while check status interrupted: {}", ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public synchronized boolean checkWorking() {
        Iterator<Future<?>> tit = tasks.iterator();
        boolean working = false;
        while (tit.hasNext()) {
            Future<?> task = tit.next();
            Boolean done = task.isDone();
            if (done) tit.remove();
            else working = true;
        }
        return working;
    }
}

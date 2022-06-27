package com.orwen.hisport.utils;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

public class TransactionRequiresNew {
    private final TransactionTemplate instance;

    public TransactionRequiresNew(PlatformTransactionManager transactionManager) {
        this.instance = new TransactionTemplate(transactionManager,
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
    }

    public <T> T execute(TransactionCallback<T> action) {
        return instance.execute(action);
    }

    public void executeWithoutResult(Consumer<TransactionStatus> action) {
        instance.executeWithoutResult(action);
    }
}

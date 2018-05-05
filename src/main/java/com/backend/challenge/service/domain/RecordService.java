package com.backend.challenge.service.domain;

import com.backend.challenge.model.TransactionModel;
import com.backend.challenge.model.TransactionsSummaryModel;

public interface RecordService {
    TransactionsSummaryModel getStatistics();

    void addTransaction(TransactionModel transactionModel);

    void clearTransactions();
}

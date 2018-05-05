package com.backend.challenge.service.app;

import com.backend.challenge.model.TransactionModel;
import com.backend.challenge.model.TransactionsSummaryModel;
import org.springframework.http.ResponseEntity;

public interface TransactionService {
    ResponseEntity<TransactionsSummaryModel> getStatistics();

    ResponseEntity addTransaction(TransactionModel transactionModel);
}

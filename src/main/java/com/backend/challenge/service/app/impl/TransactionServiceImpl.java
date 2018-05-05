package com.backend.challenge.service.app.impl;

import com.backend.challenge.model.TransactionModel;
import com.backend.challenge.model.TransactionsSummaryModel;
import com.backend.challenge.service.app.TransactionService;
import com.backend.challenge.service.domain.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    RecordService recordService;

    @Override
    public ResponseEntity<TransactionsSummaryModel> getStatistics() {
        TransactionsSummaryModel transactionsSummaryModel = recordService.getStatistics();
        return ResponseEntity.ok().body(transactionsSummaryModel);
    }

    @Override
    public ResponseEntity addTransaction(TransactionModel transactionModel) {
        recordService.addTransaction(transactionModel);
        return new ResponseEntity(HttpStatus.CREATED);
    }
}

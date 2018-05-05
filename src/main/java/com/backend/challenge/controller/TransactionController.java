package com.backend.challenge.controller;

import com.backend.challenge.model.TransactionModel;
import com.backend.challenge.service.app.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @RequestMapping(value = "transactions", method = RequestMethod.POST)
    ResponseEntity addTransactions(@RequestBody TransactionModel transactionModel) {
        return transactionService.addTransaction(transactionModel);
    }

    @RequestMapping(value = "statistics", method = RequestMethod.GET)
    ResponseEntity getStatistics() {
        return transactionService.getStatistics();
    }
}

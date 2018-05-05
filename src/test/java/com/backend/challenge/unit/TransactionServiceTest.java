package com.backend.challenge.unit;

import com.backend.challenge.exception.TransactionExpiredException;
import com.backend.challenge.model.TransactionModel;
import com.backend.challenge.model.TransactionsSummaryModel;
import com.backend.challenge.service.app.TransactionService;
import com.backend.challenge.service.app.impl.TransactionServiceImpl;
import com.backend.challenge.service.domain.RecordService;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.time.Instant;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionServiceTest {
    @InjectMocks
    TransactionServiceImpl transactionService;

    @Mock
    RecordService recordService;


    @Before
    public void setup() {
    }

    @Test
    public void addTransactionTest() {
        Instant instant = Instant.now();
        long timesamp = instant.toEpochMilli();
        TransactionModel transactionModel = createTransaction(timesamp, 12345.6545);
        Mockito.doNothing().when(recordService).addTransaction(transactionModel);
        ResponseEntity responseEntity = transactionService.addTransaction(transactionModel);
        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.CREATED, "Transaction added");
    }

    @Test(expected = TransactionExpiredException.class)
    public void addEmptyTransactionTest() {
        Instant instant = Instant.now();
        long timesamp = instant.toEpochMilli() - 60000;
        TransactionModel transactionModel = createTransaction(timesamp, 12345.6545);
        Mockito.doThrow(TransactionExpiredException.class).when(recordService).addTransaction(transactionModel);
        transactionService.addTransaction(transactionModel);
    }

    @Test
    public void getTransactionTest() throws InterruptedException {
        TransactionsSummaryModel transactionsSummaryModel = new TransactionsSummaryModel(750, 375, 500, 250, 2);
        Mockito.when(recordService.getStatistics()).thenReturn(transactionsSummaryModel);
        ResponseEntity responseEntity = transactionService.getStatistics();
        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.OK, "Retrieve transactions summary");
        Assert.isTrue(responseEntity.hasBody(), "Retrieve transactions summary");
        Assert.isTrue(responseEntity.getBody().getClass() == TransactionsSummaryModel.class, "Retrieve transactions summary");
        transactionsSummaryModel = (TransactionsSummaryModel) responseEntity.getBody();
        Assert.isTrue(transactionsSummaryModel.getSum() == 750, "Equate Sum");
        Assert.isTrue(transactionsSummaryModel.getAvg() == 375, "Equate Avg");
        Assert.isTrue(transactionsSummaryModel.getMax() == 500, "Equate Max");
        Assert.isTrue(transactionsSummaryModel.getMin() == 250, "Equate Min");
        Assert.isTrue(transactionsSummaryModel.getCount() == 2, "Equate Count");
    }

    @Test
    public void getTransactionEmptyTest() throws InterruptedException {
        TransactionsSummaryModel transactionsSummaryModel = new TransactionsSummaryModel(0, 0, 0, 0, 0);
        Mockito.when(recordService.getStatistics()).thenReturn(transactionsSummaryModel);
        ResponseEntity responseEntity = transactionService.getStatistics();
        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.OK, "Retrieve transactions summary");
        Assert.isTrue(responseEntity.hasBody(), "Retrieve transactions summary");
        Assert.isTrue(responseEntity.getBody().getClass() == TransactionsSummaryModel.class, "Retrieve transactions summary");
        transactionsSummaryModel = (TransactionsSummaryModel) responseEntity.getBody();
        Assert.isTrue(transactionsSummaryModel.getSum() == 0, "Equate Sum");
        Assert.isTrue(transactionsSummaryModel.getAvg() == 0, "Equate Avg");
        Assert.isTrue(transactionsSummaryModel.getMax() == 0, "Equate Max");
        Assert.isTrue(transactionsSummaryModel.getMin() == 0, "Equate Min");
        Assert.isTrue(transactionsSummaryModel.getCount() == 0, "Equate Count");
    }

    private TransactionModel createTransaction(long timestamp, double amount) {
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setAmount(amount);
        transactionModel.setTimestamp(timestamp);
        return transactionModel;
    }
}

package com.backend.challenge.unit;

import com.backend.challenge.exception.TransactionExpiredException;
import com.backend.challenge.model.TransactionModel;
import com.backend.challenge.model.TransactionsSummaryModel;
import com.backend.challenge.service.app.TransactionService;
import com.backend.challenge.service.domain.RecordService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.time.Instant;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServiceTest {

    @Autowired
    RecordService recordService;

    @Test
    public void addTransactionTest() {
        Instant instant = Instant.now();
        long timesamp = instant.toEpochMilli();
        TransactionModel transactionModel = createTransaction(timesamp, 12345.6545);
        recordService.addTransaction(transactionModel);
    }

    @Test(expected = TransactionExpiredException.class)
    public void addExpiredTransactionTest() {
        Instant instant = Instant.now();
        long timesamp = instant.toEpochMilli() - 61000;
        TransactionModel transactionModel = createTransaction(timesamp, 21456.5465);
        recordService.addTransaction(transactionModel);
    }

    @Test
    public void getTransactionTest() {
        recordService.clearTransactions();
        Instant instant = Instant.now();
        long timesamp = instant.toEpochMilli();
        TransactionModel transactionModel = createTransaction(timesamp, 3151156.4565);
        recordService.addTransaction(transactionModel);

        TransactionsSummaryModel transactionsSummaryModel = recordService.getStatistics();
        Assert.notNull(transactionsSummaryModel, "Retrieved Transaction Summary");
        Assert.isTrue(transactionsSummaryModel.getSum() == transactionModel.getAmount(), "Equate Sum");
        Assert.isTrue(transactionsSummaryModel.getAvg() == transactionModel.getAmount(), "Equate Avg");
        Assert.isTrue(transactionsSummaryModel.getMax() == transactionModel.getAmount(), "Equate Max");
        Assert.isTrue(transactionsSummaryModel.getMin() == transactionModel.getAmount(), "Equate Min");
        Assert.isTrue(transactionsSummaryModel.getCount() == 1, "Equate Count");
    }

    @Test
    public void getTransactionEmptyTest() throws InterruptedException {
        recordService.clearTransactions();
        TransactionsSummaryModel transactionsSummaryModel = recordService.getStatistics();
        Assert.notNull(transactionsSummaryModel, "Retrieved Transaction Summary");
        Assert.isTrue(transactionsSummaryModel.getSum() == 0, "Sum should be zero, if  no activity happened in last 60 secs");
        Assert.isTrue(transactionsSummaryModel.getAvg() == 0, "Average should be zero, if  no activity happened in last 60 secs");
        Assert.isTrue(transactionsSummaryModel.getMax() == 0, "Max should be zero, if  no activity happened in last 60 secs");
        Assert.isTrue(transactionsSummaryModel.getMin() == 0, "Min should be zero, if  no activity happened in last 60 secs");
        Assert.isTrue(transactionsSummaryModel.getCount() == 0, "Count should be zero, if  no activity happened in last 60 secs");
    }

    @Test
    public void consistantTransactionTest() throws InterruptedException {
        recordService.clearTransactions();
        Instant instant = Instant.now();
        long timesamp = instant.toEpochMilli();
        TransactionModel transactionModel = createTransaction(timesamp, 456456.49);
        recordService.addTransaction(transactionModel);

        TransactionsSummaryModel transactionsSummaryModel = recordService.getStatistics();
        Assert.isTrue(transactionsSummaryModel.getSum() == transactionModel.getAmount(), "Equate Sum");
        Assert.isTrue(transactionsSummaryModel.getAvg() == transactionModel.getAmount(), "Equate Avg");
        Assert.isTrue(transactionsSummaryModel.getMax() == transactionModel.getAmount(), "Equate Max");
        Assert.isTrue(transactionsSummaryModel.getMin() == transactionModel.getAmount(), "Equate Min");
        Assert.isTrue(transactionsSummaryModel.getCount() == 1, "Equate Count");

        Thread.sleep(60000);

        instant = Instant.now();
        timesamp = instant.toEpochMilli();
        transactionModel = createTransaction(timesamp, 654656.566);
        recordService.addTransaction(transactionModel);

        transactionsSummaryModel = recordService.getStatistics();

        Assert.isTrue(transactionsSummaryModel.getSum() > 0, "Equate Sum");
        Assert.isTrue(transactionsSummaryModel.getAvg() > 0, "Equate Avg");
        Assert.isTrue(transactionsSummaryModel.getMax() > 0, "Equate Max");
        Assert.isTrue(transactionsSummaryModel.getMin() > 0, "Equate Min");
        Assert.isTrue(transactionsSummaryModel.getCount() > 0, "Equate Count");
    }

    private TransactionModel createTransaction(long timestamp, double amount){
        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setAmount(amount);
        transactionModel.setTimestamp(timestamp);
        return transactionModel;
    }
}

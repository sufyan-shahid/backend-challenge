package com.backend.challenge.service.domain.impl;

import com.backend.challenge.comparator.TransactionTimestampComparator;
import com.backend.challenge.exception.InconsistantDataException;
import com.backend.challenge.exception.TransactionExpiredException;
import com.backend.challenge.model.TransactionModel;
import com.backend.challenge.model.TransactionsSummaryModel;
import com.backend.challenge.service.domain.RecordService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class RecordServiceImpl implements RecordService {

    static Set<TransactionModel> transactions = new ConcurrentSkipListSet<TransactionModel>(new TransactionTimestampComparator());

    static AtomicReference<TransactionsSummaryModel> transactionsSummaryModel = new AtomicReference<TransactionsSummaryModel>();

    public RecordServiceImpl(){
        transactionsSummaryModel.set(new TransactionsSummaryModel());
    }

    static final long REPORT_INTERVAL = 60000;
    static final long SCHEDULING_RATE = 1;

    @Override
    public TransactionsSummaryModel getStatistics() {
        TransactionsSummaryModel transactionsSummary = transactionsSummaryModel.get();
        if(transactions.size() > 0 && transactionsSummary.getCount() == 0){
            createSummary();  // for test only, on running application it will be skipped cause of scheduler
        }
        return transactionsSummaryModel.get();
    }

    @Override
    public void addTransaction(TransactionModel transactionModel) {
        Instant instant = Instant.now();
        long milliSeconds = instant.toEpochMilli() - REPORT_INTERVAL;
        if(transactionModel.getAmount() == null || transactionModel .getTimestamp() == null){
            throw new InconsistantDataException();
        }
        if (transactionModel.getTimestamp() < milliSeconds) {
            throw new TransactionExpiredException();
        } else {
            transactions.add(transactionModel);
        }
    }

    @Override
    public void clearTransactions() {
        transactions.clear();
        TransactionsSummaryModel newModel = new TransactionsSummaryModel();
        transactionsSummaryModel.set(newModel);
    }

    @Scheduled(fixedRate = SCHEDULING_RATE)
    private void createSummary() {
        refreshTransactionSummary();
        if (!transactions.isEmpty()) {
            DoubleSummaryStatistics doubleSummaryStatistics = transactions.stream().collect(Collectors.summarizingDouble(value -> value.getAmount()));
            TransactionsSummaryModel oldSummary = transactionsSummaryModel.get();
            TransactionsSummaryModel updatedSummary = new TransactionsSummaryModel(doubleSummaryStatistics.getSum(),
                    doubleSummaryStatistics.getAverage(), doubleSummaryStatistics.getMax(), doubleSummaryStatistics.getMin(),
                    doubleSummaryStatistics.getCount());
            transactionsSummaryModel.compareAndSet(oldSummary, updatedSummary);
        }
    }

    private void refreshTransactionSummary() {
        Instant instant = Instant.now();
        long milliSeconds = instant.toEpochMilli() - REPORT_INTERVAL;
        Iterator<TransactionModel> iterator = transactions.iterator();
        if (iterator.hasNext()) {
            TransactionModel transactionModel = iterator.next();
            if (transactionModel.getTimestamp() < milliSeconds) {
                transactions.clear();
                TransactionsSummaryModel oldSummary = transactionsSummaryModel.get();
                TransactionsSummaryModel updatedSummary = new TransactionsSummaryModel();
                transactionsSummaryModel.compareAndSet(oldSummary, updatedSummary);
            } else {
                TransactionModel[] transactionModels = transactions.toArray(new TransactionModel[transactions.size()]);
                if (transactionModels[transactionModels.length - 1].getTimestamp() < milliSeconds) {
                    cleanTransactions(milliSeconds, (transactionModels));
                }
            }
        }
    }

    private void cleanTransactions(long milliSeconds, TransactionModel[] transactionModels) {
        int low = 0, high = transactionModels.length - 1;
        while (low != high) {
            int mid = (low + high) / 2;
            if (transactionModels[mid].getTimestamp() > milliSeconds) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        List transactionsToRemove = Arrays.asList(transactionModels).subList(low, transactionModels.length);
        transactions.removeAll(transactionsToRemove);
    }
}

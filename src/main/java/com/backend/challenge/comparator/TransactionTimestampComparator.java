package com.backend.challenge.comparator;

import com.backend.challenge.model.TransactionModel;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

public class TransactionTimestampComparator implements Comparator<TransactionModel> {

    @Override
    public int compare(TransactionModel t1, TransactionModel t2) {
        if (t1.getTimestamp() == t2.getTimestamp()) {
            return 0;
        } else if (t1.getTimestamp() > t2.getTimestamp()) {
            return -1;
        } else {
            return 1;
        }
    }

}

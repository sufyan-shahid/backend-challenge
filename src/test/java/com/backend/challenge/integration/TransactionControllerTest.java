package com.backend.challenge.integration;

import com.backend.challenge.ChallengeApplication;
import com.backend.challenge.controller.TransactionController;
import com.backend.challenge.model.TransactionModel;
import com.backend.challenge.model.TransactionsSummaryModel;
import org.json.JSONException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import java.time.Instant;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ChallengeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionControllerTest {

    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    @Test
    public void addTransactionTest() throws JSONException {
        Instant instant = Instant.now();

        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setAmount(12334.456);
        transactionModel.setTimestamp(instant.toEpochMilli());

        HttpEntity<TransactionModel> entity = new HttpEntity<TransactionModel>(transactionModel, headers);

        ResponseEntity responseEntity = restTemplate.exchange(
                createURLWithPort("/transactions"),
                HttpMethod.POST, entity, Void.class);

        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.CREATED, "Check status for created transaction");
    }

    @Test
    public void addExpiredTransactionTest() throws JSONException {
        Instant instant = Instant.now();

        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setAmount(456460D);
        transactionModel.setTimestamp(instant.toEpochMilli() - 60000);

        HttpEntity<TransactionModel> entity = new HttpEntity<TransactionModel>(transactionModel, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                createURLWithPort("/transactions"),
                HttpMethod.POST, entity, Void.class);

        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.NO_CONTENT,
                "Check status for creating expired transaction");
    }

    @Test
    public void addEmptyTransactionTest() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity responseEntity = restTemplate.exchange(
                createURLWithPort("/transactions"),
                HttpMethod.POST, entity, Void.class);

        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST,
                "Check status for creating expired transaction");
    }

    @Test
    public void addNullAmountTransactionTest() {
        Instant instant = Instant.now();

        String body = "{\"amount\" : null,\"timestamp\" : 1525519241132}";
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);

        ResponseEntity responseEntity = restTemplate.exchange(
                createURLWithPort("/transactions"),
                HttpMethod.POST, entity, Void.class);

        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST,
                "Check status for creating expired transaction");
    }


    @Test
    public void addNullTimestampTransactionTest() {
        Instant instant = Instant.now();

        String body = "{\"amount\" : 12234,\"timestamp\" : null}";
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);

        ResponseEntity responseEntity = restTemplate.exchange(
                createURLWithPort("/transactions"),
                HttpMethod.POST, entity, Void.class);

        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST,
                "Check status for creating expired transaction");
    }

    @Test
    public void getStatisticsTest() throws JSONException {
        addTransactionTest();
        ResponseEntity<TransactionsSummaryModel> responseEntity = restTemplate.exchange(
                createURLWithPort("/statistics"),
                HttpMethod.GET, null, TransactionsSummaryModel.class);
        Assert.isTrue(responseEntity.getStatusCode() == HttpStatus.OK,
                "Retrieve Transaction Summary");

        Assert.isTrue(responseEntity.hasBody(), "Response body exists");
        TransactionsSummaryModel transactionsSummaryModel = responseEntity.getBody();
        Assert.notNull(transactionsSummaryModel, "Retrieved Transaction Summary");
        Assert.isTrue(transactionsSummaryModel.getSum() > 0, "Equate Sum");
        Assert.isTrue(transactionsSummaryModel.getAvg() > 0, "Equate Avg");
        Assert.isTrue(transactionsSummaryModel.getMax() > 0, "Equate Max");
        Assert.isTrue(transactionsSummaryModel.getMin() > 0, "Equate Min");
        Assert.isTrue(transactionsSummaryModel.getCount() > 0, "Equate Count");
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}

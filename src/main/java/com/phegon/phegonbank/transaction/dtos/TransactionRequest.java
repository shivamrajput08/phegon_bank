package com.phegon.phegonbank.transaction.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.phegon.phegonbank.enums.TransactionTypes;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest {
    private TransactionTypes transactionTypes;
    private BigDecimal amount;
    private String accountNumber;
    private String description;

    private String destinationAccountNumber;
}

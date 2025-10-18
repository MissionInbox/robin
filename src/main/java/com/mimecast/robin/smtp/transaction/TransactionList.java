package com.mimecast.robin.smtp.transaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Transaction list.
 *
 * <p>This provides an abstract implementation for SMTP transactions.
 */
@SuppressWarnings("squid:S1192")
public abstract class TransactionList implements Serializable {
    private static final Logger log = LogManager.getLogger(TransactionList.class);

    /**
     * Logs SMTP transaction.
     */
    private final List<Transaction> transactions = new ArrayList<>();

    /**
     * Repeatable transactions
     */
    private final List<String> repeatable = Arrays.asList("SMTP", "RCPT", "BDAT");

    /**
     * Adds new transaction with response only.
     *
     * @param command  Command string.
     * @param response Response string.
     */
    public void addTransaction(String command, String response) {
        if (!repeatable.contains(command) && !getTransactions(command).isEmpty()) return;

        transactions.add(new Transaction(command).setResponse(response));

        if (log.isTraceEnabled()) {
            log.trace("Adding transaction: Command: {}, Response: {}", command, response.replaceAll("[\\n\\r]", ""));
        }
    }

    /**
     * Adds new transaction with response and error only.
     *
     * @param command  Command string.
     * @param response Response string.
     * @param error    Error boolean.
     */
    public void addTransaction(String command, String response, boolean error) {
        if (!repeatable.contains(command) && !getTransactions(command).isEmpty()) return;

        transactions.add(new Transaction(command).setResponse(response).setError(error));

        if (log.isTraceEnabled()) {
            log.trace("Adding transaction: Command: {}, Response: {}, Error: {}", command, response.replaceAll("[\\n\\r]", ""), error);
        }
    }

    /**
     * Adds new transaction with payload and response.
     *
     * @param command  Command string.
     * @param payload  Payload string.
     * @param response Response string.
     */
    public void addTransaction(String command, String payload, String response) {
        if (!repeatable.contains(command) && !getTransactions(command).isEmpty()) return;

        transactions.add(new Transaction(command).setPayload(payload).setResponse(response));

        if (log.isTraceEnabled()) {
            log.trace("Adding transaction: Command: {}, Payload: {}, Response: {}", command, payload, response.replaceAll("[\\n\\r]", ""));
        }
    }

    /**
     * Adds new transaction with payload, response and error.
     *
     * @param command  Command string.
     * @param payload  Payload string.
     * @param response Response string.
     * @param error    Is error boolean.
     */
    public void addTransaction(String command, String payload, String response, boolean error) {
        if (!repeatable.contains(command) && !getTransactions(command).isEmpty()) return;

        transactions.add(new Transaction(command).setPayload(payload).setResponse(response).setError(error));

        if (log.isTraceEnabled()) {
            log.trace("Adding transaction: Command: {}, Payload: {}, Response: {}, Error: {}", command, payload, response.replaceAll("[\\n\\r]", ""), error);
        }
    }

    /**
     * Gets transactions list.
     *
     * @return List of Transaction.
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Gets logs for command.
     *
     * @param command Command string.
     * @return List of Transaction.
     */
    public List<Transaction> getTransactions(String command) {
        List<Transaction> found = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getCommand().equalsIgnoreCase(command)) {
                found.add(transaction);
            }
        }

        return found;
    }

    /**
     * Gets logs for errors.
     *
     * @return List of Transaction.
     */
    public List<Transaction> getErrors() {
        List<Transaction> found = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.isError()) {
                found.add(transaction);
            }
        }

        return found;
    }

    /**
     * Checks if there was a DATA command error.
     *
     * @return boolean.
     */
    public boolean isDataError() {
        for (Transaction transaction : transactions) {
            if (transaction.getCommand().equalsIgnoreCase("data") && transaction.isError()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets failed recipients from RCPT commands.
     *
     * @return List of String.
     */
    public List<String> getFailedRecipients() {
        List<String> failedRecipients = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getCommand().equalsIgnoreCase("rcpt") && transaction.isError()) {
                if (transaction.getAddress() != null) {
                    failedRecipients.add(transaction.getAddress());
                }
            }
        }
        return failedRecipients;
    }

    /**
     * Gets all recipients from RCPT commands.
     *
     * @return List of String.
     */
    public List<String> getRecipients() {
        List<String> recipients = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getCommand().equalsIgnoreCase("rcpt")) {
                if (transaction.getAddress() != null) {
                    recipients.add(transaction.getAddress());
                }
            }
        }
        return recipients;
    }

    /**
     * Clears transactions.
     *
     * @return TransactionList instance.
     */
    public TransactionList clear() {
        transactions.clear();
        return this;
    }
}

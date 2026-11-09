
package com.example.bankapp.service;

import com.example.bankapp.model.*;
import com.example.bankapp.repository.*;
import com.example.bankapp.exception.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.searchByName(keyword);
    }

    public BigDecimal getTotalBalance() {
        BigDecimal total = customerRepository.getTotalBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    public long countCustomers() {
        return customerRepository.countCustomersNative();
    }

    public long countByStatus(TransactionStatus status) {
        return transactionRepository.countByStatus(status);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(
                customer.getAccountNumber()) > 0) {
            throw new InvalidTransferException(
                    "Account number already exists");
        }

        if (customerRepository.countEmails(
                customer.getEmail()) > 0) {
            throw new InvalidTransferException(
                    "Email already exists");
        }

        return customerRepository.save(customer);
    }

    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        if (fromAccount.equals(toAccount)) {
            throw new InvalidTransferException(
                    "Sender and receiver must be different accounts");
        }

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException(
                    "Amount must be greater than zero");
        }

        customerRepository.getByAccountNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Sender account not found"));

        customerRepository.getByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Receiver account not found"));

        int debited = customerRepository.debit(fromAccount, amount);

        if (debited == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        if (simulateFailure) {
            throw new RuntimeException(
                    "Simulated system failure. All changes were rolled back");
        }

        customerRepository.credit(toAccount, amount);

        BankTransaction transaction = new BankTransaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setMessage("Transfer completed successfully");

        return transactionRepository.save(transaction);
    }
}


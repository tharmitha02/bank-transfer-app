
package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {

        if (customerRepository.count() == 0) {

            Customer kamal = new Customer();
            kamal.setAccountNumber("100001");
            kamal.setFullName("Kamal Perera");
            kamal.setEmail("kamal@example.com");
            kamal.setBalance(new BigDecimal("50000.00"));

            Customer nimali = new Customer();
            nimali.setAccountNumber("100002");
            nimali.setFullName("Nimali Silva");
            nimali.setEmail("nimali@example.com");
            nimali.setBalance(new BigDecimal("25000.00"));

            Customer saman = new Customer();
            saman.setAccountNumber("100003");
            saman.setFullName("Saman Fernando");
            saman.setEmail("saman@example.com");
            saman.setBalance(new BigDecimal("10000.00"));

            customerRepository.save(kamal);
            customerRepository.save(nimali);
            customerRepository.save(saman);
        }
    }
}


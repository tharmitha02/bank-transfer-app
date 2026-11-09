
package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {

        model.addAttribute("customers",
                keyword == null || keyword.isBlank()
                        ? bankService.getAllCustomers()
                        : bankService.searchCustomers(keyword));

        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("allCustomers", bankService.getAllCustomers());
        model.addAttribute("customerCount", bankService.countCustomers());
        model.addAttribute("successCount",
                bankService.countByStatus(TransactionStatus.SUCCESS));
        model.addAttribute("failedCount",
                bankService.countByStatus(TransactionStatus.FAILED));
        model.addAttribute("transactions",
                bankService.getLatestTransactions());
        model.addAttribute("transferRequest", new TransferRequest());
        model.addAttribute("customer", new Customer());
        model.addAttribute("keyword", keyword);

        return "bank";
    }

    @PostMapping("/customers")
    public String addCustomer(
            @ModelAttribute Customer customer,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.createCustomer(customer);
            redirectAttributes.addFlashAttribute(
                    "success", "Customer added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute TransferRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please enter valid transfer details");
            return "redirect:/";
        }

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure());

            redirectAttributes.addFlashAttribute(
                    "success", "Transfer completed successfully");

        } catch (Exception e) {

            auditService.recordFailure(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    e.getMessage());

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String history(
            @PathVariable String accountNumber,
            Model model) {

        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute("transactions",
                bankService.getAccountHistory(accountNumber));

        return "history";
    }
}


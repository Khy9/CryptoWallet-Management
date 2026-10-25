
        package com.wallet.cryptowallet.controllers;

import com.wallet.cryptowallet.models.User;
import com.wallet.cryptowallet.models.Wallet;
import com.wallet.cryptowallet.repositories.WalletRepository;
import com.wallet.cryptowallet.services.TransactionService;
import com.wallet.cryptowallet.services.UserService;
import com.wallet.cryptowallet.services.WalletService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Locale;

@Controller
public class DepositController {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final UserService userService;
    private final WalletService walletService;

    public DepositController(
            WalletRepository walletRepository,
            TransactionService transactionService,
            UserService userService,
            WalletService walletService) {

        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("/deposit")
    public String showDepositPage(Model model) {
        model.addAttribute("message", "");
        addWalletToModel(model);
        return "deposit";
    }

    @PostMapping("/deposit")
    public String processDeposit(
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "currency", defaultValue = "BTC") String currency,
            Model model) {

        try {
            // Verify that a user is logged in.
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                throw new IllegalArgumentException(
                        "Please log in before making a deposit.");
            }

            User user = userService.findByUsername(authentication.getName());

            if (user == null) {
                throw new IllegalArgumentException("User not found.");
            }

            Wallet wallet = walletService.findByUser(user);

            if (wallet == null) {
                throw new IllegalArgumentException(
                        "Your wallet was not found.");
            }

            // Validate the amount field.
            if (amount == null || amount.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Amount cannot be empty.");
            }

            BigDecimal depositAmount;

            try {
                depositAmount = new BigDecimal(amount.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Enter a valid numeric amount.");
            }

            if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Amount must be greater than zero.");
            }

            // Allow no more than 8 meaningful decimal places.
            if (depositAmount.stripTrailingZeros().scale() > 8) {
                throw new IllegalArgumentException(
                        "Amount cannot have more than 8 decimal places.");
            }

            // Validate the cryptocurrency.
            if (currency == null || currency.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Please select a cryptocurrency.");
            }

            String selectedCurrency =
                    currency.trim().toUpperCase(Locale.ROOT);

            if (!selectedCurrency.equals("BTC")
                    && !selectedCurrency.equals("ETH")
                    && !selectedCurrency.equals("LTC")) {
                throw new IllegalArgumentException(
                        "Select BTC, ETH, or LTC.");
            }

            // Record the deposit only after all checks pass.
            transactionService.recordDeposit(
                    wallet,
                    depositAmount,
                    selectedCurrency);

            model.addAttribute(
                    "message",
                    "SUCCESS: Simulated " + selectedCurrency
                            + " deposit of " + depositAmount
                            + " completed.");

        } catch (IllegalArgumentException ex) {
            model.addAttribute(
                    "message",
                    "ERROR: " + ex.getMessage());

        } catch (Exception ex) {
            model.addAttribute(
                    "message",
                    "ERROR: Deposit could not be completed. Please try again.");
        }

        addWalletToModel(model);
        return "deposit";
    }

    private void addWalletToModel(Model model) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("wallet", null);
            return;
        }

        User user = userService.findByUsername(authentication.getName());

        Wallet wallet = user != null
                ? walletRepository.findByUser(user).orElse(null)
                : null;

        model.addAttribute("wallet", wallet);
    }
}


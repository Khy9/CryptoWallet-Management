package com.wallet.cryptowallet.controllers;

import com.wallet.cryptowallet.models.User;
import com.wallet.cryptowallet.models.Wallet;
import com.wallet.cryptowallet.repositories.UserRepository;
import com.wallet.cryptowallet.services.TransactionService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Locale;

@Controller
public class WithdrawController {

    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public WithdrawController(
            UserRepository userRepository,
            TransactionService transactionService) {

        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    @GetMapping("/withdraw")
    public String showWithdrawForm(
            Authentication authentication,
            Model model) {

        User user = getCurrentUser(authentication);
        Wallet wallet = user.getWallet();

        if (wallet == null) {
            model.addAttribute("error", "Wallet not found for user.");
            return "withdraw";
        }

        addWalletDetails(model, wallet);

        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String processWithdraw(
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "currency", defaultValue = "BTC") String currency,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User user = getCurrentUser(authentication);
        Wallet wallet = user.getWallet();

        if (wallet == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Wallet not found for user.");

            return "redirect:/withdraw";
        }

        try {
            String selectedCurrency = currency == null
                    ? ""
                    : currency.trim().toUpperCase(Locale.ROOT);

            transactionService.recordWithdrawal(
                    wallet, amount, selectedCurrency);

            redirectAttributes.addFlashAttribute(
                    "success",
                    selectedCurrency + " withdrawal successful!");

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());

        } catch (Exception ex) {

            redirectAttributes.addFlashAttribute(
                    "error", "Withdrawal failed. Please try again.");
        }

        return "redirect:/withdraw";
    }

    private User getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException(
                    "No authenticated user found");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username));
    }

    private void addWalletDetails(Model model, Wallet wallet) {

        model.addAttribute("walletId", wallet.getId());
        model.addAttribute("wallet", wallet);
        model.addAttribute("btcBalance", wallet.getBtcBalance());
        model.addAttribute("ethBalance", wallet.getEthBalance());
        model.addAttribute("ltcBalance", wallet.getLtcBalance());
    }
}
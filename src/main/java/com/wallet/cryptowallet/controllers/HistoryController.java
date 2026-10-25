
        package com.wallet.cryptowallet.controllers;

import com.wallet.cryptowallet.models.Transaction;
import com.wallet.cryptowallet.models.User;
import com.wallet.cryptowallet.models.Wallet;
import com.wallet.cryptowallet.repositories.TransactionRepository;
import com.wallet.cryptowallet.services.UserService;
import com.wallet.cryptowallet.services.WalletService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class HistoryController {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final WalletService walletService;

    public HistoryController(
            TransactionRepository transactionRepository,
            UserService userService,
            WalletService walletService) {

        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping("/history")
    public String showTransactionHistory(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Check whether a real user is logged in.
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return "redirect:/login";
        }

        try {
            String username = authentication.getName();

            User user = userService.findByUsername(username);

            if (user == null) {
                model.addAttribute(
                        "transactions",
                        Collections.emptyList());

                model.addAttribute(
                        "error",
                        "User not found.");

                return "history";
            }

            Wallet wallet = walletService.findByUser(user);

            if (wallet == null) {
                model.addAttribute(
                        "transactions",
                        Collections.emptyList());

                model.addAttribute(
                        "error",
                        "No wallet found for this user.");

                return "history";
            }

            // Fetch transactions belonging only to this user's wallet.
            List<Transaction> transactions =
                    transactionRepository
                            .findByWalletOrderByTimestampDesc(wallet);

            // Wallet information.
            model.addAttribute("walletId", wallet.getId());
            model.addAttribute("balance", wallet.getBalance());

            // Cryptocurrency balances.
            model.addAttribute("btcBalance", wallet.getBtcBalance());
            model.addAttribute("ethBalance", wallet.getEthBalance());
            model.addAttribute("ltcBalance", wallet.getLtcBalance());

            // Transaction records.
            model.addAttribute("transactions", transactions);

            return "history";

        } catch (UsernameNotFoundException ex) {

            return "redirect:/login";
        }
    }
}


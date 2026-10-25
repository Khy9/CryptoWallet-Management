
        package com.wallet.cryptowallet.controllers;

import com.wallet.cryptowallet.models.Transaction;
import com.wallet.cryptowallet.models.User;
import com.wallet.cryptowallet.models.Wallet;
import com.wallet.cryptowallet.repositories.TransactionRepository;
import com.wallet.cryptowallet.services.CryptoPriceService;
import com.wallet.cryptowallet.services.UserService;
import com.wallet.cryptowallet.services.WalletService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final CryptoPriceService cryptoPriceService;

    public HomeController(
            UserService userService,
            WalletService walletService,
            TransactionRepository transactionRepository,
            CryptoPriceService cryptoPriceService) {

        this.userService = userService;
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
        this.cryptoPriceService = cryptoPriceService;
    }

    @GetMapping("/home")
    public String showDashboard(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return "redirect:/login";
        }

        String username = authentication.getName();

        User user = userService.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found: " + username);
        }

        Wallet wallet = walletService.findByUser(user);

        // User information
        model.addAttribute("username", user.getUsername());
        model.addAttribute(
                "email",
                user.getEmail() != null ? user.getEmail() : "Not available");

        model.addAttribute("wallet", wallet);

        // Wallet balances
        model.addAttribute(
                "balance",
                wallet != null && wallet.getBalance() != null
                        ? wallet.getBalance()
                        : BigDecimal.ZERO);

        model.addAttribute("btcBalance", getBalance(
                wallet != null ? wallet.getBtcBalance() : null));

        model.addAttribute("ethBalance", getBalance(
                wallet != null ? wallet.getEthBalance() : null));

        model.addAttribute("ltcBalance", getBalance(
                wallet != null ? wallet.getLtcBalance() : null));

        // Only retrieve transactions for this user's wallet
        List<Transaction> transactions =
                wallet != null
                        ? transactionRepository
                        .findByWalletOrderByTimestampDesc(wallet)
                        : Collections.emptyList();

        // Transaction statistics
        long totalTransactions = transactions.size();

        long totalDeposits = transactions.stream()
                .filter(transaction ->
                        "DEPOSIT".equalsIgnoreCase(transaction.getType()))
                .count();

        long totalWithdrawals = transactions.stream()
                .filter(transaction ->
                        "WITHDRAWAL".equalsIgnoreCase(transaction.getType()))
                .count();

        model.addAttribute("totalTransactions", totalTransactions);
        model.addAttribute("totalDeposits", totalDeposits);
        model.addAttribute("totalWithdrawals", totalWithdrawals);

        // Five most recent transactions
        List<Transaction> recentTransactions = transactions.stream()
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("recentTransactions", recentTransactions);

        // Cryptocurrency prices
        Map<String, Object> priceSummary;

        try {
            priceSummary = cryptoPriceService.getPriceSummary();

            if (priceSummary == null) {
                priceSummary = Collections.emptyMap();
            }
        } catch (Exception ex) {
            // Keep the dashboard available if the price service fails.
            priceSummary = Collections.emptyMap();
        }

        model.addAttribute("btcPrice", priceSummary.get("BTCPrice"));
        model.addAttribute("btcChange", priceSummary.get("BTCChange"));

        model.addAttribute("ethPrice", priceSummary.get("ETHPrice"));
        model.addAttribute("ethChange", priceSummary.get("ETHChange"));

        model.addAttribute("ltcPrice", priceSummary.get("LTCPrice"));
        model.addAttribute("ltcChange", priceSummary.get("LTCChange"));

        // Estimated cryptocurrency portfolio value in INR
        BigDecimal portfolioValue =
                calculatePortfolioValue(wallet, priceSummary);

        model.addAttribute("portfolioValue", portfolioValue);

        return "home";
    }

    private BigDecimal calculatePortfolioValue(
            Wallet wallet,
            Map<String, Object> priceSummary) {

        if (wallet == null) {
            return null;
        }

        BigDecimal btcPrice = toBigDecimal(
                priceSummary.get("BTCPrice"));

        BigDecimal ethPrice = toBigDecimal(
                priceSummary.get("ETHPrice"));

        BigDecimal ltcPrice = toBigDecimal(
                priceSummary.get("LTCPrice"));

        // Do not show a misleading total when a price is unavailable.
        if (btcPrice == null || ethPrice == null || ltcPrice == null) {
            return null;
        }

        BigDecimal btcValue =
                getBalance(wallet.getBtcBalance()).multiply(btcPrice);

        BigDecimal ethValue =
                getBalance(wallet.getEthBalance()).multiply(ethPrice);

        BigDecimal ltcValue =
                getBalance(wallet.getLtcBalance()).multiply(ltcPrice);

        return btcValue.add(ethValue).add(ltcValue);
    }

    private BigDecimal getBalance(BigDecimal balance) {
        return balance != null ? balance : BigDecimal.ZERO;
    }

    private BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
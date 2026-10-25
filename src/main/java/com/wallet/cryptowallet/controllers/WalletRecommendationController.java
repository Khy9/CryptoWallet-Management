
package com.wallet.cryptowallet.controllers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.wallet.cryptowallet.models.Transaction;
import com.wallet.cryptowallet.models.User;
import com.wallet.cryptowallet.models.Wallet;
import com.wallet.cryptowallet.repositories.TransactionRepository;
import com.wallet.cryptowallet.services.UserService;
import com.wallet.cryptowallet.services.WalletService;

@Controller
public class WalletRecommendationController {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;

    public WalletRecommendationController(
            UserService userService,
            WalletService walletService,
            TransactionRepository transactionRepository) {
        this.userService = userService;
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/wallet-recommendation")
    public String showRecommendation(Model model) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(authentication.getName());

        if (user == null) {
            return "redirect:/login";
        }

        Wallet wallet = walletService.findByUser(user);

        BigDecimal balance = wallet != null
                ? safe(wallet.getBalance()) : BigDecimal.ZERO;

        BigDecimal btc = wallet != null
                ? safe(wallet.getBtcBalance()) : BigDecimal.ZERO;

        BigDecimal eth = wallet != null
                ? safe(wallet.getEthBalance()) : BigDecimal.ZERO;

        BigDecimal ltc = wallet != null
                ? safe(wallet.getLtcBalance()) : BigDecimal.ZERO;

        List<Transaction> transactions = wallet != null
                ? transactionRepository.findByWalletOrderByTimestampDesc(wallet)
                : Collections.emptyList();

        int holdingsCount = 0;
        if (btc.compareTo(BigDecimal.ZERO) > 0) holdingsCount++;
        if (eth.compareTo(BigDecimal.ZERO) > 0) holdingsCount++;
        if (ltc.compareTo(BigDecimal.ZERO) > 0) holdingsCount++;

        String recommendation;
        String reason;

        if (transactions.size() >= 5) {
            recommendation = "Hybrid Storage";
            reason = "Your wallet has " + transactions.size()
                    + " recorded transactions. If you use crypto regularly, "
                    + "a hot wallet can provide convenient access while cold "
                    + "storage can protect holdings you do not need frequently.";
        } else if (holdingsCount > 0) {
            recommendation = "Consider Cold Storage";
            reason = "You currently hold " + holdingsCount
                    + " type(s) of cryptocurrency and have limited recorded "
                    + "transaction activity. If you plan to hold crypto long-term, "
                    + "consider cold storage while keeping only a small amount "
                    + "in a hot wallet for routine use.";
        } else {
            recommendation = "Start With Your Usage Needs";
            reason = "No BTC, ETH, or LTC holdings are currently recorded. "
                    + "A hot wallet suits frequent access, while cold storage "
                    + "is worth considering for longer-term holdings. "
                    + "Your recommendation can be revisited as your usage grows.";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("balance", balance);
        model.addAttribute("btcBalance", btc);
        model.addAttribute("ethBalance", eth);
        model.addAttribute("ltcBalance", ltc);
        model.addAttribute("transactionCount", transactions.size());
        model.addAttribute("holdingsCount", holdingsCount);
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("reason", reason);

        return "wallet-recommendation";
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

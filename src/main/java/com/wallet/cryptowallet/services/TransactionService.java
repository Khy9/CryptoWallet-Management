
package com.wallet.cryptowallet.services;

import com.wallet.cryptowallet.models.Transaction;
import com.wallet.cryptowallet.models.Wallet;
import com.wallet.cryptowallet.repositories.TransactionRepository;
import com.wallet.cryptowallet.repositories.WalletRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            WalletRepository walletRepository) {

        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    // Transfer INR between wallets
    @Transactional
    public void transfer(
            Long fromWalletId,
            Long toWalletId,
            BigDecimal amount) {

        validateTransferParameters(fromWalletId, toWalletId, amount);

        Wallet fromWallet = walletRepository.findById(fromWalletId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Sender wallet not found"));

        Wallet toWallet = walletRepository.findById(toWalletId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Receiver wallet not found"));

        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new IllegalArgumentException(
                    "Cannot transfer to the same wallet");
        }

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        saveTransaction(
                fromWallet,
                amount.negate(),
                "TRANSFER_OUT",
                "INR",
                "Transfer to wallet " + toWallet.getId());

        saveTransaction(
                toWallet,
                amount,
                "TRANSFER_IN",
                "INR",
                "Transfer from wallet " + fromWallet.getId());
    }

    // Deposit into the original INR balance
    @Transactional
    public Transaction recordDeposit(Wallet wallet, BigDecimal amount) {

        validateWallet(wallet);
        validateAmount(amount);

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        return saveTransaction(
                wallet,
                amount,
                "DEPOSIT",
                "INR",
                "Deposit to wallet " + wallet.getId());
    }

    // Deposit cryptocurrency
    @Transactional
    public Transaction recordDeposit(
            Wallet wallet,
            BigDecimal amount,
            String currency) {

        validateWallet(wallet);
        validateAmount(amount);

        String selectedCurrency = validateCurrency(currency);

        switch (selectedCurrency) {
            case "BTC":
                wallet.setBtcBalance(
                        wallet.getBtcBalance().add(amount));
                break;

            case "ETH":
                wallet.setEthBalance(
                        wallet.getEthBalance().add(amount));
                break;

            case "LTC":
                wallet.setLtcBalance(
                        wallet.getLtcBalance().add(amount));
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported currency");
        }

        walletRepository.save(wallet);

        return saveTransaction(
                wallet,
                amount,
                "DEPOSIT",
                selectedCurrency,
                "Simulated " + selectedCurrency
                        + " deposit to wallet " + wallet.getId());
    }

    // Withdraw from the original INR balance
    @Transactional
    public Transaction recordWithdrawal(
            Wallet wallet,
            BigDecimal amount) {

        validateWallet(wallet);
        validateAmount(amount);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance for withdrawal");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        return saveTransaction(
                wallet,
                amount.negate(),
                "WITHDRAWAL",
                "INR",
                "Withdrawal from wallet " + wallet.getId());
    }

    // Withdraw cryptocurrency
    @Transactional
    public Transaction recordWithdrawal(
            Wallet wallet,
            BigDecimal amount,
            String currency) {

        validateWallet(wallet);
        validateAmount(amount);

        String selectedCurrency = validateCurrency(currency);

        BigDecimal currentBalance;

        switch (selectedCurrency) {
            case "BTC":
                currentBalance = wallet.getBtcBalance();
                break;

            case "ETH":
                currentBalance = wallet.getEthBalance();
                break;

            case "LTC":
                currentBalance = wallet.getLtcBalance();
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported currency");
        }

        if (currentBalance == null
                || currentBalance.compareTo(amount) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient " + selectedCurrency
                            + " balance for withdrawal");
        }

        BigDecimal updatedBalance = currentBalance.subtract(amount);

        switch (selectedCurrency) {
            case "BTC":
                wallet.setBtcBalance(updatedBalance);
                break;

            case "ETH":
                wallet.setEthBalance(updatedBalance);
                break;

            case "LTC":
                wallet.setLtcBalance(updatedBalance);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported currency");
        }

        walletRepository.save(wallet);

        return saveTransaction(
                wallet,
                amount.negate(),
                "WITHDRAWAL",
                selectedCurrency,
                "Simulated " + selectedCurrency
                        + " withdrawal from wallet " + wallet.getId());
    }

    // Retrieve all transactions, newest first
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTimestampDesc();
    }

    // Create and save a transaction record
    private Transaction saveTransaction(
            Wallet wallet,
            BigDecimal amount,
            String type,
            String currency,
            String description) {

        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setWallet(wallet);
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    // Validate amount and decimal precision
    private void validateAmount(BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }

        BigDecimal normalizedAmount = amount.stripTrailingZeros();

        if (normalizedAmount.scale() > 8) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than 8 decimal places");
        }
    }

    // Validate wallet
    private void validateWallet(Wallet wallet) {

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "Wallet cannot be null");
        }
    }

    // Validate supported cryptocurrency
    private String validateCurrency(String currency) {

        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a currency");
        }

        String selectedCurrency =
                currency.trim().toUpperCase(Locale.ROOT);

        if (!selectedCurrency.equals("BTC")
                && !selectedCurrency.equals("ETH")
                && !selectedCurrency.equals("LTC")) {

            throw new IllegalArgumentException(
                    "Unsupported currency: " + selectedCurrency);
        }

        return selectedCurrency;
    }

    // Validate transfer parameters
    private void validateTransferParameters(
            Long fromWalletId,
            Long toWalletId,
            BigDecimal amount) {

        if (fromWalletId == null || toWalletId == null) {
            throw new IllegalArgumentException(
                    "Wallet IDs cannot be null");
        }

        validateAmount(amount);
    }
}


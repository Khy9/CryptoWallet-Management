
package com.wallet.cryptowallet.services;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CryptoPriceService {

    private static final String API_URL =
            "https://api.coingecko.com/api/v3/simple/price"
                    + "?ids=bitcoin,ethereum,litecoin"
                    + "&vs_currencies=inr"
                    + "&include_24hr_change=true";

    private final RestTemplate restTemplate;

    public CryptoPriceService() {
        this.restTemplate = new RestTemplate();
    }

    // Fetch raw cryptocurrency prices from CoinGecko.
    public Map<String, Object> getCryptoPrices() {

        try {
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            API_URL,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<
                                    Map<String, Object>>() {});

            Map<String, Object> body = response.getBody();

            return body != null ? body : Collections.emptyMap();

        } catch (RestClientException ex) {
            System.err.println(
                    "Unable to fetch cryptocurrency prices: "
                            + ex.getMessage());

            return Collections.emptyMap();
        }
    }

    // Return price data in the format expected by HomeController.
    public Map<String, Object> getPriceSummary() {

        Map<String, Object> apiResponse = getCryptoPrices();
        Map<String, Object> summary = new HashMap<>();

        addCoinToSummary(summary, apiResponse, "bitcoin", "BTC");
        addCoinToSummary(summary, apiResponse, "ethereum", "ETH");
        addCoinToSummary(summary, apiResponse, "litecoin", "LTC");

        return summary;
    }

    private void addCoinToSummary(
            Map<String, Object> summary,
            Map<String, Object> response,
            String coinId,
            String symbol) {

        Object coinObject = response.get(coinId);

        if (!(coinObject instanceof Map<?, ?> coinData)) {
            summary.put(symbol + "Price", null);
            summary.put(symbol + "Change", null);
            return;
        }

        Object rawPrice = coinData.get("inr");
        Object rawChange = coinData.get("inr_24h_change");

        summary.put(
                symbol + "Price",
                parseDecimal(rawPrice));

        summary.put(
                symbol + "Change",
                parseDecimal(rawChange));
    }

    // Validate numeric values returned by the API.
    private BigDecimal parseDecimal(Object value) {

        if (value == null) {
            return null;
        }

        try {
            BigDecimal number = new BigDecimal(value.toString());

            if (!Double.isFinite(number.doubleValue())) {
                return null;
            }

            return number;

        } catch (NumberFormatException ex) {
            return null;
        }
    }
}


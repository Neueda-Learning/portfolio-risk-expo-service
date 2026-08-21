package com.risk_busters.app.clients;

import com.risk_busters.app.dto.FrankfurterRatesDTO;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class FrankfurterApiClient {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterApiClient.class);
    private final WebClient webClient;

    public FrankfurterApiClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));

        this.webClient = WebClient.builder()
                .baseUrl("https://api.frankfurter.dev/v2")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }


    public List<FrankfurterRatesDTO> getRatesForBase(String baseCurrency, List<String> targetCurrencies) {
        String quotesCsv = String.join(",", targetCurrencies);
        log.debug("Fetching rates for base currency {} with targets {}", baseCurrency, quotesCsv);

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rates")
                            .queryParam("base", baseCurrency)
                            .queryParam("quotes", quotesCsv) // Server-side filtering
                            .build())
                    .retrieve()
                    .bodyToFlux(FrankfurterRatesDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch rates for {} against targets {}: {}", baseCurrency, quotesCsv, e.getMessage());
            return List.of();
        }
    }
}
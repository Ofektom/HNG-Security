//package com.example.HNG_Security.config;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class PingTask {
//
//    private final HttpClient httpClient = HttpClient.newHttpClient();
//
//    // URL to ping
//    private final String url = "https://hng-security.onrender.com";
//
//    @Scheduled(fixedRate = 600000)
//    public void pingServer() {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .GET()
//                .build();
//
//        CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//
//        responseFuture.thenApply(response -> {
//            if (response.statusCode() == 200) {
//                System.out.println("Server pinged successfully: " + response.statusCode());
//            } else {
//                System.err.println("Server ping failed with status code: " + response.statusCode());
//            }
//            return null;
//        }).exceptionally(error -> {
//            System.err.println("Error occurred: " + error.getMessage());
//            return null;
//        }).join();
//    }
//}

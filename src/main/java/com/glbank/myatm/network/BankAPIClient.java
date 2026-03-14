package com.glbank.myatm.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BankAPIClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson GSON = new Gson();

    public static class ATMResult {
        public final boolean success;
        public final String cardNumber;
        public final String cvv;
        public final String message;

        public ATMResult(boolean success, String cardNumber, String cvv, String message) {
            this.success = success;
            this.cardNumber = cardNumber;
            this.cvv = cvv;
            this.message = message;
        }
    }

    public static class TerminalResult {
        public final boolean success;
        public final String message;

        public TerminalResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static ATMResult atmRequest(String baseUrl, String apiPassword,
                                       String login, String password, String pin) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("api_password", apiPassword);
            body.addProperty("type", "ATM");
            body.addProperty("login", login);
            body.addProperty("password", password);
            body.addProperty("pin", pin);

            String responseBody = post(baseUrl.replaceAll("/$", "") + "/api/atm", body.toString());
            JsonObject json = GSON.fromJson(responseBody, JsonObject.class);

            boolean success = json.has("success") && json.get("success").getAsBoolean();
            String cardNumber = success && json.has("card_number") ? json.get("card_number").getAsString() : "";
            String cvv = success && json.has("cvv") ? json.get("cvv").getAsString() : "";
            String message = json.has("message") ? json.get("message").getAsString() : "";

            return new ATMResult(success, cardNumber, cvv, message);

        } catch (Exception e) {
            return new ATMResult(false, "", "", "Connection error: " + e.getMessage());
        }
    }

    public static TerminalResult terminalRequest(String baseUrl, String apiPassword,
                                                 String cardNumber, String cvv,
                                                 String receiverAccount,
                                                 int amount, String pin) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("api_password", apiPassword);
            body.addProperty("type", "TERMINAL");
            body.addProperty("card_number", cardNumber);
            body.addProperty("cvv", cvv);
            body.addProperty("receiver_account", receiverAccount);
            body.addProperty("amount", amount);
            body.addProperty("pin", pin);

            String responseBody = post(baseUrl.replaceAll("/$", "") + "/api/terminal", body.toString());
            JsonObject json = GSON.fromJson(responseBody, JsonObject.class);

            boolean success = json.has("success") && json.get("success").getAsBoolean();
            String message = json.has("message") ? json.get("message").getAsString() : "";

            return new TerminalResult(success, message);

        } catch (Exception e) {
            return new TerminalResult(false, "Connection error: " + e.getMessage());
        }
    }

    private static String post(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
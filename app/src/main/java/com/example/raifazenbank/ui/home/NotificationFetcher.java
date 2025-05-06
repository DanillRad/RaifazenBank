package com.example.raifazenbank.ui.home;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class NotificationFetcher {

    private static final String TAG = "NotificationFetcher";
    private static final String GRAPHQL_URL = "https://m2.it.ua/aval_dev/GraphQlServer/api/graphql";

    private static final String QUERY = "{ _testnotification { personalNotifycation { userId textTile dateSend textMsg } } }";

    // Эти значения будут устанавливаться извне
    public static String bearerToken = null;
    public static String delegateUserId = "";

    public static void fetchNotifications() {
        new FetchTask().execute();
    }

    private static class FetchTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(GRAPHQL_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept-Language", "ru"); // или "uk", если у вас i18n.locale
                connection.setRequestProperty("Application-Name", "RaifazenBank"); // замените на актуальное имя
                connection.setRequestProperty("schema", "HRPORTAL");
                connection.setRequestProperty("delegateUserId", delegateUserId != null ? delegateUserId : "");
                if (bearerToken != null && !bearerToken.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
                }

                connection.setDoOutput(true);

                JSONObject requestBody = new JSONObject();
                requestBody.put("query", QUERY);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestBody.toString().getBytes());
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    return response.toString();
                }

            } catch (Exception e) {
                Log.e(TAG, "Ошибка при выполнении запроса: " + e.getMessage(), e);
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.isEmpty()) {
                Log.e(TAG, "Ответ от сервера пустой или произошла ошибка.");
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                JSONArray notifications = json
                        .getJSONObject("data")
                        .getJSONObject("_testnotification")
                        .getJSONArray("personalNotifycation");

                for (int i = 0; i < notifications.length(); i++) {
                    JSONObject notify = notifications.getJSONObject(i);
                    Log.d(TAG, String.format("Уведомление #%d:\nuserId: %s\ntitle: %s\ndateSend: %s\ntextMsg: %s",
                            i + 1,
                            notify.optString("userId"),
                            notify.optString("textTile"),
                            notify.optString("dateSend"),
                            notify.optString("textMsg")));
                }

            } catch (Exception e) {
                Log.e(TAG, "Ошибка при разборе JSON: " + e.getMessage(), e);
            }
        }
    }
}

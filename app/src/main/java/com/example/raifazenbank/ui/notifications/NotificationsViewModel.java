package com.example.raifazenbank.ui.notifications;

// import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import com.example.raifazenbank.ui.notifications.NotifyCheck;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<List<String>> notifications;
    private List<String> previousNotifications = new ArrayList<>(); // Переменная для хранения предыдущих уведомлений

    public NotificationsViewModel() {
        notifications = new MutableLiveData<>();
        notifications.setValue(new ArrayList<>());
    }

    public LiveData<List<String>> getNotifications() {
        return notifications;
    }

    public void setNotifications(Context context, String jsonString) {
        try {
            List<String> notificationList = new ArrayList<>();

            if (jsonString != null && !jsonString.isEmpty()) {
                jsonString = jsonString
                        .replace("\\\"", "\"")
                        .replace("\\\\n", "\\n");

                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject notification = jsonArray.getJSONObject(i);

                    String userId = notification.optString("userId");
                    String textTile = notification.optString("textTile");
                    String dateSend = notification.optString("dateSend");
                    String textMsg = notification.optString("textMsg");

                    String notificationText = "Отримувач: " + userId + "\n" + textTile + "\n" + textMsg + "\nДата: " + dateSend;

                    notificationList.add(notificationText);
                }
            }

            checkForNewNotifications(context, notificationList);
            notifications.setValue(notificationList);

        } catch (JSONException e) {
            e.printStackTrace();
            notifications.setValue(new ArrayList<>());
        }
    }

    private void checkForNewNotifications(Context context, List<String> currentNotifications) {
        if (currentNotifications == null || currentNotifications.isEmpty()) return;

        if (!previousNotifications.isEmpty() && !isSameList(previousNotifications, currentNotifications)) {
            for (String notification : currentNotifications) {
                if (!previousNotifications.contains(notification)) {
                    String textTile = getTextTileFromNotification(notification);
                    NotifyCheck.showPushNotification(context, textTile);
                }
            }
        }

        previousNotifications = new ArrayList<>(currentNotifications);
    }


    // Метод сравнения двух списков по содержимому
    private boolean isSameList(List<String> oldList, List<String> newList) {
        if (oldList.size() != newList.size()) return false;
        for (int i = 0; i < oldList.size(); i++) {
            if (!oldList.get(i).equals(newList.get(i))) return false;
        }
        return true;
    }

    // Метод для извлечения textTile из строки уведомления
    private String getTextTileFromNotification(String notification) {
        // Разделяем строку уведомления на части, предполагая, что она в формате:
        // "Отримувач: <userId>\n<textTile>\n<textMsg>\nДата: <dateSend>"
        String[] parts = notification.split("\n");
        if (parts.length >= 2) {
            return parts[1]; // Возвращаем textTile (второй элемент после recipient)
        }
        return "Неизвестно"; // Если не удается извлечь textTile
    }
}

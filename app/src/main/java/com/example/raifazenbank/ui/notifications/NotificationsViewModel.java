package com.example.raifazenbank.ui.notifications;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<List<String>> notifications;

    public NotificationsViewModel() {
        notifications = new MutableLiveData<>();
        notifications.setValue(new ArrayList<>());
    }

    public LiveData<List<String>> getNotifications() {
        return notifications;
    }

    public void setNotifications(String jsonString) {
        //Log.d("NotificationsViewModel", "Входящий JSON (до обработки): " + jsonString);

        try {
            List<String> notificationList = new ArrayList<>();

            if (jsonString != null && !jsonString.isEmpty()) {
                // Удаление лишнего экранирования кавычек и подготовка строки
                jsonString = jsonString
                        .replace("\\\"", "\"")
                        .replace("\\\\n", "\\n");

                //Log.d("NotificationsViewModel", "JSON после очистки: " + jsonString);

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

            //Log.d("NotificationsViewModel", "Обработанный список уведомлений: " + notificationList.toString());
            notifications.setValue(notificationList);
        } catch (JSONException e) {
            e.printStackTrace();
            notifications.setValue(new ArrayList<>());
        }
    }
}
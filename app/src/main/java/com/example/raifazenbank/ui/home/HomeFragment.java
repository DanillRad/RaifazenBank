package com.example.raifazenbank.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.raifazenbank.R;
import com.example.raifazenbank.Config;
import com.example.raifazenbank.databinding.FragmentHomeBinding;
import com.example.raifazenbank.ui.notifications.NotificationsViewModel;

public class HomeFragment extends Fragment {

    private static final String TAG = "WebViewDebug";
    private static final String URL_TO_LOAD = Config.URL_TO_LOAD;
    private static final long CHECK_INTERVAL_MS = 3 * 60 * 1000;

    private FragmentHomeBinding binding;
    private WebView webView;
    private View rootView;
    private NotificationsViewModel notificationsViewModel;
    private WebViewViewModel webViewViewModel;

    private Runnable checkNotificationsRunnable;
    private boolean isNotificationDisplayed = false;

    private ValueCallback<Uri[]> filePathCallback;
    private ActivityResultLauncher<Intent> fileChooserLauncher;

    private static final int PERMISSION_REQUEST_CODE = 123;
    private final String[] permissions = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (rootView != null) return rootView;

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        rootView = binding.getRoot();

        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
        webViewViewModel = new ViewModelProvider(requireActivity()).get(WebViewViewModel.class);

        requestFilePermissions();

        fileChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (filePathCallback == null) return;

                    Uri[] results = null;
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            results = new Uri[]{data.getData()};
                        }
                    }
                    filePathCallback.onReceiveValue(results);
                    filePathCallback = null;
                }
        );

        // Используем WebView из XML или из ViewModel
        webView = binding.webView;  // Получаем WebView из XML
        webViewViewModel.webView = webView;  // Сохраняем WebView в ViewModel

        setupWebView();  // Настраиваем WebView
        webView.loadUrl(URL_TO_LOAD);  // Загружаем URL

        return rootView;
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Невозможно открыть ссылку", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Ошибка при открытии ссылки: " + e.getMessage());
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                checkNotificationsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("sessionStorage.getItem('notifications');", value -> {
                            Log.d(TAG, "sessionStorage.notifications: " + value);

                            if (value != null && !value.equals("null")) {
                                String json = value.replaceAll("^\"|\"$", "").replace("\\\"", "\"");
                                compareAndNotify(json);
                                notificationsViewModel.setNotifications(json);
                            } else {
                                Log.d(TAG, "sessionStorage.notifications пуст или null, повторим позже");
                            }

                            webView.postDelayed(this, CHECK_INTERVAL_MS);
                        });
                    }
                };
                webView.postDelayed(checkNotificationsRunnable, 50000);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                HomeFragment.this.filePathCallback = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    fileChooserLauncher.launch(intent);
                } catch (Exception e) {
                    HomeFragment.this.filePathCallback = null;
                    Toast.makeText(getContext(), "Невозможно открыть выбор файла", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });
    }

    private void requestFilePermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void compareAndNotify(String newJson) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String oldJson = prefs.getString("notifications", "");

        if (oldJson == null || !oldJson.equals(newJson)) {
            if (!isNotificationDisplayed) {
                Log.d(TAG, "Найдено новое уведомление");
                isNotificationDisplayed = true;
            }
        } else {
            Log.d(TAG, "Уведомления не изменились");
        }

        prefs.edit().putString("notifications", newJson).apply();
    }

    public void resetNotificationFlag() {
        isNotificationDisplayed = false;
    }

    public void refreshWebView() {
        if (webView != null) {
            webView.reload();
        }
    }
}

package com.example.raifazenbank.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.example.raifazenbank.R;

public class SettingsFragment extends Fragment {

    private boolean isUserInteraction = false;

    public SettingsFragment() {
        // Обязательный пустой публичный конструктор
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        Spinner languageSpinner = rootView.findViewById(R.id.language_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.language_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        languageSpinner.setOnTouchListener((v, event) -> {
            isUserInteraction = true;
            return false;
        });

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (isUserInteraction) {
                    String languageCode = "uk"; // Default
                    switch (position) {
                        case 0: languageCode = "en"; break;
                        case 1: languageCode = "ru"; break;
                        case 2: languageCode = "uk"; break;
                    }

                    // Сохраняем выбранный язык
                    saveLanguage(languageCode);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().putBoolean("refreshWebViewAfterRecreate", true).apply();

                    getActivity().recreate();

                    isUserInteraction = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //
            }
        });

        return rootView;
    }

    private void saveLanguage(String lang) {
        SharedPreferences prefs = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }
}

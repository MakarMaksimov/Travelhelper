package com.example.travelhelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import java.util.Locale;

public class account_fragment extends Fragment{

    private Button exit;

    private ToggleButton LanguageSwitchButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_fragment, container, false);
        exit = view.findViewById(R.id.relogbutton);
        LanguageSwitchButton = view.findViewById(R.id.switchingLanguageBut);
        //var sp = getSharedPreferences("PC", Context.MODE_PRIVATE).edit();
        exit.setOnClickListener(v -> {
            // 1. Получаем контекст через requireContext()
            Context context = requireContext();

            // 2. Очищаем SharedPreferences
            SharedPreferences sp = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            sp.edit().clear().apply();

            // 3. Перезапускаем приложение
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            // 4. Завершаем текущую активность
            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        });

        LanguageSwitchButton.setOnClickListener(v -> {
            LanguageSwitchButton.setBackgroundColor(0xFF4CAF50);
            setAppLocale("ru");
        });

        return view;




    }

    private void setAppLocale(String localeCode) {
        Resources res = getResources();
        DisplayMetrics dn = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            conf.setLocale(new Locale(localeCode.toLowerCase()));
        }
        else{
            conf.locale  = new Locale(localeCode.toLowerCase());
        }
        res.updateConfiguration(conf, dn);
    }
}
package com.example.travelhelper;

import android.annotation.SuppressLint;
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
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class account_fragment extends Fragment {

    private Button exit;
    private Switch languageSwitchButton;
    private TextView usersEmail;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_fragment, container, false);

        if (getArguments() == null || !getArguments().containsKey("userId")) {
            throw new IllegalStateException("Fragment arguments must contain userId");
        }

        String userId = getArguments().getString("userId");
        usersEmail = view.findViewById(R.id.UsersEmailTxt);
        usersEmail.setText(userId);

        exit = view.findViewById(R.id.relogbutton);
        languageSwitchButton = view.findViewById(R.id.languageswitcher);


        exit.setOnClickListener(v -> {
            Context context = requireContext();
            SharedPreferences sp = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            sp.edit().clear().apply();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        });

        String currentLang = requireContext()
                .getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                .getString("app_language", Locale.getDefault().getLanguage());
        languageSwitchButton.setChecked(currentLang.equals("ru"));

        languageSwitchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String lang = isChecked ? "ru" : "en";
            Resources res = getResources();
            DisplayMetrics dn = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                conf.setLocale(new Locale(lang.toLowerCase()));
            } else {
                conf.locale = new Locale(lang.toLowerCase());
            }
            res.updateConfiguration(conf, dn);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang));
            }

            requireContext()
                    .getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("app_language", lang)
                    .apply();
        });
        return view;
    }
}
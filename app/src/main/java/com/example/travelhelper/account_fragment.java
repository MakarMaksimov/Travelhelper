package com.example.travelhelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class account_fragment extends Fragment{

    private Button exit;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_fragment, container, false);

        exit = view.findViewById(R.id.relogbutton);
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

        return view; // Возвращаем view, к которому привязаны элементы




    }

   // private SharedPreferences getSharedPreferences(String pc, int modePrivate) {
      //  return null;
    //}


}
package com.example.travelhelper;

import android.content.Intent;
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
        View view = inflater.inflate(R.layout.fragment_account_fragment, container);
        exit = view.findViewById(R.id.relogbutton);

        //var sp = getSharedPreferences("PC", Context.MODE_PRIVATE).edit();
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sp.putString("TY", "-9").apply();
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                view.getContext().startActivity(intent);
            }
        });

        return inflater.inflate(R.layout.fragment_account_fragment, container, false);




    }

   // private SharedPreferences getSharedPreferences(String pc, int modePrivate) {
      //  return null;
    //}


}
package com.example.travelhelper;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class TravelHelperHome extends AppCompatActivity {


    private home_fragment homefr = new home_fragment();

    private account_fragment accfr = new account_fragment();
    private Button home;
    private Button add;

    private Button account;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_travel_helper_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_travel_helper_home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String userId = getIntent().getStringExtra("userId");



        home = findViewById(R.id.homebutton);
        add = findViewById(R.id.addbutton);
        account=findViewById(R.id.accountbutton);


        homefr = new home_fragment();

        Bundle args = new Bundle();
        args.putString("userId", userId);

        homefr.setArguments(args);
        Log.d("TravelHelper", "FrameLayout exists: " + (findViewById(R.id.fragmentslayout) != null));
        // Заменяем фрагмент
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentslayout, homefr)
                .addToBackStack(null)
                .commit();

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                home_fragment fragment = new home_fragment();

                // Подготавливаем аргументы
                Bundle args = new Bundle();
                args.putString("userId", userId);

                // Устанавливаем аргументы ДО транзакции
                fragment.setArguments(args);
                Log.d("TravelHelper", "FrameLayout exists: " + (findViewById(R.id.fragmentslayout) != null));
                // Заменяем фрагмент
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentslayout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем новый экземпляр фрагмента
                add_fragment fragment = new add_fragment();

                // Подготавливаем аргументы
                Bundle args = new Bundle();
                args.putString("userId", userId);

                // Устанавливаем аргументы ДО транзакции
                fragment.setArguments(args);
                Log.d("TravelHelper", "FrameLayout exists: " + (findViewById(R.id.fragmentslayout) != null));
                // Заменяем фрагмент
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentslayout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewFragment(accfr);
            }
        });
    }


    private void setNewFragment(Fragment fr){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentslayout, fr);
        ft.commit();
    }

}
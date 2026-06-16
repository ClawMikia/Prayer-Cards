package com.example.prayercards;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;
import android.widget.Button;

/*
    This activity displays about the prayer cards
*/

public class AboutPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);

        // Corrected Kotlin-style window calls to Java
        if (getWindow() != null) {
            getWindow().setStatusBarColor(android.graphics.Color.BLACK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (getWindow().getInsetsController() != null) {
                    // Set system bars appearance to 0 to ensure icons are light on the black background
                    getWindow().getInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        }

        Button btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> finish());
    }
}
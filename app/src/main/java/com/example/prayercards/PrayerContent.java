package com.example.prayercards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;

import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import Models.Prayer;

/*
    This activity displays a particular prayer: the day, prayer, and the takenFrom
 */

public class PrayerContent extends AppCompatActivity {
    private TextView txt_day, txt_prayer, txt_taken_from;
    private Button btn_back, btn_done;
    private Prayer prayer;
    private static final String FILE_NAME = "data.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_content);

        if (getWindow() != null) {
            getWindow().setStatusBarColor(android.graphics.Color.BLACK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (getWindow().getInsetsController() != null) {
                    getWindow().getInsetsController().setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        }

        txt_day = findViewById(R.id.txt_day);
        txt_prayer = findViewById(R.id.txt_prayer);
        txt_taken_from = findViewById(R.id.txt_taken_from);

        Intent prayerIntent = getIntent();
        prayer = new Prayer();
        prayer.setDay(prayerIntent.getIntExtra("day", 0));
        prayer.setPrayer(prayerIntent.getStringExtra("prayer"));
        prayer.setTakenFrom(prayerIntent.getStringExtra("takenFrom"));
        // FIXED: Retrieve the isPrayed boolean from the Intent
        prayer.setIsPrayed(prayerIntent.getBooleanExtra("isPrayed", false));

        txt_day.setText("Day " + prayer.getDay());
        txt_prayer.setText(prayer.getPrayer());
        txt_taken_from.setText(prayer.getTakenFrom());

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_done = findViewById(R.id.btn_done);

        // This will now work because prayer.getIsPrayed() is populated from the Intent
        if(prayer.getIsPrayed() != null && prayer.getIsPrayed()) {
            btn_done.setVisibility(View.GONE);
        }

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update the JSON file in internal storage before finishing
                updateJsonFile(prayer.getDay());
                finish();
            }
        });
    }

    private void updateJsonFile(int dayToUpdate) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File file = new File(getFilesDir(), FILE_NAME);
            InputStream inputStream;

            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = getAssets().open(FILE_NAME);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            inputStream.close();

            JSONArray jsonArray = new JSONArray(stringBuilder.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getInt("day") == dayToUpdate) {
                    obj.put("isPrayed", true);
                    break;
                }
            }

            // Write the updated JSON to internal storage (Overwriting the previous internal version)
            FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
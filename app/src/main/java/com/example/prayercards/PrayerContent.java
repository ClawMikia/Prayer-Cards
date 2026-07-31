package com.example.prayercards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;

import android.widget.Button;
import android.widget.ScrollView;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import Models.Prayer;

/*
    This activity displays a particular prayer: the day, prayer, and the takenFrom
 */

public class PrayerContent extends AppCompatActivity {
    private Prayer prayer;
    private ArrayList<Prayer> allPrayers = new ArrayList<>();
    private static final String FILE_NAME = "data.json";

    private TextView txt_day;
    private TextView txt_prayer;
    private TextView txt_taken_from;
    private ScrollView scroll_prayer;
    private Button btn_done;

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
        scroll_prayer = findViewById(R.id.scroll_prayer);
        btn_done = findViewById(R.id.btn_done);

        Intent prayerIntent = getIntent();
        prayer = new Prayer();
        prayer.setDay(prayerIntent.getIntExtra("day", 0));
        prayer.setPrayer(prayerIntent.getStringExtra("prayer"));
        prayer.setTakenFrom(prayerIntent.getStringExtra("takenFrom"));
        // FIXED: Retrieve the isPrayed boolean from the Intent
        prayer.setIsPrayed(prayerIntent.getBooleanExtra("isPrayed", false));

        loadAllPrayers();
        updateUI();

        Button btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> finish());

        btn_done.setOnClickListener(v -> {
            // Update the JSON file in internal storage before finishing
            updateJsonFile(prayer.getDay());

            // Proceed to the next day if it's not yet done and it's within the current date
            Prayer nextPrayer = findNextEligiblePrayer(prayer.getDay());
            if (nextPrayer != null) {
                prayer = nextPrayer;
                updateUI();
            } else {
                finish();
            }
        });
    }

    private void loadAllPrayers() {
        allPrayers.clear();
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
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Prayer p = new Prayer();
                p.setDay(jsonObject.getInt("day"));
                p.setPrayer(jsonObject.getString("prayer"));
                p.setTakenFrom(jsonObject.getString("takenFrom"));
                p.setIsPrayed(jsonObject.has("isPrayed") && jsonObject.getBoolean("isPrayed"));
                allPrayers.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        txt_day.setText(getResources().getString(R.string.day_label, prayer.getDay()));
        txt_prayer.setText(prayer.getPrayer());
        txt_taken_from.setText(prayer.getTakenFrom());

        // Scroll to top when updating the content
        scroll_prayer.smoothScrollTo(0, 0);

        // Get current day of the month
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int today = localDate.getDayOfMonth();

        // Hide "Done Praying" button if already prayed OR if it's a future day
        if ((prayer.getIsPrayed() != null && prayer.getIsPrayed()) || prayer.getDay() > today) {
            btn_done.setVisibility(View.GONE);
        } else {
            btn_done.setVisibility(View.VISIBLE);
        }
    }

    private Prayer findNextEligiblePrayer(int currentDay) {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int today = localDate.getDayOfMonth();

        for (Prayer p : allPrayers) {
            if (p.getDay() > currentDay && p.getDay() <= today) {
                if (p.getIsPrayed() == null || !p.getIsPrayed()) {
                    return p;
                }
            }
        }
        return null;
    }

    private void updateJsonFile(int dayToUpdate) {
        // Update local list
        for (Prayer p : allPrayers) {
            if (p.getDay() == dayToUpdate) {
                p.setIsPrayed(true);
                break;
            }
        }

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
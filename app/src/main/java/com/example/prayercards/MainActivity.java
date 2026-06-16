package com.example.prayercards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;

import Adapters.PrayerAdapter;
import Models.Prayer;

/*
    This activity displays all the prayers with button for each prayer to display a particular prayer
*/

public class MainActivity extends AppCompatActivity {
    private PrayerAdapter adapter;
    private ArrayList<Prayer> prayers;
    // Added TextView reference for unprayed count
    private TextView txt_unprayed_count;
    private SearchView searchView;

    private static final String FILE_NAME = "data.json";
    private static final String PREFS_NAME = "PrayerPrefs";
    private static final String KEY_LAST_RESET_MONTH = "lastResetMonth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Initialize the count TextView
        txt_unprayed_count = findViewById(R.id.txt_unprayed_count);

        prayers = new ArrayList<>();

        adapter = new PrayerAdapter(MainActivity.this, prayers); // Display all prayers

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        Button btn_about = findViewById(R.id.btn_about);
        btn_about.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutPage.class);
            startActivity(intent);
        });
    }

    // Refresh the list and count when returning from PrayerContent
    @Override
    protected void onResume() {
        super.onResume();
        prayers.clear();
        loadJsonPrayers();
        if (adapter != null) {
            adapter.updateList(prayers);
            // Re-apply filter if searchView has text
            if (searchView != null && !searchView.getQuery().toString().isEmpty()) {
                adapter.filter(searchView.getQuery().toString());
            }
        }
    }

    // This method gets all the data from the JSON file and add them to prayers list
    private void loadJsonPrayers() {
        StringBuilder stringBuilder = new StringBuilder();
        int unprayedCount = 0; // Temporary variable to hold the false count

        try {
            File file = new File(getFilesDir(), FILE_NAME);
            InputStream inputStream;

            // Check if modified version exists in internal storage, otherwise fallback to assets
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

            // Reset Logic: If the month has changed, update all isPrayed to false
            if (shouldResetPrayers()) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonArray.getJSONObject(i).put("isPrayed", false);
                }
                saveJsonToInternalStorage(jsonArray);
                updateLastResetMonth();
            }

            // Get every JSON item from JSONArray
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Prayer prayerItem = new Prayer();

                prayerItem.setDay(jsonObject.getInt("day"));
                prayerItem.setPrayer(jsonObject.getString("prayer"));
                prayerItem.setTakenFrom(jsonObject.getString("takenFrom"));

                // Load isPrayed status (defaults to false if key doesn't exist)
                boolean isPrayedStatus = false;
                if (jsonObject.has("isPrayed")) {
                    isPrayedStatus = jsonObject.getBoolean("isPrayed");
                }
                prayerItem.setIsPrayed(isPrayedStatus);

                // Increment count if the item is FALSE
                if (!isPrayedStatus) {
                    unprayedCount++;
                }

                prayers.add(prayerItem);
            }

            // Update the TextView on the screen
            txt_unprayed_count.setText(getResources().getString(R.string.remaining_prayers, unprayedCount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean shouldResetPrayers() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int lastResetMonth = prefs.getInt(KEY_LAST_RESET_MONTH, -1);

        // Return true if current month is different from the last time we reset
        return currentMonth != lastResetMonth;
    }

    private void updateLastResetMonth() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        prefs.edit().putInt(KEY_LAST_RESET_MONTH, currentMonth).apply();
    }

    private void saveJsonToInternalStorage(JSONArray jsonArray) {
        try {
            FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
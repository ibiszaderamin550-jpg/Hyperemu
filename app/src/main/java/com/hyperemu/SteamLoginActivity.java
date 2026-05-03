package com.hyperemu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SteamLoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "HyperEmuSteam";
    private static final String STEAM_API_BASE = "https://api.steampowered.com";

    private EditText etSteamId;
    private EditText etApiKey;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private LinearLayout layoutGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundColor(0xFF0A0E1A);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Steam Library");
        tvTitle.setTextColor(0xFF00D4FF);
        tvTitle.setTextSize(24);
        root.addView(tvTitle);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("Steam ID (64-bit) və API açarınızı daxil edin");
        tvInfo.setTextColor(0xFFE0E6F0);
        tvInfo.setTextSize(14);
        tvInfo.setPadding(0, 16, 0, 8);
        root.addView(tvInfo);

        etSteamId = new EditText(this);
        etSteamId.setHint("Steam ID (76561198...)");
        etSteamId.setHintTextColor(0xFF556677);
        etSteamId.setTextColor(0xFFFFFFFF);
        etSteamId.setBackgroundColor(0xFF111827);
        etSteamId.setPadding(16, 12, 16, 12);
        root.addView(etSteamId);

        etApiKey = new EditText(this);
        etApiKey.setHint("Steam Web API Key");
        etApiKey.setHintTextColor(0xFF556677);
        etApiKey.setTextColor(0xFFFFFFFF);
        etApiKey.setBackgroundColor(0xFF111827);
        etApiKey.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 12;
        etApiKey.setLayoutParams(params);
        root.addView(etApiKey);

        TextView tvApiHelp = new TextView(this);
        tvApiHelp.setText("API Key: steamcommunity.com/dev/apikey saytından alın");
        tvApiHelp.setTextColor(0xFF8899AA);
        tvApiHelp.setTextSize(11);
        tvApiHelp.setPadding(0, 4, 0, 0);
        root.addView(tvApiHelp);

        btnLogin = new Button(this);
        btnLogin.setText("Kitabxanama daxil ol");
        btnLogin.setBackgroundColor(0xFF00D4FF);
        btnLogin.setTextColor(0xFF000000);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.topMargin = 20;
        btnLogin.setLayoutParams(btnParams);
        root.addView(btnLogin);

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar);

        tvStatus = new TextView(this);
        tvStatus.setTextColor(0xFF00D4FF);
        tvStatus.setTextSize(13);
        root.addView(tvStatus);

        layoutGames = new LinearLayout(this);
        layoutGames.setOrientation(LinearLayout.VERTICAL);
        root.addView(layoutGames);

        setContentView(root);

        // Saxlanmış məlumatları yüklə
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedId = prefs.getString("steam_id", "");
        String savedKey = prefs.getString("api_key", "");
        if (!savedId.isEmpty()) etSteamId.setText(savedId);
        if (!savedKey.isEmpty()) etApiKey.setText(savedKey);

        btnLogin.setOnClickListener(v -> {
            String steamId = etSteamId.getText().toString().trim();
            String apiKey = etApiKey.getText().toString().trim();
            if (steamId.isEmpty() || apiKey.isEmpty()) {
                Toast.makeText(this, "Steam ID və API Key daxil edin", Toast.LENGTH_SHORT).show();
                return;
            }
            // Saxla
            prefs.edit().putString("steam_id", steamId).putString("api_key", apiKey).apply();
            fetchSteamLibrary(steamId, apiKey);
        });
    }

    private void fetchSteamLibrary(String steamId, String apiKey) {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        tvStatus.setText("Steam kitabxanası yüklənir...");
        layoutGames.removeAllViews();

        new AsyncTask<String, Void, JSONArray>() {
            @Override
            protected JSONArray doInBackground(String... params) {
                try {
                    String urlStr = STEAM_API_BASE +
                        "/IPlayerService/GetOwnedGames/v0001/?key=" + params[1] +
                        "&steamid=" + params[0] +
                        "&include_appinfo=true&include_played_free_games=true&format=json";
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    JSONObject json = new JSONObject(sb.toString());
                    return json.getJSONObject("response").getJSONArray("games");
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONArray games) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                if (games == null) {
                    tvStatus.setText("Xəta! Steam ID və ya API Key yanlışdır.");
                    tvStatus.setTextColor(0xFFFF4444);
                    return;
                }
                tvStatus.setText(games.length() + " oyun tapıldı");
                tvStatus.setTextColor(0xFF00D4FF);
                try {
                    for (int i = 0; i < Math.min(games.length(), 50); i++) {
                        JSONObject game = games.getJSONObject(i);
                        String name = game.optString("name", "Unknown");
                        int playtime = game.optInt("playtime_forever", 0);
                        addGameCard(name, playtime);
                    }
                } catch (Exception e) {
                    tvStatus.setText("Oyunlar yüklənərkən xəta baş verdi");
                }
            }
        }.execute(steamId, apiKey);
    }

    private void addGameCard(String name, int playtimeMinutes) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(0xFF111827);
        card.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.topMargin = 4;
        card.setLayoutParams(cardParams);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(0xFFFFFFFF);
        tvName.setTextSize(14);
        textLayout.addView(tvName);

        TextView tvPlaytime = new TextView(this);
        String hours = playtimeMinutes > 0 ? (playtimeMinutes / 60) + " saat" : "Oynanmayıb";
        tvPlaytime.setText(hours);
        tvPlaytime.setTextColor(0xFF8899AA);
        tvPlaytime.setTextSize(11);
        textLayout.addView(tvPlaytime);

        card.addView(textLayout);

        Button btnRun = new Button(this);
        btnRun.setText("Başlat");
        btnRun.setTextSize(11);
        btnRun.setBackgroundColor(0xFF00D4FF);
        btnRun.setTextColor(0xFF000000);
        btnRun.setPadding(16, 4, 16, 4);
        btnRun.setOnClickListener(v ->
            Toast.makeText(this, name + " konteynerə əlavə edildi", Toast.LENGTH_SHORT).show()
        );
        card.addView(btnRun);

        layoutGames.addView(card);
    }
}

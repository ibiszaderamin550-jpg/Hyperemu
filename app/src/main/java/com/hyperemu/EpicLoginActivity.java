package com.hyperemu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class EpicLoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "HyperEmuEpic";
    // Epic Games public OAuth client
    private static final String EPIC_CLIENT_ID = "34a02cf8f4414e29b15921876da36f9a";
    private static final String EPIC_CLIENT_SECRET = "daafbccc737745039dffe53d94fc76cf";

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private LinearLayout layoutGames;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0A0E1A);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Epic Games Library");
        tvTitle.setTextColor(0xFFFF6B00);
        tvTitle.setTextSize(24);
        root.addView(tvTitle);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("Epic Games hesabınıza daxil olun");
        tvInfo.setTextColor(0xFFE0E6F0);
        tvInfo.setTextSize(14);
        tvInfo.setPadding(0, 16, 0, 8);
        root.addView(tvInfo);

        etUsername = new EditText(this);
        etUsername.setHint("E-poçt / İstifadəçi adı");
        etUsername.setHintTextColor(0xFF556677);
        etUsername.setTextColor(0xFFFFFFFF);
        etUsername.setBackgroundColor(0xFF111827);
        etUsername.setPadding(16, 12, 16, 12);
        root.addView(etUsername);

        etPassword = new EditText(this);
        etPassword.setHint("Şifrə");
        etPassword.setHintTextColor(0xFF556677);
        etPassword.setTextColor(0xFFFFFFFF);
        etPassword.setBackgroundColor(0xFF111827);
        etPassword.setPadding(16, 12, 16, 12);
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 12;
        etPassword.setLayoutParams(params);
        root.addView(etPassword);

        btnLogin = new Button(this);
        btnLogin.setText("Kitabxanama daxil ol");
        btnLogin.setBackgroundColor(0xFFFF6B00);
        btnLogin.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.topMargin = 20;
        btnLogin.setLayoutParams(btnParams);
        root.addView(btnLogin);

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar);

        tvStatus = new TextView(this);
        tvStatus.setTextColor(0xFFFF6B00);
        tvStatus.setTextSize(13);
        root.addView(tvStatus);

        layoutGames = new LinearLayout(this);
        layoutGames.setOrientation(LinearLayout.VERTICAL);
        root.addView(layoutGames);

        scrollView.addView(root);
        setContentView(scrollView);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUser = prefs.getString("username", "");
        if (!savedUser.isEmpty()) etUsername.setText(savedUser);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "İstifadəçi adı və şifrəni daxil edin", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString("username", username).apply();
            doEpicLogin(username, password);
        });
    }

    private void doEpicLogin(String username, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        tvStatus.setText("Epic Games-ə qoşulur...");
        layoutGames.removeAllViews();

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    // Step 1: Get OAuth token
                    URL url = new URL("https://account-public-service-prod03.ol.epicgames.com/account/api/oauth/token");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    String credentials = EPIC_CLIENT_ID + ":" + EPIC_CLIENT_SECRET;
                    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
                    conn.setRequestProperty("Authorization", "basic " + encoded);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    String body = "grant_type=password&username=" +
                        java.net.URLEncoder.encode(params[0], "UTF-8") +
                        "&password=" + java.net.URLEncoder.encode(params[1], "UTF-8") +
                        "&includePerms=false";

                    OutputStream os = conn.getOutputStream();
                    os.write(body.getBytes("UTF-8"));
                    os.close();

                    int responseCode = conn.getResponseCode();
                    BufferedReader reader;
                    if (responseCode == 200) {
                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    return sb.toString();
                } catch (Exception e) {
                    return "ERROR:" + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                if (result == null || result.startsWith("ERROR:")) {
                    tvStatus.setText("Bağlantı xətası. İnternet bağlantınızı yoxlayın.");
                    tvStatus.setTextColor(0xFFFF4444);
                    return;
                }
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.has("access_token")) {
                        String token = json.getString("access_token");
                        String accountId = json.optString("account_id", "");
                        tvStatus.setText("Daxil olundu! Kitabxana yüklənir...");
                        tvStatus.setTextColor(0xFF00FF88);
                        fetchEpicLibrary(token, accountId);
                    } else {
                        String error = json.optString("errorMessage", "Giriş uğursuz oldu");
                        tvStatus.setText("Xəta: " + error);
                        tvStatus.setTextColor(0xFFFF4444);
                    }
                } catch (Exception e) {
                    tvStatus.setText("Cavab emal edilə bilmədi");
                    tvStatus.setTextColor(0xFFFF4444);
                }
            }
        }.execute(username, password);
    }

    private void fetchEpicLibrary(String token, String accountId) {
        new AsyncTask<String, Void, JSONArray>() {
            @Override
            protected JSONArray doInBackground(String... params) {
                try {
                    URL url = new URL("https://launcher-public-service-prod06.ol.epicgames.com/" +
                        "launcher/api/public/assets/Windows?label=Live");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "bearer " + params[0]);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    return new JSONArray(sb.toString());
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONArray games) {
                if (games == null) {
                    tvStatus.setText("Kitabxana yüklənə bilmədi");
                    return;
                }
                tvStatus.setText(games.length() + " oyun tapıldı ✓");
                try {
                    for (int i = 0; i < Math.min(games.length(), 30); i++) {
                        JSONObject game = games.getJSONObject(i);
                        String appName = game.optString("appName", "Unknown");
                        String labelName = game.optString("labelName", "");
                        addEpicGameCard(appName, labelName);
                    }
                } catch (Exception e) {
                    tvStatus.setText("Oyunlar emal edilərkən xəta");
                }
            }
        }.execute(token);
    }

    private void addEpicGameCard(String appName, String label) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(0xFF111827);
        card.setPadding(16, 12, 16, 12);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = 4;
        card.setLayoutParams(cardParams);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvName = new TextView(this);
        tvName.setText(appName);
        tvName.setTextColor(0xFFFFFFFF);
        tvName.setTextSize(14);
        textLayout.addView(tvName);

        if (!label.isEmpty()) {
            TextView tvLabel = new TextView(this);
            tvLabel.setText(label);
            tvLabel.setTextColor(0xFF8899AA);
            tvLabel.setTextSize(11);
            textLayout.addView(tvLabel);
        }

        card.addView(textLayout);

        Button btnRun = new Button(this);
        btnRun.setText("Endir");
        btnRun.setTextSize(11);
        btnRun.setBackgroundColor(0xFFFF6B00);
        btnRun.setTextColor(0xFFFFFFFF);
        btnRun.setPadding(16, 4, 16, 4);
        btnRun.setOnClickListener(v ->
            Toast.makeText(this, appName + " endirilməyə başladı", Toast.LENGTH_SHORT).show()
        );
        card.addView(btnRun);
        layoutGames.addView(card);
    }
}

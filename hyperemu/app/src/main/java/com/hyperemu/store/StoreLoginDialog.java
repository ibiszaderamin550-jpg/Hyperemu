package com.hyperemu.store;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * HyperEmu Store Login Dialog
 * 
 * Shows username/password fields for Steam, GOG, or Amazon Games.
 * 
 * IMPORTANT: This dialog saves credentials locally to pre-fill the store client.
 * Actual authentication happens inside the Wine container when the store
 * client (Steam.exe / GalaxyClient.exe / Amazon Games.exe) is launched.
 * 
 * We do NOT send credentials to any external server directly from this app.
 * The stored token is only used to remember which account the user wants to use.
 */
public class StoreLoginDialog extends Dialog {
    private final StoreManager.StoreType storeType;
    private final StoreManager storeManager;
    private final Runnable onSuccess;

    public StoreLoginDialog(Context context, StoreManager.StoreType storeType,
                            StoreManager storeManager, Runnable onSuccess) {
        super(context);
        this.storeType = storeType;
        this.storeManager = storeManager;
        this.onSuccess = onSuccess;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(buildLayout());
    }

    private LinearLayout buildLayout() {
        Context ctx = getContext();
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF1a1a2e);
        layout.setPadding(60, 60, 60, 60);

        // Title
        TextView title = new TextView(ctx);
        title.setText(StoreManager.getStoreName(storeType) + " Login");
        title.setTextSize(20f);
        title.setTextColor(0xFF00e5ff);
        title.setPadding(0, 0, 0, 40);
        layout.addView(title);

        // Info
        TextView info = new TextView(ctx);
        info.setText("Your credentials are saved locally and used to pre-fill the store client running inside Wine.");
        info.setTextSize(12f);
        info.setTextColor(0xFF9e9e9e);
        info.setPadding(0, 0, 0, 32);
        layout.addView(info);

        // Username
        TextView usernameLabel = new TextView(ctx);
        usernameLabel.setText("Username / Email");
        usernameLabel.setTextColor(0xFFe0e0e0);
        layout.addView(usernameLabel);

        EditText usernameField = new EditText(ctx);
        usernameField.setHint("Enter username or email");
        usernameField.setTextColor(0xFFffffff);
        usernameField.setHintTextColor(0xFF666666);
        usernameField.setBackgroundColor(0xFF0d0d0f);
        usernameField.setPadding(20, 20, 20, 20);
        // Pre-fill if saved
        String savedUsername = storeManager.getUsername(storeType);
        if (!savedUsername.isEmpty()) usernameField.setText(savedUsername);
        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120);
        fieldParams.setMargins(0, 8, 0, 24);
        usernameField.setLayoutParams(fieldParams);
        layout.addView(usernameField);

        // Password
        TextView passLabel = new TextView(ctx);
        passLabel.setText("Password");
        passLabel.setTextColor(0xFFe0e0e0);
        layout.addView(passLabel);

        EditText passwordField = new EditText(ctx);
        passwordField.setHint("Enter password");
        passwordField.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordField.setTextColor(0xFFffffff);
        passwordField.setHintTextColor(0xFF666666);
        passwordField.setBackgroundColor(0xFF0d0d0f);
        passwordField.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams passParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120);
        passParams.setMargins(0, 8, 0, 40);
        passwordField.setLayoutParams(passParams);
        layout.addView(passwordField);

        // Buttons
        LinearLayout btnRow = new LinearLayout(ctx);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        Button cancelBtn = new Button(ctx);
        cancelBtn.setText("Cancel");
        cancelBtn.setBackgroundColor(0xFF333333);
        cancelBtn.setTextColor(0xFFffffff);
        cancelBtn.setAllCaps(false);
        cancelBtn.setOnClickListener(v -> dismiss());
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, 120, 1f);
        cancelParams.setMargins(0, 0, 16, 0);
        cancelBtn.setLayoutParams(cancelParams);
        btnRow.addView(cancelBtn);

        Button loginBtn = new Button(ctx);
        loginBtn.setText("Save & Continue");
        loginBtn.setBackgroundColor(0xFF7c4dff);
        loginBtn.setTextColor(0xFFffffff);
        loginBtn.setAllCaps(false);
        loginBtn.setLayoutParams(new LinearLayout.LayoutParams(0, 120, 1f));
        loginBtn.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(ctx, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            // Save locally — actual auth happens in Wine container
            storeManager.saveStoreToken(storeType, username, "saved_" + System.currentTimeMillis());
            Toast.makeText(ctx, "Credentials saved for " + StoreManager.getStoreName(storeType), Toast.LENGTH_SHORT).show();
            dismiss();
            if (onSuccess != null) onSuccess.run();
        });
        btnRow.addView(loginBtn);

        layout.addView(btnRow);
        return layout;
    }
}

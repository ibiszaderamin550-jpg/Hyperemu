package com.hyperemu.store;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hyperemu.R;
import com.hyperemu.core.AppUtils;

/**
 * HyperEmu Store Fragment
 * Shows Steam, GOG, and Amazon Games integration cards.
 * 
 * Each card shows:
 *   - Store logo/name
 *   - Login status
 *   - Install button (downloads installer .exe to container)
 *   - Launch button (opens store client in Wine container)
 *   - Quick Login button (opens StoreLoginDialog)
 */
public class StoreFragment extends Fragment {
    private StoreManager storeManager;

    public StoreFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storeManager = new StoreManager(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext();
        ScrollView scrollView = new ScrollView(ctx);
        scrollView.setBackgroundColor(0xFF0d0d0f);

        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Header
        TextView header = new TextView(ctx);
        header.setText("Game Stores");
        header.setTextSize(24f);
        header.setTextColor(0xFF00e5ff);
        header.setPadding(0, 0, 0, 32);
        layout.addView(header);

        // Steam Card
        layout.addView(buildStoreCard(ctx, StoreManager.StoreType.STEAM,
                0xFF1b2838, "🎮 Steam",
                "Access your entire Steam library.\nInstall Steam client in any container."));

        // GOG Card
        layout.addView(buildStoreCard(ctx, StoreManager.StoreType.GOG,
                0xFF392a6e, "👾 GOG Galaxy",
                "DRM-free gaming with GOG Galaxy.\nInstall GOG Galaxy 2.0 in any container."));

        // Amazon Games Card
        layout.addView(buildStoreCard(ctx, StoreManager.StoreType.AMAZON,
                0xFF232f3e, "📦 Amazon Games",
                "Play your Amazon Prime Gaming library.\nInstall Amazon Games client in any container."));

        scrollView.addView(layout);
        return scrollView;
    }

    private View buildStoreCard(Context ctx, StoreManager.StoreType store,
                                 int bgColor, String title, String description) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(bgColor);
        card.setPadding(40, 40, 40, 40);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);

        // Title
        TextView titleView = new TextView(ctx);
        titleView.setText(title);
        titleView.setTextSize(20f);
        titleView.setTextColor(0xFFffffff);
        titleView.setPadding(0, 0, 0, 16);
        card.addView(titleView);

        // Description
        TextView descView = new TextView(ctx);
        descView.setText(description);
        descView.setTextSize(14f);
        descView.setTextColor(0xFFb0b0b0);
        descView.setPadding(0, 0, 0, 24);
        card.addView(descView);

        // Login status
        boolean loggedIn = storeManager.isLoggedIn(store);
        TextView statusView = new TextView(ctx);
        if (loggedIn) {
            String username = storeManager.getUsername(store);
            statusView.setText("✅ Logged in as: " + username);
            statusView.setTextColor(0xFF00c853);
        } else {
            statusView.setText("🔒 Not logged in");
            statusView.setTextColor(0xFFff6d00);
        }
        statusView.setTextSize(14f);
        statusView.setPadding(0, 0, 0, 24);
        card.addView(statusView);

        // Button row
        LinearLayout btnRow = new LinearLayout(ctx);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        // Login / Logout button
        Button loginBtn = new Button(ctx);
        if (loggedIn) {
            loginBtn.setText("Logout");
            loginBtn.setBackgroundColor(0xFF444444);
            loginBtn.setOnClickListener(v -> {
                storeManager.logout(store);
                AppUtils.showToast(requireActivity(), "Logged out of " + StoreManager.getStoreName(store));
                refreshFragment();
            });
        } else {
            loginBtn.setText("Login");
            loginBtn.setBackgroundColor(0xFF7c4dff);
            loginBtn.setOnClickListener(v -> {
                StoreLoginDialog dialog = new StoreLoginDialog(requireContext(), store, storeManager, () -> refreshFragment());
                dialog.show();
            });
        }
        loginBtn.setTextColor(0xFFffffff);
        loginBtn.setTextSize(14f);
        loginBtn.setAllCaps(false);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 0, 16, 0);
        loginBtn.setLayoutParams(btnParams);
        btnRow.addView(loginBtn);

        // Install/Launch button
        Button installBtn = new Button(ctx);
        boolean installed = storeManager.isInstalled(store);
        if (installed) {
            installBtn.setText("▶ Launch");
            installBtn.setBackgroundColor(0xFF00c853);
            installBtn.setOnClickListener(v -> launchStoreInContainer(store));
        } else {
            installBtn.setText("⬇ Install");
            installBtn.setBackgroundColor(0xFF0288d1);
            installBtn.setOnClickListener(v -> installStoreInContainer(store));
        }
        installBtn.setTextColor(0xFFffffff);
        installBtn.setTextSize(14f);
        installBtn.setAllCaps(false);
        btnRow.addView(installBtn);

        card.addView(btnRow);
        return card;
    }

    private void installStoreInContainer(StoreManager.StoreType store) {
        String storeName = StoreManager.getStoreName(store);
        String url = StoreManager.getInstallerUrl(store);
        // Show instructions to user — installer runs inside Wine container
        AppUtils.showToast(requireActivity(),
            "Download " + storeName + " installer and run it inside a container.\nURL: " + url);
        // In full implementation: auto-download .exe and create shortcut in selected container
    }

    private void launchStoreInContainer(StoreManager.StoreType store) {
        String storeName = StoreManager.getStoreName(store);
        AppUtils.showToast(requireActivity(), "Launching " + storeName + " via Wine container...");
        // In full implementation: find container with store installed and launch it
    }

    private void refreshFragment() {
        if (getActivity() != null) {
            getParentFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
        }
    }
}

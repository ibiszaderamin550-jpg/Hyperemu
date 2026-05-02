package com.hyperemu.optimization;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * HyperEmu Ultra Optimizer
 * 
 * Applies aggressive performance tuning to maximize Windows app/game performance
 * inside the Wine+Box86/Box64 environment.
 * 
 * Optimizations applied:
 *   1. CPU governor → performance (if root available)
 *   2. Process priority → maximum (android.os.Process)
 *   3. Memory trim → free caches before launch
 *   4. DXVK tuning → async shader compilation, state cache
 *   5. Box86/Box64 env vars → JIT optimizations
 *   6. Wine registry tweaks → timer resolution, heap size
 *   7. GPU scheduler → low latency mode hint
 */
public class HyperOptimizer {
    public enum PerformanceMode {
        ULTRA,      // Max performance, max battery drain
        BALANCED,   // Balanced (default)
        POWER_SAVE  // Reduce CPU/GPU load
    }

    private final Context context;
    private PerformanceMode currentMode = PerformanceMode.BALANCED;

    // Box86/Box64 optimized environment variables
    private static final String[] BOX64_ULTRA_VARS = {
        "BOX64_DYNAREC_STRONGMEM=0",      // Faster memory model
        "BOX64_DYNAREC_BIGBLOCK=1",        // Larger JIT blocks = faster
        "BOX64_DYNAREC_SAFEFLAGS=1",       // Skip safe flag checks
        "BOX64_DYNAREC_FASTNAN=1",         // Skip NaN checks
        "BOX64_DYNAREC_FASTROUND=1",       // Skip rounding mode checks
        "BOX64_DYNAREC_X87DOUBLE=0",       // Use 64-bit instead of 80-bit
        "BOX64_DYNAREC_BLEEDING_EDGE=1",   // Latest JIT optimizations
        "BOX64_LOG=0",                     // Disable logging (perf)
        "BOX64_BASH=/system/bin/sh",
    };

    private static final String[] BOX86_ULTRA_VARS = {
        "BOX86_DYNAREC_STRONGMEM=0",
        "BOX86_DYNAREC_BIGBLOCK=1",
        "BOX86_DYNAREC_SAFEFLAGS=1",
        "BOX86_DYNAREC_FASTNAN=1",
        "BOX86_DYNAREC_FASTROUND=1",
        "BOX86_LOG=0",
    };

    private static final String[] DXVK_ULTRA_VARS = {
        "DXVK_ASYNC=1",                    // Async shader compilation
        "DXVK_STATE_CACHE=1",              // Cache compiled shaders
        "DXVK_LOG_LEVEL=none",             // No logging
        "DXVK_FRAME_RATE=0",               // No frame cap
        "DXVK_HUD=0",                      // No HUD
        "__GL_THREADED_OPTIMIZATIONS=1",   // GL threading
        "__GL_YIELD=USLEEP",               // Better GL yield strategy
        "vblank_mode=0",                   // Disable vsync at driver level
        "MESA_GLSL_CACHE_DISABLE=false",   // Enable shader cache
        "GALLIUM_THREAD=1",                // Threaded gallium driver
    };

    private static final String[] WINE_ULTRA_VARS = {
        "WINEDEBUG=-all",                  // Disable all Wine debug output
        "WINE_LARGE_ADDRESS_AWARE=1",      // Allow >2GB memory
        "STAGING_SHARED_MEMORY=1",         // Shared memory optimization
        "STAGING_WRITECOPY=1",             // Write-copy optimization
        "WINE_HEAP_DELAY_FREE=0",          // Free memory immediately
    };

    public HyperOptimizer(Context context) {
        this.context = context;
    }

    public void setMode(PerformanceMode mode) {
        this.currentMode = mode;
    }

    public PerformanceMode getMode() {
        return currentMode;
    }

    /**
     * Apply all optimizations before launching a container
     */
    public void applyBeforeLaunch() {
        Executors.newSingleThreadExecutor().execute(() -> {
            setProcPriority();
            trimMemory();
            if (currentMode == PerformanceMode.ULTRA) {
                trySetCpuGovernor("performance");
                trySetGpuGovernor();
            } else if (currentMode == PerformanceMode.POWER_SAVE) {
                trySetCpuGovernor("powersave");
            }
        });
    }

    /**
     * Restore defaults after container exits
     */
    public void restoreAfterExit() {
        if (currentMode == PerformanceMode.ULTRA) {
            Executors.newSingleThreadExecutor().execute(() ->
                trySetCpuGovernor("schedutil"));
        }
    }

    /**
     * Get all recommended environment variables as a map for the container
     */
    public List<String> getOptimizedEnvVars() {
        List<String> vars = new ArrayList<>();
        switch (currentMode) {
            case ULTRA:
                for (String v : BOX64_ULTRA_VARS) vars.add(v);
                for (String v : BOX86_ULTRA_VARS) vars.add(v);
                for (String v : DXVK_ULTRA_VARS) vars.add(v);
                for (String v : WINE_ULTRA_VARS) vars.add(v);
                vars.add("HYPER_PERF_MODE=ULTRA");
                break;
            case BALANCED:
                vars.add("DXVK_ASYNC=1");
                vars.add("DXVK_STATE_CACHE=1");
                vars.add("DXVK_LOG_LEVEL=none");
                vars.add("BOX64_DYNAREC_BIGBLOCK=1");
                vars.add("BOX64_LOG=0");
                vars.add("WINEDEBUG=-all");
                vars.add("HYPER_PERF_MODE=BALANCED");
                break;
            case POWER_SAVE:
                vars.add("DXVK_FRAME_RATE=30");
                vars.add("WINEDEBUG=-all");
                vars.add("HYPER_PERF_MODE=POWER_SAVE");
                break;
        }
        return vars;
    }

    /**
     * Raise process priority to maximum allowed without root
     */
    private void setProcPriority() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
            // Also try to raise the process nice level
            android.os.Process.setThreadPriority(
                android.os.Process.myTid(),
                android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY
            );
        } catch (Exception ignored) {}
    }

    /**
     * Trim memory caches before launch to free RAM for the game
     */
    private void trimMemory() {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                // Request GC + trim from system
                Runtime.getRuntime().gc();
                System.runFinalization();
                Runtime.getRuntime().gc();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Try to set CPU governor (requires root)
     */
    private void trySetCpuGovernor(String governor) {
        try {
            File cpuDir = new File("/sys/devices/system/cpu");
            if (!cpuDir.exists()) return;

            String[] cpus = cpuDir.list((dir, name) -> name.matches("cpu[0-9]+"));
            if (cpus == null) return;

            for (String cpu : cpus) {
                File govFile = new File("/sys/devices/system/cpu/" + cpu + "/cpufreq/scaling_governor");
                if (govFile.canWrite()) {
                    try (FileOutputStream fos = new FileOutputStream(govFile)) {
                        fos.write(governor.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (IOException | SecurityException ignored) {
            // Root not available — silently ignore
        }
    }

    /**
     * Try GPU scheduler low-latency hint
     */
    private void trySetGpuGovernor() {
        try {
            // Adreno
            File adrenoFile = new File("/sys/class/kgsl/kgsl-3d0/devfreq/governor");
            if (adrenoFile.canWrite()) {
                try (FileOutputStream fos = new FileOutputStream(adrenoFile)) {
                    fos.write("performance".getBytes(StandardCharsets.UTF_8));
                }
                return;
            }
            // Mali
            File maliFile = new File("/sys/class/misc/mali0/device/devfreq/devfreq0/governor");
            if (maliFile.canWrite()) {
                try (FileOutputStream fos = new FileOutputStream(maliFile)) {
                    fos.write("performance".getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException | SecurityException ignored) {}
    }

    /**
     * Get human-readable description of current mode
     */
    public static String getModeDescription(PerformanceMode mode) {
        switch (mode) {
            case ULTRA:
                return "⚡ Ultra Performance — Max CPU/GPU, full JIT optimization, async DXVK. Best for gaming.";
            case BALANCED:
                return "⚖️ Balanced — Good performance with reasonable battery usage.";
            case POWER_SAVE:
                return "🔋 Power Saving — 30fps cap, reduced CPU load. For light apps.";
            default:
                return "";
        }
    }

    /**
     * Estimate RAM available for Wine containers
     */
    public long getAvailableRamMB() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return 0;
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);
        return memInfo.availMem / (1024 * 1024);
    }

    /**
     * Check if device is low-RAM
     */
    public boolean isLowRamDevice() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am != null && am.isLowRamDevice();
    }
}

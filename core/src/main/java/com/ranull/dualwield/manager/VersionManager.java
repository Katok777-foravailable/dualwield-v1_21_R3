package com.ranull.dualwield.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static com.ranull.dualwield.DualWield.VERSION_TO_REVISION;

public final class VersionManager {
    private String version;

    public VersionManager(JavaPlugin plugin) {
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            return;
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        try {
            version = "v" + VERSION_TO_REVISION.get(Bukkit.getServer().getVersion().split("-")[0]);
            return;
        } catch (ArrayIndexOutOfBoundsException ignored) {}
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_9() {
        return version.matches("(?i)v1_9_R1|v1_9_R2");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_10() {
        return version.matches("(?i)v1_10_R1");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_11() {
        return version.matches("(?i)v1_11_R1");
    }
}

package com.ranull.dualwield;

import com.ranull.dualwield.command.DualWieldCommand;
import com.ranull.dualwield.listener.BlockBreakListener;
import com.ranull.dualwield.listener.EntityDamageByEntityListener;
import com.ranull.dualwield.listener.PlayerInteractEntityListener;
import com.ranull.dualwield.listener.PlayerInteractListener;
import com.ranull.dualwield.manager.DualWieldManager;
import com.ranull.dualwield.manager.VersionManager;
import com.ranull.dualwield.nms.NMS;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class DualWield extends JavaPlugin {
    private static DualWield instance;
    public static final Map<String, String> VERSION_TO_REVISION = new HashMap<String, String>() {
        {
            this.put("1.20", "1_20_R1");
            this.put("1.20.1", "1_20_R1");
            this.put("1.20.2", "1_20_R2");
            this.put("1.20.3", "1_20_R3");
            this.put("1.20.4", "1_20_R3");
            this.put("1.20.5", "1_20_R4");
            this.put("1.20.6", "1_20_R4");
            this.put("1.21", "1_21_R1");
            this.put("1.21.1", "1_21_R1");
            this.put("1.21.2", "1_21_R2");
            this.put("1.21.3", "1_21_R2");
            this.put("1.21.4", "1_21_R3");
        }
    };
    private DualWieldManager dualWieldManager;
    private VersionManager versionManager;
    private NMS nms;

    public static boolean shouldAttack(Player player) {
        return instance != null && instance.getDualWieldManager().shouldAttack(player);
    }

    public static boolean shouldSwing(Player player) {
        return instance != null && instance.getDualWieldManager().shouldSwing(player);
    }

    public static boolean shouldBreak(Player player) {
        return instance != null && instance.getDualWieldManager().shouldMine(player);
    }

    public static boolean isDualWielding(Player player) {
        return instance != null && instance.getDualWieldManager().isDualWielding(player);
    }

    public static void attack(Player player, Entity entity) {
        if (instance != null) {
            instance.getDualWieldManager().attack(player, entity, EquipmentSlot.HAND);
        }
    }

    public static void attack(Player player, Entity entity, EquipmentSlot equipmentSlot) {
        if (instance != null) {
            instance.getDualWieldManager().attack(player, entity, equipmentSlot);
        }
    }

    @Override
    public void onEnable() {
        if (setupNMS()) {
            instance = this;
            dualWieldManager = new DualWieldManager(this);
            versionManager = new VersionManager(this);

            saveDefaultConfig();
            registerMetrics();
            registerCommands();
            registerListeners();
        } else {
            getLogger().severe("Version not supported, disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        unregisterListeners();
    }

    private void registerMetrics() {
        new Metrics(this, 12853);
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    private void registerCommands() {
        PluginCommand pluginCommand = getCommand("dualwield");

        if (pluginCommand != null) {
            DualWieldCommand dualWieldCommand = new DualWieldCommand(this);

            pluginCommand.setExecutor(dualWieldCommand);
            pluginCommand.setTabCompleter(dualWieldCommand);
        }
    }

    public NMS getNMS() {
        return nms;
    }

    public DualWieldManager getDualWieldManager() {
        return dualWieldManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    private boolean setupNMS() {
        try {
            String version = getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> clazz = Class.forName("com.ranull.dualwield.nms.NMS_" + version);

            if (NMS.class.isAssignableFrom(clazz)) {
                nms = (NMS) clazz.getDeclaredConstructor().newInstance();
            }

            if(nms != null) return true;
        } catch (ArrayIndexOutOfBoundsException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {}
        try {
            String version = getServer().getVersion().split("-")[0];
            Class<?> clazz = Class.forName("com.ranull.dualwield.nms.NMS_v" + VERSION_TO_REVISION.get(version));

            if (NMS.class.isAssignableFrom(clazz)) {
                nms = (NMS) clazz.getDeclaredConstructor().newInstance();
            }

            if(nms != null) return true;
        } catch (ArrayIndexOutOfBoundsException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {}
        return false;
    }
}

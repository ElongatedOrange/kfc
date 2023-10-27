package me.orange.kfc;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.orange.kfc.employee.SpawnEmployeeCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class KFC extends JavaPlugin {
    public static KFC PLUGIN;
    public static final HeadDatabaseAPI HEAD_API = new HeadDatabaseAPI();

    @Override
    public void onEnable() {
        PLUGIN = this;

        getCommand("hireemployee").setExecutor(new SpawnEmployeeCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

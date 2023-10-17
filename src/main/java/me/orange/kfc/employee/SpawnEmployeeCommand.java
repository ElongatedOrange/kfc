package me.orange.kfc.employee;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnEmployeeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && sender.isOp()) {
            Player player = (Player) sender;

            // Spawn the trading villager at the player's location
            SpawnEmployee villagerSpawner = new SpawnEmployee();
            villagerSpawner.spawnTradingVillager(player.getLocation(), player);

            return true;
        } else {
            sender.sendMessage("You do not have permission to use this command.");
            return false;
        }
    }
}

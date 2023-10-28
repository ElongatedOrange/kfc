package me.orange.kfc;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.orange.kfc.KFC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FoodManager implements Listener {
    static final HeadDatabaseAPI api = KFC.HEAD_API;

    public static ItemStack chicken_sandwich = create("55097", ChatColor.GOLD + "Chicken Sandwich", Arrays.asList("", ""));
    public static ItemStack fried_chicken = create("6053", ChatColor.GOLD + "Fried Chicken", Arrays.asList("", ""));
    public static ItemStack fries = create("47450", ChatColor.YELLOW + "French Fries", Arrays.asList("", ""));
    public static ItemStack pepsi = create("29838", ChatColor.BLUE + "Can Of Pepsi", Arrays.asList("", ""));
    public static ItemStack seven_up = create("23330", ChatColor.GREEN + "7up", Arrays.asList("", ""));

    public static ItemStack create(String headID, String name, List<String> lore) {
        ItemStack item = api.getItemHead(headID);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

    private final Random random = new Random();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Action action = event.getAction();

        if (item.getType() == Material.AIR) return;

        if (action == Action.RIGHT_CLICK_BLOCK &&
                (
                        item.isSimilar(FoodManager.chicken_sandwich) ||
                                item.isSimilar(FoodManager.fried_chicken) ||
                                item.isSimilar(FoodManager.fries) ||
                                item.isSimilar(FoodManager.pepsi) ||
                                item.isSimilar(FoodManager.seven_up)
                )) {

            event.setCancelled(true);
            return;
        }

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        player.playSound(player, Sound.ENTITY_PLAYER_BURP, 1, 1);

        // Apply random positive effect
        PotionEffectType[] positiveEffects = {PotionEffectType.REGENERATION, PotionEffectType.INCREASE_DAMAGE,
                PotionEffectType.SPEED, PotionEffectType.HEAL};
        PotionEffectType positiveEffect = positiveEffects[random.nextInt(positiveEffects.length)];
        player.addPotionEffect(new PotionEffect(positiveEffect, 20 * 10, 2)); // 10 seconds duration, amplifier 1

        // Apply random negative effect with a certain chance (e.g., 50%)
        if (random.nextFloat() < 0.5) {
            PotionEffectType[] negativeEffects = {PotionEffectType.POISON, PotionEffectType.BLINDNESS,
                    PotionEffectType.CONFUSION};
            PotionEffectType negativeEffect = negativeEffects[random.nextInt(negativeEffects.length)];
            player.addPotionEffect(new PotionEffect(negativeEffect, 20 * 10, 1)); // 10 seconds duration, amplifier 1
        }
    }
}

package me.orange.kfc.items;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.orange.kfc.KFC;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class FoodManager {
    static final HeadDatabaseAPI api = KFC.HEAD_API;

    public static ItemStack chicken_sandwich = create("", "Chicken Sandwich", Arrays.asList("", ""));

    public static ItemStack create(String headID, String name, List<String> lore) {
        ItemStack item = api.getItemHead(headID);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        return item;
    }
}

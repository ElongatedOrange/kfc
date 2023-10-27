package me.orange.kfc.employee;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.orange.kfc.KFC;
import me.orange.kfc.FoodManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnEmployee {
    private final HeadDatabaseAPI api = KFC.HEAD_API;

    public void spawnTradingVillager(Location location, Player player) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setVillagerType(Villager.Type.SAVANNA);
        villager.setProfession(Villager.Profession.BUTCHER);

        List<MerchantRecipe> trades = new ArrayList<>();

        trades.add(createTrade(new ItemStack(Material.EMERALD), 6, FoodManager.fried_chicken));
        trades.add(createTrade(new ItemStack(Material.EMERALD), 8, FoodManager.chicken_sandwich));
        trades.add(createTrade(new ItemStack(Material.EMERALD), 4, FoodManager.fries));
        trades.add(createTrade(new ItemStack(Material.EMERALD), 3, FoodManager.pepsi));
        trades.add(createTrade(new ItemStack(Material.EMERALD), 3, FoodManager.seven_up));

        villager.setRecipes(trades);

        villager.setCustomName("KFC Employee");
        villager.setCustomNameVisible(true);
        villager.setAdult();
        villager.setCanPickupItems(false);
        villager.setRemoveWhenFarAway(false);
        villager.setAI(false);
        villager.setInvulnerable(true);
    }

    private MerchantRecipe createTrade(String inputHeadId, int amount, ItemStack output) {
        ItemStack inputHead = api.getItemHead(inputHeadId);
        if(inputHead != null) inputHead.setAmount(amount);
        return createTrade(inputHead, output);
    }

    private MerchantRecipe createTrade(ItemStack input, ItemStack output) {
        return createTrade(Collections.singletonList(input), output);
    }

    private MerchantRecipe createTrade(ItemStack input, int amount, ItemStack output) {
        input.setAmount(amount);
        return createTrade(Collections.singletonList(input), output);
    }

    private MerchantRecipe createTrade(List<ItemStack> inputs, ItemStack output) {
        MerchantRecipe recipe = new MerchantRecipe(output, 9999999);
        recipe.setIngredients(inputs);
        return recipe;
    }
}
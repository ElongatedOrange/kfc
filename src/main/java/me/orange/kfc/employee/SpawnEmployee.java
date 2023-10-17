package me.orange.kfc.employee;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.orange.kfc.KFC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnEmployee {
    private final HeadDatabaseAPI api = KFC.HEAD_API;

    public void spawnTradingVillager(Location location, Player player) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setVillagerType(Villager.Type.SAVANNA);
        villager.setProfession(Villager.Profession.BUTCHER);

        List<MerchantRecipe> recipes = new ArrayList<>();

        recipes.add(createTrade(new ItemStack(Material.EMERALD), 2, chickenSandwich.create()));

        villager.setRecipes(recipes);

        villager.setCustomName("KFC Employee");
        villager.setCustomNameVisible(true);
        villager.setAdult();
        villager.setCanPickupItems(false);
        villager.setRemoveWhenFarAway(false);
        villager.setAI(false);
        villager.setInvulnerable(true);
    }

    private MerchantRecipe createTrade(String inputHeadId, int amount, ItemStack output) {
        //ItemStack inputHead = api.getItemHead(inputHeadId);
        //if(inputHead != null) inputHead.setAmount(amount);
        //return createTrade(inputHead, output);
        return null;
    }

    private MerchantRecipe createTrade(ItemStack input, ItemStack output) {
        return createTrade(Arrays.asList(input), output);
    }

    private MerchantRecipe createTrade(List<ItemStack> inputs, ItemStack output) {
        MerchantRecipe recipe = new MerchantRecipe(output, 9999999);
        recipe.setIngredients(inputs);
        return recipe;
    }
}
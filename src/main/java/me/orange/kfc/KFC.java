package me.orange.kfc;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.orange.kfc.employee.SpawnEmployeeCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class KFC extends JavaPlugin implements Listener {
    public static KFC PLUGIN;
    public static final HeadDatabaseAPI HEAD_API = new HeadDatabaseAPI();
    private Map<Player, Order> playerOrders = new HashMap<>();  // To track orders for each player
    private Set<Integer> deliveryNpcIds = new HashSet<>(); // To store the NPC names

    private File npcFile;
    private FileConfiguration npcConfig;

    @Override
    public void onEnable() {
        PLUGIN = this;
        createCustomConfig();
        loadNpcIds();

        getCommand("hireemployee").setExecutor(new SpawnEmployeeCommand());
        Bukkit.getPluginManager().registerEvents(this, this);

        // Schedule a delayed task to execute the NPC creation logic 100 ticks (5 seconds) after server restart
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            createOrderTakerNpc(registry);
            createDeliveryNpcs(registry);
        }, 100L);
    }


    private void createCustomConfig() {
        npcFile = new File(getDataFolder(), "npcs.yml");
        if (!npcFile.exists()) {
            npcFile.getParentFile().mkdirs();
            saveResource("npcs.yml", false);
        }

        npcConfig = YamlConfiguration.loadConfiguration(npcFile);  // Simplified loading of configuration
    }

    private void createOrderTakerNpc(NPCRegistry registry) {
        String orderTakerName = "Order Taker";
        int existingNpcId = npcConfig.getInt("order_taker.id", -1);
        if (existingNpcId != -1 && registry.getById(existingNpcId) != null) {
            // Order Taker NPC already exists, no need to create a new one
            return;
        }

        NPC orderTakerNPC = registry.createNPC(EntityType.PLAYER, orderTakerName);
        Location spawnLocation = new Location(Bukkit.getWorld("world"), 449, 70, -5);
        orderTakerNPC.spawn(spawnLocation);

        npcConfig.set("order_taker.id", orderTakerNPC.getId());
        saveNpcConfig();
    }

    private void createDeliveryNpcs(NPCRegistry registry) {
        String[] deliveryNpcNames = {"Delivery Point 1", "Delivery Point 2", "Delivery Point 3"};
        for (String name : deliveryNpcNames) {
            int existingNpcId = npcConfig.getInt(name + ".id", -1);
            if (existingNpcId != -1 && registry.getById(existingNpcId) != null) {
                // NPC already exists, no need to create a new one
                continue;
            }

            NPC deliveryNpc = registry.createNPC(EntityType.PLAYER, name);
            Location spawnLocation;
            switch (name) {
                case "Delivery Point 1":
                    spawnLocation = new Location(Bukkit.getWorld("world"), 442, 70, -3);
                    break;
                case "Delivery Point 2":
                    spawnLocation = new Location(Bukkit.getWorld("world"), 442, 70, -5);
                    break;
                case "Delivery Point 3":
                default:
                    spawnLocation = new Location(Bukkit.getWorld("world"), 442, 70, -7);
                    break;
            }
            deliveryNpc.spawn(spawnLocation);

            npcConfig.set(name + ".id", deliveryNpc.getId());
            deliveryNpcIds.add(deliveryNpc.getId());
        }
        saveNpcConfig();  // Save the npcConfig after creating the NPCs
        saveNpcIds();  // Save the deliveryNpcIds set after creating the NPCs
        getLogger().info("Current delivery NPC IDs: " + deliveryNpcIds.toString());  // Log the current delivery NPC IDs
    }

    private void saveNpcConfig() {
        try {
            npcConfig.save(npcFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadNpcIds() {
        File file = new File("npc_ids.properties");
        if (!file.exists()) {
            try {
                file.createNewFile();  // Create the file if it doesn't exist
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
            String ids = properties.getProperty("deliveryNpcIds", "");  // Default to an empty string if property not found
            for (String id : ids.split(",")) {
                if (!id.isEmpty()) {  // Avoid trying to parse an empty string
                    deliveryNpcIds.add(Integer.parseInt(id));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        getLogger().info("Loaded delivery NPC IDs: " + deliveryNpcIds.toString());
    }


    private void saveNpcIds() {
        String ids = deliveryNpcIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        Properties properties = new Properties();
        properties.setProperty("deliveryNpcIds", ids);
        try (OutputStream output = new FileOutputStream("npc_ids.properties")) {
            properties.store(output, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        getLogger().info("Saved delivery NPC IDs: " + ids);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveNpcIds();
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked());
        if (npc != null) {
            getLogger().info("NPC clicked: " + npc.getName() + ", ID: " + npc.getId());
            Player player = event.getPlayer();
            if (npc.getName().equals("Order Taker")) {
                Order order = generateNewOrder();
                assignOrderToPlayer(player, order);
            } else {
                getLogger().info("Delivery NPC IDs: " + deliveryNpcIds.toString());  // Log the IDs
                getLogger().info("Interacted NPC ID: " + npc.getId());  // Log the interacted NPC's ID
                if (deliveryNpcIds.contains(npc.getId())) {
                    getLogger().info("Delivery NPC clicked by player: " + player.getName());
                    Order order = getCurrentOrderForPlayer(player);
                    if (order != null) {
                        getLogger().info("Player has an order.");
                        if (order.getDeliveryNPC().getId() == npc.getId()) {
                            getLogger().info("Delivery NPC matches order's delivery NPC.");
                            completeOrder(player, order);
                        } else {
                            getLogger().info("Delivery NPC does not match order's delivery NPC.");
                            player.sendMessage("This is not the correct delivery point for your order.");
                        }
                    } else {
                        getLogger().info("Player does not have an order.");
                        player.sendMessage("You have no order to deliver.");
                    }
                }
            }
        } else {
            getLogger().info("Entity is not an NPC");
        }
    }


    public Order generateNewOrder() {
        getLogger().info("generateNewOrder called");
        // Array of possible destinations and items for delivery
        String[] destinations = {"Delivery Point 1", "Delivery Point 2", "Delivery Point 3"};
        Material[] items = {Material.COOKED_CHICKEN, Material.COOKED_BEEF, Material.COOKED_MUTTON};

        // Random object to help in selecting a random destination and item
        Random random = new Random();

        // Select a random destination and item
        String randomDestination = destinations[random.nextInt(destinations.length)];
        ItemStack randomItem = new ItemStack(items[random.nextInt(items.length)]);

        // Fetch a reference to an NPC from Citizens to act as the delivery point
        Iterator<NPC> npcIterator = CitizensAPI.getNPCRegistry().iterator();
        NPC deliveryNPC = null;
        while (npcIterator.hasNext()) {
            NPC npc = npcIterator.next();
            if (npc.getName().equals(randomDestination)) {
                deliveryNPC = npc;
                break;
            }
        }

        if (deliveryNPC == null) {
            // Handle the case where the delivery NPC could not be found
            getLogger().severe("Could not find delivery NPC for destination: " + randomDestination);
            return null;
        }

        // Assume a fixed points value for simplicity
        int pointsValue = 10;

        // Create and return a new Order object
        return new Order(randomDestination, randomItem, pointsValue, deliveryNPC);
    }

    public void assignOrderToPlayer(Player player, Order order) {
        getLogger().info("playerOrders: " + playerOrders.toString());
        if (player == null || order == null) {
            // Log an error if either the player or the order is null
            getLogger().severe("Player or order is null!");
            return;
        }

        // Check if the player already has an order
        if (playerOrders.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "You already have an order!");
            getLogger().info("Player already has an order");
        } else {
            // Store the association between the player and the order
            playerOrders.put(player, order);
            player.sendMessage("You picked up an order for " + order.getDestination() + "!"
                    + " Deliver " + order.getItem().getType().name().replace('_', ' ').toLowerCase()
                    + " to " + order.getDestination() + ".");
            getLogger().info("Order assigned to player");
        }
    }

    public Order getCurrentOrderForPlayer(Player player) {
        if (player == null) {
            // Log an error if the player is null
            getLogger().severe("Player is null!");
            return null;
        }

        // Retrieve the order associated with the player from the map
        Order order = playerOrders.get(player);

        if (order == null) {
            // Log a warning if the player has no associated order
            getLogger().warning("No order found for player: " + player.getName());
        }

        return order;
    }

    public void completeOrder(Player player, Order order) {
        if (player == null || order == null) {
            // Log an error if either the player or the order is null
            getLogger().severe("Player or order is null!");
            return;
        }

        // Get the item from the order
        ItemStack orderItem = order.getItem();

        // Check if the player's inventory contains at least one of the correct item
        if (player.getInventory().containsAtLeast(orderItem, orderItem.getAmount())) {
            // Remove the item from the player's inventory
            player.getInventory().removeItem(orderItem);

            // Remove the association between the player and the order
            playerOrders.remove(player);

            // Check if the order was successfully removed (optional)
            if (playerOrders.containsKey(player)) {
                // Log an error if the order was not successfully removed
                getLogger().severe("Failed to remove order for player: " + player.getName());
                return;
            }

            // Reward the player (Placeholder)

            player.sendMessage("You successfully delivered the order!");

            // Mark the order as delivered
            order.markAsDelivered();

            // Log the order completion
            getLogger().info("Player " + player.getName() + " completed an order for " + order.getDestination() + ".");
        } else {
            // Inform the player they do not have the correct item
            player.sendMessage(ChatColor.RED + "You do not have the correct item to complete this order!");
            getLogger().info("Player " + player.getName() + " attempted to complete an order without the correct item.");
        }
    }

}

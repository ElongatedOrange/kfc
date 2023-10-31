package me.orange.kfc;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.citizensnpcs.api.npc.NPC;

public class Order {

    private String destination;
    private long creationTime;
    private ItemStack item;
    private Player player;
    private boolean isDelivered;
    private int pointsValue;
    private NPC deliveryNPC;

    // Constructor
    public Order(String destination, ItemStack item, int pointsValue, NPC deliveryNPC) {
        this.destination = destination;
        this.creationTime = System.currentTimeMillis();
        this.item = item;
        this.pointsValue = pointsValue;
        this.deliveryNPC = deliveryNPC;
        this.isDelivered = false;  // initially, the order is not delivered
    }

    // Getters and Setters
    public String getDestination() {
        return destination;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public ItemStack getItem() {
        return item;
    }

    public Player getPlayer() {
        return player;
    }

    public void assignToPlayer(Player player) {
        this.player = player;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void markAsDelivered() {
        this.isDelivered = true;
    }

    public int getPointsValue() {
        return pointsValue;
    }

    public NPC getDeliveryNPC() {
        return deliveryNPC;
    }

    // Method to check if the order is still valid
    public boolean isValid() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - creationTime;
        return elapsedTime < 600000;  // Example: order expires after 10 minutes
    }
}


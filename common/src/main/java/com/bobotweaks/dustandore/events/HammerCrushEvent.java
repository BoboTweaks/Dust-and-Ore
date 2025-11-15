package com.bobotweaks.dustandore.events;

public final class HammerCrushEvent {
    private final net.minecraft.server.level.ServerLevel level;
    private final net.minecraft.server.level.ServerPlayer player;
    private final net.minecraft.world.item.ItemStack mainHand;
    private final net.minecraft.world.item.ItemStack offHand;
    private final com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe recipe;
    private boolean canceled;

    public HammerCrushEvent(net.minecraft.server.level.ServerLevel level,
                            net.minecraft.server.level.ServerPlayer player,
                            net.minecraft.world.item.ItemStack mainHand,
                            net.minecraft.world.item.ItemStack offHand,
                            com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe recipe) {
        this.level = level;
        this.player = player;
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.recipe = recipe;
        this.canceled = false;
    }

    public net.minecraft.server.level.ServerLevel getLevel() { return level; }
    public net.minecraft.server.level.ServerPlayer getPlayer() { return player; }
    public net.minecraft.world.item.ItemStack getMainHand() { return mainHand; }
    public net.minecraft.world.item.ItemStack getOffHand() { return offHand; }
    public com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe getRecipe() { return recipe; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }
}

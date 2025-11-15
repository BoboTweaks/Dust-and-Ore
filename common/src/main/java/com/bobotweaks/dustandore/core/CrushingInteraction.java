package com.bobotweaks.dustandore.core;

import com.bobotweaks.dustandore.Constants;
import com.bobotweaks.dustandore.events.CommonEventBus;
import com.bobotweaks.dustandore.events.HammerCrushEvent;
import com.bobotweaks.dustandore.gameplay.EnchantmentUtilities;
import com.bobotweaks.dustandore.gameplay.Messages;
import com.bobotweaks.dustandore.gameplay.hammer.CooldownHandler;
import com.bobotweaks.dustandore.gameplay.hammer.HammerUtilities;
import com.bobotweaks.dustandore.gameplay.hammer.PowerLoader;
import com.bobotweaks.dustandore.gameplay.hammer.SpeedLoader;
import com.bobotweaks.dustandore.init.ModItems;
import com.bobotweaks.dustandore.gameplay.ModSounds;
import com.bobotweaks.dustandore.init.RecipeTypes;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe.RewardEntry;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipeSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.List;

public class CrushingInteraction {
    private static final Random RANDOM = new Random();

    public static boolean eventResult = true;

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!validatePreConditions(world, entity)) {
            return;
        }

        LivingEntity living = (LivingEntity) entity;
        Player player = (Player) entity;
        ServerLevel level = (ServerLevel) world;

        if (isOnCooldown(level, living, player)) {
            return;
        }

        if (!validateCrushAbility(living, player)) {
            return;
        }

        RecipeMatchResult recipeResult = findMatchingRecipe(level, living, player);
        if (!recipeResult.success()) {
            if (recipeResult.requiredPower() > 0) {
                Messages.SendErrorMessage(player, -1001, recipeResult.ingredientId(), recipeResult.requiredPower());
            }
            return;
        }
        processCrushing(level, living, player, recipeResult.recipe());
    }

    private static boolean validatePreConditions(LevelAccessor world, Entity entity) {
        if (entity == null)
            return false;
        if (!isHoldingHammer(entity))
            return false;
        if (!(entity instanceof LivingEntity))
            return false;

        LivingEntity living = (LivingEntity) entity;

        if (living.getOffhandItem().isEmpty())
            return false;
        if (!(world instanceof ServerLevel))
            return false;
        return entity instanceof Player;
    }

    private static boolean validateCrushAbility(LivingEntity living, Player player) {
        int canCrush = HammerUtilities.canCrush(
                living.getMainHandItem().getItem(),
                living.getOffhandItem().getItem());

        if (canCrush != 1) {
            Messages.SendErrorMessage(player, canCrush, null, 0);
            return false;
        }
        return true;
    }

    private static boolean isOnCooldown(ServerLevel level, LivingEntity living, Player player) {
        ItemStack hammerItem = living.getMainHandItem();
        long now = level.getGameTime();
        long until = getCooldownUntil(hammerItem);

        if (until > now) {
            Messages.SendErrorMessage(player, -1002, null, 0);
            return true;
        }
        return false;
    }

    private static long getCooldownUntil(ItemStack hammerItem) {
        try {
            CustomData cd = hammerItem.get(DataComponents.CUSTOM_DATA);
            if (cd != null) {
                CompoundTag tag = cd.copyTag();
                if (tag.contains("dustandore_cooldown_until")) {
                    return tag.getLong("dustandore_cooldown_until").orElse(0L);
                }
            }
        } catch (Throwable ignored) {
        }
        return 0L;
    }

    private static record RecipeMatchResult(boolean success, HammerCrushRecipe recipe,
            String ingredientId, int requiredPower) {
    }

    private static RecipeMatchResult findMatchingRecipe(ServerLevel level, LivingEntity living, Player player) {
        ItemStack hammerItem = living.getMainHandItem();
        ItemStack offhandItem = living.getOffhandItem();

        int hammerPower = PowerLoader.getCrushPower(level, hammerItem.getItem());
        String ingredientId = HammerUtilities.idOf(offhandItem.getItem());

        var recipeManager = CrushingManager.getRecipeManagerAnySide(level);
        if (recipeManager == null) {
            return new RecipeMatchResult(false, null, ingredientId, 0);
        }

        HammerCrushRecipe match = findRecipeFromManager(recipeManager, ingredientId, hammerPower);

        if (match == null) {
            match = findRecipeFromResources(level, ingredientId, hammerPower);
        }

        if (match == null) {
            int requiredPower = findRequiredPower(recipeManager, ingredientId);
            return new RecipeMatchResult(false, null, ingredientId, requiredPower);
        }

        if (player instanceof ServerPlayer sp) {
            HammerCrushEvent event = new HammerCrushEvent(
                    level, sp, hammerItem.copy(), offhandItem.copy(), match);
            if (CommonEventBus.postHammerCrush(event)) {
                return new RecipeMatchResult(false, null, ingredientId, 0);
            }
        }

        return new RecipeMatchResult(true, match, ingredientId, 0);
    }

    private static HammerCrushRecipe findRecipeFromManager(
            RecipeManager recipeManager,
            String ingredientId, int hammerPower) {

        HammerCrushRecipe match = null;
        int requiredPowerFound = -1;

        for (var holder : recipeManager.getRecipes()) {
            var recipe = holder.value();
            if (recipe.getType() != RecipeTypes.HAMMER_CRUSH)
                continue;

            HammerCrushRecipe crushRecipe = (HammerCrushRecipe) recipe;
            if (!crushRecipe.isRequiredModLoaded())
                continue;
            if (!ingredientId.equals(crushRecipe.getLeftHandItemId()))
                continue;

            if (hammerPower < crushRecipe.getCrushPowerRequired()) {
                requiredPowerFound = Math.max(requiredPowerFound, crushRecipe.getCrushPowerRequired());
                continue;
            }

            match = crushRecipe;
            break;
        }

        return match;
    }

    private static HammerCrushRecipe findRecipeFromResources(ServerLevel level, String ingredientId, int hammerPower) {
        HammerCrushRecipe match = null;
        ResourceManager resourceManager = level.getServer().getResourceManager();

        try {
            Map<ResourceLocation, Resource> allResources = resourceManager.listResources(
                    "recipes",
                    rl -> "dustandore".equals(rl.getNamespace()) && rl.getPath().endsWith(".json"));

            for (var entry : allResources.entrySet()) {
                try (InputStream in = entry.getValue().open();
                        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {

                    JsonElement element = JsonParser.parseReader(reader);
                    if (!element.isJsonObject())
                        continue;

                    JsonObject obj = element.getAsJsonObject();
                    String type1 = obj.has("type") ? obj.get("type").getAsString() : null;
                    String type2 = obj.has("fabric:type") ? obj.get("fabric:type").getAsString() : null;

                    if (!"dustandore:hammer_crush".equals(type1) && !"dustandore:hammer_crush".equals(type2)) {
                        continue;
                    }

                    DataResult<HammerCrushRecipe> dataResult = com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe.CODEC
                            .codec().parse(com.mojang.serialization.JsonOps.INSTANCE, obj);

                    HammerCrushRecipe recipe = dataResult.result().orElse(null);
                    if (recipe == null)
                        continue;
                    if (!recipe.isRequiredModLoaded())
                        continue;
                    if (!ingredientId.equals(recipe.getLeftHandItemId()))
                        continue;

                    if (hammerPower < recipe.getCrushPowerRequired()) {
                        continue;
                    }

                    match = recipe;
                    break;
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        return match;
    }

    private static int findRequiredPower(RecipeManager recipeManager, String ingredientId) {
        int requiredPower = -1;
        for (var holder : recipeManager.getRecipes()) {
            var recipe = holder.value();
            if (recipe.getType() != RecipeTypes.HAMMER_CRUSH)
                continue;

            HammerCrushRecipe crushRecipe = (HammerCrushRecipe) recipe;
            if (!crushRecipe.isRequiredModLoaded())
                continue;
            if (!ingredientId.equals(crushRecipe.getLeftHandItemId()))
                continue;

            requiredPower = Math.max(requiredPower, crushRecipe.getCrushPowerRequired());
        }
        return requiredPower;
    }

    private static void processCrushing(ServerLevel level, LivingEntity living, Player player,
            HammerCrushRecipe recipe) {

        ItemStack hammer = living.getMainHandItem();
        ItemStack offhandItem = living.getOffhandItem();

        int oresToProcess = calculateOresToProcess(level, player, hammer, offhandItem);

        if (oresToProcess <= 0) {
            return;
        }

        List<ItemStack> results = calculateRewardsForMultipleOres(level, player, hammer, recipe, oresToProcess);
        if (results.isEmpty())
            return;

        applyCrushingFeedback(level, living, player, recipe, results, oresToProcess);
    }

    private static List<ItemStack> calculateRewardsForMultipleOres(ServerLevel level, Player player, ItemStack hammer,
            HammerCrushRecipe recipe, int oresToProcess) {

        List<RewardEntry> rewards = recipe.getRewards();
        if (rewards == null || rewards.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        int fortune = EnchantmentUtilities.getLevel(level, player, "minecraft:fortune");
        int fortuneIndex = Math.min(Math.max(fortune, 0), 3);
        RandomSource rng = level.getRandom();

        Map<Item, Integer> combinedResults = new java.util.HashMap<>();
        boolean chanceRolled = false;

        for (int oreIndex = 0; oreIndex < oresToProcess; oreIndex++) {
            for (RewardEntry rewardEntry : rewards) {
                double chance = rewardEntry.getChanceForIndex(fortuneIndex);
                if (chance <= 0.0f)
                    continue;

                int min = rewardEntry.countMin[fortuneIndex];
                int max = rewardEntry.countMax[fortuneIndex];
                if (min <= 0 || max < min)
                    continue;

                Reference<Item> outHolder = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .get(net.minecraft.resources.ResourceLocation.parse(rewardEntry.itemId))
                        .orElse(null);
                Item outItem = outHolder != null ? outHolder.value() : null;
                if (outItem == null)
                    continue;

                int count = min + (max > min ? rng.nextInt(max - min + 1) : 0);

                boolean granted = true;
                if (player instanceof ServerPlayer sp) {
                    chanceRolled = true;
                    ItemStack candidate = new ItemStack(outItem, count);
                    try {
                        granted = com.bobotweaks.dustandore.events.CrushingEventHandler
                                .CrushingEventTriggered(level, sp, hammer, candidate, recipe, chance);
                    } catch (Throwable ignored) {
                        granted = false;
                    }
                }

                if (granted && count > 0) {
                    combinedResults.merge(outItem, count, Integer::sum);
                }
            }
        }

        if (combinedResults.isEmpty()) {
            if (chanceRolled) {
                applyCooldown(level, player, hammer, recipe);
            }
            return java.util.Collections.emptyList();
        }

        List<ItemStack> grantedStacks = new java.util.ArrayList<>();
        for (Map.Entry<Item, Integer> entry : combinedResults.entrySet()) {
            if (entry.getValue() > 0) {
                grantedStacks.add(new ItemStack(entry.getKey(), entry.getValue()));
            }
        }

        return grantedStacks;
    }

    private static List<ItemStack> calculateRewards(ServerLevel level, Player player, ItemStack hammer,
            HammerCrushRecipe recipe) {
        List<RewardEntry> rewards = recipe.getRewards();
        if (rewards == null || rewards.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        int fortune = EnchantmentUtilities.getLevel(level, player, "minecraft:fortune");
        int fortuneIndex = Math.min(Math.max(fortune, 0), 3);
        RandomSource rng = level.getRandom();

        List<ItemStack> grantedStacks = new java.util.ArrayList<>();
        boolean chanceRolled = false;

        for (RewardEntry rewardEntry : rewards) {
            double chance = rewardEntry.getChanceForIndex(fortuneIndex);
            if (chance <= 0.0f)
                continue;

            int min = rewardEntry.countMin[fortuneIndex];
            int max = rewardEntry.countMax[fortuneIndex];
            if (min <= 0 || max < min)
                continue;

            Reference<Item> outHolder = net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .get(net.minecraft.resources.ResourceLocation.parse(rewardEntry.itemId))
                    .orElse(null);
            Item outItem = outHolder != null ? outHolder.value() : null;
            if (outItem == null)
                continue;

            int count = min + (max > min ? rng.nextInt(max - min + 1) : 0);
            ItemStack candidate = new ItemStack(outItem, count);

            boolean granted = true;
            if (player instanceof ServerPlayer sp) {
                chanceRolled = true;
                try {
                    granted = com.bobotweaks.dustandore.events.CrushingEventHandler
                            .CrushingEventTriggered(level, sp, hammer, candidate, recipe, chance);
                } catch (Throwable ignored) {
                    granted = false;
                }
            }

            if (granted && !candidate.isEmpty()) {
                grantedStacks.add(candidate);
            }
        }

        if (grantedStacks.isEmpty()) {
            if (chanceRolled) {
                applyCooldown(level, player, hammer, recipe);
            }
            return java.util.Collections.emptyList();
        }

        return grantedStacks;
    }

    private static void applyCrushingFeedback(ServerLevel level, LivingEntity living, Player player,
            HammerCrushRecipe recipe, List<ItemStack> results, int oresToProcess) {

        living.swing(InteractionHand.MAIN_HAND, true);
        living.swing(InteractionHand.OFF_HAND, true);

        applyDurabilityCost(level, living, living.getMainHandItem(), recipe, oresToProcess);

        applyCooldown(level, player, living.getMainHandItem(), recipe);

        consumeMultipleOffhandItems(player, oresToProcess);
        for (ItemStack result : results) {
            if (result == null || result.isEmpty())
                continue;
            giveOrDropItem(player, result);
        }
    }

    private static void consumeMultipleOffhandItems(Player player, int count) {
        if (player.isCreative())
            return;
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            offhand.shrink(count);
        }
    }

    private static void applyDurabilityCost(ServerLevel level, LivingEntity living, ItemStack hammer,
            HammerCrushRecipe recipe, int oresProcessed) {

        int durabilityCost = Math.max(1, recipe.getDurabilityCost()) * oresProcessed;

        if (EnchantmentUtilities.isUnbreakable(hammer))
            return;

        if (hammer.isDamageableItem()) {
            int unbreakingLevel = EnchantmentUtilities.getLevel(level, living, "minecraft:unbreaking");
            int damageToApply = calculateDamageToApply(durabilityCost, unbreakingLevel, level.getRandom());

            if (damageToApply > 0) {
                hammer.hurtAndBreak(damageToApply, level, null, broken -> {
                    ModSounds.playItemBreakSound(level, living.getX(), living.getY(), living.getZ(), living);
                });
            }
        }
    }

    private static int calculateDamageToApply(int baseDamage, int unbreakingLevel, RandomSource rng) {
        if (unbreakingLevel <= 0) {
            return baseDamage;
        }

        float nerfFactor = 0.25f;
        float probability = 1.0f / (1.0f + nerfFactor * unbreakingLevel);
        int actualDamage = 0;

        for (int i = 0; i < baseDamage; i++) {
            if (rng.nextFloat() < probability) {
                actualDamage++;
            }
        }

        int minDamage = Math.max(1, (int) Math.ceil(0.70f * baseDamage));
        return Math.max(actualDamage, minDamage);
    }

    private static int calculateOresToProcess(ServerLevel level, Player player, ItemStack hammer,
            ItemStack offhandItem) {
        int oreShatterLevel = EnchantmentUtilities.getLevel(level, player, "dustandore:ore_shatter");

        if (oreShatterLevel <= 0) {
            return 1;
        }

        int maxOres;
        switch (oreShatterLevel) {
            case 1:
                maxOres = 2;
                break;
            case 2:
                maxOres = 4;
                break;
            case 3:
                maxOres = 8;
                break;
            default:
                maxOres = 1;
        }

        return Math.min(maxOres, offhandItem.getCount());
    }

    private static void applyCooldown(ServerLevel level, Player player, ItemStack hammer, HammerCrushRecipe recipe) {
        int baseCooldown = CooldownHandler.getBaseCooldown(1, recipe.getCooldownTicks());
        float speedBase = SpeedLoader.getHammerSpeedBase(level, hammer.getItem());
        int efficiencyLevel = EnchantmentUtilities.getLevel(level, player, "minecraft:efficiency");
        float effectiveSpeed = SpeedLoader.effectiveSpeed(speedBase, efficiencyLevel);
        int finalCooldown = CooldownHandler.getFinalCooldown(baseCooldown, effectiveSpeed);

        CooldownHandler.applyCooldown(level, player, hammer, finalCooldown);
    }

    private static boolean isHoldingHammer(Entity entity) {
        if (!(entity instanceof LivingEntity living))
            return false;
        return HammerUtilities.isCrushingHammer(living.getMainHandItem().getItem());
    }

    private static void consumeOffhandItem(Player player) {
        if (player.isCreative())
            return;
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            offhand.shrink(1);
        }
    }

    private static void giveOrDropItem(Player player, ItemStack item) {
        ItemStack copy = item.copy();
        if (!player.addItem(copy)) {
            player.drop(copy, false);
        }
    }
}
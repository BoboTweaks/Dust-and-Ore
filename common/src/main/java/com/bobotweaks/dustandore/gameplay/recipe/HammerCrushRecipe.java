package com.bobotweaks.dustandore.gameplay.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.DataResult;

import com.bobotweaks.dustandore.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.level.Level;
import java.util.Optional;

public class HammerCrushRecipe implements Recipe<RecipeInput> {
    private final ResourceLocation id;
    private final String leftHandItemId;
    private final int crushPowerRequired;
    private final int durabilityCost;
    private final int cooldownTicks;
    private final String crushingSound;
    private final List<RewardEntry> rewards;
    private final String requiredMod;

    public HammerCrushRecipe(ResourceLocation id,
            String leftHandItemId,
            int crushPowerRequired,
            int durabilityCost,
            int cooldownTicks,
            String crushingSound,
            List<RewardEntry> rewards) {
        this(id, leftHandItemId, crushPowerRequired, durabilityCost, cooldownTicks, crushingSound, rewards, null);
    }

    public HammerCrushRecipe(ResourceLocation id,
            String leftHandItemId,
            int crushPowerRequired,
            int durabilityCost,
            int cooldownTicks,
            String crushingSound,
            List<RewardEntry> rewards,
            String requiredMod) {
        this.id = id;
        this.leftHandItemId = Objects.requireNonNull(leftHandItemId);
        this.crushPowerRequired = crushPowerRequired;
        this.durabilityCost = durabilityCost;
        this.cooldownTicks = cooldownTicks;
        this.crushingSound = crushingSound;
        this.rewards = rewards != null ? rewards : new ArrayList<>();
        this.requiredMod = normalizeRequiredMod(requiredMod);
    }

    public String getLeftHandItemId() {
        return leftHandItemId;
    }

    public int getCrushPowerRequired() {
        return crushPowerRequired;
    }

    public int getDurabilityCost() {
        return durabilityCost;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public String getCrushingSound() {
        return crushingSound;
    }

    public List<RewardEntry> getRewards() {
        return rewards;
    }

    public String getRequiredMod() {
        return requiredMod;
    }

    // Codec for datapack loading
    private static final Codec<int[]> INT4_CODEC = Codec.list(Codec.INT)
            .comapFlatMap(list -> {
                if (list.size() != 4)
                    return DataResult.error(() -> "count arrays must have exactly 4 elements");
                int[] arr = new int[4];
                for (int i = 0; i < 4; i++)
                    arr[i] = list.get(i);
                return DataResult.success(arr);
            }, ints -> java.util.List.of(ints[0], ints[1], ints[2], ints[3]));

    private static final Codec<double[]> DOUBLE4_CODEC = Codec.list(Codec.doubleRange(0.0, 1.0))
            .comapFlatMap(list -> {
                if (list.size() != 4)
                    return DataResult.error(() -> "chance arrays must have exactly 4 elements");
                double[] arr = new double[4];
                for (int i = 0; i < 4; i++)
                    arr[i] = list.get(i);
                return DataResult.success(arr);
            }, doubles -> java.util.List.of(doubles[0], doubles[1], doubles[2], doubles[3]));

    public static final Codec<RewardEntry> REWARD_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.xmap(ResourceLocation::toString, ResourceLocation::parse)
                    .fieldOf("item").forGetter(r -> r.itemId),
            INT4_CODEC.fieldOf("count_min").forGetter(r -> r.countMin),
            INT4_CODEC.fieldOf("count_max").forGetter(r -> r.countMax),
            DOUBLE4_CODEC.optionalFieldOf("chance", RewardEntry.defaultChance()).forGetter(r -> r.chance))
            .apply(inst, RewardEntry::new));

    private static final Codec<List<RewardEntry>> REWARDS_CODEC = Codec.either(REWARD_CODEC, REWARD_CODEC.listOf())
            .xmap(e -> e.map(r -> java.util.List.of(r), l -> l),
                    list -> list.size() == 1 ? com.mojang.datafixers.util.Either.left(list.get(0))
                            : com.mojang.datafixers.util.Either.right(list));

    public static final MapCodec<HammerCrushRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.xmap(ResourceLocation::toString, ResourceLocation::parse)
                    .fieldOf("left_hand_item").forGetter(HammerCrushRecipe::getLeftHandItemId),
            Codec.INT.fieldOf("crush_power_required").forGetter(HammerCrushRecipe::getCrushPowerRequired),
            Codec.INT.fieldOf("durability_cost").forGetter(HammerCrushRecipe::getDurabilityCost),
            Codec.INT.optionalFieldOf("cooldown_ticks", 10).forGetter(HammerCrushRecipe::getCooldownTicks),
            Codec.STRING.optionalFieldOf("crushing_sound")
                    .xmap(opt -> opt.orElse(null), s -> java.util.Optional.ofNullable(s))
                    .forGetter(HammerCrushRecipe::getCrushingSound),
            REWARDS_CODEC.fieldOf("reward").forGetter(HammerCrushRecipe::getRewards),
            Codec.STRING.optionalFieldOf("required_mod")
                    .xmap(opt -> opt
                            .flatMap(str -> java.util.Optional.ofNullable(HammerCrushRecipe.normalizeRequiredMod(str))),
                            opt -> opt)
                    .forGetter(recipe -> java.util.Optional.ofNullable(recipe.getRequiredMod())))
            .apply(inst, (left, req, cost, cooldown, crushingS, rewards, requiredModOpt) -> new HammerCrushRecipe(null,
                    left, req, cost, cooldown, crushingS, rewards, requiredModOpt.orElse(null))));

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return HammerCrushRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return com.bobotweaks.dustandore.init.RecipeTypes.HAMMER_CRUSH;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public boolean isRequiredModLoaded() {
        if (this.requiredMod == null)
            return true;
        try {
            return Services.PLATFORM.isModLoaded(this.requiredMod);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean hasRequiredMod() {
        return this.requiredMod != null;
    }

    public static String normalizeRequiredMod(String raw) {
        if (raw == null)
            return null;
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static class RewardEntry {
        private static final double[] DEFAULT_CHANCE = new double[] { 1.0, 1.0, 1.0, 1.0 };

        public final String itemId; // e.g. "dustandore:copper_dust"
        public final int[] countMin; // length 4, index by fortune level [0..3]
        public final int[] countMax; // length 4, index by fortune level [0..3]
        public final double[] chance; // length 4, index by fortune level [0..3]

        public RewardEntry(String itemId, int[] countMin, int[] countMax) {
            this(itemId, countMin, countMax, DEFAULT_CHANCE);
        }

        public RewardEntry(String itemId, int[] countMin, int[] countMax, double[] chance) {
            this.itemId = Objects.requireNonNull(itemId);
            this.countMin = Objects.requireNonNull(countMin);
            this.countMax = Objects.requireNonNull(countMax);
            if (this.countMin.length != 4 || this.countMax.length != 4)
                throw new IllegalArgumentException("count arrays must have exactly 4 elements");
            this.chance = sanitizeChance(chance);
        }

        private static double[] sanitizeChance(double[] source) {
            double[] provided = source != null ? Arrays.copyOf(source, source.length)
                    : Arrays.copyOf(DEFAULT_CHANCE,
                            DEFAULT_CHANCE.length);
            if (provided.length != 4)
                return Arrays.copyOf(DEFAULT_CHANCE, DEFAULT_CHANCE.length);
            for (int i = 0; i < provided.length; i++) {
                double value = provided[i];
                if (value < 0.0)
                    value = 0.0;
                if (value > 1.0)
                    value = 1.0;
                provided[i] = value;
            }
            return provided;
        }

        public double getChanceForIndex(int index) {
            if (index < 0 || index >= this.chance.length)
                return 1.0;
            double value = this.chance[index];
            if (value < 0.0)
                return 0.0;
            if (value > 1.0)
                return 1.0;
            return value;
        }

        public static double[] defaultChance() {
            return Arrays.copyOf(DEFAULT_CHANCE, DEFAULT_CHANCE.length);
        }
    }
}

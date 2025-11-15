package com.bobotweaks.dustandore.gameplay.recipe;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class HammerCrushRecipeSerializer implements RecipeSerializer<HammerCrushRecipe> {
    public static final HammerCrushRecipeSerializer INSTANCE = new HammerCrushRecipeSerializer();

    private static final Codec<int[]> INT4_CODEC = Codec.list(Codec.INT)
            .comapFlatMap(list -> {
                if (list.size() != 4)
                    return DataResult.error(() -> "count arrays must have exactly 4 elements");
                int[] arr = new int[4];
                for (int i = 0; i < 4; i++) arr[i] = list.get(i);
                return DataResult.success(arr);
            }, ints -> java.util.List.of(ints[0], ints[1], ints[2], ints[3]));

    private static final Codec<double[]> DOUBLE4_CODEC = Codec.list(Codec.doubleRange(0.0, 1.0))
            .comapFlatMap(list -> {
                if (list.size() != 4)
                    return DataResult.error(() -> "chance arrays must have exactly 4 elements");
                double[] arr = new double[4];
                for (int i = 0; i < 4; i++) arr[i] = list.get(i);
                return DataResult.success(arr);
            }, doubles -> java.util.List.of(doubles[0], doubles[1], doubles[2], doubles[3]));

    public static final Codec<HammerCrushRecipe.RewardEntry> REWARD_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.xmap(ResourceLocation::toString, ResourceLocation::parse)
                    .fieldOf("item").forGetter(r -> r.itemId),
            INT4_CODEC.fieldOf("count_min").forGetter(r -> r.countMin),
            INT4_CODEC.fieldOf("count_max").forGetter(r -> r.countMax),
            DOUBLE4_CODEC.optionalFieldOf("chance", HammerCrushRecipe.RewardEntry.defaultChance()).forGetter(r -> r.chance)
    ).apply(inst, HammerCrushRecipe.RewardEntry::new));

    // reward can be a single object or array, normalize to list
    private static final Codec<java.util.List<HammerCrushRecipe.RewardEntry>> REWARDS_CODEC = Codec.either(REWARD_CODEC, REWARD_CODEC.listOf())
            .xmap(e -> e.map(r -> java.util.List.of(r), l -> l),
                  list -> list.size() == 1 ? com.mojang.datafixers.util.Either.left(list.get(0)) : com.mojang.datafixers.util.Either.right(list));

    private static final MapCodec<HammerCrushRecipe> RECIPE_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceLocation.CODEC.xmap(ResourceLocation::toString, ResourceLocation::parse)
                    .fieldOf("left_hand_item").forGetter(HammerCrushRecipe::getLeftHandItemId),
            Codec.INT.fieldOf("crush_power_required").forGetter(HammerCrushRecipe::getCrushPowerRequired),
            Codec.INT.optionalFieldOf("durability_cost").xmap(opt -> opt.orElse(1), v -> java.util.Optional.ofNullable(v)).forGetter(HammerCrushRecipe::getDurabilityCost),
            Codec.INT.optionalFieldOf("cooldown_ticks", 10).forGetter(HammerCrushRecipe::getCooldownTicks),
            Codec.STRING.optionalFieldOf("crushing_sound").xmap(opt -> opt.orElse(null), s -> java.util.Optional.ofNullable(s)).forGetter(HammerCrushRecipe::getCrushingSound),
            REWARDS_CODEC.fieldOf("reward").forGetter(HammerCrushRecipe::getRewards),
            Codec.STRING.optionalFieldOf("required_mod").forGetter(recipe -> java.util.Optional.ofNullable(recipe.getRequiredMod()))
    ).apply(inst, (left, req, cost, cooldown, crushingS, rewards, requiredModOpt) -> new HammerCrushRecipe(null, left, req, cost, cooldown, crushingS, rewards, requiredModOpt.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, HammerCrushRecipe> STREAM_CODEC = StreamCodec.of(
        (buf, recipe) -> {
            // core fields
            ResourceLocation.STREAM_CODEC.encode(buf, ResourceLocation.parse(recipe.getLeftHandItemId()));
            buf.writeVarInt(recipe.getCrushPowerRequired());
            buf.writeVarInt(recipe.getDurabilityCost());
            buf.writeVarInt(recipe.getCooldownTicks());
            // sounds (nullable)
            buf.writeBoolean(recipe.getCrushingSound() != null);
            if (recipe.getCrushingSound() != null)
                ResourceLocation.STREAM_CODEC.encode(buf, ResourceLocation.parse(recipe.getCrushingSound()));
            // rewards
            List<HammerCrushRecipe.RewardEntry> rewards = recipe.getRewards();
            buf.writeVarInt(rewards.size());
            for (HammerCrushRecipe.RewardEntry r : rewards) {
                ResourceLocation.STREAM_CODEC.encode(buf, ResourceLocation.parse(r.itemId));
                for (int i = 0; i < 4; i++) buf.writeVarInt(r.countMin[i]);
                for (int i = 0; i < 4; i++) buf.writeVarInt(r.countMax[i]);
                for (int i = 0; i < 4; i++) buf.writeDouble(r.chance[i]);
            }
            String requiredMod = recipe.getRequiredMod();
            buf.writeBoolean(requiredMod != null);
            if (requiredMod != null)
                buf.writeUtf(requiredMod, 64);
        },
        buf -> {
            String left = ResourceLocation.STREAM_CODEC.decode(buf).toString();
            int req = buf.readVarInt();
            int cost = buf.readVarInt();
            int cooldown = buf.readVarInt();
            String crushingS = null;
            if (buf.readBoolean()) crushingS = ResourceLocation.STREAM_CODEC.decode(buf).toString();
            int rewardCount = buf.readVarInt();
            List<HammerCrushRecipe.RewardEntry> rewards = new ArrayList<>(rewardCount);
            for (int j = 0; j < rewardCount; j++) {
                String itemId = ResourceLocation.STREAM_CODEC.decode(buf).toString();
                int[] mins = new int[4];
                int[] maxs = new int[4];
                double[] chance = new double[4];
                for (int k = 0; k < 4; k++) mins[k] = buf.readVarInt();
                for (int k = 0; k < 4; k++) maxs[k] = buf.readVarInt();
                for (int k = 0; k < 4; k++) chance[k] = buf.readDouble();
                rewards.add(new HammerCrushRecipe.RewardEntry(itemId, mins, maxs, chance));
            }
            String requiredMod = null;
            if (buf.readBoolean())
                requiredMod = buf.readUtf(64);
            return new HammerCrushRecipe(null, left, req, cost, cooldown, crushingS, rewards, requiredMod);
        }
    );

    @Override
    public MapCodec<HammerCrushRecipe> codec() {
        return RECIPE_CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, HammerCrushRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

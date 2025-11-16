package com.bobotweaks.dustandore.gameplay;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class TooltipManager {

    private static final String KEY_CP_TEXT = "tooltip.dustandore.hammer_crushing_power_text";
    private static final String KEY_CP_TEXT_COLOR = "tooltip.dustandore.hammer_crushing_power_text_color";
    private static final String KEY_CP_NUMBER_COLOR = "tooltip.dustandore.hammer_crushing_power_number_value_color";
    private static final String KEY_SPEED_TEXT = "tooltip.dustandore.hammer_speed_text";
    private static final String KEY_SPEED_TEXT_COLOR = "tooltip.dustandore.hammer_speed_text_color";
    private static final String KEY_SPEED_NUMBER_COLOR = "tooltip.dustandore.hammer_speed_number_value_color";
    private static final String KEY_HOLD_SHIFT_TEXT = "tooltip.dustandore.hammer_hold_shift_text";
    private static final String KEY_HOLD_SHIFT_COLOR = "tooltip.dustandore.hammer_hold_shift_text_color";
    private static final String KEY_CRUSHABLE_HEADER_TEXT = "tooltip.dustandore.hammer_crushable_materials_text";
    private static final String KEY_CRUSHABLE_HEADER_COLOR = "tooltip.dustandore.hammer_crushable_materials_text_color";
    private static final String KEY_NO_CRUSHABLE_TEXT = "tooltip.dustandore.hammer_no_crushable_text";
    private static final String KEY_NO_CRUSHABLE_COLOR = "tooltip.dustandore.hammer_no_crushable_text_color";

    private static final String DEFAULT_CRUSHING_POWER_TEXT = "Crushing Power: ";
    private static final int DEFAULT_CRUSHING_POWER_TEXT_COLOR = 0xA8A8A8;
    private static final int DEFAULT_CRUSHING_POWER_NUMBER_COLOR = 0x54FC54;
    private static final String DEFAULT_SPEED_TEXT = "Cooldown Speed: ";
    private static final int DEFAULT_SPEED_TEXT_COLOR = 0xA8A8A8;
    private static final int DEFAULT_SPEED_NUMBER_COLOR = 0x54FC54;
    private static final String DEFAULT_HOLD_SHIFT_TEXT = "§6Hold Shift: §8view crushable materials";
    private static final String DEFAULT_CRUSHABLE_HEADER_TEXT = "§6Crushable Materials:";
    private static final String DEFAULT_NO_CRUSHABLE_TEXT = "§8(no crushables found)";

    private static final class HammerProps {
        final int crushPower;
        final float speedBase;
        final int durability;
        final boolean tooltipActive;
        final Boolean showNonShiftOverride;
        final Boolean showCrushablesOverride;

        HammerProps(int crushPower, float speedBase, int durability, boolean tooltipActive,
                Boolean showNonShiftOverride, Boolean showCrushablesOverride) {
            this.crushPower = crushPower;
            this.speedBase = speedBase;
            this.durability = durability;
            this.tooltipActive = tooltipActive;
            this.showNonShiftOverride = showNonShiftOverride;
            this.showCrushablesOverride = showCrushablesOverride;
        }
    }

    private static final Map<String, HammerProps> CACHE = new ConcurrentHashMap<>();

    public static void CreateTooltipFromList(Object stackObj, Object context, java.util.List<?> lines) {
        if (!(stackObj instanceof ItemStack))
            return;
        ItemStack itemstack = (ItemStack) stackObj;
        java.util.function.Consumer<Component> adder = c -> ((java.util.List) lines).add(c);
        CreateTooltip(itemstack, null, null, adder, null);
    }

    public static void CreateTooltipFromList(ItemStack stack, Item.TooltipContext context,
            java.util.List<Component> lines) {
        CreateTooltip(stack, context, null, lines::add, null);
    }

    public static void CreateTooltip(ItemStack itemstack, Item.TooltipContext context, Object tooltipDisplay,
            Consumer<Component> componentConsumer, TooltipFlag flag) {
        if (itemstack == null || itemstack.isEmpty())
            return;
        try {
            TooltipConfig cfg = getTooltipConfig();
            String curId = idOf(itemstack.getItem());
            HammerProps propsForItem = null;
            try {
                propsForItem = curId != null ? getPropsFromResources(curId) : null;
            } catch (Throwable ignored) {

            }

            if (propsForItem == null)
                return;
            boolean showHammerEffective = (cfg == null || cfg.showHammer);
            boolean showCrushableEffective = (cfg == null || cfg.showCrushable);
            if (propsForItem != null) {
                if (!propsForItem.tooltipActive) {
                    showHammerEffective = false;
                    showCrushableEffective = false;
                } else {
                    if (propsForItem.showNonShiftOverride != null)
                        showHammerEffective = propsForItem.showNonShiftOverride.booleanValue();
                    if (propsForItem.showCrushablesOverride != null)
                        showCrushableEffective = propsForItem.showCrushablesOverride.booleanValue();
                }
            }
            if (isShiftDown()) {
                if (!showCrushableEffective) {
                    if (!showHammerEffective)
                        return;
                    String id = curId;
                    if (id == null || id.isEmpty())
                        return;
                    HammerProps props = propsForItem;
                    if (props == null) {
                        return;
                    }

                    String cpLabel = getLocalizedText(KEY_CP_TEXT, DEFAULT_CRUSHING_POWER_TEXT);
                    int cpLabelColor = getLocalizedColor(KEY_CP_TEXT_COLOR, DEFAULT_CRUSHING_POWER_TEXT_COLOR);
                    int cpNumColor = getLocalizedColor(KEY_CP_NUMBER_COLOR, DEFAULT_CRUSHING_POWER_NUMBER_COLOR);
                    componentConsumer.accept(
                            Component.empty()
                                    .append(Component.literal(cpLabel).withStyle(Style.EMPTY.withColor(cpLabelColor)))
                                    .append(Component.literal(String.valueOf(props.crushPower))
                                            .withStyle(Style.EMPTY.withColor(cpNumColor))));

                    float effSpeed = props.speedBase * (1.0f + 0.20f * getEfficiencyLevelClient(itemstack));
                    String spLabel = getLocalizedText(KEY_SPEED_TEXT, DEFAULT_SPEED_TEXT);
                    int spLabelColor = getLocalizedColor(KEY_SPEED_TEXT_COLOR, DEFAULT_SPEED_TEXT_COLOR);
                    int spNumColor = getLocalizedColor(KEY_SPEED_NUMBER_COLOR, DEFAULT_SPEED_NUMBER_COLOR);
                    componentConsumer.accept(
                            Component.empty()
                                    .append(Component.literal(spLabel).withStyle(Style.EMPTY.withColor(spLabelColor)))
                                    .append(Component.literal(trim(effSpeed))
                                            .withStyle(Style.EMPTY.withColor(spNumColor))));

                    return;
                }

                String headerText = getLocalizedText(KEY_CRUSHABLE_HEADER_TEXT, DEFAULT_CRUSHABLE_HEADER_TEXT);
                Integer headerColor = getLocalizedColorOrNull(KEY_CRUSHABLE_HEADER_COLOR);
                MutableComponent headerComp = Component.literal(headerText);
                if (headerColor != null && headerText.indexOf('§') < 0)
                    headerComp = headerComp.withStyle(Style.EMPTY.withColor(headerColor));
                componentConsumer.accept((Component) headerComp);

                int fortune = getFortuneLevelClient(itemstack);
                java.util.List<com.bobotweaks.dustandore.core.CrushingManager.DropEntry> drops = com.bobotweaks.dustandore.core.CrushingManager
                        .getJsonDropsForHammer(itemstack.getItem(), fortune, true);

                if (drops.isEmpty()) {
                    String noText = getLocalizedText(KEY_NO_CRUSHABLE_TEXT, DEFAULT_NO_CRUSHABLE_TEXT);
                    Integer noColor = getLocalizedColorOrNull(KEY_NO_CRUSHABLE_COLOR);
                    MutableComponent noComp = Component.literal(noText);
                    if (noColor != null && noText.indexOf('§') < 0)
                        noComp = noComp.withStyle(Style.EMPTY.withColor(noColor));
                    componentConsumer.accept((Component) noComp);

                    try {
                        String dbg = com.bobotweaks.dustandore.core.CrushingManager
                                .getClientCrushDebug(itemstack.getItem(), 0, true);
                        if (dbg != null && !dbg.isEmpty()) {
                            componentConsumer.accept(Component.literal("§8" + dbg));
                        }
                    } catch (Throwable ignored) {
                    }
                }
                createCrushableTooltips(componentConsumer, drops, fortune);
            } else {
                if (!showHammerEffective)
                    return;
                String id = curId;
                if (id == null || id.isEmpty())
                    return;
                HammerProps props = propsForItem;
                if (props == null) {
                    return;
                }

                String cpLabel = getLocalizedText(KEY_CP_TEXT, DEFAULT_CRUSHING_POWER_TEXT);
                int cpLabelColor = getLocalizedColor(KEY_CP_TEXT_COLOR, DEFAULT_CRUSHING_POWER_TEXT_COLOR);
                int cpNumColor = getLocalizedColor(KEY_CP_NUMBER_COLOR, DEFAULT_CRUSHING_POWER_NUMBER_COLOR);
                componentConsumer.accept(
                        Component.empty()
                                .append(Component.literal(cpLabel).withStyle(Style.EMPTY.withColor(cpLabelColor)))
                                .append(Component.literal(String.valueOf(props.crushPower))
                                        .withStyle(Style.EMPTY.withColor(cpNumColor))));

                float effSpeed = props.speedBase * (1.0f + 0.20f * getEfficiencyLevelClient(itemstack));
                String spLabel = getLocalizedText(KEY_SPEED_TEXT, DEFAULT_SPEED_TEXT);
                int spLabelColor = getLocalizedColor(KEY_SPEED_TEXT_COLOR, DEFAULT_SPEED_TEXT_COLOR);
                int spNumColor = getLocalizedColor(KEY_SPEED_NUMBER_COLOR, DEFAULT_SPEED_NUMBER_COLOR);
                componentConsumer.accept(
                        Component.empty()
                                .append(Component.literal(spLabel).withStyle(Style.EMPTY.withColor(spLabelColor)))
                                .append(Component.literal(trim(effSpeed))
                                        .withStyle(Style.EMPTY.withColor(spNumColor))));

                if (showCrushableEffective) {
                    componentConsumer.accept(Component.literal(" "));
                    String hintText = getLocalizedText(KEY_HOLD_SHIFT_TEXT, DEFAULT_HOLD_SHIFT_TEXT);
                    Integer hintColor = getLocalizedColorOrNull(KEY_HOLD_SHIFT_COLOR);
                    MutableComponent hintComp = Component.literal(hintText);
                    if (hintColor != null && hintText.indexOf('§') < 0)
                        hintComp = hintComp.withStyle(Style.EMPTY.withColor(hintColor));
                    componentConsumer.accept((Component) hintComp);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static boolean isShiftDown() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.options == null || mc.getWindow() == null)
                return false;

            // Prefer the keybinding state
            if (mc.options.keyShift.isDown())
                return true;

            // Fallback: check raw keyboard state for left/right Shift
            var window = mc.getWindow();
            return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                    || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static final class TooltipConfig {
        final boolean showHammer;
        final boolean showCrushable;
        final boolean showRawOre;

        TooltipConfig(boolean showHammer, boolean showCrushable, boolean showRawOre) {
            this.showHammer = showHammer;
            this.showCrushable = showCrushable;
            this.showRawOre = showRawOre;
        }
    }

    private static volatile TooltipConfig TOOLTIP_CONFIG;

    private static TooltipConfig getTooltipConfig() {
        TooltipConfig cfg = TOOLTIP_CONFIG;
        if (cfg != null)
            return cfg;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null)
                return defaultCfg();
            ResourceManager rm = mc.getResourceManager();
            ResourceLocation rl = ResourceLocation.parse("dustandore:properties/dustandore.json");
            java.util.Optional<Resource> resOpt = rm.getResource(rl);
            if (resOpt.isEmpty())
                return setCfg(defaultCfg());
            try (InputStream in = resOpt.get().open();
                    InputStreamReader rdr = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(rdr).getAsJsonObject();
                JsonObject section = root.has("dustandore") ? root.getAsJsonObject("dustandore") : null;
                JsonObject tooltip = section != null && section.has("tooltip") ? section.getAsJsonObject("tooltip")
                        : null;
                boolean showHammer = tooltip != null && tooltip.has("show_hammer_tooltip")
                        ? tooltip.get("show_hammer_tooltip").getAsBoolean()
                        : false;
                boolean showCrushable = tooltip != null && tooltip.has("show_crushable_tooltip")
                        ? tooltip.get("show_crushable_tooltip").getAsBoolean()
                        : true;
                boolean showRawOre = tooltip != null && tooltip.has("show_raw_ore_tooltip")
                        ? tooltip.get("show_raw_ore_tooltip").getAsBoolean()
                        : true;
                return setCfg(new TooltipConfig(showHammer, showCrushable, showRawOre));
            }
        } catch (Throwable ignored) {
        }
        return defaultCfg();
    }

    private static TooltipConfig defaultCfg() {
        return setCfg(new TooltipConfig(true, true, true));
    }

    private static TooltipConfig setCfg(TooltipConfig c) {
        TOOLTIP_CONFIG = c;
        return c;
    }

    private static HammerProps getPropsFromResources(String itemId) {
        HammerProps cached = CACHE.get(itemId);
        if (cached != null)
            return cached;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null)
                return null;
            ResourceManager rm = mc.getResourceManager();
            ResourceLocation rlItem = ResourceLocation.parse(itemId);
            String propsPath = rlItem.getNamespace() + ":" + "properties/" + rlItem.getPath() + ".json";
            ResourceLocation propsRL = ResourceLocation.parse(propsPath);
            java.util.Optional<Resource> resOpt = rm.getResource(propsRL);
            InputStream in = null;
            InputStreamReader rdr = null;
            try {
                if (resOpt.isPresent()) {
                    in = resOpt.get().open();
                } else {
                    String dataPath = "data/" + rlItem.getNamespace() + "/properties/" + rlItem.getPath() + ".json";
                    in = TooltipManager.class.getClassLoader().getResourceAsStream(dataPath);
                    if (in == null) {
                        return null;
                    }
                }
                rdr = new InputStreamReader(in, StandardCharsets.UTF_8);
                JsonObject obj = JsonParser.parseReader(rdr).getAsJsonObject();
                int cp = obj.has("crush_power") ? Math.max(0, obj.get("crush_power").getAsInt()) : 0;
                float sb = obj.has("speed_base") ? Math.max(0f, obj.get("speed_base").getAsFloat()) : 1.0f;
                int dur = obj.has("durability") ? Math.max(1, obj.get("durability").getAsInt())
                        : itemstackDefaultDurability();
                try {
                    com.bobotweaks.dustandore.gameplay.hammer.PowerLoader.putHammerPowerCache(itemId, cp);
                } catch (Throwable ignored) {

                }
                boolean active = true;
                Boolean showNonShift = null;
                Boolean showCrushables = null;
                if (obj.has("tooltip") && obj.get("tooltip").isJsonObject()) {
                    JsonObject t = obj.getAsJsonObject("tooltip");
                    active = !t.has("active") || t.get("active").getAsBoolean();
                    if (t.has("show_non_shift"))
                        showNonShift = Boolean.valueOf(t.get("show_non_shift").getAsBoolean());
                    if (t.has("show_crushables")) {
                        showCrushables = Boolean.valueOf(t.get("show_crushables").getAsBoolean());
                    } else if (t.has("show_crushable_materials")) {
                        showCrushables = Boolean.valueOf(t.get("show_crushable_materials").getAsBoolean());
                    }
                }
                HammerProps hp = new HammerProps(cp, sb, dur, active, showNonShift, showCrushables);
                CACHE.put(itemId, hp);
                return hp;
            } finally {
                try {
                    if (rdr != null)
                        rdr.close();
                } catch (Throwable ignore) {
                }
                try {
                    if (in != null)
                        in.close();
                } catch (Throwable ignore) {
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getLocalizedText(String key, String fallback) {
        String value = getLocalizedValue(key);
        return (value != null && !value.isEmpty()) ? value : fallback;
    }

    private static Integer getLocalizedColorOrNull(String key) {
        String value = getLocalizedValue(key);
        if (value == null || value.isEmpty())
            return null;
        try {
            return parseColorString(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int getLocalizedColor(String key, int fallback) {
        Integer color = getLocalizedColorOrNull(key);
        return color != null ? color.intValue() : fallback;
    }

    private static String getLocalizedValue(String key) {
        try {
            if (key != null && I18n.exists(key)) {
                String value = I18n.get(key);
                if (value != null)
                    return value;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static int parseColorString(String s) {
        if (s.startsWith("#"))
            return Integer.parseInt(s.substring(1), 16);
        if (s.startsWith("0x") || s.startsWith("0X"))
            return Integer.parseInt(s.substring(2), 16);
        return Integer.parseInt(s, 16);
    }

    private static void createCrushableTooltips(Consumer<Component> componentConsumer,
            List<com.bobotweaks.dustandore.core.CrushingManager.DropEntry> drops,
            int fortune) {
        for (com.bobotweaks.dustandore.core.CrushingManager.DropEntry e : drops) {
            componentConsumer.accept(
                    GetCrushableMaterialTooltip(
                            e.ingredientId, e.outputItemId, e.min, e.max, e.chance));
        }
    }

    private static int getEfficiencyLevelClient(ItemStack stack) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null)
                return 0;
            HolderLookup<Enchantment> lookup = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            Holder<Enchantment> holder = lookup.get(Enchantments.EFFICIENCY).orElse(null);
            if (holder == null)
                return 0;
            return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
        } catch (Throwable t) {
            return 0;
        }
    }

    private static int getFortuneLevelClient(ItemStack stack) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null)
                return 0;
            HolderLookup<Enchantment> lookup = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            Holder<Enchantment> holder = lookup.get(Enchantments.FORTUNE).orElse(null);
            if (holder == null)
                return 0;
            return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
        } catch (Throwable t) {
            return 0;
        }
    }

    private static String idOf(Item item) {
        ResourceLocation rl = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        return rl == null ? null : rl.toString();
    }

    private static String trim(float f) {
        return String.format(java.util.Locale.ROOT, "%.1f", f);
    }

    private static int itemstackDefaultDurability() {
        return 100;
    }

    public static Component GetCrushableMaterialTooltip(String material, String output, int min, int max,
            double chance) {
        String mat = displayNameFromIngredient(material);
        String out = displayNameFromId(output);

        String chanceText = "";
        if (chance < 1.0) {
            chanceText = String.format(" §8(%.0f%%)", chance * 100);
        }

        return Component.literal("§8• " + mat + " → §f" + min + "-" + max + " " + out + chanceText);
    }

    private static String displayNameFromIngredient(String id) {
        return displayNameFromId(id);
    }

    private static String capitalizeWords(String s) {
        String[] parts = s.split(" ");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty())
                continue;
            String p = parts[i];
            b.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1)
                b.append(p.substring(1));
            if (i < parts.length - 1)
                b.append(' ');
        }
        return b.toString();
    }

    public static String displayNameFromId(String id) {
        if (id == null)
            return "???";
        String trimmed = id.trim();
        if (trimmed.isEmpty())
            return "???";
        boolean isTag = trimmed.startsWith("#");
        String raw = isTag ? trimmed.substring(1) : trimmed;
        try {
            ResourceLocation rl = ResourceLocation.parse(raw);
            try {
                Item item = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
                if (item != null) {
                    try {
                        ItemStack stack = new ItemStack(item);
                        Component c = stack.getHoverName();
                        if (c != null) {
                            String s = c.getString();
                            if (s != null && !s.isEmpty())
                                return s;
                        }
                    } catch (Throwable ignored) {
                    }
                    try {
                        String key = item.getDescriptionId();
                        if (key != null && !key.isEmpty() && I18n.exists(key)) {
                            String value = I18n.get(key);
                            if (value != null && !value.isEmpty())
                                return value;
                        }
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }
            String[] guessKeys = { "item." + rl.getNamespace() + "." + rl.getPath(),
                    "block." + rl.getNamespace() + "." + rl.getPath() };
            for (String key : guessKeys) {
                try {
                    if (I18n.exists(key)) {
                        String value = I18n.get(key);
                        if (value != null && !value.isEmpty())
                            return value;
                    }
                } catch (Throwable ignored) {
                }
            }
            String pretty = capitalizeWords(rl.getPath().replace('_', ' ').replace('/', ' '));
            if (pretty != null && !pretty.isEmpty())
                return isTag ? "#" + pretty : pretty;
        } catch (Throwable ignored) {
        }
        String cleaned = raw.replace(':', ' ').replace('_', ' ').replace('/', ' ');
        String pretty = capitalizeWords(cleaned);
        if (pretty == null || pretty.isEmpty())
            pretty = raw;
        return isTag ? "#" + pretty : pretty;
    }

    public static void appendRawOreTooltip(ItemStack stack, java.util.List<Component> lines) {
        if (stack == null || stack.isEmpty())
            return;

        try {
            // First: check if this item is actually a hammer_crush left_hand_item
            String dustId = com.bobotweaks.dustandore.core.CrushingManager
                    .getDustItemIdForIngredient(stack.getItem());
            if (dustId == null || dustId.isEmpty())
                return; // not a crushable raw ore -> no hint, no tooltip

            // If Shift is NOT held, only show the hint and stop
            if (!isShiftDown()) {
                String hint = getLocalizedValue("tooltip.dustandore.raw_ore_hold_shift_text");
                if (hint == null || hint.isEmpty())
                    hint = getLocalizedValue("tooltip.dustandore.hold_shift");

                if (hint != null && !hint.isEmpty()) {
                    lines.add(net.minecraft.network.chat.Component.literal(hint));
                }
                return;
            }

            // Shift IS held -> show full raw ore description
            String dustName = displayNameFromId(dustId);

            String template = getLocalizedValue("tooltip.dustandore.raw_ore_desc");
            if (template == null || template.isEmpty())
                return;

            String text = template.replace("{dust_name}", dustName);
            lines.add(net.minecraft.network.chat.Component.literal(text));
        } catch (Throwable ignored) {
        }
    }
}

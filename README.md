# Dust & Ore

Dust & Ore is a Minecraft mod that adds a simple but powerful crushing system and metal dusts, available for **Fabric**, **Forge**, and **NeoForge**.

Crush raw ores with specialized Crushing Hammers to obtain dusts, then smelt those dusts for your ingots. The mod is designed to be datapack‑friendly and play nicely with other content mods.

---

## Features

- **Crushing Hammers**
  - Multiple tiers of Crushing Hammer (e.g. Copper, Iron, Gold, Diamond, Netherite).
  - Each hammer has its own **Crushing Power** and **Speed**.
  - Hammers consume durability and have a short cooldown between crushes.

- **Hammer Crushing Recipes**
  - JSON‑based `dustandore:hammer_crush` recipes.
  - Fortune‑aware outputs:
    - Per‑fortune `count_min` / `count_max` values per recipe.
  - Optional `"required_mod"` field to conditionally enable recipes only when a specific mod is loaded.
  - Server‑authoritative via datapacks (no hardcoded recipes).

- **Metal Dusts**
  - Dust variants for vanilla metals (e.g. Copper Dust, Iron Dust, Gold Dust, Zinc Dust).
  - Dusts can be smelted in a Furnace or Blast Furnace into their ingot forms.

- **Smart Tooltips**
  - **Hammer tooltips** show:
    - Crushing power, speed, and crushable materials.
    - Correct per‑fortune output ranges for each recipe.
  - **Raw ore tooltips**:
    - When an item is a `left_hand_item` in a `dustandore:hammer_crush` recipe, hovering it:
      - Shows a “Hold Shift: view more content” hint.
      - Shows a detailed description (with the actual dust name) when Shift is held.
    - Recipes with `required_mod` only show these tooltips if the required mod is present.

---

## How to Use

1. **Craft a Crushing Hammer**  
   Craft any Crushing Hammer.

2. **Crush raw ores**
   - Hold a Crushing Hammer in your **main hand**.
   - Hold a crushable raw ore (or other crushable item) in your **off‑hand**.
   - Right‑click to crush and get dusts.
   - Fortune on the hammer affects the amount of dust produced.

3. **Smelt dusts**
   - Put dusts in a Furnace or Blast Furnace.
   - Get the corresponding ingot.

Tooltips and JEI (for Neoforge) integration help you discover what is crushable and what you get out of it.

---

## Supported Loaders & Versions

- **Minecraft:** 1.21.8  
- **Loaders:**
  - Fabric
  - Forge
  - NeoForge

Each loader has its own module (`fabric`, `forge`, `neoforge`), sharing common code in the `common` module.

---

## Datapack & Mod Integration

- All crushing recipes are defined via datapacks as `dustandore:hammer_crush` recipes.
- Recipes support:
  - `left_hand_item`: the item to be crushed.
  - `crush_power_required`, `durability_cost`, `cooldown_ticks`, `crushing_sound`.
  - `reward`: output dust(s) with per‑fortune `count_min` / `count_max` and optional `chance`.
  - `required_mod`: optional mod id string; recipe is only active if that mod is loaded.
- This makes it easy for other mods or packs to add or adjust crushing behavior.

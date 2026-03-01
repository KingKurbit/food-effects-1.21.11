package net.kingkurbit.foodeffects;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FoodFxModifiers {

    // ---------- TUNABLE SETTINGS ----------
    // How long the "same-food streak" lasts (ticks). 20 ticks = 1 second.
    private static final long FATIGUE_WINDOW_TICKS = 20L * 45L; // 45 seconds

    // How much chance gets reduced per extra same-food eat in the window
    private static final double FATIGUE_STEP = 0.12; // 12% less each streak step

    // Minimum multiplier so it never becomes impossible
    private static final double FATIGUE_MIN_MULT = 0.40;

    // Bonuses
    private static final double FULL_HUNGER_BONUS = 0.20;  // +20%
    private static final double LOW_HP_BONUS = 0.25;       // +25%

    // Synergy bonuses
    private static final double WEATHER_WATER_BONUS = 0.25; // +25% in rain for water foods
    private static final double BIOME_BERRY_BONUS = 0.25;   // +25% in berry biomes for berries
    private static final double BIOME_FISH_BONUS = 0.25;    // +25% in water biomes for fish

    // ---------- FATIGUE STATE ----------
    private static final Map<UUID, Map<Item, Streak>> STREAKS = new HashMap<>();

    private record Streak(long lastTick, int count) {}

    public static double computeMultiplier(UUID playerId,
                                           long serverTick,
                                           Item item,
                                           boolean isFullHunger,
                                           boolean isLowHealth,
                                           boolean isRaining,
                                           String biomeId) {

        double mult = 1.0;

        // --- Food Fatigue (streak on same item) ---
        int streakCount = updateAndGetStreakCount(playerId, serverTick, item);

        // streakCount = 1 means first time (no penalty)
        if (streakCount > 1) {
            double fatigueMult = 1.0 - (FATIGUE_STEP * (streakCount - 1));
            if (fatigueMult < FATIGUE_MIN_MULT) fatigueMult = FATIGUE_MIN_MULT;
            mult *= fatigueMult;
        }

        // --- Full Hunger Bonus ---
        if (isFullHunger) mult *= (1.0 + FULL_HUNGER_BONUS);

        // --- Low Health Boost ---
        if (isLowHealth) mult *= (1.0 + LOW_HP_BONUS);

        // --- Weather Synergy (rain boosts water foods) ---
        if (isRaining && isWaterFood(item)) {
            mult *= (1.0 + WEATHER_WATER_BONUS);
        }

        // --- Biome-Based Boosts ---
        if (biomeId != null) {
            if (isBerryFood(item) && isBerryBiome(biomeId)) {
                mult *= (1.0 + BIOME_BERRY_BONUS);
            }
            if (isFishFood(item) && isFishBiome(biomeId)) {
                mult *= (1.0 + BIOME_FISH_BONUS);
            }
        }

        return mult;
    }

    private static int updateAndGetStreakCount(UUID playerId, long tick, Item item) {
        Map<Item, Streak> map = STREAKS.computeIfAbsent(playerId, k -> new HashMap<>());
        Streak prev = map.get(item);

        if (prev == null) {
            map.put(item, new Streak(tick, 1));
            return 1;
        }

        // If within window, increase streak; otherwise reset
        if (tick - prev.lastTick <= FATIGUE_WINDOW_TICKS) {
            int newCount = prev.count + 1;
            map.put(item, new Streak(tick, newCount));
            return newCount;
        } else {
            map.put(item, new Streak(tick, 1));
            return 1;
        }
    }

    private static boolean isWaterFood(Item item) {
        return item == Items.DRIED_KELP
                || item == Items.COD || item == Items.SALMON || item == Items.TROPICAL_FISH
                || item == Items.COOKED_COD || item == Items.COOKED_SALMON
                || item == Items.MELON_SLICE;
    }

    private static boolean isBerryFood(Item item) {
        return item == Items.SWEET_BERRIES || item == Items.GLOW_BERRIES;
    }

    private static boolean isFishFood(Item item) {
        return item == Items.COD || item == Items.SALMON || item == Items.TROPICAL_FISH
                || item == Items.COOKED_COD || item == Items.COOKED_SALMON;
    }

    // simple string contains checks so you don't need biome registries gymnastics
    private static boolean isBerryBiome(String biomeId) {
        // forests/taigas are berry-ish vibes; tweak as you want
        String b = biomeId.toLowerCase();
        return b.contains("forest") || b.contains("taiga") || b.contains("grove") || b.contains("old_growth");
    }

    private static boolean isFishBiome(String biomeId) {
        String b = biomeId.toLowerCase();
        return b.contains("ocean") || b.contains("river") || b.contains("beach") || b.contains("swamp");
    }
}
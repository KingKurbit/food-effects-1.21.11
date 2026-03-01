// FILE: src/main/java/net/kingkurbit/foodeffects/FoodFxData.java
package net.kingkurbit.foodeffects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class FoodFxData {

    // ---------------- RULES ----------------
    private static final Map<Item, List<Roll>> RULES = new HashMap<>();
    private static final Random RNG = new Random();

    private record Roll(double chance, StatusEffectInstance effect) {}

    // ---------------- FATIGUE ----------------
    private static final Map<UUID, Map<Item, Streak>> STREAKS = new HashMap<>();
    private record Streak(long tick, int count) {}

    // Food Fatigue settings
    private static final long FATIGUE_WINDOW = 20L * 45L; // 45 seconds
    private static final double FATIGUE_STEP = 0.12;      // -12% multiplier per extra same-food eat
    private static final double FATIGUE_MIN = 0.40;       // minimum multiplier

    // Bonuses
    private static final double FULL_HUNGER_BONUS = 0.20; // +20%
    private static final double LOW_HP_BONUS = 0.25;      // +25%

    // Synergies
    private static final double RAIN_WATER_BONUS = 0.25;  // +25% in rain for water foods
    private static final double BERRY_BIOME_BONUS = 0.25; // +25% in forest/taiga for berries
    private static final double FISH_BIOME_BONUS = 0.25;  // +25% in ocean/river for fish

    // ---------------- SOUNDS (PACKET, MAPPING-PROOF) ----------------
    // Using registry entries avoids the RegistryEntry<SoundEvent> vs SoundEvent mess.
    private static final Reference<SoundEvent> PROC_GOOD =
            Registries.SOUND_EVENT.getEntry(Identifier.of("minecraft", "entity.experience_orb.pickup")).orElseThrow();
    private static final Reference<SoundEvent> PROC_BAD =
            Registries.SOUND_EVENT.getEntry(Identifier.of("minecraft", "block.note_block.bass")).orElseThrow();

    public static void init() {

        // Fruits & Crops
        add(Items.APPLE, 0.28, fx(StatusEffects.SPEED, secs(8), 0));
        add(Items.MELON_SLICE, 0.14, fx(StatusEffects.WATER_BREATHING, secs(20), 0));

        add(Items.SWEET_BERRIES, 0.24, fx(StatusEffects.JUMP_BOOST, secs(45), 0));
        add(Items.SWEET_BERRIES, 0.06, fx(StatusEffects.NAUSEA, secs(10), 0));

        add(Items.GLOW_BERRIES, 0.34, fx(StatusEffects.NIGHT_VISION, secs(90), 0));
        add(Items.GLOW_BERRIES, 0.08, fx(StatusEffects.MINING_FATIGUE, secs(20), 0));

        add(Items.CARROT, 0.18, fx(StatusEffects.NIGHT_VISION, secs(30), 0));
        add(Items.GOLDEN_CARROT, 0.16, fx(StatusEffects.REGENERATION, secs(6), 0));

        add(Items.POTATO, 0.32, fx(StatusEffects.SPEED, secs(20), 0));
        add(Items.BAKED_POTATO, 0.20, fx(StatusEffects.HASTE, secs(50), 0));

        add(Items.POISONOUS_POTATO, 0.18, fx(StatusEffects.STRENGTH, secs(5), 0));

        add(Items.BEETROOT, 0.24, fx(StatusEffects.REGENERATION, secs(2), 0));
        add(Items.BEETROOT, 0.03, fx(StatusEffects.STRENGTH, secs(15), 0));

        add(Items.DRIED_KELP, 0.18, fx(StatusEffects.WATER_BREATHING, secs(60), 0));

        // Crafted / Meals
        add(Items.BREAD, 0.18, fx(StatusEffects.RESISTANCE, secs(5), 0));
        add(Items.COOKIE, 0.32, fx(StatusEffects.SPEED, secs(25), 0));
        add(Items.PUMPKIN_PIE, 0.26, fx(StatusEffects.JUMP_BOOST, secs(25), 0));

        add(Items.MUSHROOM_STEW, 0.22, fx(StatusEffects.SLOW_FALLING, secs(30), 0));
        add(Items.RABBIT_STEW, 0.24, fx(StatusEffects.FIRE_RESISTANCE, secs(30), 0));

        add(Items.BEETROOT_SOUP, 0.26, fx(StatusEffects.REGENERATION, secs(2), 0));
        add(Items.BEETROOT_SOUP, 0.06, fx(StatusEffects.STRENGTH, secs(15), 0));

        // Fish / Special
        add(Items.TROPICAL_FISH, 0.22, fx(StatusEffects.GLOWING, secs(60), 0));
        add(Items.COOKED_COD, 0.20, fx(StatusEffects.WATER_BREATHING, secs(60), 0));
        add(Items.COOKED_SALMON, 0.20, fx(StatusEffects.WATER_BREATHING, secs(60), 0));

        add(Items.SPIDER_EYE, 0.22, fx(StatusEffects.NIGHT_VISION, secs(45), 0));
        add(Items.HONEY_BOTTLE, 0.26, fx(StatusEffects.INVISIBILITY, secs(50), 0));

        // NOTE: Foods you marked "None" are simply not added here (they stay vanilla).
    }

    public static boolean hasRules(Item item) {
        return RULES.containsKey(item);
    }

    public static void applyRolls(ServerPlayerEntity player,
                                  Item item,
                                  long serverTick,
                                  boolean wasRaining,
                                  String biomeId) {

        List<Roll> rolls = RULES.get(item);
        if (rolls == null) return;

        double mult = computeMultiplier(player, item, serverTick, wasRaining, biomeId);

        for (Roll r : rolls) {
            double finalChance = clamp(r.chance() * mult, 0.0, 0.95);

            if (RNG.nextDouble() < finalChance) {
                player.addStatusEffect(new StatusEffectInstance(r.effect()));

                boolean beneficial = r.effect().getEffectType().value().isBeneficial();
                playProcSoundPacket(player, beneficial);
            }
        }
    }

    // ---------------- MULTIPLIERS ----------------

    private static double computeMultiplier(ServerPlayerEntity player,
                                            Item item,
                                            long tick,
                                            boolean wasRaining,
                                            String biomeId) {

        double mult = 1.0;

        // Food Fatigue (same-food streak penalty)
        int streak = updateStreak(player.getUuid(), item, tick);
        if (streak > 1) {
            double fatigueMult = 1.0 - (FATIGUE_STEP * (streak - 1));
            if (fatigueMult < FATIGUE_MIN) fatigueMult = FATIGUE_MIN;
            mult *= fatigueMult;
        }

        // Full Hunger Bonus
        if (player.getHungerManager().getFoodLevel() >= 20) {
            mult *= (1.0 + FULL_HUNGER_BONUS);
        }

        // Low Health Boost (<= 4 hearts)
        if (player.getHealth() <= 8.0f) {
            mult *= (1.0 + LOW_HP_BONUS);
        }

        // Weather Synergy: rain boosts water foods
        if (wasRaining && isWaterFood(item)) {
            mult *= (1.0 + RAIN_WATER_BONUS);
        }

        // Biome boosts (simple string matching)
        if (biomeId != null) {
            String b = biomeId.toLowerCase();
            if (isBerryFood(item) && (b.contains("forest") || b.contains("taiga") || b.contains("grove"))) {
                mult *= (1.0 + BERRY_BIOME_BONUS);
            }
            if (isFishFood(item) && (b.contains("ocean") || b.contains("river") || b.contains("beach"))) {
                mult *= (1.0 + FISH_BIOME_BONUS);
            }
        }

        return mult;
    }

    private static int updateStreak(UUID playerId, Item item, long tick) {
        Map<Item, Streak> map = STREAKS.computeIfAbsent(playerId, k -> new HashMap<>());
        Streak prev = map.get(item);

        if (prev == null || tick - prev.tick() > FATIGUE_WINDOW) {
            map.put(item, new Streak(tick, 1));
            return 1;
        }

        int newCount = prev.count() + 1;
        map.put(item, new Streak(tick, newCount));
        return newCount;
    }

    // ---------------- SOUND (PACKET) ----------------

    private static void playProcSoundPacket(ServerPlayerEntity player, boolean beneficial) {
        RegistryEntry<SoundEvent> entry = beneficial ? PROC_GOOD : PROC_BAD;

        // This sends the sound to that player only, reliably.
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                entry,
                SoundCategory.PLAYERS,
                player.getX(), player.getY(), player.getZ(),
                0.8f,
                beneficial ? 1.5f : 0.8f,
                ThreadLocalRandom.current().nextLong()
        ));
    }

    // ---------------- HELPERS ----------------

    private static boolean isWaterFood(Item item) {
        return item == Items.DRIED_KELP
                || item == Items.COOKED_COD
                || item == Items.COOKED_SALMON
                || item == Items.TROPICAL_FISH
                || item == Items.MELON_SLICE;
    }

    private static boolean isBerryFood(Item item) {
        return item == Items.SWEET_BERRIES || item == Items.GLOW_BERRIES;
    }

    private static boolean isFishFood(Item item) {
        return item == Items.COOKED_COD || item == Items.COOKED_SALMON || item == Items.TROPICAL_FISH;
    }

    private static void add(Item item, double chance, StatusEffectInstance effect) {
        RULES.computeIfAbsent(item, k -> new ArrayList<>()).add(new Roll(chance, effect));
    }

    private static int secs(int s) {
        return 20 * s;
    }

    // StatusEffects.* are RegistryEntry<StatusEffect> in your setup
    private static StatusEffectInstance fx(RegistryEntry<StatusEffect> effect, int durationTicks, int amp) {
        return new StatusEffectInstance(effect, durationTicks, amp);
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
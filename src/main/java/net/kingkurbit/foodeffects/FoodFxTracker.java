// FILE: src/main/java/net/kingkurbit/foodeffects/FoodFxTracker.java
package net.kingkurbit.foodeffects;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FoodFxTracker {

    private static final Map<UUID, EatState> EATING = new HashMap<>();

    private record EatState(
            Item item,
            Hand hand,
            int countBefore,
            long startTick,
            boolean wasRaining,
            String biomeId
    ) {}

    private static long serverTick = 0;

    public static void init() {

        // Track server ticks + detect finish
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            serverTick++;

            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                EatState state = EATING.get(p.getUuid());
                if (state == null) continue;

                // still eating
                if (p.isUsingItem()) continue;

                // too long -> canceled
                if (serverTick - state.startTick() > 140) { // ~7 seconds
                    EATING.remove(p.getUuid());
                    continue;
                }

                ItemStack current = p.getStackInHand(state.hand());

                boolean consumed = false;

                // survival: count decreased
                if (current.getItem() == state.item() && current.getCount() < state.countBefore()) {
                    consumed = true;
                }

                // soups/honey: item changes to container etc
                if (current.getItem() != state.item()) {
                    consumed = true;
                }

                // creative: count might not decrease, but animation finished
                if (p.getAbilities().creativeMode) {
                    consumed = true;
                }

                if (consumed) {
                    FoodFxData.applyRolls(p, state.item(), serverTick, state.wasRaining(), state.biomeId());
                }

                EATING.remove(p.getUuid());
            }
        });

        // Start tracking when they begin using a food
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;

            ItemStack stack = sp.getStackInHand(hand);

            // only foods
            if (stack.get(DataComponentTypes.FOOD) == null) return ActionResult.PASS;

            // only foods with rules
            if (!FoodFxData.hasRules(stack.getItem())) return ActionResult.PASS;

            boolean raining = false;
            String biomeId = null;

            if (world instanceof ServerWorld sw) {
                raining = sw.isRaining();
                biomeId = sw.getBiome(sp.getBlockPos())
                        .getKey()
                        .map(k -> k.getValue().toString()) // ex: "minecraft:forest"
                        .orElse(null);
            }

            EATING.put(sp.getUuid(), new EatState(
                    stack.getItem(),
                    hand,
                    stack.getCount(),
                    serverTick,
                    raining,
                    biomeId
            ));

            return ActionResult.PASS;
        });
    }
}
// FILE: src/main/java/net/kingkurbit/foodeffects/FoodEffects.java
package net.kingkurbit.foodeffects;

import net.fabricmc.api.ModInitializer;

public class FoodEffects implements ModInitializer {
	@Override
	public void onInitialize() {
		FoodFxData.init();
		FoodFxTracker.init();
		System.out.println("Food Effects+ loaded ✅");
	}
}
# 🥕 Food Effects+ 

**Food Effects+** is a _vanilla-plus gameplay mod_ that gives specific foods a chance to grant temporary status effects when eaten. Instead of every food always behaving the same way, common foods can occasionally **proc a buff** — or a rare downside — adding variety and decision-making to survival without adding new items, blocks, GUIs, or complex systems.

---

## 🍽️ How It Works

- When a player finishes eating a supported food, the mod checks that food’s configured rules.
- The mod rolls the listed chance(s).
- A single food can have **multiple independent rolls**.  
  _Example: Sweet Berries can roll Jump Boost and separately roll Nausea._
- If a roll succeeds, the corresponding status effect is applied for its configured duration.
- Foods set to **None** behave exactly like vanilla.

---

## 🍎 Food Effects

### 🌿 Fruits & Crops
- **Apple** → chance to grant **Speed**
- **Melon Slice** → chance to grant **Water Breathing**
- **Sweet Berries** → chance for **Jump Boost**, small chance for **Nausea**
- **Glow Berries** → chance for **Night Vision**, small chance for **Mining Fatigue**
- **Carrot** → chance for **Night Vision**
- **Golden Carrot** → chance for **Regeneration**
- **Potato** → chance for **Speed**
- **Baked Potato** → chance for **Haste**
- **Poisonous Potato** → chance for **Strength**
- **Beetroot** → chance for **Regeneration**, small chance for **Strength**
- **Dried Kelp** → chance for **Water Breathing**
- **Golden Apple / Enchanted Golden Apple** → _unchanged (kept vanilla & special)_

### 🍲 Crafted Meals
- **Bread** → chance for **Resistance**
- **Cookie** → chance for **Speed**
- **Pumpkin Pie** → chance for **Jump Boost**
- **Mushroom Stew** → chance for **Slow Falling**
- **Rabbit Stew** → chance for **Fire Resistance**
- **Beetroot Soup** → chance for **Regeneration**, additional chance for **Strength**
- **Suspicious Stew** → _unchanged (kept vanilla & special)_
- **Cake** → not included _(block interaction; requires per-slice handler)_

### 🥩 Raw Measts
- Raw meats are intentionally unchanged to preserve survival balance.

### 🐟 Fish & Special
- **Tropical Fish** → chance for **Glowing**
- **Cooked Cod / Salmon** → chance for **Water Breathing**
- **Spider Eye** → chance for **Night Vision**
- **Honey Bottle** → chance for **Invisibility**

---

## ⚖️ Balance Systems

### 🧂 Food Fatigue (Anti-Spam)
Repeatedly eating the same food temporarily reduces its proc chance, encouraging variety.

### 🍗 Full Hunger Bonus
Eating at full hunger increases proc chances, rewarding proper food management.

### ❤️ Low Health Boost
At low health (~4 hearts or below), proc chances increase — enabling clutch survival moments.

### 🌧️ Weather Synergies
Rain boosts proc chances for water-adjacent foods like fish and kelp.

### 🌲 Biome Bonuses
- Berries gain bonuses in **forest & taiga** biomes  
- Fish & water foods gain bonuses near **rivers & oceans**  
_Adds exploration flavor without forcing relocation._

---

## ✨ Proc Feedback

When a proc occurs, players receive feedback:

- 🔊 **Sound cues**
  - Positive sound for beneficial effects
  - Negative sound for harmful effects

- ✨ _Optional particles_  
  Small visual burst for clarity.

---

## 🎯 Design Philosophy

Food Effects+ enhances survival through **temporary, chance-based bonuses** tied to real eating behavior. Effects are subtle, balanced, and respect vanilla progression. Powerful vanilla foods remain unchanged to preserve their special role.

---

## 🚫 What it DOESN'T Do

- ❌ No new foods, blocks, or items  
- ❌ Does not change hunger or saturation values  
- ❌ No custom GUIs or screens required  
- ❌ No guaranteed buffs — everything is chance-based  
- ❌ Does not affect non-food items  

---

## 🔧 Compatibility

- ✔ Server & singleplayer friendly  
- ✔ Vanilla-style balance  
- ✔ Configurable & modpack friendly  

---

## 🏷️ Tags

`fabric` `vanilla-plus` `survival` `gameplay` `food` `status-effects` `balanced` `lightweight` `immersive` `modpack-friendly`

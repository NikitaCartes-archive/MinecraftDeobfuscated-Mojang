package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentTags {
	TagKey<Enchantment> TOOLTIP_ORDER = create("tooltip_order");
	TagKey<Enchantment> ARMOR_EXCLUSIVE = create("exclusive_set/armor");
	TagKey<Enchantment> BOOTS_EXCLUSIVE = create("exclusive_set/boots");
	TagKey<Enchantment> BOW_EXCLUSIVE = create("exclusive_set/bow");
	TagKey<Enchantment> CROSSBOW_EXCLUSIVE = create("exclusive_set/crossbow");
	TagKey<Enchantment> DAMAGE_EXCLUSIVE = create("exclusive_set/damage");
	TagKey<Enchantment> MINING_EXCLUSIVE = create("exclusive_set/mining");
	TagKey<Enchantment> RIPTIDE_EXCLUSIVE = create("exclusive_set/riptide");
	TagKey<Enchantment> TRADEABLE = create("tradeable");
	TagKey<Enchantment> DOUBLE_TRADE_PRICE = create("double_trade_price");
	TagKey<Enchantment> IN_ENCHANTING_TABLE = create("in_enchanting_table");
	TagKey<Enchantment> ON_MOB_SPAWN_EQUIPMENT = create("on_mob_spawn_equipment");
	TagKey<Enchantment> ON_TRADED_EQUIPMENT = create("on_traded_equipment");
	TagKey<Enchantment> ON_RANDOM_LOOT = create("on_random_loot");
	TagKey<Enchantment> CURSE = create("curse");
	TagKey<Enchantment> SMELTS_LOOT = create("smelts_loot");
	TagKey<Enchantment> PREVENTS_BEE_SPAWNS_WHEN_MINING = create("prevents_bee_spawns_when_mining");
	TagKey<Enchantment> PREVENTS_DECORATED_POT_SHATTERING = create("prevents_decorated_pot_shattering");
	TagKey<Enchantment> PREVENTS_ICE_MELTING = create("prevents_ice_melting");
	TagKey<Enchantment> PREVENTS_INFESTED_SPAWNS = create("prevents_infested_spawns");
	TagKey<Enchantment> TREASURE = create("treasure");
	TagKey<Enchantment> NON_TREASURE = create("non_treasure");
	TagKey<Enchantment> TRADES_DESERT_COMMON = create("trades/desert_common");
	TagKey<Enchantment> TRADES_JUNGLE_COMMON = create("trades/jungle_common");
	TagKey<Enchantment> TRADES_PLAINS_COMMON = create("trades/plains_common");
	TagKey<Enchantment> TRADES_SAVANNA_COMMON = create("trades/savanna_common");
	TagKey<Enchantment> TRADES_SNOW_COMMON = create("trades/snow_common");
	TagKey<Enchantment> TRADES_SWAMP_COMMON = create("trades/swamp_common");
	TagKey<Enchantment> TRADES_TAIGA_COMMON = create("trades/taiga_common");
	TagKey<Enchantment> TRADES_DESERT_SPECIAL = create("trades/desert_special");
	TagKey<Enchantment> TRADES_JUNGLE_SPECIAL = create("trades/jungle_special");
	TagKey<Enchantment> TRADES_PLAINS_SPECIAL = create("trades/plains_special");
	TagKey<Enchantment> TRADES_SAVANNA_SPECIAL = create("trades/savanna_special");
	TagKey<Enchantment> TRADES_SNOW_SPECIAL = create("trades/snow_special");
	TagKey<Enchantment> TRADES_SWAMP_SPECIAL = create("trades/swamp_special");
	TagKey<Enchantment> TRADES_TAIGA_SPECIAL = create("trades/taiga_special");

	private static TagKey<Enchantment> create(String string) {
		return TagKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace(string));
	}
}

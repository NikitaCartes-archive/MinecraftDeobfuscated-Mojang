package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class VanillaArchaeologyLoot implements LootTableSubProvider {
	@Override
	public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
		biConsumer.accept(
			BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.ARMS_UP_POTTERY_SHERD).setWeight(2))
						.add(LootItem.lootTableItem(Items.BREWER_POTTERY_SHERD).setWeight(2))
						.add(LootItem.lootTableItem(Items.BRICK))
						.add(LootItem.lootTableItem(Items.EMERALD))
						.add(LootItem.lootTableItem(Items.STICK))
						.add(
							LootItem.lootTableItem(Items.SUSPICIOUS_STEW)
								.apply(
									SetStewEffectFunction.stewEffect()
										.withEffect(MobEffects.NIGHT_VISION, UniformGenerator.between(7.0F, 10.0F))
										.withEffect(MobEffects.JUMP, UniformGenerator.between(7.0F, 10.0F))
										.withEffect(MobEffects.WEAKNESS, UniformGenerator.between(6.0F, 8.0F))
										.withEffect(MobEffects.BLINDNESS, UniformGenerator.between(5.0F, 7.0F))
										.withEffect(MobEffects.POISON, UniformGenerator.between(10.0F, 20.0F))
										.withEffect(MobEffects.SATURATION, UniformGenerator.between(7.0F, 10.0F))
								)
						)
				)
		);
		biConsumer.accept(
			BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.ARCHER_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.MINER_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.PRIZE_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.SKULL_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.DIAMOND))
						.add(LootItem.lootTableItem(Items.TNT))
						.add(LootItem.lootTableItem(Items.GUNPOWDER))
						.add(LootItem.lootTableItem(Items.EMERALD))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(2))
						.add(LootItem.lootTableItem(Items.WOODEN_HOE).setWeight(2))
						.add(LootItem.lootTableItem(Items.CLAY).setWeight(2))
						.add(LootItem.lootTableItem(Items.BRICK).setWeight(2))
						.add(LootItem.lootTableItem(Items.YELLOW_DYE).setWeight(2))
						.add(LootItem.lootTableItem(Items.BLUE_DYE).setWeight(2))
						.add(LootItem.lootTableItem(Items.LIGHT_BLUE_DYE).setWeight(2))
						.add(LootItem.lootTableItem(Items.WHITE_DYE).setWeight(2))
						.add(LootItem.lootTableItem(Items.ORANGE_DYE).setWeight(2))
						.add(LootItem.lootTableItem(Items.RED_CANDLE).setWeight(2))
						.add(LootItem.lootTableItem(Items.GREEN_CANDLE).setWeight(2))
						.add(LootItem.lootTableItem(Items.PURPLE_CANDLE).setWeight(2))
						.add(LootItem.lootTableItem(Items.BROWN_CANDLE).setWeight(2))
						.add(LootItem.lootTableItem(Items.MAGENTA_STAINED_GLASS_PANE))
						.add(LootItem.lootTableItem(Items.PINK_STAINED_GLASS_PANE))
						.add(LootItem.lootTableItem(Items.BLUE_STAINED_GLASS_PANE))
						.add(LootItem.lootTableItem(Items.LIGHT_BLUE_STAINED_GLASS_PANE))
						.add(LootItem.lootTableItem(Items.RED_STAINED_GLASS_PANE))
						.add(LootItem.lootTableItem(Items.YELLOW_STAINED_GLASS_PANE))
						.add(LootItem.lootTableItem(Items.PURPLE_STAINED_GLASS_PANE))
						.add(LootItem.lootTableItem(Items.SPRUCE_HANGING_SIGN))
						.add(LootItem.lootTableItem(Items.OAK_HANGING_SIGN))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET))
						.add(LootItem.lootTableItem(Items.COAL))
						.add(LootItem.lootTableItem(Items.WHEAT_SEEDS))
						.add(LootItem.lootTableItem(Items.BEETROOT_SEEDS))
						.add(LootItem.lootTableItem(Items.DEAD_BUSH))
						.add(LootItem.lootTableItem(Items.FLOWER_POT))
						.add(LootItem.lootTableItem(Items.STRING))
						.add(LootItem.lootTableItem(Items.LEAD))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.BURN_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.DANGER_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.FRIEND_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.HEART_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.HEARTBREAK_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.HOWL_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.SHEAF_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE))
						.add(LootItem.lootTableItem(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE))
						.add(LootItem.lootTableItem(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE))
						.add(LootItem.lootTableItem(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.ANGLER_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.SHELTER_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.SNORT_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.SNIFFER_EGG))
						.add(LootItem.lootTableItem(Items.IRON_AXE))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(2))
						.add(LootItem.lootTableItem(Items.WOODEN_HOE).setWeight(2))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(2))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(2))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.BLADE_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.EXPLORER_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.MOURNER_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.PLENTY_POTTERY_SHERD))
						.add(LootItem.lootTableItem(Items.IRON_AXE))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(2))
						.add(LootItem.lootTableItem(Items.WOODEN_HOE).setWeight(2))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(2))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(2))
				)
		);
	}
}

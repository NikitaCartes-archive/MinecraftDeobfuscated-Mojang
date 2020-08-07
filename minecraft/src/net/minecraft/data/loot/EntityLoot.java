package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;

public class EntityLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
	private static final EntityPredicate.Builder ENTITY_ON_FIRE = EntityPredicate.Builder.entity()
		.flags(EntityFlagsPredicate.Builder.flags().setOnFire(true).build());
	private static final Set<EntityType<?>> SPECIAL_LOOT_TABLE_TYPES = ImmutableSet.of(
		EntityType.PLAYER, EntityType.ARMOR_STAND, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.VILLAGER
	);
	private final Map<ResourceLocation, LootTable.Builder> map = Maps.<ResourceLocation, LootTable.Builder>newHashMap();

	private static LootTable.Builder createSheepTable(ItemLike itemLike) {
		return LootTable.lootTable()
			.withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike)))
			.withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootTableReference.lootTableReference(EntityType.SHEEP.getDefaultLootTable())));
	}

	public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
		this.add(EntityType.ARMOR_STAND, LootTable.lootTable());
		this.add(EntityType.BAT, LootTable.lootTable());
		this.add(EntityType.BEE, LootTable.lootTable());
		this.add(
			EntityType.BLAZE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.BLAZE_ROD)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		);
		this.add(
			EntityType.CAT,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.STRING).apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F))))
				)
		);
		this.add(
			EntityType.CAVE_SPIDER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.STRING)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.SPIDER_EYE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(-1.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		);
		this.add(
			EntityType.CHICKEN,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.FEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.CHICKEN)
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.COD,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.COD)
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.BONE_MEAL))
						.when(LootItemRandomChanceCondition.randomChance(0.05F))
				)
		);
		this.add(
			EntityType.COW,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.LEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.BEEF)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.CREEPER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.GUNPOWDER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.add(TagEntry.expandTag(ItemTags.CREEPER_DROP_MUSIC_DISCS))
						.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.KILLER, EntityPredicate.Builder.entity().of(EntityTypeTags.SKELETONS)))
				)
		);
		this.add(
			EntityType.DOLPHIN,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.COD)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
						)
				)
		);
		this.add(
			EntityType.DONKEY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.LEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.DROWNED,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ROTTEN_FLESH)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.05F, 0.01F))
				)
		);
		this.add(
			EntityType.ELDER_GUARDIAN,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.PRISMARINE_SHARD)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.COD)
								.setWeight(3)
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
						)
						.add(
							LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(2).apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(EmptyLootItem.emptyItem())
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Blocks.WET_SPONGE))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootTableReference.lootTableReference(BuiltInLootTables.FISHING_FISH))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))
				)
		);
		this.add(EntityType.ENDER_DRAGON, LootTable.lootTable());
		this.add(
			EntityType.ENDERMAN,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ENDER_PEARL)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(EntityType.ENDERMITE, LootTable.lootTable());
		this.add(
			EntityType.EVOKER,
			LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Items.TOTEM_OF_UNDYING)))
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.EMERALD)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		);
		this.add(EntityType.FOX, LootTable.lootTable());
		this.add(
			EntityType.GHAST,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.GHAST_TEAR)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.GUNPOWDER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(EntityType.GIANT, LootTable.lootTable());
		this.add(
			EntityType.GUARDIAN,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.PRISMARINE_SHARD)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.COD)
								.setWeight(2)
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
						)
						.add(
							LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(2).apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(EmptyLootItem.emptyItem())
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootTableReference.lootTableReference(BuiltInLootTables.FISHING_FISH))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))
				)
		);
		this.add(
			EntityType.HORSE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.LEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.HUSK,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ROTTEN_FLESH)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.IRON_INGOT))
						.add(LootItem.lootTableItem(Items.CARROT))
						.add(LootItem.lootTableItem(Items.POTATO))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))
				)
		);
		this.add(
			EntityType.RAVAGER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.SADDLE).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
				)
		);
		this.add(EntityType.ILLUSIONER, LootTable.lootTable());
		this.add(
			EntityType.IRON_GOLEM,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Blocks.POPPY).apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 5.0F))))
				)
		);
		this.add(
			EntityType.LLAMA,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.LEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.MAGMA_CUBE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.MAGMA_CREAM)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(-2.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.MULE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.LEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.MOOSHROOM,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.LEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.BEEF)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(EntityType.OCELOT, LootTable.lootTable());
		this.add(
			EntityType.PANDA,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Blocks.BAMBOO).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
				)
		);
		this.add(
			EntityType.PARROT,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.FEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.PHANTOM,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.PHANTOM_MEMBRANE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		);
		this.add(
			EntityType.PIG,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.PORKCHOP)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(EntityType.PILLAGER, LootTable.lootTable());
		this.add(EntityType.PLAYER, LootTable.lootTable());
		this.add(
			EntityType.POLAR_BEAR,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.COD)
								.setWeight(3)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(
							LootItem.lootTableItem(Items.SALMON)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.PUFFERFISH,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.PUFFERFISH).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.BONE_MEAL))
						.when(LootItemRandomChanceCondition.randomChance(0.05F))
				)
		);
		this.add(
			EntityType.RABBIT,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.RABBIT_HIDE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.RABBIT)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.RABBIT_FOOT))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.1F, 0.03F))
				)
		);
		this.add(
			EntityType.SALMON,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.SALMON)
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.BONE_MEAL))
						.when(LootItemRandomChanceCondition.randomChance(0.05F))
				)
		);
		this.add(
			EntityType.SHEEP,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.MUTTON)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(BuiltInLootTables.SHEEP_BLACK, createSheepTable(Blocks.BLACK_WOOL));
		this.add(BuiltInLootTables.SHEEP_BLUE, createSheepTable(Blocks.BLUE_WOOL));
		this.add(BuiltInLootTables.SHEEP_BROWN, createSheepTable(Blocks.BROWN_WOOL));
		this.add(BuiltInLootTables.SHEEP_CYAN, createSheepTable(Blocks.CYAN_WOOL));
		this.add(BuiltInLootTables.SHEEP_GRAY, createSheepTable(Blocks.GRAY_WOOL));
		this.add(BuiltInLootTables.SHEEP_GREEN, createSheepTable(Blocks.GREEN_WOOL));
		this.add(BuiltInLootTables.SHEEP_LIGHT_BLUE, createSheepTable(Blocks.LIGHT_BLUE_WOOL));
		this.add(BuiltInLootTables.SHEEP_LIGHT_GRAY, createSheepTable(Blocks.LIGHT_GRAY_WOOL));
		this.add(BuiltInLootTables.SHEEP_LIME, createSheepTable(Blocks.LIME_WOOL));
		this.add(BuiltInLootTables.SHEEP_MAGENTA, createSheepTable(Blocks.MAGENTA_WOOL));
		this.add(BuiltInLootTables.SHEEP_ORANGE, createSheepTable(Blocks.ORANGE_WOOL));
		this.add(BuiltInLootTables.SHEEP_PINK, createSheepTable(Blocks.PINK_WOOL));
		this.add(BuiltInLootTables.SHEEP_PURPLE, createSheepTable(Blocks.PURPLE_WOOL));
		this.add(BuiltInLootTables.SHEEP_RED, createSheepTable(Blocks.RED_WOOL));
		this.add(BuiltInLootTables.SHEEP_WHITE, createSheepTable(Blocks.WHITE_WOOL));
		this.add(BuiltInLootTables.SHEEP_YELLOW, createSheepTable(Blocks.YELLOW_WOOL));
		this.add(
			EntityType.SHULKER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.SHULKER_SHELL))
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.5F, 0.0625F))
				)
		);
		this.add(EntityType.SILVERFISH, LootTable.lootTable());
		this.add(
			EntityType.SKELETON,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ARROW)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.BONE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.SKELETON_HORSE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.BONE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.SLIME,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.SLIME_BALL)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.SNOW_GOLEM,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.SNOWBALL).apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 15.0F))))
				)
		);
		this.add(
			EntityType.SPIDER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.STRING)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.SPIDER_EYE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(-1.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		);
		this.add(
			EntityType.SQUID,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.INK_SAC)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.STRAY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ARROW)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.BONE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.TIPPED_ARROW)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)).setLimit(1))
								.apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), compoundTag -> compoundTag.putString("Potion", "minecraft:slowness"))))
						)
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		);
		this.add(
			EntityType.STRIDER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.STRING)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 5.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.TRADER_LLAMA,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.LEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.TROPICAL_FISH,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.TROPICAL_FISH).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.BONE_MEAL))
						.when(LootItemRandomChanceCondition.randomChance(0.05F))
				)
		);
		this.add(
			EntityType.TURTLE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Blocks.SEAGRASS)
								.setWeight(3)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.BOWL))
						.when(DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isLightning(true)))
				)
		);
		this.add(EntityType.VEX, LootTable.lootTable());
		this.add(EntityType.VILLAGER, LootTable.lootTable());
		this.add(EntityType.WANDERING_TRADER, LootTable.lootTable());
		this.add(
			EntityType.VINDICATOR,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.EMERALD)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		);
		this.add(
			EntityType.WITCH,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 3.0F))
						.add(
							LootItem.lootTableItem(Items.GLOWSTONE_DUST)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(
							LootItem.lootTableItem(Items.SUGAR)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(
							LootItem.lootTableItem(Items.REDSTONE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(
							LootItem.lootTableItem(Items.SPIDER_EYE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(
							LootItem.lootTableItem(Items.GLASS_BOTTLE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(
							LootItem.lootTableItem(Items.GUNPOWDER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
						.add(
							LootItem.lootTableItem(Items.STICK)
								.setWeight(2)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(EntityType.WITHER, LootTable.lootTable());
		this.add(
			EntityType.WITHER_SKELETON,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.COAL)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(-1.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.BONE)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Blocks.WITHER_SKELETON_SKULL))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))
				)
		);
		this.add(EntityType.WOLF, LootTable.lootTable());
		this.add(
			EntityType.ZOGLIN,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ROTTEN_FLESH)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.ZOMBIE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ROTTEN_FLESH)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.IRON_INGOT))
						.add(LootItem.lootTableItem(Items.CARROT))
						.add(LootItem.lootTableItem(Items.POTATO))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))
				)
		);
		this.add(
			EntityType.ZOMBIE_HORSE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ROTTEN_FLESH)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(
			EntityType.ZOMBIFIED_PIGLIN,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ROTTEN_FLESH)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.GOLD_NUGGET)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))
				)
		);
		this.add(
			EntityType.HOGLIN,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.PORKCHOP)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F)))
								.apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.LEATHER)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 1.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
		);
		this.add(EntityType.PIGLIN, LootTable.lootTable());
		this.add(EntityType.PIGLIN_BRUTE, LootTable.lootTable());
		this.add(
			EntityType.ZOMBIE_VILLAGER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.ROTTEN_FLESH)
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
								.apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.IRON_INGOT))
						.add(LootItem.lootTableItem(Items.CARROT))
						.add(LootItem.lootTableItem(Items.POTATO))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))
				)
		);
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

		for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
			ResourceLocation resourceLocation = entityType.getDefaultLootTable();
			if (!SPECIAL_LOOT_TABLE_TYPES.contains(entityType) && entityType.getCategory() == MobCategory.MISC) {
				if (resourceLocation != BuiltInLootTables.EMPTY && this.map.remove(resourceLocation) != null) {
					throw new IllegalStateException(
						String.format("Weird loottable '%s' for '%s', not a LivingEntity so should not have loot", resourceLocation, Registry.ENTITY_TYPE.getKey(entityType))
					);
				}
			} else if (resourceLocation != BuiltInLootTables.EMPTY && set.add(resourceLocation)) {
				LootTable.Builder builder = (LootTable.Builder)this.map.remove(resourceLocation);
				if (builder == null) {
					throw new IllegalStateException(String.format("Missing loottable '%s' for '%s'", resourceLocation, Registry.ENTITY_TYPE.getKey(entityType)));
				}

				biConsumer.accept(resourceLocation, builder);
			}
		}

		this.map.forEach(biConsumer::accept);
	}

	private void add(EntityType<?> entityType, LootTable.Builder builder) {
		this.add(entityType.getDefaultLootTable(), builder);
	}

	private void add(ResourceLocation resourceLocation, LootTable.Builder builder) {
		this.map.put(resourceLocation, builder);
	}
}

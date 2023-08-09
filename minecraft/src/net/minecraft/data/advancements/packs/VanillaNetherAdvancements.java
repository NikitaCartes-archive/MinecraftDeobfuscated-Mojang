package net.minecraft.data.advancements.packs;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.BrewedPotionTrigger;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.ConstructBeaconTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemDurabilityTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.LootTableTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PickedUpItemTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public class VanillaNetherAdvancements implements AdvancementSubProvider {
	private static final ContextAwarePredicate DISTRACT_PIGLIN_PLAYER_ARMOR_PREDICATE = ContextAwarePredicate.create(
		LootItemEntityPropertyCondition.hasProperties(
				LootContext.EntityTarget.THIS,
				EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().head(ItemPredicate.Builder.item().of(Items.GOLDEN_HELMET)))
			)
			.invert()
			.build(),
		LootItemEntityPropertyCondition.hasProperties(
				LootContext.EntityTarget.THIS,
				EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().chest(ItemPredicate.Builder.item().of(Items.GOLDEN_CHESTPLATE)))
			)
			.invert()
			.build(),
		LootItemEntityPropertyCondition.hasProperties(
				LootContext.EntityTarget.THIS,
				EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().legs(ItemPredicate.Builder.item().of(Items.GOLDEN_LEGGINGS)))
			)
			.invert()
			.build(),
		LootItemEntityPropertyCondition.hasProperties(
				LootContext.EntityTarget.THIS,
				EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(Items.GOLDEN_BOOTS)))
			)
			.invert()
			.build()
	);

	@Override
	public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer) {
		Advancement advancement = Advancement.Builder.advancement()
			.display(
				Blocks.RED_NETHER_BRICKS,
				Component.translatable("advancements.nether.root.title"),
				Component.translatable("advancements.nether.root.description"),
				new ResourceLocation("textures/gui/advancements/backgrounds/nether.png"),
				FrameType.TASK,
				false,
				false,
				false
			)
			.addCriterion("entered_nether", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.NETHER))
			.save(consumer, "nether/root");
		Advancement advancement2 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Items.FIRE_CHARGE,
				Component.translatable("advancements.nether.return_to_sender.title"),
				Component.translatable("advancements.nether.return_to_sender.description"),
				null,
				FrameType.CHALLENGE,
				true,
				true,
				false
			)
			.rewards(AdvancementRewards.Builder.experience(50))
			.addCriterion(
				"killed_ghast",
				KilledTrigger.TriggerInstance.playerKilledEntity(
					EntityPredicate.Builder.entity().of(EntityType.GHAST),
					DamageSourcePredicate.Builder.damageType()
						.tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
						.direct(EntityPredicate.Builder.entity().of(EntityType.FIREBALL))
				)
			)
			.save(consumer, "nether/return_to_sender");
		Advancement advancement3 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Blocks.NETHER_BRICKS,
				Component.translatable("advancements.nether.find_fortress.title"),
				Component.translatable("advancements.nether.find_fortress.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("fortress", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(BuiltinStructures.FORTRESS)))
			.save(consumer, "nether/find_fortress");
		Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Items.MAP,
				Component.translatable("advancements.nether.fast_travel.title"),
				Component.translatable("advancements.nether.fast_travel.description"),
				null,
				FrameType.CHALLENGE,
				true,
				true,
				false
			)
			.rewards(AdvancementRewards.Builder.experience(100))
			.addCriterion("travelled", DistanceTrigger.TriggerInstance.travelledThroughNether(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(7000.0))))
			.save(consumer, "nether/fast_travel");
		Advancement.Builder.advancement()
			.parent(advancement2)
			.display(
				Items.GHAST_TEAR,
				Component.translatable("advancements.nether.uneasy_alliance.title"),
				Component.translatable("advancements.nether.uneasy_alliance.description"),
				null,
				FrameType.CHALLENGE,
				true,
				true,
				false
			)
			.rewards(AdvancementRewards.Builder.experience(100))
			.addCriterion(
				"killed_ghast",
				KilledTrigger.TriggerInstance.playerKilledEntity(
					EntityPredicate.Builder.entity().of(EntityType.GHAST).located(LocationPredicate.Builder.inDimension(Level.OVERWORLD))
				)
			)
			.save(consumer, "nether/uneasy_alliance");
		Advancement advancement4 = Advancement.Builder.advancement()
			.parent(advancement3)
			.display(
				Blocks.WITHER_SKELETON_SKULL,
				Component.translatable("advancements.nether.get_wither_skull.title"),
				Component.translatable("advancements.nether.get_wither_skull.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("wither_skull", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.WITHER_SKELETON_SKULL))
			.save(consumer, "nether/get_wither_skull");
		Advancement advancement5 = Advancement.Builder.advancement()
			.parent(advancement4)
			.display(
				Items.NETHER_STAR,
				Component.translatable("advancements.nether.summon_wither.title"),
				Component.translatable("advancements.nether.summon_wither.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("summoned", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(EntityType.WITHER)))
			.save(consumer, "nether/summon_wither");
		Advancement advancement6 = Advancement.Builder.advancement()
			.parent(advancement3)
			.display(
				Items.BLAZE_ROD,
				Component.translatable("advancements.nether.obtain_blaze_rod.title"),
				Component.translatable("advancements.nether.obtain_blaze_rod.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("blaze_rod", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BLAZE_ROD))
			.save(consumer, "nether/obtain_blaze_rod");
		Advancement advancement7 = Advancement.Builder.advancement()
			.parent(advancement5)
			.display(
				Blocks.BEACON,
				Component.translatable("advancements.nether.create_beacon.title"),
				Component.translatable("advancements.nether.create_beacon.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("beacon", ConstructBeaconTrigger.TriggerInstance.constructedBeacon(MinMaxBounds.Ints.atLeast(1)))
			.save(consumer, "nether/create_beacon");
		Advancement.Builder.advancement()
			.parent(advancement7)
			.display(
				Blocks.BEACON,
				Component.translatable("advancements.nether.create_full_beacon.title"),
				Component.translatable("advancements.nether.create_full_beacon.description"),
				null,
				FrameType.GOAL,
				true,
				true,
				false
			)
			.addCriterion("beacon", ConstructBeaconTrigger.TriggerInstance.constructedBeacon(MinMaxBounds.Ints.exactly(4)))
			.save(consumer, "nether/create_full_beacon");
		Advancement advancement8 = Advancement.Builder.advancement()
			.parent(advancement6)
			.display(
				Items.POTION,
				Component.translatable("advancements.nether.brew_potion.title"),
				Component.translatable("advancements.nether.brew_potion.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("potion", BrewedPotionTrigger.TriggerInstance.brewedPotion())
			.save(consumer, "nether/brew_potion");
		Advancement advancement9 = Advancement.Builder.advancement()
			.parent(advancement8)
			.display(
				Items.MILK_BUCKET,
				Component.translatable("advancements.nether.all_potions.title"),
				Component.translatable("advancements.nether.all_potions.description"),
				null,
				FrameType.CHALLENGE,
				true,
				true,
				false
			)
			.rewards(AdvancementRewards.Builder.experience(100))
			.addCriterion(
				"all_effects",
				EffectsChangedTrigger.TriggerInstance.hasEffects(
					MobEffectsPredicate.Builder.effects()
						.and(MobEffects.MOVEMENT_SPEED)
						.and(MobEffects.MOVEMENT_SLOWDOWN)
						.and(MobEffects.DAMAGE_BOOST)
						.and(MobEffects.JUMP)
						.and(MobEffects.REGENERATION)
						.and(MobEffects.FIRE_RESISTANCE)
						.and(MobEffects.WATER_BREATHING)
						.and(MobEffects.INVISIBILITY)
						.and(MobEffects.NIGHT_VISION)
						.and(MobEffects.WEAKNESS)
						.and(MobEffects.POISON)
						.and(MobEffects.SLOW_FALLING)
						.and(MobEffects.DAMAGE_RESISTANCE)
				)
			)
			.save(consumer, "nether/all_potions");
		Advancement.Builder.advancement()
			.parent(advancement9)
			.display(
				Items.BUCKET,
				Component.translatable("advancements.nether.all_effects.title"),
				Component.translatable("advancements.nether.all_effects.description"),
				null,
				FrameType.CHALLENGE,
				true,
				true,
				true
			)
			.rewards(AdvancementRewards.Builder.experience(1000))
			.addCriterion(
				"all_effects",
				EffectsChangedTrigger.TriggerInstance.hasEffects(
					MobEffectsPredicate.Builder.effects()
						.and(MobEffects.MOVEMENT_SPEED)
						.and(MobEffects.MOVEMENT_SLOWDOWN)
						.and(MobEffects.DAMAGE_BOOST)
						.and(MobEffects.JUMP)
						.and(MobEffects.REGENERATION)
						.and(MobEffects.FIRE_RESISTANCE)
						.and(MobEffects.WATER_BREATHING)
						.and(MobEffects.INVISIBILITY)
						.and(MobEffects.NIGHT_VISION)
						.and(MobEffects.WEAKNESS)
						.and(MobEffects.POISON)
						.and(MobEffects.WITHER)
						.and(MobEffects.DIG_SPEED)
						.and(MobEffects.DIG_SLOWDOWN)
						.and(MobEffects.LEVITATION)
						.and(MobEffects.GLOWING)
						.and(MobEffects.ABSORPTION)
						.and(MobEffects.HUNGER)
						.and(MobEffects.CONFUSION)
						.and(MobEffects.DAMAGE_RESISTANCE)
						.and(MobEffects.SLOW_FALLING)
						.and(MobEffects.CONDUIT_POWER)
						.and(MobEffects.DOLPHINS_GRACE)
						.and(MobEffects.BLINDNESS)
						.and(MobEffects.BAD_OMEN)
						.and(MobEffects.HERO_OF_THE_VILLAGE)
						.and(MobEffects.DARKNESS)
				)
			)
			.save(consumer, "nether/all_effects");
		Advancement advancement10 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Items.ANCIENT_DEBRIS,
				Component.translatable("advancements.nether.obtain_ancient_debris.title"),
				Component.translatable("advancements.nether.obtain_ancient_debris.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("ancient_debris", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ANCIENT_DEBRIS))
			.save(consumer, "nether/obtain_ancient_debris");
		Advancement.Builder.advancement()
			.parent(advancement10)
			.display(
				Items.NETHERITE_CHESTPLATE,
				Component.translatable("advancements.nether.netherite_armor.title"),
				Component.translatable("advancements.nether.netherite_armor.description"),
				null,
				FrameType.CHALLENGE,
				true,
				true,
				false
			)
			.rewards(AdvancementRewards.Builder.experience(100))
			.addCriterion(
				"netherite_armor",
				InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)
			)
			.save(consumer, "nether/netherite_armor");
		Advancement.Builder.advancement()
			.parent(advancement10)
			.display(
				Items.LODESTONE,
				Component.translatable("advancements.nether.use_lodestone.title"),
				Component.translatable("advancements.nether.use_lodestone.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion(
				"use_lodestone",
				ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
					LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.LODESTONE)), ItemPredicate.Builder.item().of(Items.COMPASS)
				)
			)
			.save(consumer, "nether/use_lodestone");
		Advancement advancement11 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Items.CRYING_OBSIDIAN,
				Component.translatable("advancements.nether.obtain_crying_obsidian.title"),
				Component.translatable("advancements.nether.obtain_crying_obsidian.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("crying_obsidian", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CRYING_OBSIDIAN))
			.save(consumer, "nether/obtain_crying_obsidian");
		Advancement.Builder.advancement()
			.parent(advancement11)
			.display(
				Items.RESPAWN_ANCHOR,
				Component.translatable("advancements.nether.charge_respawn_anchor.title"),
				Component.translatable("advancements.nether.charge_respawn_anchor.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion(
				"charge_respawn_anchor",
				ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
					LocationPredicate.Builder.location()
						.setBlock(
							BlockPredicate.Builder.block()
								.of(Blocks.RESPAWN_ANCHOR)
								.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(RespawnAnchorBlock.CHARGE, 4))
						),
					ItemPredicate.Builder.item().of(Blocks.GLOWSTONE)
				)
			)
			.save(consumer, "nether/charge_respawn_anchor");
		Advancement advancement12 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Items.WARPED_FUNGUS_ON_A_STICK,
				Component.translatable("advancements.nether.ride_strider.title"),
				Component.translatable("advancements.nether.ride_strider.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion(
				"used_warped_fungus_on_a_stick",
				ItemDurabilityTrigger.TriggerInstance.changedDurability(
					EntityPredicate.wrap(EntityPredicate.Builder.entity().vehicle(EntityPredicate.Builder.entity().of(EntityType.STRIDER))),
					ItemPredicate.Builder.item().of(Items.WARPED_FUNGUS_ON_A_STICK).build(),
					MinMaxBounds.Ints.ANY
				)
			)
			.save(consumer, "nether/ride_strider");
		Advancement.Builder.advancement()
			.parent(advancement12)
			.display(
				Items.WARPED_FUNGUS_ON_A_STICK,
				Component.translatable("advancements.nether.ride_strider_in_overworld_lava.title"),
				Component.translatable("advancements.nether.ride_strider_in_overworld_lava.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion(
				"ride_entity_distance",
				DistanceTrigger.TriggerInstance.rideEntityInLava(
					EntityPredicate.Builder.entity()
						.located(LocationPredicate.Builder.inDimension(Level.OVERWORLD))
						.vehicle(EntityPredicate.Builder.entity().of(EntityType.STRIDER)),
					DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(50.0))
				)
			)
			.save(consumer, "nether/ride_strider_in_overworld_lava");
		VanillaAdventureAdvancements.addBiomes(Advancement.Builder.advancement(), MultiNoiseBiomeSourceParameterList.Preset.NETHER.usedBiomes().toList())
			.parent(advancement12)
			.display(
				Items.NETHERITE_BOOTS,
				Component.translatable("advancements.nether.explore_nether.title"),
				Component.translatable("advancements.nether.explore_nether.description"),
				null,
				FrameType.CHALLENGE,
				true,
				true,
				false
			)
			.rewards(AdvancementRewards.Builder.experience(500))
			.save(consumer, "nether/explore_nether");
		Advancement advancement13 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Items.POLISHED_BLACKSTONE_BRICKS,
				Component.translatable("advancements.nether.find_bastion.title"),
				Component.translatable("advancements.nether.find_bastion.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("bastion", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure(BuiltinStructures.BASTION_REMNANT)))
			.save(consumer, "nether/find_bastion");
		Advancement.Builder.advancement()
			.parent(advancement13)
			.display(
				Blocks.CHEST,
				Component.translatable("advancements.nether.loot_bastion.title"),
				Component.translatable("advancements.nether.loot_bastion.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.requirements(RequirementsStrategy.OR)
			.addCriterion("loot_bastion_other", LootTableTrigger.TriggerInstance.lootTableUsed(new ResourceLocation("minecraft:chests/bastion_other")))
			.addCriterion("loot_bastion_treasure", LootTableTrigger.TriggerInstance.lootTableUsed(new ResourceLocation("minecraft:chests/bastion_treasure")))
			.addCriterion("loot_bastion_hoglin_stable", LootTableTrigger.TriggerInstance.lootTableUsed(new ResourceLocation("minecraft:chests/bastion_hoglin_stable")))
			.addCriterion("loot_bastion_bridge", LootTableTrigger.TriggerInstance.lootTableUsed(new ResourceLocation("minecraft:chests/bastion_bridge")))
			.save(consumer, "nether/loot_bastion");
		Advancement.Builder.advancement()
			.parent(advancement)
			.requirements(RequirementsStrategy.OR)
			.display(
				Items.GOLD_INGOT,
				Component.translatable("advancements.nether.distract_piglin.title"),
				Component.translatable("advancements.nether.distract_piglin.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion(
				"distract_piglin",
				PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByEntity(
					DISTRACT_PIGLIN_PLAYER_ARMOR_PREDICATE,
					ItemPredicate.Builder.item().of(ItemTags.PIGLIN_LOVED).build(),
					EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PIGLIN).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false)))
				)
			)
			.addCriterion(
				"distract_piglin_directly",
				PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
					Optional.of(DISTRACT_PIGLIN_PLAYER_ARMOR_PREDICATE),
					ItemPredicate.Builder.item().of(PiglinAi.BARTERING_ITEM),
					EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PIGLIN).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false)))
				)
			)
			.save(consumer, "nether/distract_piglin");
	}
}

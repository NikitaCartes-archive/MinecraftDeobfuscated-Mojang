package net.minecraft.world.item.enchantment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MovementPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.ApplyMobEffect;
import net.minecraft.world.item.enchantment.effects.DamageEntity;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.DamageItem;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.ExplodeEffect;
import net.minecraft.world.item.enchantment.effects.Ignite;
import net.minecraft.world.item.enchantment.effects.MultiplyValue;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import net.minecraft.world.item.enchantment.effects.RemoveBinomial;
import net.minecraft.world.item.enchantment.effects.ReplaceDisc;
import net.minecraft.world.item.enchantment.effects.SetBlockProperties;
import net.minecraft.world.item.enchantment.effects.SetValue;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.item.enchantment.effects.SummonEntityEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.EnchantmentActiveCheck;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import net.minecraft.world.level.storage.loot.providers.number.EnchantmentLevelProvider;
import net.minecraft.world.phys.Vec3;

public class Enchantments {
	public static final ResourceKey<Enchantment> PROTECTION = key("protection");
	public static final ResourceKey<Enchantment> FIRE_PROTECTION = key("fire_protection");
	public static final ResourceKey<Enchantment> FEATHER_FALLING = key("feather_falling");
	public static final ResourceKey<Enchantment> BLAST_PROTECTION = key("blast_protection");
	public static final ResourceKey<Enchantment> PROJECTILE_PROTECTION = key("projectile_protection");
	public static final ResourceKey<Enchantment> RESPIRATION = key("respiration");
	public static final ResourceKey<Enchantment> AQUA_AFFINITY = key("aqua_affinity");
	public static final ResourceKey<Enchantment> THORNS = key("thorns");
	public static final ResourceKey<Enchantment> DEPTH_STRIDER = key("depth_strider");
	public static final ResourceKey<Enchantment> FROST_WALKER = key("frost_walker");
	public static final ResourceKey<Enchantment> BINDING_CURSE = key("binding_curse");
	public static final ResourceKey<Enchantment> SOUL_SPEED = key("soul_speed");
	public static final ResourceKey<Enchantment> SWIFT_SNEAK = key("swift_sneak");
	public static final ResourceKey<Enchantment> SHARPNESS = key("sharpness");
	public static final ResourceKey<Enchantment> SMITE = key("smite");
	public static final ResourceKey<Enchantment> BANE_OF_ARTHROPODS = key("bane_of_arthropods");
	public static final ResourceKey<Enchantment> KNOCKBACK = key("knockback");
	public static final ResourceKey<Enchantment> FIRE_ASPECT = key("fire_aspect");
	public static final ResourceKey<Enchantment> LOOTING = key("looting");
	public static final ResourceKey<Enchantment> SWEEPING_EDGE = key("sweeping_edge");
	public static final ResourceKey<Enchantment> EFFICIENCY = key("efficiency");
	public static final ResourceKey<Enchantment> SILK_TOUCH = key("silk_touch");
	public static final ResourceKey<Enchantment> UNBREAKING = key("unbreaking");
	public static final ResourceKey<Enchantment> FORTUNE = key("fortune");
	public static final ResourceKey<Enchantment> POWER = key("power");
	public static final ResourceKey<Enchantment> PUNCH = key("punch");
	public static final ResourceKey<Enchantment> FLAME = key("flame");
	public static final ResourceKey<Enchantment> INFINITY = key("infinity");
	public static final ResourceKey<Enchantment> LUCK_OF_THE_SEA = key("luck_of_the_sea");
	public static final ResourceKey<Enchantment> LURE = key("lure");
	public static final ResourceKey<Enchantment> LOYALTY = key("loyalty");
	public static final ResourceKey<Enchantment> IMPALING = key("impaling");
	public static final ResourceKey<Enchantment> RIPTIDE = key("riptide");
	public static final ResourceKey<Enchantment> CHANNELING = key("channeling");
	public static final ResourceKey<Enchantment> MULTISHOT = key("multishot");
	public static final ResourceKey<Enchantment> QUICK_CHARGE = key("quick_charge");
	public static final ResourceKey<Enchantment> PIERCING = key("piercing");
	public static final ResourceKey<Enchantment> DENSITY = key("density");
	public static final ResourceKey<Enchantment> BREACH = key("breach");
	public static final ResourceKey<Enchantment> WIND_BURST = key("wind_burst");
	public static final ResourceKey<Enchantment> MENDING = key("mending");
	public static final ResourceKey<Enchantment> VANISHING_CURSE = key("vanishing_curse");

	public static void bootstrap(BootstrapContext<Enchantment> bootstrapContext) {
		HolderGetter<DamageType> holderGetter = bootstrapContext.lookup(Registries.DAMAGE_TYPE);
		HolderGetter<Enchantment> holderGetter2 = bootstrapContext.lookup(Registries.ENCHANTMENT);
		HolderGetter<Item> holderGetter3 = bootstrapContext.lookup(Registries.ITEM);
		HolderGetter<Block> holderGetter4 = bootstrapContext.lookup(Registries.BLOCK);
		register(
			bootstrapContext,
			PROTECTION,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.ARMOR_ENCHANTABLE), 10, 4, Enchantment.dynamicCost(1, 11), Enchantment.dynamicCost(12, 11), 1, EquipmentSlotGroup.ARMOR
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.DAMAGE_PROTECTION,
					new AddValue(LevelBasedValue.perLevel(1.0F)),
					DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))
				)
		);
		register(
			bootstrapContext,
			FIRE_PROTECTION,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.ARMOR_ENCHANTABLE), 5, 4, Enchantment.dynamicCost(10, 8), Enchantment.dynamicCost(18, 8), 2, EquipmentSlotGroup.ARMOR
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.DAMAGE_PROTECTION,
					new AddValue(LevelBasedValue.perLevel(2.0F)),
					AllOfCondition.allOf(
						DamageSourceCondition.hasDamageSource(
							DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_FIRE)).tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
						)
					)
				)
				.withEffect(
					EnchantmentEffectComponents.ATTRIBUTES,
					new EnchantmentAttributeEffect(
						"enchantment.fire_protection",
						Attributes.BURNING_TIME,
						LevelBasedValue.perLevel(-0.15F),
						AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
						UUID.fromString("b572ecd2-ac0c-4071-abde-9594af072a37")
					)
				)
		);
		register(
			bootstrapContext,
			FEATHER_FALLING,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
						5,
						4,
						Enchantment.dynamicCost(5, 6),
						Enchantment.dynamicCost(11, 6),
						2,
						EquipmentSlotGroup.ARMOR
					)
				)
				.withEffect(
					EnchantmentEffectComponents.DAMAGE_PROTECTION,
					new AddValue(LevelBasedValue.perLevel(3.0F)),
					DamageSourceCondition.hasDamageSource(
						DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_FALL)).tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
					)
				)
		);
		register(
			bootstrapContext,
			BLAST_PROTECTION,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.ARMOR_ENCHANTABLE), 2, 4, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(13, 8), 4, EquipmentSlotGroup.ARMOR
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.DAMAGE_PROTECTION,
					new AddValue(LevelBasedValue.perLevel(2.0F)),
					DamageSourceCondition.hasDamageSource(
						DamageSourcePredicate.Builder.damageType()
							.tag(TagPredicate.is(DamageTypeTags.IS_EXPLOSION))
							.tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
					)
				)
				.withEffect(
					EnchantmentEffectComponents.ATTRIBUTES,
					new EnchantmentAttributeEffect(
						"enchantment.blast_protection",
						Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
						LevelBasedValue.perLevel(0.15F),
						AttributeModifier.Operation.ADD_VALUE,
						UUID.fromString("40a9968f-5c66-4e2f-b7f4-2ec2f4b3e450")
					)
				)
		);
		register(
			bootstrapContext,
			PROJECTILE_PROTECTION,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.ARMOR_ENCHANTABLE), 5, 4, Enchantment.dynamicCost(3, 6), Enchantment.dynamicCost(9, 6), 2, EquipmentSlotGroup.ARMOR
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.ARMOR_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.DAMAGE_PROTECTION,
					new AddValue(LevelBasedValue.perLevel(2.0F)),
					DamageSourceCondition.hasDamageSource(
						DamageSourcePredicate.Builder.damageType()
							.tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
							.tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
					)
				)
		);
		register(
			bootstrapContext,
			RESPIRATION,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE),
						2,
						3,
						Enchantment.dynamicCost(10, 10),
						Enchantment.dynamicCost(40, 10),
						4,
						EquipmentSlotGroup.HEAD
					)
				)
				.withEffect(
					EnchantmentEffectComponents.ATTRIBUTES,
					new EnchantmentAttributeEffect(
						"enchantment.respiration",
						Attributes.OXYGEN_BONUS,
						LevelBasedValue.perLevel(1.0F),
						AttributeModifier.Operation.ADD_VALUE,
						UUID.fromString("07a65791-f64d-4e79-86c7-f83932f007ec")
					)
				)
		);
		register(
			bootstrapContext,
			AQUA_AFFINITY,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.HEAD_ARMOR_ENCHANTABLE), 2, 1, Enchantment.constantCost(1), Enchantment.constantCost(41), 4, EquipmentSlotGroup.HEAD
					)
				)
				.withEffect(
					EnchantmentEffectComponents.ATTRIBUTES,
					new EnchantmentAttributeEffect(
						"enchantment.aqua_affinity",
						Attributes.SUBMERGED_MINING_SPEED,
						LevelBasedValue.perLevel(4.0F),
						AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
						UUID.fromString("60b1b7db-fffd-4ad0-817c-d6c6a93d8a45")
					)
				)
		);
		register(
			bootstrapContext,
			THORNS,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
						holderGetter3.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
						1,
						3,
						Enchantment.dynamicCost(10, 20),
						Enchantment.dynamicCost(60, 20),
						8,
						EquipmentSlotGroup.ARMOR
					)
				)
				.withEffect(
					EnchantmentEffectComponents.POST_ATTACK,
					EnchantmentTarget.VICTIM,
					EnchantmentTarget.ATTACKER,
					AllOf.entityEffects(
						new DamageEntity(LevelBasedValue.constant(1.0F), LevelBasedValue.constant(5.0F), holderGetter.getOrThrow(DamageTypes.THORNS)),
						new DamageItem(LevelBasedValue.constant(2.0F))
					),
					LootItemRandomChanceCondition.randomChance(EnchantmentLevelProvider.forEnchantmentLevel(LevelBasedValue.perLevel(0.15F)))
				)
		);
		register(
			bootstrapContext,
			DEPTH_STRIDER,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
						2,
						3,
						Enchantment.dynamicCost(10, 10),
						Enchantment.dynamicCost(25, 10),
						4,
						EquipmentSlotGroup.FEET
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.BOOTS_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.ATTRIBUTES,
					new EnchantmentAttributeEffect(
						"enchantment.depth_strider",
						Attributes.WATER_MOVEMENT_EFFICIENCY,
						LevelBasedValue.perLevel(0.33333334F),
						AttributeModifier.Operation.ADD_VALUE,
						UUID.fromString("11dc269a-4476-46c0-aff3-9e17d7eb6801")
					)
				)
		);
		register(
			bootstrapContext,
			FROST_WALKER,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
						2,
						2,
						Enchantment.dynamicCost(10, 10),
						Enchantment.dynamicCost(25, 10),
						4,
						EquipmentSlotGroup.FEET
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.BOOTS_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.DAMAGE_IMMUNITY,
					DamageImmunity.INSTANCE,
					DamageSourceCondition.hasDamageSource(
						DamageSourcePredicate.Builder.damageType()
							.tag(TagPredicate.is(DamageTypeTags.BURN_FROM_STEPPING))
							.tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))
					)
				)
				.withEffect(
					EnchantmentEffectComponents.LOCATION_CHANGED,
					new ReplaceDisc(
						new LevelBasedValue.Clamped(LevelBasedValue.perLevel(3.0F, 1.0F), 0.0F, 16.0F),
						LevelBasedValue.constant(1.0F),
						new Vec3i(0, -1, 0),
						Optional.of(
							BlockPredicate.allOf(
								BlockPredicate.matchesBlocks(new Vec3i(0, 1, 0), Blocks.AIR), BlockPredicate.matchesFluids(Fluids.WATER), BlockPredicate.unobstructed()
							)
						),
						BlockStateProvider.simple(Blocks.FROSTED_ICE)
					),
					LootItemEntityPropertyCondition.hasProperties(
						LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(true))
					)
				)
		);
		register(
			bootstrapContext,
			BINDING_CURSE,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.EQUIPPABLE_ENCHANTABLE), 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlotGroup.ARMOR
					)
				)
				.withEffect(EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
		);
		EntityPredicate.Builder builder = EntityPredicate.Builder.entity()
			.periodicTick(5)
			.flags(EntityFlagsPredicate.Builder.flags().setIsFlying(false).setOnGround(true))
			.moving(MovementPredicate.horizontalSpeed(MinMaxBounds.Doubles.atLeast(1.0E-5F)))
			.steppingOn(
				LocationPredicate.Builder.location().setBlock(net.minecraft.advancements.critereon.BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS))
			);
		register(
			bootstrapContext,
			SOUL_SPEED,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
						1,
						3,
						Enchantment.dynamicCost(10, 10),
						Enchantment.dynamicCost(25, 10),
						8,
						EquipmentSlotGroup.FEET
					)
				)
				.withEffect(
					EnchantmentEffectComponents.LOCATION_CHANGED,
					new EnchantmentAttributeEffect(
						"Soul speed boost",
						Attributes.MOVEMENT_SPEED,
						LevelBasedValue.perLevel(0.0405F, 0.0105F),
						AttributeModifier.Operation.ADD_VALUE,
						UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038")
					),
					AllOfCondition.allOf(
						InvertedLootItemCondition.invert(
							LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().vehicle(EntityPredicate.Builder.entity()))
						),
						AnyOfCondition.anyOf(
							AllOfCondition.allOf(
								EnchantmentActiveCheck.enchantmentActiveCheck(),
								LootItemEntityPropertyCondition.hasProperties(
									LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setIsFlying(false))
								),
								AnyOfCondition.anyOf(
									LootItemEntityPropertyCondition.hasProperties(
										LootContext.EntityTarget.THIS,
										EntityPredicate.Builder.entity()
											.steppingOn(
												LocationPredicate.Builder.location().setBlock(net.minecraft.advancements.critereon.BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS))
											)
									),
									LootItemEntityPropertyCondition.hasProperties(
										LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(false)).build()
									)
								)
							),
							AllOfCondition.allOf(
								EnchantmentActiveCheck.enchantmentInactiveCheck(),
								LootItemEntityPropertyCondition.hasProperties(
									LootContext.EntityTarget.THIS,
									EntityPredicate.Builder.entity()
										.steppingOn(
											LocationPredicate.Builder.location().setBlock(net.minecraft.advancements.critereon.BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS))
										)
										.flags(EntityFlagsPredicate.Builder.flags().setIsFlying(false))
								)
							)
						)
					)
				)
				.withEffect(
					EnchantmentEffectComponents.LOCATION_CHANGED,
					new EnchantmentAttributeEffect(
						"Soul speed movement efficiency",
						Attributes.MOVEMENT_EFFICIENCY,
						LevelBasedValue.constant(1.0F),
						AttributeModifier.Operation.ADD_VALUE,
						UUID.fromString("b9716dbd-50df-4080-850e-70347d24e687")
					),
					LootItemEntityPropertyCondition.hasProperties(
						LootContext.EntityTarget.THIS,
						EntityPredicate.Builder.entity()
							.steppingOn(
								LocationPredicate.Builder.location().setBlock(net.minecraft.advancements.critereon.BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS))
							)
					)
				)
				.withEffect(
					EnchantmentEffectComponents.LOCATION_CHANGED,
					new DamageItem(LevelBasedValue.constant(1.0F)),
					AllOfCondition.allOf(
						LootItemRandomChanceCondition.randomChance(EnchantmentLevelProvider.forEnchantmentLevel(LevelBasedValue.constant(0.04F))),
						LootItemEntityPropertyCondition.hasProperties(
							LootContext.EntityTarget.THIS,
							EntityPredicate.Builder.entity()
								.flags(EntityFlagsPredicate.Builder.flags().setOnGround(true))
								.steppingOn(
									LocationPredicate.Builder.location().setBlock(net.minecraft.advancements.critereon.BlockPredicate.Builder.block().of(BlockTags.SOUL_SPEED_BLOCKS))
								)
						)
					)
				)
				.withEffect(
					EnchantmentEffectComponents.TICK,
					new SpawnParticlesEffect(
						ParticleTypes.SOUL,
						SpawnParticlesEffect.inBoundingBox(),
						SpawnParticlesEffect.offsetFromEntityPosition(0.1F),
						SpawnParticlesEffect.movementScaled(-0.2F),
						SpawnParticlesEffect.fixedVelocity(ConstantFloat.of(0.1F)),
						ConstantFloat.of(1.0F)
					),
					LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, builder)
				)
				.withEffect(
					EnchantmentEffectComponents.TICK,
					new PlaySoundEffect(SoundEvents.SOUL_ESCAPE, ConstantFloat.of(0.6F), UniformFloat.of(0.6F, 1.0F)),
					AllOfCondition.allOf(
						LootItemRandomChanceCondition.randomChance(0.35F), LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, builder)
					)
				)
		);
		register(
			bootstrapContext,
			SWIFT_SNEAK,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.LEG_ARMOR_ENCHANTABLE),
						1,
						3,
						Enchantment.dynamicCost(25, 25),
						Enchantment.dynamicCost(75, 25),
						8,
						EquipmentSlotGroup.LEGS
					)
				)
				.withEffect(
					EnchantmentEffectComponents.ATTRIBUTES,
					new EnchantmentAttributeEffect(
						"enchantment.swift_sneak",
						Attributes.SNEAKING_SPEED,
						LevelBasedValue.perLevel(0.15F),
						AttributeModifier.Operation.ADD_VALUE,
						UUID.fromString("92437d00-c3a7-4f2e-8f6c-1f21585d5dd0")
					)
				)
		);
		register(
			bootstrapContext,
			SHARPNESS,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
						holderGetter3.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
						10,
						5,
						Enchantment.dynamicCost(1, 11),
						Enchantment.dynamicCost(21, 11),
						1,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
				.withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(1.0F, 0.5F)))
		);
		register(
			bootstrapContext,
			SMITE,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
						holderGetter3.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
						5,
						5,
						Enchantment.dynamicCost(5, 8),
						Enchantment.dynamicCost(25, 8),
						2,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.DAMAGE,
					new AddValue(LevelBasedValue.perLevel(2.5F)),
					LootItemEntityPropertyCondition.hasProperties(
						LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_SMITE))
					)
				)
		);
		register(
			bootstrapContext,
			BANE_OF_ARTHROPODS,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
						holderGetter3.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
						5,
						5,
						Enchantment.dynamicCost(5, 8),
						Enchantment.dynamicCost(25, 8),
						2,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.DAMAGE,
					new AddValue(LevelBasedValue.perLevel(2.5F)),
					LootItemEntityPropertyCondition.hasProperties(
						LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS))
					)
				)
				.withEffect(
					EnchantmentEffectComponents.POST_ATTACK,
					EnchantmentTarget.ATTACKER,
					EnchantmentTarget.VICTIM,
					new ApplyMobEffect(
						HolderSet.direct(MobEffects.MOVEMENT_SLOWDOWN),
						LevelBasedValue.constant(1.5F),
						LevelBasedValue.perLevel(1.5F, 0.5F),
						LevelBasedValue.constant(3.0F),
						LevelBasedValue.constant(3.0F)
					),
					LootItemEntityPropertyCondition.hasProperties(
						LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS))
					)
				)
		);
		register(
			bootstrapContext,
			KNOCKBACK,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
						5,
						2,
						Enchantment.dynamicCost(5, 20),
						Enchantment.dynamicCost(55, 20),
						2,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.KNOCKBACK, new AddValue(LevelBasedValue.perLevel(1.0F)))
		);
		register(
			bootstrapContext,
			FIRE_ASPECT,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
						2,
						2,
						Enchantment.dynamicCost(10, 20),
						Enchantment.dynamicCost(60, 20),
						4,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new Ignite(LevelBasedValue.perLevel(4.0F)))
				.withEffect(
					EnchantmentEffectComponents.HIT_BLOCK,
					AllOf.entityEffects(new SetBlockProperties(BlockItemStateProperties.EMPTY.with(CandleBlock.LIT, true)), new DamageItem(LevelBasedValue.constant(1.0F))),
					LocationCheck.checkLocation(
						LocationPredicate.Builder.location()
							.setBlock(
								net.minecraft.advancements.critereon.BlockPredicate.Builder.block()
									.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.LIT, false))
							)
					)
				)
		);
		register(
			bootstrapContext,
			LOOTING,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
						2,
						3,
						Enchantment.dynamicCost(15, 9),
						Enchantment.dynamicCost(65, 9),
						4,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(
					EnchantmentEffectComponents.EQUIPMENT_DROPS,
					EnchantmentTarget.ATTACKER,
					EnchantmentTarget.VICTIM,
					new AddValue(LevelBasedValue.perLevel(0.01F)),
					LootItemEntityPropertyCondition.hasProperties(
						LootContext.EntityTarget.ATTACKER, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.PLAYER))
					)
				)
		);
		register(
			bootstrapContext,
			SWEEPING_EDGE,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.SWORD_ENCHANTABLE), 2, 3, Enchantment.dynamicCost(5, 9), Enchantment.dynamicCost(20, 9), 4, EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(
					EnchantmentEffectComponents.ATTRIBUTES,
					new EnchantmentAttributeEffect(
						"enchantment.sweeping_edge",
						Attributes.SWEEPING_DAMAGE_RATIO,
						new LevelBasedValue.Fraction(LevelBasedValue.perLevel(1.0F), LevelBasedValue.perLevel(2.0F, 1.0F)),
						AttributeModifier.Operation.ADD_VALUE,
						UUID.fromString("5d3d087b-debe-4037-b53e-d84f3ff51f17")
					)
				)
		);
		register(
			bootstrapContext,
			EFFICIENCY,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.MINING_ENCHANTABLE),
						10,
						5,
						Enchantment.dynamicCost(1, 10),
						Enchantment.dynamicCost(51, 10),
						1,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(
					EnchantmentEffectComponents.ATTRIBUTES,
					new EnchantmentAttributeEffect(
						"enchantment.efficiency",
						Attributes.MINING_EFFICIENCY,
						new LevelBasedValue.LevelsSquared(1.0F),
						AttributeModifier.Operation.ADD_VALUE,
						UUID.fromString("3ceb37c0-db62-46b5-bd02-785457b01d96")
					)
				)
		);
		register(
			bootstrapContext,
			SILK_TOUCH,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.MINING_LOOT_ENCHANTABLE),
						1,
						1,
						Enchantment.constantCost(15),
						Enchantment.constantCost(65),
						8,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.MINING_EXCLUSIVE))
				.withEffect(EnchantmentEffectComponents.BLOCK_EXPERIENCE, new SetValue(LevelBasedValue.constant(0.0F)))
		);
		register(
			bootstrapContext,
			UNBREAKING,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
						5,
						3,
						Enchantment.dynamicCost(5, 8),
						Enchantment.dynamicCost(55, 8),
						2,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(
					EnchantmentEffectComponents.ITEM_DAMAGE,
					new RemoveBinomial(new LevelBasedValue.Fraction(LevelBasedValue.perLevel(2.0F), LevelBasedValue.perLevel(10.0F, 5.0F))),
					MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.ARMOR_ENCHANTABLE))
				)
				.withEffect(
					EnchantmentEffectComponents.ITEM_DAMAGE,
					new RemoveBinomial(new LevelBasedValue.Fraction(LevelBasedValue.perLevel(1.0F), LevelBasedValue.perLevel(2.0F, 1.0F))),
					InvertedLootItemCondition.invert(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.ARMOR_ENCHANTABLE)))
				)
		);
		register(
			bootstrapContext,
			FORTUNE,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.MINING_LOOT_ENCHANTABLE),
						2,
						3,
						Enchantment.dynamicCost(15, 9),
						Enchantment.dynamicCost(65, 9),
						4,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.MINING_EXCLUSIVE))
		);
		register(
			bootstrapContext,
			POWER,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.BOW_ENCHANTABLE),
						10,
						5,
						Enchantment.dynamicCost(1, 10),
						Enchantment.dynamicCost(16, 10),
						1,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(
					EnchantmentEffectComponents.DAMAGE,
					new AddValue(LevelBasedValue.perLevel(0.5F)),
					LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS).build())
				)
		);
		register(
			bootstrapContext,
			PUNCH,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.BOW_ENCHANTABLE),
						2,
						2,
						Enchantment.dynamicCost(12, 20),
						Enchantment.dynamicCost(37, 20),
						4,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(
					EnchantmentEffectComponents.KNOCKBACK,
					new AddValue(LevelBasedValue.perLevel(1.0F)),
					LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS).build())
				)
		);
		register(
			bootstrapContext,
			FLAME,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.BOW_ENCHANTABLE), 2, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 4, EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.PROJECTILE_SPAWNED, new Ignite(LevelBasedValue.constant(100.0F)))
		);
		register(
			bootstrapContext,
			INFINITY,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.BOW_ENCHANTABLE), 1, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 8, EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.BOW_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.AMMO_USE, new SetValue(LevelBasedValue.constant(0.0F)), MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.ARROW))
				)
		);
		register(
			bootstrapContext,
			LUCK_OF_THE_SEA,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.FISHING_ENCHANTABLE),
						2,
						3,
						Enchantment.dynamicCost(15, 9),
						Enchantment.dynamicCost(65, 9),
						4,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.FISHING_LUCK_BONUS, new AddValue(LevelBasedValue.perLevel(1.0F)))
		);
		register(
			bootstrapContext,
			LURE,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.FISHING_ENCHANTABLE),
						2,
						3,
						Enchantment.dynamicCost(15, 9),
						Enchantment.dynamicCost(65, 9),
						4,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.FISHING_TIME_REDUCTION, new AddValue(LevelBasedValue.perLevel(5.0F)))
		);
		register(
			bootstrapContext,
			LOYALTY,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
						5,
						3,
						Enchantment.dynamicCost(12, 7),
						Enchantment.constantCost(50),
						2,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION, new AddValue(LevelBasedValue.perLevel(1.0F)))
		);
		register(
			bootstrapContext,
			IMPALING,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
						2,
						5,
						Enchantment.dynamicCost(1, 8),
						Enchantment.dynamicCost(21, 8),
						4,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
				.withEffect(
					EnchantmentEffectComponents.DAMAGE,
					new AddValue(LevelBasedValue.perLevel(2.5F)),
					LootItemEntityPropertyCondition.hasProperties(
						LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityTypeTags.SENSITIVE_TO_IMPALING)).build()
					)
				)
		);
		register(
			bootstrapContext,
			RIPTIDE,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
						2,
						3,
						Enchantment.dynamicCost(17, 7),
						Enchantment.constantCost(50),
						4,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.RIPTIDE_EXCLUSIVE))
				.withEffect(EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH, new AddValue(LevelBasedValue.perLevel(1.5F, 0.75F)))
				.withSpecialEffect(
					EnchantmentEffectComponents.TRIDENT_SOUND, List.of(SoundEvents.TRIDENT_RIPTIDE_1, SoundEvents.TRIDENT_RIPTIDE_2, SoundEvents.TRIDENT_RIPTIDE_3)
				)
		);
		register(
			bootstrapContext,
			CHANNELING,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE), 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(
					EnchantmentEffectComponents.POST_ATTACK,
					EnchantmentTarget.ATTACKER,
					EnchantmentTarget.VICTIM,
					AllOf.entityEffects(
						new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()), false),
						new PlaySoundEffect(SoundEvents.TRIDENT_THUNDER, ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
					),
					AllOfCondition.allOf(
						WeatherCheck.weather().setThundering(true),
						LootItemEntityPropertyCondition.hasProperties(
							LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setCanSeeSky(true))
						),
						LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().of(EntityType.TRIDENT))
					)
				)
				.withEffect(
					EnchantmentEffectComponents.HIT_BLOCK,
					AllOf.entityEffects(
						new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()), false),
						new PlaySoundEffect(SoundEvents.TRIDENT_THUNDER, ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
					),
					AllOfCondition.allOf(
						WeatherCheck.weather().setThundering(true),
						LootItemEntityPropertyCondition.hasProperties(
							LootContext.EntityTarget.THIS,
							EntityPredicate.Builder.entity()
								.of(EntityType.TRIDENT)
								.located(
									LocationPredicate.Builder.location()
										.setCanSeeSky(true)
										.setBlock(net.minecraft.advancements.critereon.BlockPredicate.Builder.block().of(Blocks.LIGHTNING_ROD))
								)
						)
					)
				)
		);
		register(
			bootstrapContext,
			MULTISHOT,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.CROSSBOW_ENCHANTABLE), 2, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 4, EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.CROSSBOW_EXCLUSIVE))
				.withEffect(EnchantmentEffectComponents.PROJECTILE_COUNT, new AddValue(LevelBasedValue.perLevel(2.0F)))
				.withEffect(EnchantmentEffectComponents.PROJECTILE_SPREAD, new AddValue(LevelBasedValue.perLevel(10.0F)))
		);
		register(
			bootstrapContext,
			QUICK_CHARGE,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.CROSSBOW_ENCHANTABLE),
						5,
						3,
						Enchantment.dynamicCost(12, 20),
						Enchantment.constantCost(50),
						2,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, new AddValue(LevelBasedValue.perLevel(-0.25F)))
				.withSpecialEffect(
					EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS,
					List.of(
						new CrossbowItem.ChargingSounds(Optional.of(SoundEvents.CROSSBOW_QUICK_CHARGE_1), Optional.empty(), Optional.of(SoundEvents.CROSSBOW_LOADING_END)),
						new CrossbowItem.ChargingSounds(Optional.of(SoundEvents.CROSSBOW_QUICK_CHARGE_2), Optional.empty(), Optional.of(SoundEvents.CROSSBOW_LOADING_END)),
						new CrossbowItem.ChargingSounds(Optional.of(SoundEvents.CROSSBOW_QUICK_CHARGE_3), Optional.empty(), Optional.of(SoundEvents.CROSSBOW_LOADING_END))
					)
				)
		);
		register(
			bootstrapContext,
			PIERCING,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.CROSSBOW_ENCHANTABLE),
						10,
						4,
						Enchantment.dynamicCost(1, 10),
						Enchantment.constantCost(50),
						1,
						EquipmentSlotGroup.MAINHAND
					)
				)
				.exclusiveWith(holderGetter2.getOrThrow(EnchantmentTags.CROSSBOW_EXCLUSIVE))
				.withEffect(EnchantmentEffectComponents.PROJECTILE_PIERCING, new AddValue(LevelBasedValue.perLevel(1.0F)))
		);
		register(
			bootstrapContext,
			DENSITY,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.MACE_ENCHANTABLE), 5, 5, Enchantment.dynamicCost(5, 8), Enchantment.dynamicCost(25, 8), 2, EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK, new AddValue(LevelBasedValue.perLevel(0.5F)))
		);
		register(
			bootstrapContext,
			BREACH,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.MACE_ENCHANTABLE), 2, 4, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, new AddValue(LevelBasedValue.perLevel(-0.15F)))
		);
		register(
			bootstrapContext,
			WIND_BURST,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.MACE_ENCHANTABLE), 2, 3, Enchantment.dynamicCost(15, 9), Enchantment.dynamicCost(65, 9), 4, EquipmentSlotGroup.MAINHAND
					)
				)
				.withEffect(
					EnchantmentEffectComponents.POST_ATTACK,
					EnchantmentTarget.ATTACKER,
					EnchantmentTarget.ATTACKER,
					new ExplodeEffect(
						false,
						Optional.empty(),
						Optional.of(LevelBasedValue.perLevel(0.5F, 0.25F)),
						holderGetter4.get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()),
						Vec3.ZERO,
						LevelBasedValue.constant(3.5F),
						false,
						Level.ExplosionInteraction.TRIGGER,
						ParticleTypes.GUST_EMITTER_SMALL,
						ParticleTypes.GUST_EMITTER_LARGE,
						SoundEvents.WIND_CHARGE_BURST
					),
					LootItemEntityPropertyCondition.hasProperties(
						LootContext.EntityTarget.ATTACKER,
						EntityPredicate.Builder.entity()
							.flags(EntityFlagsPredicate.Builder.flags().setIsFlying(false))
							.moving(MovementPredicate.fallDistance(MinMaxBounds.Doubles.atLeast(1.5)))
					)
				)
		);
		register(
			bootstrapContext,
			MENDING,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
						2,
						1,
						Enchantment.dynamicCost(25, 25),
						Enchantment.dynamicCost(75, 25),
						4,
						EquipmentSlotGroup.ANY
					)
				)
				.withEffect(EnchantmentEffectComponents.REPAIR_WITH_XP, new MultiplyValue(LevelBasedValue.constant(2.0F)))
		);
		register(
			bootstrapContext,
			VANISHING_CURSE,
			Enchantment.enchantment(
					Enchantment.definition(
						holderGetter3.getOrThrow(ItemTags.VANISHING_ENCHANTABLE), 1, 1, Enchantment.constantCost(25), Enchantment.constantCost(50), 8, EquipmentSlotGroup.ANY
					)
				)
				.withEffect(EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
		);
	}

	private static void register(BootstrapContext<Enchantment> bootstrapContext, ResourceKey<Enchantment> resourceKey, Enchantment.Builder builder) {
		bootstrapContext.register(resourceKey, builder.build(resourceKey.location()));
	}

	private static ResourceKey<Enchantment> key(String string) {
		return ResourceKey.create(Registries.ENCHANTMENT, new ResourceLocation(string));
	}
}

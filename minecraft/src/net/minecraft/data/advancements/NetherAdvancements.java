package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.BrewedPotionTrigger;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.ConstructBeaconTrigger;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.LocationTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.NetherTravelTrigger;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;

public class NetherAdvancements implements Consumer<Consumer<Advancement>> {
	public void accept(Consumer<Advancement> consumer) {
		Advancement advancement = Advancement.Builder.advancement()
			.display(
				Blocks.RED_NETHER_BRICKS,
				new TranslatableComponent("advancements.nether.root.title"),
				new TranslatableComponent("advancements.nether.root.description"),
				new ResourceLocation("textures/gui/advancements/backgrounds/nether.png"),
				FrameType.TASK,
				false,
				false,
				false
			)
			.addCriterion("entered_nether", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(DimensionType.NETHER))
			.save(consumer, "nether/root");
		Advancement advancement2 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Items.FIRE_CHARGE,
				new TranslatableComponent("advancements.nether.return_to_sender.title"),
				new TranslatableComponent("advancements.nether.return_to_sender.description"),
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
					DamageSourcePredicate.Builder.damageType().isProjectile(true).direct(EntityPredicate.Builder.entity().of(EntityType.FIREBALL))
				)
			)
			.save(consumer, "nether/return_to_sender");
		Advancement advancement3 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Blocks.NETHER_BRICKS,
				new TranslatableComponent("advancements.nether.find_fortress.title"),
				new TranslatableComponent("advancements.nether.find_fortress.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("fortress", LocationTrigger.TriggerInstance.located(LocationPredicate.inFeature(Feature.NETHER_BRIDGE)))
			.save(consumer, "nether/find_fortress");
		Advancement advancement4 = Advancement.Builder.advancement()
			.parent(advancement)
			.display(
				Items.MAP,
				new TranslatableComponent("advancements.nether.fast_travel.title"),
				new TranslatableComponent("advancements.nether.fast_travel.description"),
				null,
				FrameType.CHALLENGE,
				true,
				true,
				false
			)
			.rewards(AdvancementRewards.Builder.experience(100))
			.addCriterion("travelled", NetherTravelTrigger.TriggerInstance.travelledThroughNether(DistancePredicate.horizontal(MinMaxBounds.Floats.atLeast(7000.0F))))
			.save(consumer, "nether/fast_travel");
		Advancement advancement5 = Advancement.Builder.advancement()
			.parent(advancement2)
			.display(
				Items.GHAST_TEAR,
				new TranslatableComponent("advancements.nether.uneasy_alliance.title"),
				new TranslatableComponent("advancements.nether.uneasy_alliance.description"),
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
					EntityPredicate.Builder.entity().of(EntityType.GHAST).located(LocationPredicate.inDimension(DimensionType.OVERWORLD))
				)
			)
			.save(consumer, "nether/uneasy_alliance");
		Advancement advancement6 = Advancement.Builder.advancement()
			.parent(advancement3)
			.display(
				Blocks.WITHER_SKELETON_SKULL,
				new TranslatableComponent("advancements.nether.get_wither_skull.title"),
				new TranslatableComponent("advancements.nether.get_wither_skull.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("wither_skull", InventoryChangeTrigger.TriggerInstance.hasItem(Blocks.WITHER_SKELETON_SKULL))
			.save(consumer, "nether/get_wither_skull");
		Advancement advancement7 = Advancement.Builder.advancement()
			.parent(advancement6)
			.display(
				Items.NETHER_STAR,
				new TranslatableComponent("advancements.nether.summon_wither.title"),
				new TranslatableComponent("advancements.nether.summon_wither.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("summoned", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of(EntityType.WITHER)))
			.save(consumer, "nether/summon_wither");
		Advancement advancement8 = Advancement.Builder.advancement()
			.parent(advancement3)
			.display(
				Items.BLAZE_ROD,
				new TranslatableComponent("advancements.nether.obtain_blaze_rod.title"),
				new TranslatableComponent("advancements.nether.obtain_blaze_rod.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("blaze_rod", InventoryChangeTrigger.TriggerInstance.hasItem(Items.BLAZE_ROD))
			.save(consumer, "nether/obtain_blaze_rod");
		Advancement advancement9 = Advancement.Builder.advancement()
			.parent(advancement7)
			.display(
				Blocks.BEACON,
				new TranslatableComponent("advancements.nether.create_beacon.title"),
				new TranslatableComponent("advancements.nether.create_beacon.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("beacon", ConstructBeaconTrigger.TriggerInstance.constructedBeacon(MinMaxBounds.Ints.atLeast(1)))
			.save(consumer, "nether/create_beacon");
		Advancement advancement10 = Advancement.Builder.advancement()
			.parent(advancement9)
			.display(
				Blocks.BEACON,
				new TranslatableComponent("advancements.nether.create_full_beacon.title"),
				new TranslatableComponent("advancements.nether.create_full_beacon.description"),
				null,
				FrameType.GOAL,
				true,
				true,
				false
			)
			.addCriterion("beacon", ConstructBeaconTrigger.TriggerInstance.constructedBeacon(MinMaxBounds.Ints.exactly(4)))
			.save(consumer, "nether/create_full_beacon");
		Advancement advancement11 = Advancement.Builder.advancement()
			.parent(advancement8)
			.display(
				Items.POTION,
				new TranslatableComponent("advancements.nether.brew_potion.title"),
				new TranslatableComponent("advancements.nether.brew_potion.description"),
				null,
				FrameType.TASK,
				true,
				true,
				false
			)
			.addCriterion("potion", BrewedPotionTrigger.TriggerInstance.brewedPotion())
			.save(consumer, "nether/brew_potion");
		Advancement advancement12 = Advancement.Builder.advancement()
			.parent(advancement11)
			.display(
				Items.MILK_BUCKET,
				new TranslatableComponent("advancements.nether.all_potions.title"),
				new TranslatableComponent("advancements.nether.all_potions.description"),
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
					MobEffectsPredicate.effects()
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
		Advancement advancement13 = Advancement.Builder.advancement()
			.parent(advancement12)
			.display(
				Items.BUCKET,
				new TranslatableComponent("advancements.nether.all_effects.title"),
				new TranslatableComponent("advancements.nether.all_effects.description"),
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
					MobEffectsPredicate.effects()
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
				)
			)
			.save(consumer, "nether/all_effects");
	}
}

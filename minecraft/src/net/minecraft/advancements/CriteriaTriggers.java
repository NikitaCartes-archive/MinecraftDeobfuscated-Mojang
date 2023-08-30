package net.minecraft.advancements;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.BeeNestDestroyedTrigger;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.advancements.critereon.BrewedPotionTrigger;
import net.minecraft.advancements.critereon.ChangeDimensionTrigger;
import net.minecraft.advancements.critereon.ChanneledLightningTrigger;
import net.minecraft.advancements.critereon.ConstructBeaconTrigger;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.CuredZombieVillagerTrigger;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityHurtPlayerTrigger;
import net.minecraft.advancements.critereon.FilledBucketTrigger;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemDurabilityTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.KilledByCrossbowTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LevitationTrigger;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.advancements.critereon.LootTableTrigger;
import net.minecraft.advancements.critereon.PickedUpItemTrigger;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.advancements.critereon.ShotCrossbowTrigger;
import net.minecraft.advancements.critereon.SlideDownBlockTrigger;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.advancements.critereon.TargetBlockTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.advancements.critereon.UsedEnderEyeTrigger;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.resources.ResourceLocation;

public class CriteriaTriggers {
	private static final BiMap<ResourceLocation, CriterionTrigger<?>> CRITERIA = HashBiMap.create();
	public static final ImpossibleTrigger IMPOSSIBLE = register("impossible", new ImpossibleTrigger());
	public static final KilledTrigger PLAYER_KILLED_ENTITY = register("player_killed_entity", new KilledTrigger());
	public static final KilledTrigger ENTITY_KILLED_PLAYER = register("entity_killed_player", new KilledTrigger());
	public static final EnterBlockTrigger ENTER_BLOCK = register("enter_block", new EnterBlockTrigger());
	public static final InventoryChangeTrigger INVENTORY_CHANGED = register("inventory_changed", new InventoryChangeTrigger());
	public static final RecipeUnlockedTrigger RECIPE_UNLOCKED = register("recipe_unlocked", new RecipeUnlockedTrigger());
	public static final PlayerHurtEntityTrigger PLAYER_HURT_ENTITY = register("player_hurt_entity", new PlayerHurtEntityTrigger());
	public static final EntityHurtPlayerTrigger ENTITY_HURT_PLAYER = register("entity_hurt_player", new EntityHurtPlayerTrigger());
	public static final EnchantedItemTrigger ENCHANTED_ITEM = register("enchanted_item", new EnchantedItemTrigger());
	public static final FilledBucketTrigger FILLED_BUCKET = register("filled_bucket", new FilledBucketTrigger());
	public static final BrewedPotionTrigger BREWED_POTION = register("brewed_potion", new BrewedPotionTrigger());
	public static final ConstructBeaconTrigger CONSTRUCT_BEACON = register("construct_beacon", new ConstructBeaconTrigger());
	public static final UsedEnderEyeTrigger USED_ENDER_EYE = register("used_ender_eye", new UsedEnderEyeTrigger());
	public static final SummonedEntityTrigger SUMMONED_ENTITY = register("summoned_entity", new SummonedEntityTrigger());
	public static final BredAnimalsTrigger BRED_ANIMALS = register("bred_animals", new BredAnimalsTrigger());
	public static final PlayerTrigger LOCATION = register("location", new PlayerTrigger());
	public static final PlayerTrigger SLEPT_IN_BED = register("slept_in_bed", new PlayerTrigger());
	public static final CuredZombieVillagerTrigger CURED_ZOMBIE_VILLAGER = register("cured_zombie_villager", new CuredZombieVillagerTrigger());
	public static final TradeTrigger TRADE = register("villager_trade", new TradeTrigger());
	public static final ItemDurabilityTrigger ITEM_DURABILITY_CHANGED = register("item_durability_changed", new ItemDurabilityTrigger());
	public static final LevitationTrigger LEVITATION = register("levitation", new LevitationTrigger());
	public static final ChangeDimensionTrigger CHANGED_DIMENSION = register("changed_dimension", new ChangeDimensionTrigger());
	public static final PlayerTrigger TICK = register("tick", new PlayerTrigger());
	public static final TameAnimalTrigger TAME_ANIMAL = register("tame_animal", new TameAnimalTrigger());
	public static final ItemUsedOnLocationTrigger PLACED_BLOCK = register("placed_block", new ItemUsedOnLocationTrigger());
	public static final ConsumeItemTrigger CONSUME_ITEM = register("consume_item", new ConsumeItemTrigger());
	public static final EffectsChangedTrigger EFFECTS_CHANGED = register("effects_changed", new EffectsChangedTrigger());
	public static final UsedTotemTrigger USED_TOTEM = register("used_totem", new UsedTotemTrigger());
	public static final DistanceTrigger NETHER_TRAVEL = register("nether_travel", new DistanceTrigger());
	public static final FishingRodHookedTrigger FISHING_ROD_HOOKED = register("fishing_rod_hooked", new FishingRodHookedTrigger());
	public static final ChanneledLightningTrigger CHANNELED_LIGHTNING = register("channeled_lightning", new ChanneledLightningTrigger());
	public static final ShotCrossbowTrigger SHOT_CROSSBOW = register("shot_crossbow", new ShotCrossbowTrigger());
	public static final KilledByCrossbowTrigger KILLED_BY_CROSSBOW = register("killed_by_crossbow", new KilledByCrossbowTrigger());
	public static final PlayerTrigger RAID_WIN = register("hero_of_the_village", new PlayerTrigger());
	public static final PlayerTrigger BAD_OMEN = register("voluntary_exile", new PlayerTrigger());
	public static final SlideDownBlockTrigger HONEY_BLOCK_SLIDE = register("slide_down_block", new SlideDownBlockTrigger());
	public static final BeeNestDestroyedTrigger BEE_NEST_DESTROYED = register("bee_nest_destroyed", new BeeNestDestroyedTrigger());
	public static final TargetBlockTrigger TARGET_BLOCK_HIT = register("target_hit", new TargetBlockTrigger());
	public static final ItemUsedOnLocationTrigger ITEM_USED_ON_BLOCK = register("item_used_on_block", new ItemUsedOnLocationTrigger());
	public static final LootTableTrigger GENERATE_LOOT = register("player_generates_container_loot", new LootTableTrigger());
	public static final PickedUpItemTrigger THROWN_ITEM_PICKED_UP_BY_ENTITY = register("thrown_item_picked_up_by_entity", new PickedUpItemTrigger());
	public static final PickedUpItemTrigger THROWN_ITEM_PICKED_UP_BY_PLAYER = register("thrown_item_picked_up_by_player", new PickedUpItemTrigger());
	public static final PlayerInteractTrigger PLAYER_INTERACTED_WITH_ENTITY = register("player_interacted_with_entity", new PlayerInteractTrigger());
	public static final StartRidingTrigger START_RIDING_TRIGGER = register("started_riding", new StartRidingTrigger());
	public static final LightningStrikeTrigger LIGHTNING_STRIKE = register("lightning_strike", new LightningStrikeTrigger());
	public static final UsingItemTrigger USING_ITEM = register("using_item", new UsingItemTrigger());
	public static final DistanceTrigger FALL_FROM_HEIGHT = register("fall_from_height", new DistanceTrigger());
	public static final DistanceTrigger RIDE_ENTITY_IN_LAVA_TRIGGER = register("ride_entity_in_lava", new DistanceTrigger());
	public static final KilledTrigger KILL_MOB_NEAR_SCULK_CATALYST = register("kill_mob_near_sculk_catalyst", new KilledTrigger());
	public static final ItemUsedOnLocationTrigger ALLAY_DROP_ITEM_ON_BLOCK = register("allay_drop_item_on_block", new ItemUsedOnLocationTrigger());
	public static final PlayerTrigger AVOID_VIBRATION = register("avoid_vibration", new PlayerTrigger());
	public static final RecipeCraftedTrigger RECIPE_CRAFTED = register("recipe_crafted", new RecipeCraftedTrigger());

	private static <T extends CriterionTrigger<?>> T register(String string, T criterionTrigger) {
		ResourceLocation resourceLocation = new ResourceLocation(string);
		if (CRITERIA.putIfAbsent(resourceLocation, criterionTrigger) != null) {
			throw new IllegalArgumentException("Duplicate criterion id " + resourceLocation);
		} else {
			return criterionTrigger;
		}
	}

	@Nullable
	public static <T extends CriterionTriggerInstance> CriterionTrigger<T> getCriterion(ResourceLocation resourceLocation) {
		return (CriterionTrigger<T>)CRITERIA.get(resourceLocation);
	}

	@Nullable
	public static ResourceLocation getId(CriterionTrigger<?> criterionTrigger) {
		return (ResourceLocation)CRITERIA.inverse().get(criterionTrigger);
	}

	public static Iterable<? extends CriterionTrigger<?>> all() {
		return CRITERIA.values();
	}
}

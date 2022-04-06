/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
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
import net.minecraft.advancements.critereon.ItemInteractWithBlockTrigger;
import net.minecraft.advancements.critereon.ItemPickedUpByEntityTrigger;
import net.minecraft.advancements.critereon.KilledByCrossbowTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LevitationTrigger;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.advancements.critereon.LootTableTrigger;
import net.minecraft.advancements.critereon.PlacedBlockTrigger;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
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
import org.jetbrains.annotations.Nullable;

public class CriteriaTriggers {
    private static final Map<ResourceLocation, CriterionTrigger<?>> CRITERIA = Maps.newHashMap();
    public static final ImpossibleTrigger IMPOSSIBLE = CriteriaTriggers.register(new ImpossibleTrigger());
    public static final KilledTrigger PLAYER_KILLED_ENTITY = CriteriaTriggers.register(new KilledTrigger(new ResourceLocation("player_killed_entity")));
    public static final KilledTrigger ENTITY_KILLED_PLAYER = CriteriaTriggers.register(new KilledTrigger(new ResourceLocation("entity_killed_player")));
    public static final EnterBlockTrigger ENTER_BLOCK = CriteriaTriggers.register(new EnterBlockTrigger());
    public static final InventoryChangeTrigger INVENTORY_CHANGED = CriteriaTriggers.register(new InventoryChangeTrigger());
    public static final RecipeUnlockedTrigger RECIPE_UNLOCKED = CriteriaTriggers.register(new RecipeUnlockedTrigger());
    public static final PlayerHurtEntityTrigger PLAYER_HURT_ENTITY = CriteriaTriggers.register(new PlayerHurtEntityTrigger());
    public static final EntityHurtPlayerTrigger ENTITY_HURT_PLAYER = CriteriaTriggers.register(new EntityHurtPlayerTrigger());
    public static final EnchantedItemTrigger ENCHANTED_ITEM = CriteriaTriggers.register(new EnchantedItemTrigger());
    public static final FilledBucketTrigger FILLED_BUCKET = CriteriaTriggers.register(new FilledBucketTrigger());
    public static final BrewedPotionTrigger BREWED_POTION = CriteriaTriggers.register(new BrewedPotionTrigger());
    public static final ConstructBeaconTrigger CONSTRUCT_BEACON = CriteriaTriggers.register(new ConstructBeaconTrigger());
    public static final UsedEnderEyeTrigger USED_ENDER_EYE = CriteriaTriggers.register(new UsedEnderEyeTrigger());
    public static final SummonedEntityTrigger SUMMONED_ENTITY = CriteriaTriggers.register(new SummonedEntityTrigger());
    public static final BredAnimalsTrigger BRED_ANIMALS = CriteriaTriggers.register(new BredAnimalsTrigger());
    public static final PlayerTrigger LOCATION = CriteriaTriggers.register(new PlayerTrigger(new ResourceLocation("location")));
    public static final PlayerTrigger SLEPT_IN_BED = CriteriaTriggers.register(new PlayerTrigger(new ResourceLocation("slept_in_bed")));
    public static final CuredZombieVillagerTrigger CURED_ZOMBIE_VILLAGER = CriteriaTriggers.register(new CuredZombieVillagerTrigger());
    public static final TradeTrigger TRADE = CriteriaTriggers.register(new TradeTrigger());
    public static final ItemDurabilityTrigger ITEM_DURABILITY_CHANGED = CriteriaTriggers.register(new ItemDurabilityTrigger());
    public static final LevitationTrigger LEVITATION = CriteriaTriggers.register(new LevitationTrigger());
    public static final ChangeDimensionTrigger CHANGED_DIMENSION = CriteriaTriggers.register(new ChangeDimensionTrigger());
    public static final PlayerTrigger TICK = CriteriaTriggers.register(new PlayerTrigger(new ResourceLocation("tick")));
    public static final TameAnimalTrigger TAME_ANIMAL = CriteriaTriggers.register(new TameAnimalTrigger());
    public static final PlacedBlockTrigger PLACED_BLOCK = CriteriaTriggers.register(new PlacedBlockTrigger());
    public static final ConsumeItemTrigger CONSUME_ITEM = CriteriaTriggers.register(new ConsumeItemTrigger());
    public static final EffectsChangedTrigger EFFECTS_CHANGED = CriteriaTriggers.register(new EffectsChangedTrigger());
    public static final UsedTotemTrigger USED_TOTEM = CriteriaTriggers.register(new UsedTotemTrigger());
    public static final DistanceTrigger NETHER_TRAVEL = CriteriaTriggers.register(new DistanceTrigger(new ResourceLocation("nether_travel")));
    public static final FishingRodHookedTrigger FISHING_ROD_HOOKED = CriteriaTriggers.register(new FishingRodHookedTrigger());
    public static final ChanneledLightningTrigger CHANNELED_LIGHTNING = CriteriaTriggers.register(new ChanneledLightningTrigger());
    public static final ShotCrossbowTrigger SHOT_CROSSBOW = CriteriaTriggers.register(new ShotCrossbowTrigger());
    public static final KilledByCrossbowTrigger KILLED_BY_CROSSBOW = CriteriaTriggers.register(new KilledByCrossbowTrigger());
    public static final PlayerTrigger RAID_WIN = CriteriaTriggers.register(new PlayerTrigger(new ResourceLocation("hero_of_the_village")));
    public static final PlayerTrigger BAD_OMEN = CriteriaTriggers.register(new PlayerTrigger(new ResourceLocation("voluntary_exile")));
    public static final SlideDownBlockTrigger HONEY_BLOCK_SLIDE = CriteriaTriggers.register(new SlideDownBlockTrigger());
    public static final BeeNestDestroyedTrigger BEE_NEST_DESTROYED = CriteriaTriggers.register(new BeeNestDestroyedTrigger());
    public static final TargetBlockTrigger TARGET_BLOCK_HIT = CriteriaTriggers.register(new TargetBlockTrigger());
    public static final ItemInteractWithBlockTrigger ITEM_USED_ON_BLOCK = CriteriaTriggers.register(new ItemInteractWithBlockTrigger(new ResourceLocation("item_used_on_block")));
    public static final LootTableTrigger GENERATE_LOOT = CriteriaTriggers.register(new LootTableTrigger());
    public static final ItemPickedUpByEntityTrigger ITEM_PICKED_UP_BY_ENTITY = CriteriaTriggers.register(new ItemPickedUpByEntityTrigger());
    public static final PlayerInteractTrigger PLAYER_INTERACTED_WITH_ENTITY = CriteriaTriggers.register(new PlayerInteractTrigger());
    public static final StartRidingTrigger START_RIDING_TRIGGER = CriteriaTriggers.register(new StartRidingTrigger());
    public static final LightningStrikeTrigger LIGHTNING_STRIKE = CriteriaTriggers.register(new LightningStrikeTrigger());
    public static final UsingItemTrigger USING_ITEM = CriteriaTriggers.register(new UsingItemTrigger());
    public static final DistanceTrigger FALL_FROM_HEIGHT = CriteriaTriggers.register(new DistanceTrigger(new ResourceLocation("fall_from_height")));
    public static final DistanceTrigger RIDE_ENTITY_IN_LAVA_TRIGGER = CriteriaTriggers.register(new DistanceTrigger(new ResourceLocation("ride_entity_in_lava")));
    public static final KilledTrigger KILL_MOB_NEAR_SCULK_CATALYST = CriteriaTriggers.register(new KilledTrigger(new ResourceLocation("kill_mob_near_sculk_catalyst")));
    public static final PlayerTrigger ITEM_DELIVERED_TO_PLAYER = CriteriaTriggers.register(new PlayerTrigger(new ResourceLocation("item_delivered_to_player")));
    public static final ItemInteractWithBlockTrigger ALLAY_DROP_ITEM_ON_BLOCK = CriteriaTriggers.register(new ItemInteractWithBlockTrigger(new ResourceLocation("allay_drop_item_on_block")));

    private static <T extends CriterionTrigger<?>> T register(T criterionTrigger) {
        if (CRITERIA.containsKey(criterionTrigger.getId())) {
            throw new IllegalArgumentException("Duplicate criterion id " + criterionTrigger.getId());
        }
        CRITERIA.put(criterionTrigger.getId(), criterionTrigger);
        return criterionTrigger;
    }

    @Nullable
    public static <T extends CriterionTriggerInstance> CriterionTrigger<T> getCriterion(ResourceLocation resourceLocation) {
        return CRITERIA.get(resourceLocation);
    }

    public static Iterable<? extends CriterionTrigger<?>> all() {
        return CRITERIA.values();
    }
}


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
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EnchantedItemTrigger;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityHurtPlayerTrigger;
import net.minecraft.advancements.critereon.FilledBucketTrigger;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemDurabilityTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnBlockTrigger;
import net.minecraft.advancements.critereon.KilledByCrossbowTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LevitationTrigger;
import net.minecraft.advancements.critereon.LocationTrigger;
import net.minecraft.advancements.critereon.NetherTravelTrigger;
import net.minecraft.advancements.critereon.PlacedBlockTrigger;
import net.minecraft.advancements.critereon.PlayerHurtEntityTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.advancements.critereon.ShotCrossbowTrigger;
import net.minecraft.advancements.critereon.SlideDownBlockTrigger;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.advancements.critereon.TickTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.advancements.critereon.UsedEnderEyeTrigger;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
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
    public static final LocationTrigger LOCATION = CriteriaTriggers.register(new LocationTrigger(new ResourceLocation("location")));
    public static final LocationTrigger SLEPT_IN_BED = CriteriaTriggers.register(new LocationTrigger(new ResourceLocation("slept_in_bed")));
    public static final CuredZombieVillagerTrigger CURED_ZOMBIE_VILLAGER = CriteriaTriggers.register(new CuredZombieVillagerTrigger());
    public static final TradeTrigger TRADE = CriteriaTriggers.register(new TradeTrigger());
    public static final ItemDurabilityTrigger ITEM_DURABILITY_CHANGED = CriteriaTriggers.register(new ItemDurabilityTrigger());
    public static final LevitationTrigger LEVITATION = CriteriaTriggers.register(new LevitationTrigger());
    public static final ChangeDimensionTrigger CHANGED_DIMENSION = CriteriaTriggers.register(new ChangeDimensionTrigger());
    public static final TickTrigger TICK = CriteriaTriggers.register(new TickTrigger());
    public static final TameAnimalTrigger TAME_ANIMAL = CriteriaTriggers.register(new TameAnimalTrigger());
    public static final PlacedBlockTrigger PLACED_BLOCK = CriteriaTriggers.register(new PlacedBlockTrigger());
    public static final ConsumeItemTrigger CONSUME_ITEM = CriteriaTriggers.register(new ConsumeItemTrigger());
    public static final EffectsChangedTrigger EFFECTS_CHANGED = CriteriaTriggers.register(new EffectsChangedTrigger());
    public static final UsedTotemTrigger USED_TOTEM = CriteriaTriggers.register(new UsedTotemTrigger());
    public static final NetherTravelTrigger NETHER_TRAVEL = CriteriaTriggers.register(new NetherTravelTrigger());
    public static final FishingRodHookedTrigger FISHING_ROD_HOOKED = CriteriaTriggers.register(new FishingRodHookedTrigger());
    public static final ChanneledLightningTrigger CHANNELED_LIGHTNING = CriteriaTriggers.register(new ChanneledLightningTrigger());
    public static final ShotCrossbowTrigger SHOT_CROSSBOW = CriteriaTriggers.register(new ShotCrossbowTrigger());
    public static final KilledByCrossbowTrigger KILLED_BY_CROSSBOW = CriteriaTriggers.register(new KilledByCrossbowTrigger());
    public static final LocationTrigger RAID_WIN = CriteriaTriggers.register(new LocationTrigger(new ResourceLocation("hero_of_the_village")));
    public static final LocationTrigger BAD_OMEN = CriteriaTriggers.register(new LocationTrigger(new ResourceLocation("voluntary_exile")));
    public static final ItemUsedOnBlockTrigger SAFELY_HARVEST_HONEY = CriteriaTriggers.register(new ItemUsedOnBlockTrigger(new ResourceLocation("safely_harvest_honey")));
    public static final SlideDownBlockTrigger HONEY_BLOCK_SLIDE = CriteriaTriggers.register(new SlideDownBlockTrigger());
    public static final BeeNestDestroyedTrigger BEE_NEST_DESTROYED = CriteriaTriggers.register(new BeeNestDestroyedTrigger());

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


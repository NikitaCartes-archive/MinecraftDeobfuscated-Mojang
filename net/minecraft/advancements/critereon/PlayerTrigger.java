/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    final ResourceLocation id;

    public PlayerTrigger(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return new TriggerInstance(this.id, composite);
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, triggerInstance -> true);
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate.Composite composite) {
            super(resourceLocation, composite);
        }

        public static TriggerInstance located(LocationPredicate locationPredicate) {
            return new TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.wrap(EntityPredicate.Builder.entity().located(locationPredicate).build()));
        }

        public static TriggerInstance located(EntityPredicate entityPredicate) {
            return new TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.wrap(entityPredicate));
        }

        public static TriggerInstance sleptInBed() {
            return new TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, EntityPredicate.Composite.ANY);
        }

        public static TriggerInstance raidWon() {
            return new TriggerInstance(CriteriaTriggers.RAID_WIN.id, EntityPredicate.Composite.ANY);
        }

        public static TriggerInstance avoidVibration() {
            return new TriggerInstance(CriteriaTriggers.AVOID_VIBRATION.id, EntityPredicate.Composite.ANY);
        }

        public static TriggerInstance walkOnBlockWithEquipment(Block block, Item item) {
            return TriggerInstance.located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(item).build()).build()).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).build()).build()).build());
        }
    }
}


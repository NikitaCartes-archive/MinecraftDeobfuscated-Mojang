/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemUsedOnBlockTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("item_used_on_block");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        return new TriggerInstance(composite, locationPredicate, itemPredicate);
    }

    public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
        BlockState blockState = serverPlayer.getLevel().getBlockState(blockPos);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState, serverPlayer.getLevel(), blockPos, itemStack));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite composite, LocationPredicate locationPredicate, ItemPredicate itemPredicate) {
            super(ID, composite);
            this.location = locationPredicate;
            this.item = itemPredicate;
        }

        public static TriggerInstance itemUsedOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, builder.build(), builder2.build());
        }

        public boolean matches(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
            if (!this.location.matches(serverLevel, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5)) {
                return false;
            }
            return this.item.matches(itemStack);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("location", this.location.serializeToJson());
            jsonObject.add("item", this.item.serializeToJson());
            return jsonObject;
        }
    }
}


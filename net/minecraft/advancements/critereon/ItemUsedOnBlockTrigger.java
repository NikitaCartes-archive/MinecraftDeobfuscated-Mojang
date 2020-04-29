/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemUsedOnBlockTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private final ResourceLocation id;

    public ItemUsedOnBlockTrigger(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        BlockPredicate blockPredicate = BlockPredicate.fromJson(jsonObject.get("block"));
        StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        return new TriggerInstance(this.id, composite, blockPredicate, statePropertiesPredicate, itemPredicate);
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
        private final BlockPredicate block;
        private final StatePropertiesPredicate state;
        private final ItemPredicate item;

        public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate.Composite composite, BlockPredicate blockPredicate, StatePropertiesPredicate statePropertiesPredicate, ItemPredicate itemPredicate) {
            super(resourceLocation, composite);
            this.block = blockPredicate;
            this.state = statePropertiesPredicate;
            this.item = itemPredicate;
        }

        public static TriggerInstance safelyHarvestedHoney(BlockPredicate.Builder builder, ItemPredicate.Builder builder2) {
            return new TriggerInstance(CriteriaTriggers.SAFELY_HARVEST_HONEY.id, EntityPredicate.Composite.ANY, builder.build(), StatePropertiesPredicate.ANY, builder2.build());
        }

        public boolean matches(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
            if (!this.block.matches(serverLevel, blockPos)) {
                return false;
            }
            if (!this.state.matches(blockState)) {
                return false;
            }
            return this.item.matches(itemStack);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("block", this.block.serializeToJson());
            jsonObject.add("state", this.state.serializeToJson());
            jsonObject.add("item", this.item.serializeToJson());
            return jsonObject;
        }
    }
}


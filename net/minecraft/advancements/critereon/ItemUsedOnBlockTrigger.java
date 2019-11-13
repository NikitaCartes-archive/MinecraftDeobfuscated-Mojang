/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
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
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        BlockPredicate blockPredicate = BlockPredicate.fromJson(jsonObject.get("block"));
        StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        return new TriggerInstance(this.id, blockPredicate, statePropertiesPredicate, itemPredicate);
    }

    public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
        BlockState blockState = serverPlayer.getLevel().getBlockState(blockPos);
        this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(blockState, serverPlayer.getLevel(), blockPos, itemStack));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final BlockPredicate block;
        private final StatePropertiesPredicate state;
        private final ItemPredicate item;

        public TriggerInstance(ResourceLocation resourceLocation, BlockPredicate blockPredicate, StatePropertiesPredicate statePropertiesPredicate, ItemPredicate itemPredicate) {
            super(resourceLocation);
            this.block = blockPredicate;
            this.state = statePropertiesPredicate;
            this.item = itemPredicate;
        }

        public static TriggerInstance safelyHarvestedHoney(BlockPredicate.Builder builder, ItemPredicate.Builder builder2) {
            return new TriggerInstance(CriteriaTriggers.SAFELY_HARVEST_HONEY.id, builder.build(), StatePropertiesPredicate.ANY, builder2.build());
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
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("block", this.block.serializeToJson());
            jsonObject.add("state", this.state.serializeToJson());
            jsonObject.add("item", this.item.serializeToJson());
            return jsonObject;
        }
    }
}


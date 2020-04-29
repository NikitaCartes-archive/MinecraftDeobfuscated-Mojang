/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PlacedBlockTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("placed_block");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        Block block = PlacedBlockTrigger.deserializeBlock(jsonObject);
        StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
        if (block != null) {
            statePropertiesPredicate.checkState(block.getStateDefinition(), string -> {
                throw new JsonSyntaxException("Block " + block + " has no property " + string + ":");
            });
        }
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        return new TriggerInstance(composite, block, statePropertiesPredicate, locationPredicate, itemPredicate);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject jsonObject) {
        if (jsonObject.has("block")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            return (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourceLocation + "'"));
        }
        return null;
    }

    public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
        BlockState blockState = serverPlayer.getLevel().getBlockState(blockPos);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState, blockPos, serverPlayer.getLevel(), itemStack));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final Block block;
        private final StatePropertiesPredicate state;
        private final LocationPredicate location;
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite composite, @Nullable Block block, StatePropertiesPredicate statePropertiesPredicate, LocationPredicate locationPredicate, ItemPredicate itemPredicate) {
            super(ID, composite);
            this.block = block;
            this.state = statePropertiesPredicate;
            this.location = locationPredicate;
            this.item = itemPredicate;
        }

        public static TriggerInstance placedBlock(Block block) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, block, StatePropertiesPredicate.ANY, LocationPredicate.ANY, ItemPredicate.ANY);
        }

        public boolean matches(BlockState blockState, BlockPos blockPos, ServerLevel serverLevel, ItemStack itemStack) {
            if (this.block != null && !blockState.is(this.block)) {
                return false;
            }
            if (!this.state.matches(blockState)) {
                return false;
            }
            if (!this.location.matches(serverLevel, blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
                return false;
            }
            return this.item.matches(itemStack);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            if (this.block != null) {
                jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
            }
            jsonObject.add("state", this.state.serializeToJson());
            jsonObject.add("location", this.location.serializeToJson());
            jsonObject.add("item", this.item.serializeToJson());
            return jsonObject;
        }
    }
}


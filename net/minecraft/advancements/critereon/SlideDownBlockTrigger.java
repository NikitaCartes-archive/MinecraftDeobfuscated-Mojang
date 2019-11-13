/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SlideDownBlockTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("slide_down_block");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        Block block = SlideDownBlockTrigger.deserializeBlock(jsonObject);
        StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
        if (block != null) {
            statePropertiesPredicate.checkState(block.getStateDefinition(), string -> {
                throw new JsonSyntaxException("Block " + block + " has no property " + string);
            });
        }
        return new TriggerInstance(block, statePropertiesPredicate);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject jsonObject) {
        if (jsonObject.has("block")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            return (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourceLocation + "'"));
        }
        return null;
    }

    public void trigger(ServerPlayer serverPlayer, BlockState blockState) {
        this.trigger(serverPlayer.getAdvancements(), (T triggerInstance) -> triggerInstance.matches(blockState));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final Block block;
        private final StatePropertiesPredicate state;

        public TriggerInstance(@Nullable Block block, StatePropertiesPredicate statePropertiesPredicate) {
            super(ID);
            this.block = block;
            this.state = statePropertiesPredicate;
        }

        public static TriggerInstance slidesDownBlock(Block block) {
            return new TriggerInstance(block, StatePropertiesPredicate.ANY);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            if (this.block != null) {
                jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
            }
            jsonObject.add("state", this.state.serializeToJson());
            return jsonObject;
        }

        public boolean matches(BlockState blockState) {
            if (this.block != null && blockState.getBlock() != this.block) {
                return false;
            }
            return this.state.matches(blockState);
        }
    }
}


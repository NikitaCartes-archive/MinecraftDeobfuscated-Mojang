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
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class BeeNestDestroyedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("bee_nest_destroyed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        Block block = BeeNestDestroyedTrigger.deserializeBlock(jsonObject);
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("num_bees_inside"));
        return new TriggerInstance(block, itemPredicate, ints);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject jsonObject) {
        if (jsonObject.has("block")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            return (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourceLocation + "'"));
        }
        return null;
    }

    public void trigger(ServerPlayer serverPlayer, Block block, ItemStack itemStack, int i) {
        this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(block, itemStack, i));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final Block block;
        private final ItemPredicate item;
        private final MinMaxBounds.Ints numBees;

        public TriggerInstance(Block block, ItemPredicate itemPredicate, MinMaxBounds.Ints ints) {
            super(ID);
            this.block = block;
            this.item = itemPredicate;
            this.numBees = ints;
        }

        public static TriggerInstance destroyedBeeNest(Block block, ItemPredicate.Builder builder, MinMaxBounds.Ints ints) {
            return new TriggerInstance(block, builder.build(), ints);
        }

        public boolean matches(Block block, ItemStack itemStack, int i) {
            if (this.block != null && block != this.block) {
                return false;
            }
            if (!this.item.matches(itemStack)) {
                return false;
            }
            return this.numBees.matches(i);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            if (this.block != null) {
                jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
            }
            jsonObject.add("item", this.item.serializeToJson());
            jsonObject.add("num_bees_inside", this.numBees.serializeToJson());
            return jsonObject;
        }
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("used_ender_eye");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        MinMaxBounds.Floats floats = MinMaxBounds.Floats.fromJson(jsonObject.get("distance"));
        return new TriggerInstance(floats);
    }

    public void trigger(ServerPlayer serverPlayer, BlockPos blockPos) {
        double d = serverPlayer.getX() - (double)blockPos.getX();
        double e = serverPlayer.getZ() - (double)blockPos.getZ();
        double f = d * d + e * e;
        this.trigger(serverPlayer.getAdvancements(), (T triggerInstance) -> triggerInstance.matches(f));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Floats level;

        public TriggerInstance(MinMaxBounds.Floats floats) {
            super(ID);
            this.level = floats;
        }

        public boolean matches(double d) {
            return this.level.matchesSqr(d);
        }
    }
}


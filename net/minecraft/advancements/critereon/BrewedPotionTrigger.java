/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;

public class BrewedPotionTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("brewed_potion");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        Potion potion = null;
        if (jsonObject.has("potion")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "potion"));
            potion = (Potion)Registry.POTION.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + resourceLocation + "'"));
        }
        return new TriggerInstance(composite, potion);
    }

    public void trigger(ServerPlayer serverPlayer, Potion potion) {
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(potion));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final Potion potion;

        public TriggerInstance(EntityPredicate.Composite composite, @Nullable Potion potion) {
            super(ID, composite);
            this.potion = potion;
        }

        public static TriggerInstance brewedPotion() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, null);
        }

        public boolean matches(Potion potion) {
            return this.potion == null || this.potion == potion;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            if (this.potion != null) {
                jsonObject.addProperty("potion", Registry.POTION.getKey(this.potion).toString());
            }
            return jsonObject;
        }
    }
}


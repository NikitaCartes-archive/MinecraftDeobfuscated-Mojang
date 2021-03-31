/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class FluidPredicate {
    public static final FluidPredicate ANY = new FluidPredicate(null, null, StatePropertiesPredicate.ANY);
    @Nullable
    private final Tag<Fluid> tag;
    @Nullable
    private final Fluid fluid;
    private final StatePropertiesPredicate properties;

    public FluidPredicate(@Nullable Tag<Fluid> tag, @Nullable Fluid fluid, StatePropertiesPredicate statePropertiesPredicate) {
        this.tag = tag;
        this.fluid = fluid;
        this.properties = statePropertiesPredicate;
    }

    public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
        if (this == ANY) {
            return true;
        }
        if (!serverLevel.isLoaded(blockPos)) {
            return false;
        }
        FluidState fluidState = serverLevel.getFluidState(blockPos);
        Fluid fluid = fluidState.getType();
        if (this.tag != null && !fluid.is(this.tag)) {
            return false;
        }
        if (this.fluid != null && fluid != this.fluid) {
            return false;
        }
        return this.properties.matches(fluidState);
    }

    public static FluidPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "fluid");
        Fluid fluid = null;
        if (jsonObject.has("fluid")) {
            ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "fluid"));
            fluid = Registry.FLUID.get(resourceLocation2);
        }
        Tag<Fluid> tag = null;
        if (jsonObject.has("tag")) {
            ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
            tag = SerializationTags.getInstance().getTagOrThrow(Registry.FLUID_REGISTRY, resourceLocation2, resourceLocation -> new JsonSyntaxException("Unknown fluid tag '" + resourceLocation + "'"));
        }
        StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
        return new FluidPredicate(tag, fluid, statePropertiesPredicate);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (this.fluid != null) {
            jsonObject.addProperty("fluid", Registry.FLUID.getKey(this.fluid).toString());
        }
        if (this.tag != null) {
            jsonObject.addProperty("tag", SerializationTags.getInstance().getIdOrThrow(Registry.FLUID_REGISTRY, this.tag, () -> new IllegalStateException("Unknown fluid tag")).toString());
        }
        jsonObject.add("state", this.properties.serializeToJson());
        return jsonObject;
    }

    public static class Builder {
        @Nullable
        private Fluid fluid;
        @Nullable
        private Tag<Fluid> fluids;
        private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

        private Builder() {
        }

        public static Builder fluid() {
            return new Builder();
        }

        public Builder of(Fluid fluid) {
            this.fluid = fluid;
            return this;
        }

        public Builder of(Tag<Fluid> tag) {
            this.fluids = tag;
            return this;
        }

        public Builder setProperties(StatePropertiesPredicate statePropertiesPredicate) {
            this.properties = statePropertiesPredicate;
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.fluids, this.fluid, this.properties);
        }
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class LocationPredicate {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final LocationPredicate ANY = new LocationPredicate(MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, null, null, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    private final MinMaxBounds.Floats x;
    private final MinMaxBounds.Floats y;
    private final MinMaxBounds.Floats z;
    @Nullable
    private final ResourceKey<Biome> biome;
    @Nullable
    private final StructureFeature<?> feature;
    @Nullable
    private final ResourceKey<Level> dimension;
    @Nullable
    private final Boolean smokey;
    private final LightPredicate light;
    private final BlockPredicate block;
    private final FluidPredicate fluid;

    public LocationPredicate(MinMaxBounds.Floats floats, MinMaxBounds.Floats floats2, MinMaxBounds.Floats floats3, @Nullable ResourceKey<Biome> resourceKey, @Nullable StructureFeature<?> structureFeature, @Nullable ResourceKey<Level> resourceKey2, @Nullable Boolean boolean_, LightPredicate lightPredicate, BlockPredicate blockPredicate, FluidPredicate fluidPredicate) {
        this.x = floats;
        this.y = floats2;
        this.z = floats3;
        this.biome = resourceKey;
        this.feature = structureFeature;
        this.dimension = resourceKey2;
        this.smokey = boolean_;
        this.light = lightPredicate;
        this.block = blockPredicate;
        this.fluid = fluidPredicate;
    }

    public static LocationPredicate inBiome(ResourceKey<Biome> resourceKey) {
        return new LocationPredicate(MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, resourceKey, null, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    }

    public static LocationPredicate inDimension(ResourceKey<Level> resourceKey) {
        return new LocationPredicate(MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, null, null, resourceKey, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    }

    public static LocationPredicate inFeature(StructureFeature<?> structureFeature) {
        return new LocationPredicate(MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, null, structureFeature, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    }

    public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
        return this.matches(serverLevel, (float)d, (float)e, (float)f);
    }

    public boolean matches(ServerLevel serverLevel, float f, float g, float h) {
        if (!this.x.matches(f)) {
            return false;
        }
        if (!this.y.matches(g)) {
            return false;
        }
        if (!this.z.matches(h)) {
            return false;
        }
        if (this.dimension != null && this.dimension != serverLevel.dimension()) {
            return false;
        }
        BlockPos blockPos = new BlockPos(f, g, h);
        boolean bl = serverLevel.isLoaded(blockPos);
        if (!(this.biome == null || bl && this.biome == serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(serverLevel.getBiome(blockPos)).orElseThrow(() -> new IllegalStateException("Unregistered biome")))) {
            return false;
        }
        if (!(this.feature == null || bl && serverLevel.structureFeatureManager().getStructureAt(blockPos, true, this.feature).isValid())) {
            return false;
        }
        if (!(this.smokey == null || bl && this.smokey == CampfireBlock.isSmokeyPos(serverLevel, blockPos))) {
            return false;
        }
        if (!this.light.matches(serverLevel, blockPos)) {
            return false;
        }
        if (!this.block.matches(serverLevel, blockPos)) {
            return false;
        }
        return this.fluid.matches(serverLevel, blockPos);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        if (!(this.x.isAny() && this.y.isAny() && this.z.isAny())) {
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.add("x", this.x.serializeToJson());
            jsonObject2.add("y", this.y.serializeToJson());
            jsonObject2.add("z", this.z.serializeToJson());
            jsonObject.add("position", jsonObject2);
        }
        if (this.dimension != null) {
            Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, this.dimension).resultOrPartial(LOGGER::error).ifPresent(jsonElement -> jsonObject.add("dimension", (JsonElement)jsonElement));
        }
        if (this.feature != null) {
            jsonObject.addProperty("feature", this.feature.getFeatureName());
        }
        if (this.biome != null) {
            jsonObject.addProperty("biome", this.biome.location().toString());
        }
        if (this.smokey != null) {
            jsonObject.addProperty("smokey", this.smokey);
        }
        jsonObject.add("light", this.light.serializeToJson());
        jsonObject.add("block", this.block.serializeToJson());
        jsonObject.add("fluid", this.fluid.serializeToJson());
        return jsonObject;
    }

    public static LocationPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "location");
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "position", new JsonObject());
        MinMaxBounds.Floats floats = MinMaxBounds.Floats.fromJson(jsonObject2.get("x"));
        MinMaxBounds.Floats floats2 = MinMaxBounds.Floats.fromJson(jsonObject2.get("y"));
        MinMaxBounds.Floats floats3 = MinMaxBounds.Floats.fromJson(jsonObject2.get("z"));
        ResourceKey resourceKey = jsonObject.has("dimension") ? (ResourceKey)ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("dimension")).resultOrPartial(LOGGER::error).map(resourceLocation -> ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceLocation)).orElse(null) : null;
        StructureFeature structureFeature = jsonObject.has("feature") ? (StructureFeature)StructureFeature.STRUCTURES_REGISTRY.get(GsonHelper.getAsString(jsonObject, "feature")) : null;
        ResourceKey<Biome> resourceKey2 = null;
        if (jsonObject.has("biome")) {
            ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "biome"));
            resourceKey2 = ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation2);
        }
        Boolean boolean_ = jsonObject.has("smokey") ? Boolean.valueOf(jsonObject.get("smokey").getAsBoolean()) : null;
        LightPredicate lightPredicate = LightPredicate.fromJson(jsonObject.get("light"));
        BlockPredicate blockPredicate = BlockPredicate.fromJson(jsonObject.get("block"));
        FluidPredicate fluidPredicate = FluidPredicate.fromJson(jsonObject.get("fluid"));
        return new LocationPredicate(floats, floats2, floats3, resourceKey2, structureFeature, resourceKey, boolean_, lightPredicate, blockPredicate, fluidPredicate);
    }

    public static class Builder {
        private MinMaxBounds.Floats x = MinMaxBounds.Floats.ANY;
        private MinMaxBounds.Floats y = MinMaxBounds.Floats.ANY;
        private MinMaxBounds.Floats z = MinMaxBounds.Floats.ANY;
        @Nullable
        private ResourceKey<Biome> biome;
        @Nullable
        private StructureFeature<?> feature;
        @Nullable
        private ResourceKey<Level> dimension;
        @Nullable
        private Boolean smokey;
        private LightPredicate light = LightPredicate.ANY;
        private BlockPredicate block = BlockPredicate.ANY;
        private FluidPredicate fluid = FluidPredicate.ANY;

        public static Builder location() {
            return new Builder();
        }

        public Builder setBiome(@Nullable ResourceKey<Biome> resourceKey) {
            this.biome = resourceKey;
            return this;
        }

        public Builder setBlock(BlockPredicate blockPredicate) {
            this.block = blockPredicate;
            return this;
        }

        public Builder setSmokey(Boolean boolean_) {
            this.smokey = boolean_;
            return this;
        }

        public LocationPredicate build() {
            return new LocationPredicate(this.x, this.y, this.z, this.biome, this.feature, this.dimension, this.smokey, this.light, this.block, this.fluid);
        }
    }
}


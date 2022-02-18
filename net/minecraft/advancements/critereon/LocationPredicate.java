/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
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
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LocationPredicate {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final LocationPredicate ANY = new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, null, null, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    private final MinMaxBounds.Doubles x;
    private final MinMaxBounds.Doubles y;
    private final MinMaxBounds.Doubles z;
    @Nullable
    private final ResourceKey<Biome> biome;
    @Nullable
    private final ResourceKey<ConfiguredStructureFeature<?, ?>> feature;
    @Nullable
    private final ResourceKey<Level> dimension;
    @Nullable
    private final Boolean smokey;
    private final LightPredicate light;
    private final BlockPredicate block;
    private final FluidPredicate fluid;

    public LocationPredicate(MinMaxBounds.Doubles doubles, MinMaxBounds.Doubles doubles2, MinMaxBounds.Doubles doubles3, @Nullable ResourceKey<Biome> resourceKey, @Nullable ResourceKey<ConfiguredStructureFeature<?, ?>> resourceKey2, @Nullable ResourceKey<Level> resourceKey3, @Nullable Boolean boolean_, LightPredicate lightPredicate, BlockPredicate blockPredicate, FluidPredicate fluidPredicate) {
        this.x = doubles;
        this.y = doubles2;
        this.z = doubles3;
        this.biome = resourceKey;
        this.feature = resourceKey2;
        this.dimension = resourceKey3;
        this.smokey = boolean_;
        this.light = lightPredicate;
        this.block = blockPredicate;
        this.fluid = fluidPredicate;
    }

    public static LocationPredicate inBiome(ResourceKey<Biome> resourceKey) {
        return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, resourceKey, null, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    }

    public static LocationPredicate inDimension(ResourceKey<Level> resourceKey) {
        return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, null, null, resourceKey, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    }

    public static LocationPredicate inFeature(ResourceKey<ConfiguredStructureFeature<?, ?>> resourceKey) {
        return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, null, resourceKey, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    }

    public static LocationPredicate atYLocation(MinMaxBounds.Doubles doubles) {
        return new LocationPredicate(MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY, null, null, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
    }

    public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
        if (!this.x.matches(d)) {
            return false;
        }
        if (!this.y.matches(e)) {
            return false;
        }
        if (!this.z.matches(f)) {
            return false;
        }
        if (this.dimension != null && this.dimension != serverLevel.dimension()) {
            return false;
        }
        BlockPos blockPos = new BlockPos(d, e, f);
        boolean bl = serverLevel.isLoaded(blockPos);
        if (!(this.biome == null || bl && serverLevel.getBiome(blockPos).is(this.biome))) {
            return false;
        }
        if (!(this.feature == null || bl && serverLevel.structureFeatureManager().getStructureWithPieceAt(blockPos, this.feature).isValid())) {
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
            jsonObject.addProperty("feature", this.feature.location().toString());
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
        ResourceKey resourceKey;
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "location");
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "position", new JsonObject());
        MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromJson(jsonObject2.get("x"));
        MinMaxBounds.Doubles doubles2 = MinMaxBounds.Doubles.fromJson(jsonObject2.get("y"));
        MinMaxBounds.Doubles doubles3 = MinMaxBounds.Doubles.fromJson(jsonObject2.get("z"));
        ResourceKey resourceKey2 = jsonObject.has("dimension") ? (ResourceKey)ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("dimension")).resultOrPartial(LOGGER::error).map(resourceLocation -> ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceLocation)).orElse(null) : (resourceKey = null);
        ResourceKey resourceKey22 = jsonObject.has("feature") ? (ResourceKey)ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonObject.get("feature")).resultOrPartial(LOGGER::error).map(resourceLocation -> ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, resourceLocation)).orElse(null) : null;
        ResourceKey<Biome> resourceKey3 = null;
        if (jsonObject.has("biome")) {
            ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(jsonObject, "biome"));
            resourceKey3 = ResourceKey.create(Registry.BIOME_REGISTRY, resourceLocation2);
        }
        Boolean boolean_ = jsonObject.has("smokey") ? Boolean.valueOf(jsonObject.get("smokey").getAsBoolean()) : null;
        LightPredicate lightPredicate = LightPredicate.fromJson(jsonObject.get("light"));
        BlockPredicate blockPredicate = BlockPredicate.fromJson(jsonObject.get("block"));
        FluidPredicate fluidPredicate = FluidPredicate.fromJson(jsonObject.get("fluid"));
        return new LocationPredicate(doubles, doubles2, doubles3, resourceKey3, resourceKey22, resourceKey, boolean_, lightPredicate, blockPredicate, fluidPredicate);
    }

    public static class Builder {
        private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
        @Nullable
        private ResourceKey<Biome> biome;
        @Nullable
        private ResourceKey<ConfiguredStructureFeature<?, ?>> feature;
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

        public Builder setX(MinMaxBounds.Doubles doubles) {
            this.x = doubles;
            return this;
        }

        public Builder setY(MinMaxBounds.Doubles doubles) {
            this.y = doubles;
            return this;
        }

        public Builder setZ(MinMaxBounds.Doubles doubles) {
            this.z = doubles;
            return this;
        }

        public Builder setBiome(@Nullable ResourceKey<Biome> resourceKey) {
            this.biome = resourceKey;
            return this;
        }

        public Builder setFeature(@Nullable ResourceKey<ConfiguredStructureFeature<?, ?>> resourceKey) {
            this.feature = resourceKey;
            return this;
        }

        public Builder setDimension(@Nullable ResourceKey<Level> resourceKey) {
            this.dimension = resourceKey;
            return this;
        }

        public Builder setLight(LightPredicate lightPredicate) {
            this.light = lightPredicate;
            return this;
        }

        public Builder setBlock(BlockPredicate blockPredicate) {
            this.block = blockPredicate;
            return this;
        }

        public Builder setFluid(FluidPredicate fluidPredicate) {
            this.fluid = fluidPredicate;
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


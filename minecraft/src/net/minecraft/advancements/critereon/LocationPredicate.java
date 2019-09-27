package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class LocationPredicate {
	public static final LocationPredicate ANY = new LocationPredicate(
		MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, null, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY
	);
	private final MinMaxBounds.Floats x;
	private final MinMaxBounds.Floats y;
	private final MinMaxBounds.Floats z;
	@Nullable
	private final Biome biome;
	@Nullable
	private final StructureFeature<?> feature;
	@Nullable
	private final DimensionType dimension;
	private final LightPredicate light;
	private final BlockPredicate block;
	private final FluidPredicate fluid;

	public LocationPredicate(
		MinMaxBounds.Floats floats,
		MinMaxBounds.Floats floats2,
		MinMaxBounds.Floats floats3,
		@Nullable Biome biome,
		@Nullable StructureFeature<?> structureFeature,
		@Nullable DimensionType dimensionType,
		LightPredicate lightPredicate,
		BlockPredicate blockPredicate,
		FluidPredicate fluidPredicate
	) {
		this.x = floats;
		this.y = floats2;
		this.z = floats3;
		this.biome = biome;
		this.feature = structureFeature;
		this.dimension = dimensionType;
		this.light = lightPredicate;
		this.block = blockPredicate;
		this.fluid = fluidPredicate;
	}

	public static LocationPredicate inBiome(Biome biome) {
		return new LocationPredicate(
			MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, MinMaxBounds.Floats.ANY, biome, null, null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY
		);
	}

	public static LocationPredicate inDimension(DimensionType dimensionType) {
		return new LocationPredicate(
			MinMaxBounds.Floats.ANY,
			MinMaxBounds.Floats.ANY,
			MinMaxBounds.Floats.ANY,
			null,
			null,
			dimensionType,
			LightPredicate.ANY,
			BlockPredicate.ANY,
			FluidPredicate.ANY
		);
	}

	public static LocationPredicate inFeature(StructureFeature<?> structureFeature) {
		return new LocationPredicate(
			MinMaxBounds.Floats.ANY,
			MinMaxBounds.Floats.ANY,
			MinMaxBounds.Floats.ANY,
			null,
			structureFeature,
			null,
			LightPredicate.ANY,
			BlockPredicate.ANY,
			FluidPredicate.ANY
		);
	}

	public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
		return this.matches(serverLevel, (float)d, (float)e, (float)f);
	}

	public boolean matches(ServerLevel serverLevel, float f, float g, float h) {
		if (!this.x.matches(f)) {
			return false;
		} else if (!this.y.matches(g)) {
			return false;
		} else if (!this.z.matches(h)) {
			return false;
		} else if (this.dimension != null && this.dimension != serverLevel.dimension.getType()) {
			return false;
		} else {
			BlockPos blockPos = new BlockPos((double)f, (double)g, (double)h);
			boolean bl = serverLevel.isLoaded(blockPos);
			if (this.biome == null || bl && this.biome == serverLevel.getBiome(blockPos)) {
				if (this.feature == null || bl && this.feature.isInsideFeature(serverLevel, blockPos)) {
					if (!this.light.matches(serverLevel, blockPos)) {
						return false;
					} else {
						return !this.block.matches(serverLevel, blockPos) ? false : this.fluid.matches(serverLevel, blockPos);
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			if (!this.x.isAny() || !this.y.isAny() || !this.z.isAny()) {
				JsonObject jsonObject2 = new JsonObject();
				jsonObject2.add("x", this.x.serializeToJson());
				jsonObject2.add("y", this.y.serializeToJson());
				jsonObject2.add("z", this.z.serializeToJson());
				jsonObject.add("position", jsonObject2);
			}

			if (this.dimension != null) {
				jsonObject.addProperty("dimension", DimensionType.getName(this.dimension).toString());
			}

			if (this.feature != null) {
				jsonObject.addProperty("feature", (String)Feature.STRUCTURES_REGISTRY.inverse().get(this.feature));
			}

			if (this.biome != null) {
				jsonObject.addProperty("biome", Registry.BIOME.getKey(this.biome).toString());
			}

			jsonObject.add("light", this.light.serializeToJson());
			jsonObject.add("block", this.block.serializeToJson());
			jsonObject.add("fluid", this.fluid.serializeToJson());
			return jsonObject;
		}
	}

	public static LocationPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "location");
			JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "position", new JsonObject());
			MinMaxBounds.Floats floats = MinMaxBounds.Floats.fromJson(jsonObject2.get("x"));
			MinMaxBounds.Floats floats2 = MinMaxBounds.Floats.fromJson(jsonObject2.get("y"));
			MinMaxBounds.Floats floats3 = MinMaxBounds.Floats.fromJson(jsonObject2.get("z"));
			DimensionType dimensionType = jsonObject.has("dimension")
				? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "dimension")))
				: null;
			StructureFeature<?> structureFeature = jsonObject.has("feature")
				? (StructureFeature)Feature.STRUCTURES_REGISTRY.get(GsonHelper.getAsString(jsonObject, "feature"))
				: null;
			Biome biome = null;
			if (jsonObject.has("biome")) {
				ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "biome"));
				biome = (Biome)Registry.BIOME.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown biome '" + resourceLocation + "'"));
			}

			LightPredicate lightPredicate = LightPredicate.fromJson(jsonObject.get("light"));
			BlockPredicate blockPredicate = BlockPredicate.fromJson(jsonObject.get("block"));
			FluidPredicate fluidPredicate = FluidPredicate.fromJson(jsonObject.get("fluid"));
			return new LocationPredicate(floats, floats2, floats3, biome, structureFeature, dimensionType, lightPredicate, blockPredicate, fluidPredicate);
		} else {
			return ANY;
		}
	}

	public static class Builder {
		private MinMaxBounds.Floats x = MinMaxBounds.Floats.ANY;
		private MinMaxBounds.Floats y = MinMaxBounds.Floats.ANY;
		private MinMaxBounds.Floats z = MinMaxBounds.Floats.ANY;
		@Nullable
		private Biome biome;
		@Nullable
		private StructureFeature<?> feature;
		@Nullable
		private DimensionType dimension;
		private LightPredicate light = LightPredicate.ANY;
		private BlockPredicate block = BlockPredicate.ANY;
		private FluidPredicate fluid = FluidPredicate.ANY;

		public static LocationPredicate.Builder location() {
			return new LocationPredicate.Builder();
		}

		public LocationPredicate.Builder setBiome(@Nullable Biome biome) {
			this.biome = biome;
			return this;
		}

		public LocationPredicate build() {
			return new LocationPredicate(this.x, this.y, this.z, this.biome, this.feature, this.dimension, this.light, this.block, this.fluid);
		}
	}
}

package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;

public record LocationPredicate(
	Optional<LocationPredicate.PositionPredicate> position,
	Optional<ResourceKey<Biome>> biome,
	Optional<ResourceKey<Structure>> structure,
	Optional<ResourceKey<Level>> dimension,
	Optional<Boolean> smokey,
	Optional<LightPredicate> light,
	Optional<BlockPredicate> block,
	Optional<FluidPredicate> fluid
) {
	public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(LocationPredicate.PositionPredicate.CODEC, "position").forGetter(LocationPredicate::position),
					ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.BIOME), "biome").forGetter(LocationPredicate::biome),
					ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.STRUCTURE), "structure").forGetter(LocationPredicate::structure),
					ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.DIMENSION), "dimension").forGetter(LocationPredicate::dimension),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "smokey").forGetter(LocationPredicate::smokey),
					ExtraCodecs.strictOptionalField(LightPredicate.CODEC, "light").forGetter(LocationPredicate::light),
					ExtraCodecs.strictOptionalField(BlockPredicate.CODEC, "block").forGetter(LocationPredicate::block),
					ExtraCodecs.strictOptionalField(FluidPredicate.CODEC, "fluid").forGetter(LocationPredicate::fluid)
				)
				.apply(instance, LocationPredicate::new)
	);

	static Optional<LocationPredicate> of(
		Optional<LocationPredicate.PositionPredicate> optional,
		Optional<ResourceKey<Biome>> optional2,
		Optional<ResourceKey<Structure>> optional3,
		Optional<ResourceKey<Level>> optional4,
		Optional<Boolean> optional5,
		Optional<LightPredicate> optional6,
		Optional<BlockPredicate> optional7,
		Optional<FluidPredicate> optional8
	) {
		return optional.isEmpty()
				&& optional2.isEmpty()
				&& optional3.isEmpty()
				&& optional4.isEmpty()
				&& optional5.isEmpty()
				&& optional6.isEmpty()
				&& optional7.isEmpty()
				&& optional8.isEmpty()
			? Optional.empty()
			: Optional.of(new LocationPredicate(optional, optional2, optional3, optional4, optional5, optional6, optional7, optional8));
	}

	public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
		if (this.position.isPresent() && !((LocationPredicate.PositionPredicate)this.position.get()).matches(d, e, f)) {
			return false;
		} else if (this.dimension.isPresent() && this.dimension.get() != serverLevel.dimension()) {
			return false;
		} else {
			BlockPos blockPos = BlockPos.containing(d, e, f);
			boolean bl = serverLevel.isLoaded(blockPos);
			if (!this.biome.isPresent() || bl && serverLevel.getBiome(blockPos).is((ResourceKey<Biome>)this.biome.get())) {
				if (!this.structure.isPresent()
					|| bl && serverLevel.structureManager().getStructureWithPieceAt(blockPos, (ResourceKey<Structure>)this.structure.get()).isValid()) {
					if (!this.smokey.isPresent() || bl && (Boolean)this.smokey.get() == CampfireBlock.isSmokeyPos(serverLevel, blockPos)) {
						if (this.light.isPresent() && !((LightPredicate)this.light.get()).matches(serverLevel, blockPos)) {
							return false;
						} else {
							return this.block.isPresent() && !((BlockPredicate)this.block.get()).matches(serverLevel, blockPos)
								? false
								: !this.fluid.isPresent() || ((FluidPredicate)this.fluid.get()).matches(serverLevel, blockPos);
						}
					} else {
						return false;
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
		return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalStateException::new);
	}

	public static Optional<LocationPredicate> fromJson(@Nullable JsonElement jsonElement) {
		return jsonElement != null && !jsonElement.isJsonNull()
			? Optional.of(Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, jsonElement), JsonParseException::new))
			: Optional.empty();
	}

	public static class Builder {
		private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
		private Optional<ResourceKey<Biome>> biome = Optional.empty();
		private Optional<ResourceKey<Structure>> structure = Optional.empty();
		private Optional<ResourceKey<Level>> dimension = Optional.empty();
		private Optional<Boolean> smokey = Optional.empty();
		private Optional<LightPredicate> light = Optional.empty();
		private Optional<BlockPredicate> block = Optional.empty();
		private Optional<FluidPredicate> fluid = Optional.empty();

		public static LocationPredicate.Builder location() {
			return new LocationPredicate.Builder();
		}

		public static LocationPredicate.Builder inBiome(ResourceKey<Biome> resourceKey) {
			return location().setBiome(resourceKey);
		}

		public static LocationPredicate.Builder inDimension(ResourceKey<Level> resourceKey) {
			return location().setDimension(resourceKey);
		}

		public static LocationPredicate.Builder inStructure(ResourceKey<Structure> resourceKey) {
			return location().setStructure(resourceKey);
		}

		public static LocationPredicate.Builder atYLocation(MinMaxBounds.Doubles doubles) {
			return location().setY(doubles);
		}

		public LocationPredicate.Builder setX(MinMaxBounds.Doubles doubles) {
			this.x = doubles;
			return this;
		}

		public LocationPredicate.Builder setY(MinMaxBounds.Doubles doubles) {
			this.y = doubles;
			return this;
		}

		public LocationPredicate.Builder setZ(MinMaxBounds.Doubles doubles) {
			this.z = doubles;
			return this;
		}

		public LocationPredicate.Builder setBiome(ResourceKey<Biome> resourceKey) {
			this.biome = Optional.of(resourceKey);
			return this;
		}

		public LocationPredicate.Builder setStructure(ResourceKey<Structure> resourceKey) {
			this.structure = Optional.of(resourceKey);
			return this;
		}

		public LocationPredicate.Builder setDimension(ResourceKey<Level> resourceKey) {
			this.dimension = Optional.of(resourceKey);
			return this;
		}

		public LocationPredicate.Builder setLight(LightPredicate.Builder builder) {
			this.light = builder.build();
			return this;
		}

		public LocationPredicate.Builder setBlock(BlockPredicate.Builder builder) {
			this.block = builder.build();
			return this;
		}

		public LocationPredicate.Builder setFluid(FluidPredicate.Builder builder) {
			this.fluid = builder.build();
			return this;
		}

		public LocationPredicate.Builder setSmokey(boolean bl) {
			this.smokey = Optional.of(bl);
			return this;
		}

		public Optional<LocationPredicate> build() {
			return LocationPredicate.of(
				LocationPredicate.PositionPredicate.of(this.x, this.y, this.z), this.biome, this.structure, this.dimension, this.smokey, this.light, this.block, this.fluid
			);
		}
	}

	static record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
		public static final Codec<LocationPredicate.PositionPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "x", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::x),
						ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "y", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::y),
						ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "z", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::z)
					)
					.apply(instance, LocationPredicate.PositionPredicate::new)
		);

		static Optional<LocationPredicate.PositionPredicate> of(MinMaxBounds.Doubles doubles, MinMaxBounds.Doubles doubles2, MinMaxBounds.Doubles doubles3) {
			return doubles.isAny() && doubles2.isAny() && doubles3.isAny()
				? Optional.empty()
				: Optional.of(new LocationPredicate.PositionPredicate(doubles, doubles2, doubles3));
		}

		public boolean matches(double d, double e, double f) {
			return this.x.matches(d) && this.y.matches(e) && this.z.matches(f);
		}
	}
}

package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;

public record LocationPredicate(
	Optional<LocationPredicate.PositionPredicate> position,
	Optional<HolderSet<Biome>> biomes,
	Optional<HolderSet<Structure>> structures,
	Optional<ResourceKey<Level>> dimension,
	Optional<Boolean> smokey,
	Optional<LightPredicate> light,
	Optional<BlockPredicate> block,
	Optional<FluidPredicate> fluid,
	Optional<Boolean> canSeeSky
) {
	public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					LocationPredicate.PositionPredicate.CODEC.optionalFieldOf("position").forGetter(LocationPredicate::position),
					RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(LocationPredicate::biomes),
					RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(LocationPredicate::structures),
					ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(LocationPredicate::dimension),
					Codec.BOOL.optionalFieldOf("smokey").forGetter(LocationPredicate::smokey),
					LightPredicate.CODEC.optionalFieldOf("light").forGetter(LocationPredicate::light),
					BlockPredicate.CODEC.optionalFieldOf("block").forGetter(LocationPredicate::block),
					FluidPredicate.CODEC.optionalFieldOf("fluid").forGetter(LocationPredicate::fluid),
					Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(LocationPredicate::canSeeSky)
				)
				.apply(instance, LocationPredicate::new)
	);

	public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
		if (this.position.isPresent() && !((LocationPredicate.PositionPredicate)this.position.get()).matches(d, e, f)) {
			return false;
		} else if (this.dimension.isPresent() && this.dimension.get() != serverLevel.dimension()) {
			return false;
		} else {
			BlockPos blockPos = BlockPos.containing(d, e, f);
			boolean bl = serverLevel.isLoaded(blockPos);
			if (!this.biomes.isPresent() || bl && ((HolderSet)this.biomes.get()).contains(serverLevel.getBiome(blockPos))) {
				if (!this.structures.isPresent()
					|| bl && serverLevel.structureManager().getStructureWithPieceAt(blockPos, (HolderSet<Structure>)this.structures.get()).isValid()) {
					if (!this.smokey.isPresent() || bl && (Boolean)this.smokey.get() == CampfireBlock.isSmokeyPos(serverLevel, blockPos)) {
						if (this.light.isPresent() && !((LightPredicate)this.light.get()).matches(serverLevel, blockPos)) {
							return false;
						} else if (this.block.isPresent() && !((BlockPredicate)this.block.get()).matches(serverLevel, blockPos)) {
							return false;
						} else {
							return this.fluid.isPresent() && !((FluidPredicate)this.fluid.get()).matches(serverLevel, blockPos)
								? false
								: !this.canSeeSky.isPresent() || (Boolean)this.canSeeSky.get() == serverLevel.canSeeSky(blockPos);
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

	public static class Builder {
		private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
		private Optional<HolderSet<Biome>> biomes = Optional.empty();
		private Optional<HolderSet<Structure>> structures = Optional.empty();
		private Optional<ResourceKey<Level>> dimension = Optional.empty();
		private Optional<Boolean> smokey = Optional.empty();
		private Optional<LightPredicate> light = Optional.empty();
		private Optional<BlockPredicate> block = Optional.empty();
		private Optional<FluidPredicate> fluid = Optional.empty();
		private Optional<Boolean> canSeeSky = Optional.empty();

		public static LocationPredicate.Builder location() {
			return new LocationPredicate.Builder();
		}

		public static LocationPredicate.Builder inBiome(Holder<Biome> holder) {
			return location().setBiomes(HolderSet.direct(holder));
		}

		public static LocationPredicate.Builder inDimension(ResourceKey<Level> resourceKey) {
			return location().setDimension(resourceKey);
		}

		public static LocationPredicate.Builder inStructure(Holder<Structure> holder) {
			return location().setStructures(HolderSet.direct(holder));
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

		public LocationPredicate.Builder setBiomes(HolderSet<Biome> holderSet) {
			this.biomes = Optional.of(holderSet);
			return this;
		}

		public LocationPredicate.Builder setStructures(HolderSet<Structure> holderSet) {
			this.structures = Optional.of(holderSet);
			return this;
		}

		public LocationPredicate.Builder setDimension(ResourceKey<Level> resourceKey) {
			this.dimension = Optional.of(resourceKey);
			return this;
		}

		public LocationPredicate.Builder setLight(LightPredicate.Builder builder) {
			this.light = Optional.of(builder.build());
			return this;
		}

		public LocationPredicate.Builder setBlock(BlockPredicate.Builder builder) {
			this.block = Optional.of(builder.build());
			return this;
		}

		public LocationPredicate.Builder setFluid(FluidPredicate.Builder builder) {
			this.fluid = Optional.of(builder.build());
			return this;
		}

		public LocationPredicate.Builder setSmokey(boolean bl) {
			this.smokey = Optional.of(bl);
			return this;
		}

		public LocationPredicate.Builder setCanSeeSky(boolean bl) {
			this.canSeeSky = Optional.of(bl);
			return this;
		}

		public LocationPredicate build() {
			Optional<LocationPredicate.PositionPredicate> optional = LocationPredicate.PositionPredicate.of(this.x, this.y, this.z);
			return new LocationPredicate(optional, this.biomes, this.structures, this.dimension, this.smokey, this.light, this.block, this.fluid, this.canSeeSky);
		}
	}

	static record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
		public static final Codec<LocationPredicate.PositionPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::x),
						MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::y),
						MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(LocationPredicate.PositionPredicate::z)
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

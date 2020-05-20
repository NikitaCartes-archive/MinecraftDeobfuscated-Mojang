package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceSpheroidConfiguration implements FeatureConfiguration {
	public static final Codec<ReplaceSpheroidConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("target").forGetter(replaceSpheroidConfiguration -> replaceSpheroidConfiguration.targetState),
					BlockState.CODEC.fieldOf("state").forGetter(replaceSpheroidConfiguration -> replaceSpheroidConfiguration.replaceState),
					Vec3i.CODEC.fieldOf("minimum_reach").forGetter(replaceSpheroidConfiguration -> replaceSpheroidConfiguration.minimumReach),
					Vec3i.CODEC.fieldOf("maximum_reach").forGetter(replaceSpheroidConfiguration -> replaceSpheroidConfiguration.maximumReach)
				)
				.apply(instance, ReplaceSpheroidConfiguration::new)
	);
	public final BlockState targetState;
	public final BlockState replaceState;
	public final Vec3i minimumReach;
	public final Vec3i maximumReach;

	public ReplaceSpheroidConfiguration(BlockState blockState, BlockState blockState2, Vec3i vec3i, Vec3i vec3i2) {
		this.targetState = blockState;
		this.replaceState = blockState2;
		this.minimumReach = vec3i;
		this.maximumReach = vec3i2;
	}

	public static class Builder {
		private BlockState target = Blocks.AIR.defaultBlockState();
		private BlockState state = Blocks.AIR.defaultBlockState();
		private Vec3i minimumReach = Vec3i.ZERO;
		private Vec3i maximumReach = Vec3i.ZERO;

		public ReplaceSpheroidConfiguration.Builder targetBlockState(BlockState blockState) {
			this.target = blockState;
			return this;
		}

		public ReplaceSpheroidConfiguration.Builder replaceWithBlockState(BlockState blockState) {
			this.state = blockState;
			return this;
		}

		public ReplaceSpheroidConfiguration.Builder minimumReach(Vec3i vec3i) {
			this.minimumReach = vec3i;
			return this;
		}

		public ReplaceSpheroidConfiguration.Builder maximumReach(Vec3i vec3i) {
			this.maximumReach = vec3i;
			return this;
		}

		public ReplaceSpheroidConfiguration build() {
			if (this.minimumReach.getX() >= 0 && this.minimumReach.getY() >= 0 && this.minimumReach.getZ() >= 0) {
				if (this.minimumReach.getX() <= this.maximumReach.getX()
					&& this.minimumReach.getY() <= this.maximumReach.getY()
					&& this.minimumReach.getZ() <= this.maximumReach.getZ()) {
					return new ReplaceSpheroidConfiguration(this.target, this.state, this.minimumReach, this.maximumReach);
				} else {
					throw new IllegalArgumentException("Maximum reach must be greater than minimum reach for each axis");
				}
			} else {
				throw new IllegalArgumentException("Minimum reach cannot be less than zero");
			}
		}
	}
}

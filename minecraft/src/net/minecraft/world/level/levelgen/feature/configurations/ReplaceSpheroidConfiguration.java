package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceSpheroidConfiguration implements FeatureConfiguration {
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

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("target"), BlockState.serialize(dynamicOps, this.targetState).getValue());
		builder.put(dynamicOps.createString("state"), BlockState.serialize(dynamicOps, this.replaceState).getValue());
		builder.put(dynamicOps.createString("minimum_reach_x"), dynamicOps.createInt(this.minimumReach.getX()));
		builder.put(dynamicOps.createString("minimum_reach_y"), dynamicOps.createInt(this.minimumReach.getY()));
		builder.put(dynamicOps.createString("minimum_reach_z"), dynamicOps.createInt(this.minimumReach.getZ()));
		builder.put(dynamicOps.createString("maximum_reach_x"), dynamicOps.createInt(this.maximumReach.getX()));
		builder.put(dynamicOps.createString("maximum_reach_y"), dynamicOps.createInt(this.maximumReach.getY()));
		builder.put(dynamicOps.createString("maximum_reach_z"), dynamicOps.createInt(this.maximumReach.getZ()));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
	}

	public static <T> ReplaceSpheroidConfiguration deserialize(Dynamic<T> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("target").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState2 = (BlockState)dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		int i = dynamic.get("minimum_reach_x").asInt(0);
		int j = dynamic.get("minimum_reach_y").asInt(0);
		int k = dynamic.get("minimum_reach_z").asInt(0);
		int l = dynamic.get("maximum_reach_x").asInt(0);
		int m = dynamic.get("maximum_reach_y").asInt(0);
		int n = dynamic.get("maximum_reach_z").asInt(0);
		return new ReplaceSpheroidConfiguration(blockState, blockState2, new Vec3i(i, j, k), new Vec3i(l, m, n));
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

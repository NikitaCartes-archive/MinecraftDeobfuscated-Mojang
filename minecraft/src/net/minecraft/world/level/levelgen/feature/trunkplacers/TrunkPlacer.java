package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class TrunkPlacer {
	private final int baseHeight;
	private final int heightRandA;
	private final int heightRandB;
	protected final TrunkPlacerType<?> type;

	public TrunkPlacer(int i, int j, int k, TrunkPlacerType<?> trunkPlacerType) {
		this.baseHeight = i;
		this.heightRandA = j;
		this.heightRandB = k;
		this.type = trunkPlacerType;
	}

	public abstract Map<BlockPos, Integer> placeTrunk(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		int i,
		BlockPos blockPos,
		int j,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	);

	public int getBaseHeight() {
		return this.baseHeight;
	}

	public int getTreeHeight(Random random, SmallTreeConfiguration smallTreeConfiguration) {
		return this.baseHeight + random.nextInt(this.heightRandA + 1) + random.nextInt(this.heightRandB + 1);
	}

	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.TRUNK_PLACER_TYPES.getKey(this.type).toString()))
			.put(dynamicOps.createString("base_height"), dynamicOps.createInt(this.baseHeight))
			.put(dynamicOps.createString("height_rand_a"), dynamicOps.createInt(this.heightRandA))
			.put(dynamicOps.createString("height_rand_b"), dynamicOps.createInt(this.heightRandB));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
	}
}

package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaPineFoliagePlacer extends FoliagePlacer {
	private final int heightRand;
	private final int crownHeight;

	public MegaPineFoliagePlacer(int i, int j, int k, int l, int m, int n) {
		super(i, j, k, l, FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER);
		this.heightRand = m;
		this.crownHeight = n;
	}

	public <T> MegaPineFoliagePlacer(Dynamic<T> dynamic) {
		this(
			dynamic.get("radius").asInt(0),
			dynamic.get("radius_random").asInt(0),
			dynamic.get("offset").asInt(0),
			dynamic.get("offset_random").asInt(0),
			dynamic.get("height_rand").asInt(0),
			dynamic.get("crown_height").asInt(0)
		);
	}

	@Override
	protected void createFoliage(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k,
		Set<BlockPos> set,
		int l
	) {
		BlockPos blockPos = foliageAttachment.foliagePos();
		int m = 0;

		for (int n = blockPos.getY() - j + l; n <= blockPos.getY() + l; n++) {
			int o = blockPos.getY() - n;
			int p = k + foliageAttachment.radiusOffset() + Mth.floor((float)o / (float)j * 3.5F);
			int q;
			if (o > 0 && p == m && (n & 1) == 0) {
				q = p + 1;
			} else {
				q = p;
			}

			this.placeLeavesRow(
				levelSimulatedRW, random, treeConfiguration, new BlockPos(blockPos.getX(), n, blockPos.getZ()), q, set, 0, foliageAttachment.doubleTrunk()
			);
			m = p;
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return random.nextInt(this.heightRand + 1) + this.crownHeight;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		return i + k >= 7 ? true : i * i + k * k > l * l;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("height_rand"), dynamicOps.createInt(this.heightRand));
		builder.put(dynamicOps.createString("crown_height"), dynamicOps.createInt(this.crownHeight));
		return dynamicOps.merge(super.serialize(dynamicOps), dynamicOps.createMap(builder.build()));
	}
}

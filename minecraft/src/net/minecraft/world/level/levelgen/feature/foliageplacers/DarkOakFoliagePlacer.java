package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class DarkOakFoliagePlacer extends FoliagePlacer {
	public static final Codec<DarkOakFoliagePlacer> CODEC = RecordCodecBuilder.create(
		instance -> foliagePlacerParts(instance).apply(instance, DarkOakFoliagePlacer::new)
	);

	public DarkOakFoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
		super(intProvider, intProvider2);
	}

	@Override
	protected FoliagePlacerType<?> type() {
		return FoliagePlacerType.DARK_OAK_FOLIAGE_PLACER;
	}

	@Override
	protected void createFoliage(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		Random random,
		TreeConfiguration treeConfiguration,
		int i,
		FoliagePlacer.FoliageAttachment foliageAttachment,
		int j,
		int k,
		int l
	) {
		BlockPos blockPos = foliageAttachment.pos().above(l);
		boolean bl = foliageAttachment.doubleTrunk();
		if (bl) {
			this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k + 2, -1, bl);
			this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k + 3, 0, bl);
			this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k + 2, 1, bl);
			if (random.nextBoolean()) {
				this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k, 2, bl);
			}
		} else {
			this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k + 2, -1, bl);
			this.placeLeavesRow(levelSimulatedReader, biConsumer, random, treeConfiguration, blockPos, k + 1, 0, bl);
		}
	}

	@Override
	public int foliageHeight(Random random, int i, TreeConfiguration treeConfiguration) {
		return 4;
	}

	@Override
	protected boolean shouldSkipLocationSigned(Random random, int i, int j, int k, int l, boolean bl) {
		return j != 0 || !bl || i != -l && i < l || k != -l && k < l ? super.shouldSkipLocationSigned(random, i, j, k, l, bl) : true;
	}

	@Override
	protected boolean shouldSkipLocation(Random random, int i, int j, int k, int l, boolean bl) {
		if (j == -1 && !bl) {
			return i == l && k == l;
		} else {
			return j == 1 ? i + k > l * 2 - 2 : false;
		}
	}
}

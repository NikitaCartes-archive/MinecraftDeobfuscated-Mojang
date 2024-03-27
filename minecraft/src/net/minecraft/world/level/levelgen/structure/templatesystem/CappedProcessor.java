package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ServerLevelAccessor;

public class CappedProcessor extends StructureProcessor {
	public static final MapCodec<CappedProcessor> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					StructureProcessorType.SINGLE_CODEC.fieldOf("delegate").forGetter(cappedProcessor -> cappedProcessor.delegate),
					IntProvider.POSITIVE_CODEC.fieldOf("limit").forGetter(cappedProcessor -> cappedProcessor.limit)
				)
				.apply(instance, CappedProcessor::new)
	);
	private final StructureProcessor delegate;
	private final IntProvider limit;

	public CappedProcessor(StructureProcessor structureProcessor, IntProvider intProvider) {
		this.delegate = structureProcessor;
		this.limit = intProvider;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return StructureProcessorType.CAPPED;
	}

	@Override
	public final List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
		ServerLevelAccessor serverLevelAccessor,
		BlockPos blockPos,
		BlockPos blockPos2,
		List<StructureTemplate.StructureBlockInfo> list,
		List<StructureTemplate.StructureBlockInfo> list2,
		StructurePlaceSettings structurePlaceSettings
	) {
		if (this.limit.getMaxValue() != 0 && !list2.isEmpty()) {
			if (list.size() != list2.size()) {
				Util.logAndPauseIfInIde(
					"Original block info list not in sync with processed list, skipping processing. Original size: " + list.size() + ", Processed size: " + list2.size()
				);
				return list2;
			} else {
				RandomSource randomSource = RandomSource.create(serverLevelAccessor.getLevel().getSeed()).forkPositional().at(blockPos);
				int i = Math.min(this.limit.sample(randomSource), list2.size());
				if (i < 1) {
					return list2;
				} else {
					IntArrayList intArrayList = Util.toShuffledList(IntStream.range(0, list2.size()), randomSource);
					IntIterator intIterator = intArrayList.intIterator();
					int j = 0;

					while (intIterator.hasNext() && j < i) {
						int k = intIterator.nextInt();
						StructureTemplate.StructureBlockInfo structureBlockInfo = (StructureTemplate.StructureBlockInfo)list.get(k);
						StructureTemplate.StructureBlockInfo structureBlockInfo2 = (StructureTemplate.StructureBlockInfo)list2.get(k);
						StructureTemplate.StructureBlockInfo structureBlockInfo3 = this.delegate
							.processBlock(serverLevelAccessor, blockPos, blockPos2, structureBlockInfo, structureBlockInfo2, structurePlaceSettings);
						if (structureBlockInfo3 != null && !structureBlockInfo2.equals(structureBlockInfo3)) {
							j++;
							list2.set(k, structureBlockInfo3);
						}
					}

					return list2;
				}
			}
		} else {
			return list2;
		}
	}
}

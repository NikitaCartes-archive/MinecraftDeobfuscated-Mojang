package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class RuleProcessor extends StructureProcessor {
	private final ImmutableList<ProcessorRule> rules;

	public RuleProcessor(List<ProcessorRule> list) {
		this.rules = ImmutableList.copyOf(list);
	}

	public RuleProcessor(Dynamic<?> dynamic) {
		this(dynamic.get("rules").asList(ProcessorRule::deserialize));
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo processBlock(
		LevelReader levelReader,
		BlockPos blockPos,
		BlockPos blockPos2,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		StructureTemplate.StructureBlockInfo structureBlockInfo2,
		StructurePlaceSettings structurePlaceSettings
	) {
		Random random = new Random(Mth.getSeed(structureBlockInfo2.pos));
		BlockState blockState = levelReader.getBlockState(structureBlockInfo2.pos);

		for (ProcessorRule processorRule : this.rules) {
			if (processorRule.test(structureBlockInfo2.state, blockState, structureBlockInfo.pos, structureBlockInfo2.pos, blockPos2, random)) {
				return new StructureTemplate.StructureBlockInfo(structureBlockInfo2.pos, processorRule.getOutputState(), processorRule.getOutputTag());
			}
		}

		return structureBlockInfo2;
	}

	@Override
	protected StructureProcessorType getType() {
		return StructureProcessorType.RULE;
	}

	@Override
	protected <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("rules"), dynamicOps.createList(this.rules.stream().map(processorRule -> processorRule.serialize(dynamicOps).getValue()))
				)
			)
		);
	}
}

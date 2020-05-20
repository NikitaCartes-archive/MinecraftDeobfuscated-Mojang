package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ProcessorRule {
	public static final Codec<ProcessorRule> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					RuleTest.CODEC.fieldOf("input_predicate").forGetter(processorRule -> processorRule.inputPredicate),
					RuleTest.CODEC.fieldOf("location_predicate").forGetter(processorRule -> processorRule.locPredicate),
					PosRuleTest.CODEC.fieldOf("position_predicate").forGetter(processorRule -> processorRule.posPredicate),
					BlockState.CODEC.fieldOf("output_state").forGetter(processorRule -> processorRule.outputState),
					CompoundTag.CODEC.optionalFieldOf("output_nbt").forGetter(processorRule -> Optional.ofNullable(processorRule.outputTag))
				)
				.apply(instance, ProcessorRule::new)
	);
	private final RuleTest inputPredicate;
	private final RuleTest locPredicate;
	private final PosRuleTest posPredicate;
	private final BlockState outputState;
	@Nullable
	private final CompoundTag outputTag;

	public ProcessorRule(RuleTest ruleTest, RuleTest ruleTest2, BlockState blockState) {
		this(ruleTest, ruleTest2, PosAlwaysTrueTest.INSTANCE, blockState, Optional.empty());
	}

	public ProcessorRule(RuleTest ruleTest, RuleTest ruleTest2, PosRuleTest posRuleTest, BlockState blockState) {
		this(ruleTest, ruleTest2, posRuleTest, blockState, Optional.empty());
	}

	public ProcessorRule(RuleTest ruleTest, RuleTest ruleTest2, PosRuleTest posRuleTest, BlockState blockState, Optional<CompoundTag> optional) {
		this.inputPredicate = ruleTest;
		this.locPredicate = ruleTest2;
		this.posPredicate = posRuleTest;
		this.outputState = blockState;
		this.outputTag = (CompoundTag)optional.orElse(null);
	}

	public boolean test(BlockState blockState, BlockState blockState2, BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
		return this.inputPredicate.test(blockState, random)
			&& this.locPredicate.test(blockState2, random)
			&& this.posPredicate.test(blockPos, blockPos2, blockPos3, random);
	}

	public BlockState getOutputState() {
		return this.outputState;
	}

	@Nullable
	public CompoundTag getOutputTag() {
		return this.outputTag;
	}
}

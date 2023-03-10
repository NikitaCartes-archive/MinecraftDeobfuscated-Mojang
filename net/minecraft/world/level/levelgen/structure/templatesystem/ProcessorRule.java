/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.Nullable;

public class ProcessorRule {
    public static final Codec<ProcessorRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RuleTest.CODEC.fieldOf("input_predicate")).forGetter(processorRule -> processorRule.inputPredicate), ((MapCodec)RuleTest.CODEC.fieldOf("location_predicate")).forGetter(processorRule -> processorRule.locPredicate), PosRuleTest.CODEC.optionalFieldOf("position_predicate", PosAlwaysTrueTest.INSTANCE).forGetter(processorRule -> processorRule.posPredicate), ((MapCodec)BlockState.CODEC.fieldOf("output_state")).forGetter(processorRule -> processorRule.outputState), CompoundTag.CODEC.optionalFieldOf("output_nbt").forGetter(processorRule -> Optional.ofNullable(processorRule.outputTag))).apply((Applicative<ProcessorRule, ?>)instance, ProcessorRule::new));
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
        this.outputTag = optional.orElse(null);
    }

    public boolean test(BlockState blockState, BlockState blockState2, BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, RandomSource randomSource) {
        return this.inputPredicate.test(blockState, randomSource) && this.locPredicate.test(blockState2, randomSource) && this.posPredicate.test(blockPos, blockPos2, blockPos3, randomSource);
    }

    public BlockState getOutputState() {
        return this.outputState;
    }

    @Nullable
    public CompoundTag getOutputTag() {
        return this.outputTag;
    }
}


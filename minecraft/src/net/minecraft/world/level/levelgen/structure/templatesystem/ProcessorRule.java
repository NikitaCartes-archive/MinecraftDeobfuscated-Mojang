package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.block.state.BlockState;

public class ProcessorRule {
	private final RuleTest inputPredicate;
	private final RuleTest locPredicate;
	private final BlockState outputState;
	@Nullable
	private final CompoundTag outputTag;

	public ProcessorRule(RuleTest ruleTest, RuleTest ruleTest2, BlockState blockState) {
		this(ruleTest, ruleTest2, blockState, null);
	}

	public ProcessorRule(RuleTest ruleTest, RuleTest ruleTest2, BlockState blockState, @Nullable CompoundTag compoundTag) {
		this.inputPredicate = ruleTest;
		this.locPredicate = ruleTest2;
		this.outputState = blockState;
		this.outputTag = compoundTag;
	}

	public boolean test(BlockState blockState, BlockState blockState2, Random random) {
		return this.inputPredicate.test(blockState, random) && this.locPredicate.test(blockState2, random);
	}

	public BlockState getOutputState() {
		return this.outputState;
	}

	@Nullable
	public CompoundTag getOutputTag() {
		return this.outputTag;
	}

	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		T object = dynamicOps.createMap(
			ImmutableMap.of(
				dynamicOps.createString("input_predicate"),
				this.inputPredicate.serialize(dynamicOps).getValue(),
				dynamicOps.createString("location_predicate"),
				this.locPredicate.serialize(dynamicOps).getValue(),
				dynamicOps.createString("output_state"),
				BlockState.serialize(dynamicOps, this.outputState).getValue()
			)
		);
		return this.outputTag == null
			? new Dynamic<>(dynamicOps, object)
			: new Dynamic<>(
				dynamicOps,
				dynamicOps.mergeInto(object, dynamicOps.createString("output_nbt"), new Dynamic<>(NbtOps.INSTANCE, this.outputTag).convert(dynamicOps).getValue())
			);
	}

	public static <T> ProcessorRule deserialize(Dynamic<T> dynamic) {
		Dynamic<T> dynamic2 = dynamic.get("input_predicate").orElseEmptyMap();
		Dynamic<T> dynamic3 = dynamic.get("location_predicate").orElseEmptyMap();
		RuleTest ruleTest = Deserializer.deserialize(dynamic2, Registry.RULE_TEST, "predicate_type", AlwaysTrueTest.INSTANCE);
		RuleTest ruleTest2 = Deserializer.deserialize(dynamic3, Registry.RULE_TEST, "predicate_type", AlwaysTrueTest.INSTANCE);
		BlockState blockState = BlockState.deserialize(dynamic.get("output_state").orElseEmptyMap());
		CompoundTag compoundTag = (CompoundTag)dynamic.get("output_nbt").map(dynamicx -> dynamicx.convert(NbtOps.INSTANCE).getValue()).orElse(null);
		return new ProcessorRule(ruleTest, ruleTest2, blockState, compoundTag);
	}
}

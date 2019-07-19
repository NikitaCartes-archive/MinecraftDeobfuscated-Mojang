/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.Nullable;

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
        T object = dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("input_predicate"), this.inputPredicate.serialize(dynamicOps).getValue(), dynamicOps.createString("location_predicate"), this.locPredicate.serialize(dynamicOps).getValue(), dynamicOps.createString("output_state"), BlockState.serialize(dynamicOps, this.outputState).getValue()));
        if (this.outputTag == null) {
            return new Dynamic<T>(dynamicOps, object);
        }
        return new Dynamic<T>(dynamicOps, dynamicOps.mergeInto(object, dynamicOps.createString("output_nbt"), new Dynamic<CompoundTag>(NbtOps.INSTANCE, this.outputTag).convert(dynamicOps).getValue()));
    }

    public static <T> ProcessorRule deserialize(Dynamic<T> dynamic2) {
        Dynamic<T> dynamic22 = dynamic2.get("input_predicate").orElseEmptyMap();
        Dynamic<T> dynamic3 = dynamic2.get("location_predicate").orElseEmptyMap();
        RuleTest ruleTest = Deserializer.deserialize(dynamic22, Registry.RULE_TEST, "predicate_type", AlwaysTrueTest.INSTANCE);
        RuleTest ruleTest2 = Deserializer.deserialize(dynamic3, Registry.RULE_TEST, "predicate_type", AlwaysTrueTest.INSTANCE);
        BlockState blockState = BlockState.deserialize(dynamic2.get("output_state").orElseEmptyMap());
        CompoundTag compoundTag = dynamic2.get("output_nbt").map(dynamic -> dynamic.convert(NbtOps.INSTANCE).getValue()).orElse(null);
        return new ProcessorRule(ruleTest, ruleTest2, blockState, compoundTag);
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public abstract class RuleTest {
    public abstract boolean test(BlockState var1, Random var2);

    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.mergeInto(this.getDynamic(dynamicOps).getValue(), dynamicOps.createString("predicate_type"), dynamicOps.createString(Registry.RULE_TEST.getKey(this.getType()).toString())));
    }

    protected abstract RuleTestType getType();

    protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> var1);
}


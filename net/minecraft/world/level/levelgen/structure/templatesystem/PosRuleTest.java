/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;

public abstract class PosRuleTest {
    public abstract boolean test(BlockPos var1, BlockPos var2, BlockPos var3, Random var4);

    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.mergeInto(this.getDynamic(dynamicOps).getValue(), dynamicOps.createString("predicate_type"), dynamicOps.createString(Registry.POS_RULE_TEST.getKey(this.getType()).toString())));
    }

    protected abstract PosRuleTestType getType();

    protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> var1);
}


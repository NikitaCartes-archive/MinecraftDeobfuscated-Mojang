/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityKeepPacked
extends NamedEntityFix {
    public BlockEntityKeepPacked(Schema schema, boolean bl) {
        super(schema, bl, "BlockEntityKeepPacked", References.BLOCK_ENTITY, "DUMMY");
    }

    private static Dynamic<?> fixTag(Dynamic<?> dynamic) {
        return dynamic.set("keepPacked", dynamic.createBoolean(true));
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), BlockEntityKeepPacked::fixTag);
    }
}


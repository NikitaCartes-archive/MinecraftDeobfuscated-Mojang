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

public class CatTypeFix
extends NamedEntityFix {
    public CatTypeFix(Schema schema, boolean bl) {
        super(schema, bl, "CatTypeFix", References.ENTITY, "minecraft:cat");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        if (dynamic.get("CatType").asInt(0) == 9) {
            return dynamic.set("CatType", dynamic.createInt(10));
        }
        return dynamic;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}


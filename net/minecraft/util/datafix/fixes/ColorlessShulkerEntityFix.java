/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class ColorlessShulkerEntityFix
extends NamedEntityFix {
    public ColorlessShulkerEntityFix(Schema schema, boolean bl) {
        super(schema, bl, "Colorless shulker entity fix", References.ENTITY, "minecraft:shulker");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> {
            if (dynamic.get("Color").asInt(0) == 10) {
                return dynamic.set("Color", dynamic.createByte((byte)16));
            }
            return dynamic;
        });
    }
}


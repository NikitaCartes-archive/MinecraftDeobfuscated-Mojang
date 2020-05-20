/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityShulkerColorFix
extends NamedEntityFix {
    public EntityShulkerColorFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityShulkerColorFix", References.ENTITY, "minecraft:shulker");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        if (!dynamic.get("Color").map(Dynamic::asNumber).result().isPresent()) {
            return dynamic.set("Color", dynamic.createByte((byte)10));
        }
        return dynamic;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}


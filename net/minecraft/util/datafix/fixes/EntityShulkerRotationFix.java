/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.List;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityShulkerRotationFix
extends NamedEntityFix {
    public EntityShulkerRotationFix(Schema schema) {
        super(schema, false, "EntityShulkerRotationFix", References.ENTITY, "minecraft:shulker");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic2) {
        List<Double> list = dynamic2.get("Rotation").asList(dynamic -> dynamic.asDouble(180.0));
        if (!list.isEmpty()) {
            list.set(0, list.get(0) - 180.0);
            return dynamic2.set("Rotation", dynamic2.createList(list.stream().map(dynamic2::createDouble)));
        }
        return dynamic2;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}


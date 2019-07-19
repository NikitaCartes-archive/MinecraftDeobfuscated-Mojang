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

public class EntityItemFrameDirectionFix
extends NamedEntityFix {
    public EntityItemFrameDirectionFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityItemFrameDirectionFix", References.ENTITY, "minecraft:item_frame");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        return dynamic.set("Facing", dynamic.createByte(EntityItemFrameDirectionFix.direction2dTo3d(dynamic.get("Facing").asByte((byte)0))));
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }

    private static byte direction2dTo3d(byte b) {
        switch (b) {
            default: {
                return 2;
            }
            case 0: {
                return 3;
            }
            case 1: {
                return 4;
            }
            case 3: 
        }
        return 5;
    }
}


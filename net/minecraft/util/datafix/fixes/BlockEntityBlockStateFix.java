/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityBlockStateFix
extends NamedEntityFix {
    public BlockEntityBlockStateFix(Schema schema, boolean bl) {
        super(schema, bl, "BlockEntityBlockStateFix", References.BLOCK_ENTITY, "minecraft:piston");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        Type<?> type = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:piston");
        Type<?> type2 = type.findFieldType("blockState");
        OpticFinder<?> opticFinder = DSL.fieldFinder("blockState", type2);
        Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
        int i = dynamic.get("blockId").asInt(0);
        dynamic = dynamic.remove("blockId");
        int j = dynamic.get("blockData").asInt(0) & 0xF;
        dynamic = dynamic.remove("blockData");
        Dynamic<?> dynamic2 = BlockStateData.getTag(i << 4 | j);
        Typed<?> typed2 = type.pointTyped(typed.getOps()).orElseThrow(() -> new IllegalStateException("Could not create new piston block entity."));
        return typed2.set(DSL.remainderFinder(), dynamic).set(opticFinder, type2.readTyped(dynamic2).getSecond().orElseThrow(() -> new IllegalStateException("Could not parse newly created block state tag.")));
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.EntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityCustomNameToComponentFix
extends DataFix {
    public BlockEntityCustomNameToComponentFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<String> opticFinder = DSL.fieldFinder("id", DSL.namespacedString());
        return this.fixTypeEverywhereTyped("BlockEntityCustomNameToComponentFix", this.getInputSchema().getType(References.BLOCK_ENTITY), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && Objects.equals(optional.get(), "minecraft:command_block")) {
                return dynamic;
            }
            return EntityCustomNameToComponentFix.fixTagCustomName(dynamic);
        }));
    }
}


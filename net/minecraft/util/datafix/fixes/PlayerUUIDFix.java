/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;

public class PlayerUUIDFix
extends AbstractUUIDFix {
    public PlayerUUIDFix(Schema schema) {
        super(schema, References.PLAYER);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("PlayerUUIDFix", this.getInputSchema().getType(this.typeReference), typed2 -> {
            OpticFinder<?> opticFinder = typed2.getType().findField("RootVehicle");
            return typed2.updateTyped(opticFinder, opticFinder.type(), typed -> typed.update(DSL.remainderFinder(), dynamic -> PlayerUUIDFix.replaceUUIDLeastMost(dynamic, "Attach", "Attach").orElse((Dynamic<?>)dynamic)));
        });
    }
}


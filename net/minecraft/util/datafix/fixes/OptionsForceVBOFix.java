/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class OptionsForceVBOFix
extends DataFix {
    public OptionsForceVBOFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsForceVBOFix", this.getInputSchema().getType(References.OPTIONS), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.set("useVbo", dynamic.createString("true"))));
    }
}


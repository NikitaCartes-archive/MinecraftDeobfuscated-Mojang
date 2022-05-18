/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.util.datafix.fixes.NamedEntityFix;

public class VariantRenameFix
extends NamedEntityFix {
    private final Map<String, String> renames;

    public VariantRenameFix(Schema schema, String string, DSL.TypeReference typeReference, String string2, Map<String, String> map) {
        super(schema, false, string, typeReference, string2);
        this.renames = map;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("variant", dynamic -> DataFixUtils.orElse(dynamic.asString().map(string -> dynamic.createString(this.renames.getOrDefault(string, (String)string))).result(), dynamic)));
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.util.datafix.fixes.NamedEntityFix;

public class EntityVariantFix
extends NamedEntityFix {
    private final String fieldName;
    private final IntFunction<String> idConversions;

    public EntityVariantFix(Schema schema, String string, DSL.TypeReference typeReference, String string2, String string3, IntFunction<String> intFunction) {
        super(schema, false, string, typeReference, string2);
        this.fieldName = string3;
        this.idConversions = intFunction;
    }

    private static <T> Dynamic<T> updateAndRename(Dynamic<T> dynamic, String string, String string2, Function<Dynamic<T>, Dynamic<T>> function) {
        return dynamic.map(object3 -> {
            DynamicOps<Object> dynamicOps = dynamic.getOps();
            Function<Object, Object> function2 = object -> ((Dynamic)function.apply(new Dynamic<Object>(dynamicOps, object))).getValue();
            return dynamicOps.get(object3, string).map(object2 -> dynamicOps.set(object3, string2, function2.apply(object2))).result().orElse(object3);
        });
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> EntityVariantFix.updateAndRename(dynamic2, this.fieldName, "variant", dynamic -> DataFixUtils.orElse(dynamic.asNumber().map(number -> dynamic.createString(this.idConversions.apply(number.intValue()))).result(), dynamic)));
    }
}


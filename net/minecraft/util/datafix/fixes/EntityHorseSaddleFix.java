/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityHorseSaddleFix
extends NamedEntityFix {
    public EntityHorseSaddleFix(Schema schema, boolean bl) {
        super(schema, bl, "EntityHorseSaddleFix", References.ENTITY, "EntityHorse");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        Type<?> type = this.getInputSchema().getTypeRaw(References.ITEM_STACK);
        OpticFinder<?> opticFinder2 = DSL.fieldFinder("SaddleItem", type);
        Optional<Typed<?>> optional = typed.getOptionalTyped(opticFinder2);
        Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
        if (!optional.isPresent() && dynamic.get("Saddle").asBoolean(false)) {
            Typed<?> typed2 = type.pointTyped(typed.getOps()).orElseThrow(IllegalStateException::new);
            typed2 = typed2.set(opticFinder, Pair.of(References.ITEM_NAME.typeName(), "minecraft:saddle"));
            Dynamic dynamic2 = dynamic.emptyMap();
            dynamic2 = dynamic2.set("Count", dynamic2.createByte((byte)1));
            dynamic2 = dynamic2.set("Damage", dynamic2.createShort((short)0));
            typed2 = typed2.set(DSL.remainderFinder(), dynamic2);
            dynamic.remove("Saddle");
            return typed.set(opticFinder2, typed2).set(DSL.remainderFinder(), dynamic);
        }
        return typed;
    }
}


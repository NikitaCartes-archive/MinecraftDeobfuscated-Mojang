/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class IglooMetadataRemovalFix
extends DataFix {
    public IglooMetadataRemovalFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", type, typed -> typed.update(DSL.remainderFinder(), IglooMetadataRemovalFix::fixTag));
    }

    private static <T> Dynamic<T> fixTag(Dynamic<T> dynamic) {
        boolean bl = dynamic.get("Children").asStreamOpt().map(stream -> stream.allMatch(IglooMetadataRemovalFix::isIglooPiece)).result().orElse(false);
        if (bl) {
            return dynamic.set("id", dynamic.createString("Igloo")).remove("Children");
        }
        return dynamic.update("Children", IglooMetadataRemovalFix::removeIglooPieces);
    }

    private static <T> Dynamic<T> removeIglooPieces(Dynamic<T> dynamic) {
        return dynamic.asStreamOpt().map(stream -> stream.filter(dynamic -> !IglooMetadataRemovalFix.isIglooPiece(dynamic))).map(dynamic::createList).result().orElse(dynamic);
    }

    private static boolean isIglooPiece(Dynamic<?> dynamic) {
        return dynamic.get("id").asString("").equals("Iglu");
    }
}


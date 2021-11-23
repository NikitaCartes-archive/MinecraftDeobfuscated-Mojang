/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;

public class ChunkRenamesFix
extends DataFix {
    public ChunkRenamesFix(Schema schema) {
        super(schema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> opticFinder = type.findField("Level");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("Structures");
        Type<?> type2 = this.getOutputSchema().getType(References.CHUNK);
        Type<?> type3 = type2.findFieldType("structures");
        return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", type, type2, (Typed<?> typed2) -> {
            Typed<Dynamic<?>> typed22 = typed2.getTyped(opticFinder);
            Typed<Pair<String, Object>> typed3 = ChunkRenamesFix.appendChunkName(typed22);
            typed3 = typed3.set(DSL.remainderFinder(), ChunkRenamesFix.mergeRemainders(typed2, typed22.get(DSL.remainderFinder())));
            typed3 = ChunkRenamesFix.renameField(typed3, "TileEntities", "block_entities");
            typed3 = ChunkRenamesFix.renameField(typed3, "TileTicks", "block_ticks");
            typed3 = ChunkRenamesFix.renameField(typed3, "Entities", "entities");
            typed3 = ChunkRenamesFix.renameField(typed3, "Sections", "sections");
            typed3 = typed3.updateTyped(opticFinder2, type3, typed -> ChunkRenamesFix.renameField(typed, "Starts", "starts"));
            typed3 = ChunkRenamesFix.renameField(typed3, "Structures", "structures");
            return typed3.update(DSL.remainderFinder(), dynamic -> dynamic.remove("Level"));
        });
    }

    private static Typed<?> renameField(Typed<?> typed, String string, String string2) {
        return ChunkRenamesFix.renameFieldHelper(typed, string, string2, typed.getType().findFieldType(string)).update(DSL.remainderFinder(), dynamic -> dynamic.remove(string));
    }

    private static <A> Typed<?> renameFieldHelper(Typed<?> typed, String string, String string2, Type<A> type) {
        Type<Either<A, Unit>> type2 = DSL.optional(DSL.field(string, type));
        Type<Either<A, Unit>> type3 = DSL.optional(DSL.field(string2, type));
        return typed.update(type2.finder(), type3, Function.identity());
    }

    private static <A> Typed<Pair<String, A>> appendChunkName(Typed<A> typed) {
        return new Typed<Pair<String, A>>(DSL.named("chunk", typed.getType()), typed.getOps(), Pair.of("chunk", typed.getValue()));
    }

    private static <T> Dynamic<T> mergeRemainders(Typed<?> typed, Dynamic<T> dynamic) {
        DynamicOps dynamicOps = dynamic.getOps();
        Dynamic dynamic2 = typed.get(DSL.remainderFinder()).convert(dynamicOps);
        DataResult dataResult = dynamicOps.getMap(dynamic.getValue()).flatMap(mapLike -> dynamicOps.mergeToMap(dynamic2.getValue(), (MapLike)mapLike));
        return dataResult.result().map(object -> new Dynamic<Object>(dynamicOps, object)).orElse(dynamic);
    }
}


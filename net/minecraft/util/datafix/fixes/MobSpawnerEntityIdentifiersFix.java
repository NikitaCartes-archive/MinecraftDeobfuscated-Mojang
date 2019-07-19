/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class MobSpawnerEntityIdentifiersFix
extends DataFix {
    public MobSpawnerEntityIdentifiersFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    private Dynamic<?> fix(Dynamic<?> dynamic2) {
        Optional<Stream<Dynamic<?>>> optional2;
        if (!"MobSpawner".equals(dynamic2.get("id").asString(""))) {
            return dynamic2;
        }
        Optional<String> optional = dynamic2.get("EntityId").asString();
        if (optional.isPresent()) {
            Dynamic dynamic22 = DataFixUtils.orElse(dynamic2.get("SpawnData").get(), dynamic2.emptyMap());
            dynamic22 = dynamic22.set("id", dynamic22.createString(optional.get().isEmpty() ? "Pig" : optional.get()));
            dynamic2 = dynamic2.set("SpawnData", dynamic22);
            dynamic2 = dynamic2.remove("EntityId");
        }
        if ((optional2 = dynamic2.get("SpawnPotentials").asStreamOpt()).isPresent()) {
            dynamic2 = dynamic2.set("SpawnPotentials", dynamic2.createList(optional2.get().map(dynamic -> {
                Optional<String> optional = dynamic.get("Type").asString();
                if (optional.isPresent()) {
                    Dynamic dynamic2 = DataFixUtils.orElse(dynamic.get("Properties").get(), dynamic.emptyMap()).set("id", dynamic.createString(optional.get()));
                    return dynamic.set("Entity", dynamic2).remove("Type").remove("Properties");
                }
                return dynamic;
            })));
        }
        return dynamic2;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getOutputSchema().getType(References.UNTAGGED_SPAWNER);
        return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(References.UNTAGGED_SPAWNER), type, (Typed<?> typed) -> {
            Dynamic dynamic = typed.get(DSL.remainderFinder());
            Pair pair = type.readTyped(this.fix(dynamic = dynamic.set("id", dynamic.createString("MobSpawner"))));
            if (!pair.getSecond().isPresent()) {
                return typed;
            }
            return pair.getSecond().get();
        });
    }
}


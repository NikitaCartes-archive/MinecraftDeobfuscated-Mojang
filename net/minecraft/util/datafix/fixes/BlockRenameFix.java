/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class BlockRenameFix
extends DataFix {
    private final String name;

    public BlockRenameFix(Schema schema, String string) {
        super(schema, false);
        this.name = string;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<Pair<String, String>> type2;
        Type<?> type = this.getInputSchema().getType(References.BLOCK_NAME);
        if (!Objects.equals(type, type2 = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.namespacedString()))) {
            throw new IllegalStateException("block type is not what was expected.");
        }
        TypeRewriteRule typeRewriteRule = this.fixTypeEverywhere(this.name + " for block", type2, dynamicOps -> pair -> pair.mapSecond(this::fixBlock));
        TypeRewriteRule typeRewriteRule2 = this.fixTypeEverywhereTyped(this.name + " for block_state", this.getInputSchema().getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Optional<String> optional = dynamic.get("Name").asString().result();
            if (optional.isPresent()) {
                return dynamic.set("Name", dynamic.createString(this.fixBlock(optional.get())));
            }
            return dynamic;
        }));
        return TypeRewriteRule.seq(typeRewriteRule, typeRewriteRule2);
    }

    protected abstract String fixBlock(String var1);

    public static DataFix create(Schema schema, String string, final Function<String, String> function) {
        return new BlockRenameFix(schema, string){

            @Override
            protected String fixBlock(String string) {
                return (String)function.apply(string);
            }
        };
    }
}


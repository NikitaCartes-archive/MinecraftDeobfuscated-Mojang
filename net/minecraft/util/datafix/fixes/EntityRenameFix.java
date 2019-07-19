/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.fixes.References;

public abstract class EntityRenameFix
extends DataFix {
    protected final String name;

    public EntityRenameFix(String string, Schema schema, boolean bl) {
        super(schema, bl);
        this.name = string;
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType2 = this.getOutputSchema().findChoiceType(References.ENTITY);
        return this.fixTypeEverywhere(this.name, taggedChoiceType, taggedChoiceType2, dynamicOps -> pair -> {
            String string = (String)pair.getFirst();
            Type<?> type = taggedChoiceType.types().get(string);
            Pair<String, Typed<?>> pair2 = this.fix(string, this.getEntity(pair.getSecond(), (DynamicOps<?>)dynamicOps, type));
            Type<?> type2 = taggedChoiceType2.types().get(pair2.getFirst());
            if (!type2.equals(pair2.getSecond().getType(), true, true)) {
                throw new IllegalStateException(String.format("Dynamic type check failed: %s not equal to %s", type2, pair2.getSecond().getType()));
            }
            return Pair.of(pair2.getFirst(), pair2.getSecond().getValue());
        });
    }

    private <A> Typed<A> getEntity(Object object, DynamicOps<?> dynamicOps, Type<A> type) {
        return new Typed<Object>(type, dynamicOps, object);
    }

    protected abstract Pair<String, Typed<?>> fix(String var1, Typed<?> var2);
}


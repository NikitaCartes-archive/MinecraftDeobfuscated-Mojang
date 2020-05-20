/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.References;

public class EntityMinecartIdentifiersFix
extends DataFix {
    private static final List<String> MINECART_BY_ID = Lists.newArrayList("MinecartRideable", "MinecartChest", "MinecartFurnace");

    public EntityMinecartIdentifiersFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType2 = this.getOutputSchema().findChoiceType(References.ENTITY);
        return this.fixTypeEverywhere("EntityMinecartIdentifiersFix", taggedChoiceType, taggedChoiceType2, dynamicOps -> pair -> {
            if (Objects.equals(pair.getFirst(), "Minecart")) {
                Typed<Pair<String, ?>> typed = taggedChoiceType.point((DynamicOps<?>)dynamicOps, "Minecart", pair.getSecond()).orElseThrow(IllegalStateException::new);
                Dynamic<?> dynamic2 = typed.getOrCreate(DSL.remainderFinder());
                int i = dynamic2.get("Type").asInt(0);
                String string = i > 0 && i < MINECART_BY_ID.size() ? MINECART_BY_ID.get(i) : "MinecartRideable";
                return Pair.of(string, typed.write().map(dynamic -> taggedChoiceType2.types().get(string).read(dynamic)).result().orElseThrow(() -> new IllegalStateException("Could not read the new minecart.")));
            }
            return pair;
        });
    }
}


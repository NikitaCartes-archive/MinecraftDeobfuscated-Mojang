/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.EntityRenameFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityHorseSplitFix
extends EntityRenameFix {
    public EntityHorseSplitFix(Schema schema, boolean bl) {
        super("EntityHorseSplitFix", schema, bl);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String string, Typed<?> typed) {
        Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
        if (Objects.equals("EntityHorse", string)) {
            String string2;
            int i = dynamic.get("Type").asInt(0);
            switch (i) {
                default: {
                    string2 = "Horse";
                    break;
                }
                case 1: {
                    string2 = "Donkey";
                    break;
                }
                case 2: {
                    string2 = "Mule";
                    break;
                }
                case 3: {
                    string2 = "ZombieHorse";
                    break;
                }
                case 4: {
                    string2 = "SkeletonHorse";
                }
            }
            dynamic.remove("Type");
            Type<?> type = this.getOutputSchema().findChoiceType(References.ENTITY).types().get(string2);
            return Pair.of(string2, type.readTyped(typed.write()).getSecond().orElseThrow(() -> new IllegalStateException("Could not parse the new horse")));
        }
        return Pair.of(string, typed);
    }
}


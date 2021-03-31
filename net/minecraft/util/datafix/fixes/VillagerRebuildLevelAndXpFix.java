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
import com.mojang.datafixers.types.templates.List;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.References;

public class VillagerRebuildLevelAndXpFix
extends DataFix {
    private static final int TRADES_PER_LEVEL = 2;
    private static final int[] LEVEL_XP_THRESHOLDS = new int[]{0, 10, 50, 100, 150};

    public static int getMinXpPerLevel(int i) {
        return LEVEL_XP_THRESHOLDS[Mth.clamp(i - 1, 0, LEVEL_XP_THRESHOLDS.length - 1)];
    }

    public VillagerRebuildLevelAndXpFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, "minecraft:villager");
        OpticFinder<?> opticFinder = DSL.namedChoice("minecraft:villager", type);
        OpticFinder<?> opticFinder2 = type.findField("Offers");
        Type<?> type2 = opticFinder2.type();
        OpticFinder<?> opticFinder3 = type2.findField("Recipes");
        List.ListType listType = (List.ListType)opticFinder3.type();
        OpticFinder opticFinder4 = listType.getElement().finder();
        return this.fixTypeEverywhereTyped("Villager level and xp rebuild", this.getInputSchema().getType(References.ENTITY), typed -> typed.updateTyped(opticFinder, type, typed2 -> {
            Optional<Number> optional;
            int j;
            Dynamic<?> dynamic = typed2.get(DSL.remainderFinder());
            int i = dynamic.get("VillagerData").get("level").asInt(0);
            Typed<?> typed22 = typed2;
            if ((i == 0 || i == 1) && (i = Mth.clamp((j = typed2.getOptionalTyped(opticFinder2).flatMap(typed -> typed.getOptionalTyped(opticFinder3)).map(typed -> typed.getAllTyped(opticFinder4).size()).orElse(0).intValue()) / 2, 1, 5)) > 1) {
                typed22 = VillagerRebuildLevelAndXpFix.addLevel(typed22, i);
            }
            if (!(optional = dynamic.get("Xp").asNumber().result()).isPresent()) {
                typed22 = VillagerRebuildLevelAndXpFix.addXpFromLevel(typed22, i);
            }
            return typed22;
        }));
    }

    private static Typed<?> addLevel(Typed<?> typed, int i) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("VillagerData", dynamic -> dynamic.set("level", dynamic.createInt(i))));
    }

    private static Typed<?> addXpFromLevel(Typed<?> typed, int i) {
        int j = VillagerRebuildLevelAndXpFix.getMinXpPerLevel(i);
        return typed.update(DSL.remainderFinder(), dynamic -> dynamic.set("Xp", dynamic.createInt(j)));
    }
}


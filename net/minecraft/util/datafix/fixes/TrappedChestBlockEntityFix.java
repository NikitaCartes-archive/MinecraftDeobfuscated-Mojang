/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.types.templates.TaggedChoice;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.LeavesFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class TrappedChestBlockEntityFix
extends DataFix {
    private static final Logger LOGGER = LogManager.getLogger();

    public TrappedChestBlockEntityFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getOutputSchema().getType(References.CHUNK);
        Type<?> type2 = type.findFieldType("Level");
        Type<?> type3 = type2.findFieldType("TileEntities");
        if (!(type3 instanceof List.ListType)) {
            throw new IllegalStateException("Tile entity type is not a list type.");
        }
        List.ListType listType = (List.ListType)type3;
        OpticFinder opticFinder = DSL.fieldFinder("TileEntities", listType);
        Type<?> type4 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> opticFinder2 = type4.findField("Level");
        OpticFinder<?> opticFinder3 = opticFinder2.type().findField("Sections");
        Type<?> type5 = opticFinder3.type();
        if (!(type5 instanceof List.ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
        }
        Type type6 = ((List.ListType)type5).getElement();
        OpticFinder opticFinder4 = DSL.typeFinder(type6);
        return TypeRewriteRule.seq(new AddNewChoices(this.getOutputSchema(), "AddTrappedChestFix", References.BLOCK_ENTITY).makeRule(), this.fixTypeEverywhereTyped("Trapped Chest fix", type4, typed2 -> typed2.updateTyped(opticFinder2, typed -> {
            Optional optional = typed.getOptionalTyped(opticFinder3);
            if (!optional.isPresent()) {
                return typed;
            }
            List list = optional.get().getAllTyped(opticFinder4);
            IntOpenHashSet intSet = new IntOpenHashSet();
            for (Typed typed22 : list) {
                TrappedChestSection trappedChestSection = new TrappedChestSection(typed22, this.getInputSchema());
                if (trappedChestSection.isSkippable()) continue;
                for (int i = 0; i < 4096; ++i) {
                    int j = trappedChestSection.getBlock(i);
                    if (!trappedChestSection.isTrappedChest(j)) continue;
                    intSet.add(trappedChestSection.getIndex() << 12 | i);
                }
            }
            Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
            int k = dynamic.get("xPos").asInt(0);
            int l = dynamic.get("zPos").asInt(0);
            TaggedChoice.TaggedChoiceType<?> taggedChoiceType = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
            return typed.updateTyped(opticFinder, typed2 -> typed2.updateTyped(taggedChoiceType.finder(), typed -> {
                int m;
                int l;
                Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
                int k = dynamic.get("x").asInt(0) - (k << 4);
                if (intSet.contains(LeavesFix.getIndex(k, l = dynamic.get("y").asInt(0), m = dynamic.get("z").asInt(0) - (l << 4)))) {
                    return typed.update(taggedChoiceType.finder(), pair -> pair.mapFirst(string -> {
                        if (!Objects.equals(string, "minecraft:chest")) {
                            LOGGER.warn("Block Entity was expected to be a chest");
                        }
                        return "minecraft:trapped_chest";
                    }));
                }
                return typed;
            }));
        })));
    }

    public static final class TrappedChestSection
    extends LeavesFix.Section {
        @Nullable
        private IntSet chestIds;

        public TrappedChestSection(Typed<?> typed, Schema schema) {
            super(typed, schema);
        }

        @Override
        protected boolean skippable() {
            this.chestIds = new IntOpenHashSet();
            for (int i = 0; i < this.palette.size(); ++i) {
                Dynamic dynamic = (Dynamic)this.palette.get(i);
                String string = dynamic.get("Name").asString("");
                if (!Objects.equals(string, "minecraft:trapped_chest")) continue;
                this.chestIds.add(i);
            }
            return this.chestIds.isEmpty();
        }

        public boolean isTrappedChest(int i) {
            return this.chestIds.contains(i);
        }
    }
}


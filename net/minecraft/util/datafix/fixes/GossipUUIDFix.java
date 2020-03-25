/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class GossipUUIDFix
extends NamedEntityFix {
    public GossipUUIDFix(Schema schema, String string) {
        super(schema, false, "Gossip for for " + string, References.ENTITY, string);
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("Gossips", dynamic -> DataFixUtils.orElse(dynamic.asStreamOpt().map(stream -> stream.map(dynamic -> AbstractUUIDFix.replaceUUIDLeastMost(dynamic, "Target", "Target").orElse((Dynamic<?>)dynamic))).map(dynamic::createList), dynamic)));
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class RedstoneWireConnectionsFix
extends DataFix {
    public RedstoneWireConnectionsFix(Schema schema) {
        super(schema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        return this.fixTypeEverywhereTyped("RedstoneConnectionsFix", schema.getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), this::updateRedstoneConnections));
    }

    private <T> Dynamic<T> updateRedstoneConnections(Dynamic<T> dynamic) {
        boolean bl = dynamic.get("Name").asString().filter("minecraft:redstone_wire"::equals).isPresent();
        if (!bl) {
            return dynamic;
        }
        return dynamic.update("Properties", dynamic2 -> {
            String string = dynamic2.get("east").asString().orElseGet(() -> "none");
            String string2 = dynamic2.get("west").asString().orElseGet(() -> "none");
            String string3 = dynamic2.get("north").asString().orElseGet(() -> "none");
            String string4 = dynamic2.get("south").asString().orElseGet(() -> "none");
            boolean bl = RedstoneWireConnectionsFix.isConnected(string) || RedstoneWireConnectionsFix.isConnected(string2);
            boolean bl2 = RedstoneWireConnectionsFix.isConnected(string3) || RedstoneWireConnectionsFix.isConnected(string4);
            String string5 = !RedstoneWireConnectionsFix.isConnected(string) && !bl2 ? "side" : string;
            String string6 = !RedstoneWireConnectionsFix.isConnected(string2) && !bl2 ? "side" : string2;
            String string7 = !RedstoneWireConnectionsFix.isConnected(string3) && !bl ? "side" : string3;
            String string8 = !RedstoneWireConnectionsFix.isConnected(string4) && !bl ? "side" : string4;
            return dynamic2.update("east", dynamic -> dynamic.createString(string5)).update("west", dynamic -> dynamic.createString(string6)).update("north", dynamic -> dynamic.createString(string7)).update("south", dynamic -> dynamic.createString(string8));
        });
    }

    private static boolean isConnected(String string) {
        return !"none".equals(string);
    }
}


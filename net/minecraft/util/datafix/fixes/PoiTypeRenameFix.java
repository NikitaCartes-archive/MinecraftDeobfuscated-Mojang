/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.AbstractPoiSectionFix;

public class PoiTypeRenameFix
extends AbstractPoiSectionFix {
    private final Function<String, String> renamer;

    public PoiTypeRenameFix(Schema schema, String string, Function<String, String> function) {
        super(schema, string);
        this.renamer = function;
    }

    @Override
    protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> stream) {
        return stream.map(dynamic2 -> dynamic2.update("type", dynamic -> DataFixUtils.orElse(dynamic.asString().map(this.renamer).map(dynamic::createString).result(), dynamic)));
    }
}


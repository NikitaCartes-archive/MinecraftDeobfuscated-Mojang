/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;

public interface InstrumentTags {
    public static final TagKey<Instrument> REGULAR_GOAT_HORNS = InstrumentTags.create("regular_goat_horns");
    public static final TagKey<Instrument> SCREAMING_GOAT_HORNS = InstrumentTags.create("screaming_goat_horns");
    public static final TagKey<Instrument> GOAT_HORNS = InstrumentTags.create("goat_horns");

    private static TagKey<Instrument> create(String string) {
        return TagKey.create(Registry.INSTRUMENT_REGISTRY, new ResourceLocation(string));
    }
}


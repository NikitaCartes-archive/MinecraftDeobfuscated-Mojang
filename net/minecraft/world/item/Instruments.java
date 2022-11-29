/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Instrument;

public interface Instruments {
    public static final int GOAT_HORN_RANGE_BLOCKS = 256;
    public static final int GOAT_HORN_DURATION = 140;
    public static final ResourceKey<Instrument> PONDER_GOAT_HORN = Instruments.create("ponder_goat_horn");
    public static final ResourceKey<Instrument> SING_GOAT_HORN = Instruments.create("sing_goat_horn");
    public static final ResourceKey<Instrument> SEEK_GOAT_HORN = Instruments.create("seek_goat_horn");
    public static final ResourceKey<Instrument> FEEL_GOAT_HORN = Instruments.create("feel_goat_horn");
    public static final ResourceKey<Instrument> ADMIRE_GOAT_HORN = Instruments.create("admire_goat_horn");
    public static final ResourceKey<Instrument> CALL_GOAT_HORN = Instruments.create("call_goat_horn");
    public static final ResourceKey<Instrument> YEARN_GOAT_HORN = Instruments.create("yearn_goat_horn");
    public static final ResourceKey<Instrument> DREAM_GOAT_HORN = Instruments.create("dream_goat_horn");

    private static ResourceKey<Instrument> create(String string) {
        return ResourceKey.create(Registries.INSTRUMENT, new ResourceLocation(string));
    }

    public static Instrument bootstrap(Registry<Instrument> registry) {
        Registry.register(registry, PONDER_GOAT_HORN, new Instrument((Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0), 140, 256.0f));
        Registry.register(registry, SING_GOAT_HORN, new Instrument((Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(1), 140, 256.0f));
        Registry.register(registry, SEEK_GOAT_HORN, new Instrument((Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(2), 140, 256.0f));
        Registry.register(registry, FEEL_GOAT_HORN, new Instrument((Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3), 140, 256.0f));
        Registry.register(registry, ADMIRE_GOAT_HORN, new Instrument((Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(4), 140, 256.0f));
        Registry.register(registry, CALL_GOAT_HORN, new Instrument((Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(5), 140, 256.0f));
        Registry.register(registry, YEARN_GOAT_HORN, new Instrument((Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(6), 140, 256.0f));
        return Registry.register(registry, DREAM_GOAT_HORN, new Instrument((Holder)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(7), 140, 256.0f));
    }
}


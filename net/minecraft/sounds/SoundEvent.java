/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.sounds;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
    public static final Codec<SoundEvent> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.fieldOf("sound_id")).forGetter(SoundEvent::getLocation), Codec.FLOAT.optionalFieldOf("range").forGetter(SoundEvent::fixedRange)).apply((Applicative<SoundEvent, ?>)instance, SoundEvent::create));
    public static final Codec<Holder<SoundEvent>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, DIRECT_CODEC);
    private static final float DEFAULT_RANGE = 16.0f;
    private final ResourceLocation location;
    private final float range;
    private final boolean newSystem;

    private static SoundEvent create(ResourceLocation resourceLocation, Optional<Float> optional) {
        return optional.map(float_ -> SoundEvent.createFixedRangeEvent(resourceLocation, float_.floatValue())).orElseGet(() -> SoundEvent.createVariableRangeEvent(resourceLocation));
    }

    public static SoundEvent createVariableRangeEvent(ResourceLocation resourceLocation) {
        return new SoundEvent(resourceLocation, 16.0f, false);
    }

    public static SoundEvent createFixedRangeEvent(ResourceLocation resourceLocation, float f) {
        return new SoundEvent(resourceLocation, f, true);
    }

    private SoundEvent(ResourceLocation resourceLocation, float f, boolean bl) {
        this.location = resourceLocation;
        this.range = f;
        this.newSystem = bl;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public float getRange(float f) {
        if (this.newSystem) {
            return this.range;
        }
        return f > 1.0f ? 16.0f * f : 16.0f;
    }

    private Optional<Float> fixedRange() {
        return this.newSystem ? Optional.of(Float.valueOf(this.range)) : Optional.empty();
    }

    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(this.location);
        friendlyByteBuf.writeOptional(this.fixedRange(), FriendlyByteBuf::writeFloat);
    }

    public static SoundEvent readFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
        Optional<Float> optional = friendlyByteBuf.readOptional(FriendlyByteBuf::readFloat);
        return SoundEvent.create(resourceLocation, optional);
    }
}


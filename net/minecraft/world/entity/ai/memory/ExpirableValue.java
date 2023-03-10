/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.memory;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.VisibleForDebug;

public class ExpirableValue<T> {
    private final T value;
    private long timeToLive;

    public ExpirableValue(T object, long l) {
        this.value = object;
        this.timeToLive = l;
    }

    public void tick() {
        if (this.canExpire()) {
            --this.timeToLive;
        }
    }

    public static <T> ExpirableValue<T> of(T object) {
        return new ExpirableValue<T>(object, Long.MAX_VALUE);
    }

    public static <T> ExpirableValue<T> of(T object, long l) {
        return new ExpirableValue<T>(object, l);
    }

    public long getTimeToLive() {
        return this.timeToLive;
    }

    public T getValue() {
        return this.value;
    }

    public boolean hasExpired() {
        return this.timeToLive <= 0L;
    }

    public String toString() {
        return this.value + (String)(this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
    }

    @VisibleForDebug
    public boolean canExpire() {
        return this.timeToLive != Long.MAX_VALUE;
    }

    public static <T> Codec<ExpirableValue<T>> codec(Codec<T> codec) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)codec.fieldOf("value")).forGetter(expirableValue -> expirableValue.value), Codec.LONG.optionalFieldOf("ttl").forGetter(expirableValue -> expirableValue.canExpire() ? Optional.of(expirableValue.timeToLive) : Optional.empty())).apply((Applicative<ExpirableValue, ?>)instance, (object, optional) -> new ExpirableValue<Object>(object, optional.orElse(Long.MAX_VALUE))));
    }
}


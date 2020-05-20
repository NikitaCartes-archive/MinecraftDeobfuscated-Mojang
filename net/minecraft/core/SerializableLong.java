/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.serialization.Codec;

public final class SerializableLong {
    public static final Codec<SerializableLong> CODEC = Codec.LONG.xmap(SerializableLong::new, serializableLong -> serializableLong.value);
    private final long value;

    private SerializableLong(long l) {
        this.value = l;
    }

    public long value() {
        return this.value;
    }

    public static SerializableLong of(long l) {
        return new SerializableLong(l);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        SerializableLong serializableLong = (SerializableLong)object;
        return this.value == serializableLong.value;
    }

    public int hashCode() {
        return Long.hashCode(this.value);
    }

    public String toString() {
        return Long.toString(this.value);
    }
}


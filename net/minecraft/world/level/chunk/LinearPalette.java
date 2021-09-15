/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.MissingPaletteEntryException;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;

public class LinearPalette<T>
implements Palette<T> {
    private final IdMap<T> registry;
    private final T[] values;
    private final PaletteResize<T> resizeHandler;
    private final int bits;
    private int size;

    public LinearPalette(IdMap<T> idMap, int i, PaletteResize<T> paletteResize) {
        this.registry = idMap;
        this.values = new Object[1 << i];
        this.bits = i;
        this.resizeHandler = paletteResize;
    }

    public static <A> Palette<A> create(int i, IdMap<A> idMap, PaletteResize<A> paletteResize) {
        return new LinearPalette<A>(idMap, i, paletteResize);
    }

    @Override
    public int idFor(T object) {
        int i;
        for (i = 0; i < this.size; ++i) {
            if (this.values[i] != object) continue;
            return i;
        }
        if ((i = this.size++) < this.values.length) {
            this.values[i] = object;
            return i;
        }
        return this.resizeHandler.onResize(this.bits + 1, object);
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        for (int i = 0; i < this.size; ++i) {
            if (!predicate.test(this.values[i])) continue;
            return true;
        }
        return false;
    }

    @Override
    public T valueFor(int i) {
        if (i >= 0 && i < this.size) {
            return this.values[i];
        }
        throw new MissingPaletteEntryException(i);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.size = friendlyByteBuf.readVarInt();
        for (int i = 0; i < this.size; ++i) {
            this.values[i] = this.registry.byId(friendlyByteBuf.readVarInt());
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.size);
        for (int i = 0; i < this.size; ++i) {
            friendlyByteBuf.writeVarInt(this.registry.getId(this.values[i]));
        }
    }

    @Override
    public int getSerializedSize() {
        int i = FriendlyByteBuf.getVarIntSize(this.getSize());
        for (int j = 0; j < this.getSize(); ++j) {
            i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values[j]));
        }
        return i;
    }

    @Override
    public int getSize() {
        return this.size;
    }
}


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

public class GlobalPalette<T>
implements Palette<T> {
    private final IdMap<T> registry;

    public GlobalPalette(IdMap<T> idMap) {
        this.registry = idMap;
    }

    public static <A> Palette<A> create(int i, IdMap<A> idMap, PaletteResize<A> paletteResize) {
        return new GlobalPalette<A>(idMap);
    }

    @Override
    public int idFor(T object) {
        int i = this.registry.getId(object);
        return i == -1 ? 0 : i;
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        return true;
    }

    @Override
    public T valueFor(int i) {
        T object = this.registry.byId(i);
        if (object == null) {
            throw new MissingPaletteEntryException(i);
        }
        return object;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public int getSerializedSize() {
        return FriendlyByteBuf.getVarIntSize(0);
    }

    @Override
    public int getSize() {
        return this.registry.size();
    }
}


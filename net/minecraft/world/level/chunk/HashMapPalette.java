/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.level.chunk.MissingPaletteEntryException;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;

public class HashMapPalette<T>
implements Palette<T> {
    private final IdMap<T> registry;
    private final CrudeIncrementalIntIdentityHashBiMap<T> values;
    private final PaletteResize<T> resizeHandler;
    private final int bits;

    public HashMapPalette(IdMap<T> idMap, int i, PaletteResize<T> paletteResize, List<T> list) {
        this(idMap, i, paletteResize);
        list.forEach(this.values::add);
    }

    public HashMapPalette(IdMap<T> idMap, int i, PaletteResize<T> paletteResize) {
        this(idMap, i, paletteResize, CrudeIncrementalIntIdentityHashBiMap.create(1 << i));
    }

    private HashMapPalette(IdMap<T> idMap, int i, PaletteResize<T> paletteResize, CrudeIncrementalIntIdentityHashBiMap<T> crudeIncrementalIntIdentityHashBiMap) {
        this.registry = idMap;
        this.bits = i;
        this.resizeHandler = paletteResize;
        this.values = crudeIncrementalIntIdentityHashBiMap;
    }

    public static <A> Palette<A> create(int i, IdMap<A> idMap, PaletteResize<A> paletteResize, List<A> list) {
        return new HashMapPalette<A>(idMap, i, paletteResize, list);
    }

    @Override
    public int idFor(T object) {
        int i = this.values.getId(object);
        if (i == -1 && (i = this.values.add(object)) >= 1 << this.bits) {
            i = this.resizeHandler.onResize(this.bits + 1, object);
        }
        return i;
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        for (int i = 0; i < this.getSize(); ++i) {
            if (!predicate.test(this.values.byId(i))) continue;
            return true;
        }
        return false;
    }

    @Override
    public T valueFor(int i) {
        T object = this.values.byId(i);
        if (object == null) {
            throw new MissingPaletteEntryException(i);
        }
        return object;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.values.clear();
        int i = friendlyByteBuf.readVarInt();
        for (int j = 0; j < i; ++j) {
            this.values.add(this.registry.byIdOrThrow(friendlyByteBuf.readVarInt()));
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        int i = this.getSize();
        friendlyByteBuf.writeVarInt(i);
        for (int j = 0; j < i; ++j) {
            friendlyByteBuf.writeVarInt(this.registry.getId(this.values.byId(j)));
        }
    }

    @Override
    public int getSerializedSize() {
        int i = FriendlyByteBuf.getVarIntSize(this.getSize());
        for (int j = 0; j < this.getSize(); ++j) {
            i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values.byId(j)));
        }
        return i;
    }

    public List<T> getEntries() {
        ArrayList arrayList = new ArrayList();
        this.values.iterator().forEachRemaining(arrayList::add);
        return arrayList;
    }

    @Override
    public int getSize() {
        return this.values.size();
    }

    @Override
    public Palette<T> copy() {
        return new HashMapPalette<T>(this.registry, this.bits, this.resizeHandler, this.values.copy());
    }
}


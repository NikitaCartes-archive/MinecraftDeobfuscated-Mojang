/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.PalettedContainer;

public interface PalettedContainerRO<T> {
    public T get(int var1, int var2, int var3);

    public void getAll(Consumer<T> var1);

    public void write(FriendlyByteBuf var1);

    public int getSerializedSize();

    public boolean maybeHas(Predicate<T> var1);

    public void count(PalettedContainer.CountConsumer<T> var1);

    public PalettedContainer<T> recreate();

    public PackedData<T> pack(IdMap<T> var1, PalettedContainer.Strategy var2);

    public static interface Unpacker<T, C extends PalettedContainerRO<T>> {
        public DataResult<C> read(IdMap<T> var1, PalettedContainer.Strategy var2, PackedData<T> var3);
    }

    public record PackedData<T>(List<T> paletteEntries, Optional<LongStream> storage) {
    }
}


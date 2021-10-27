/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.BitSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.LongStream;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

public final class BelowZeroRetrogen {
    private static final BitSet EMPTY = new BitSet(0);
    private static final Codec<BitSet> BITSET_CODEC = Codec.LONG_STREAM.xmap(longStream -> BitSet.valueOf(longStream.toArray()), bitSet -> LongStream.of(bitSet.toLongArray()));
    private static final Codec<ChunkStatus> NON_EMPTY_CHUNK_STATUS = Registry.CHUNK_STATUS.comapFlatMap(chunkStatus -> chunkStatus == ChunkStatus.EMPTY ? DataResult.error("target_status cannot be empty") : DataResult.success(chunkStatus), Function.identity());
    public static final Codec<BelowZeroRetrogen> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)NON_EMPTY_CHUNK_STATUS.fieldOf("target_status")).forGetter(BelowZeroRetrogen::targetStatus), BITSET_CODEC.optionalFieldOf("missing_bedrock").forGetter(belowZeroRetrogen -> belowZeroRetrogen.missingBedrock.isEmpty() ? Optional.empty() : Optional.of(belowZeroRetrogen.missingBedrock))).apply((Applicative<BelowZeroRetrogen, ?>)instance, BelowZeroRetrogen::new));
    private final ChunkStatus targetStatus;
    private final BitSet missingBedrock;

    private BelowZeroRetrogen(ChunkStatus chunkStatus, Optional<BitSet> optional) {
        this.targetStatus = chunkStatus;
        this.missingBedrock = optional.orElse(EMPTY);
    }

    @Nullable
    public static BelowZeroRetrogen read(CompoundTag compoundTag) {
        ChunkStatus chunkStatus = ChunkStatus.byName(compoundTag.getString("target_status"));
        if (chunkStatus == ChunkStatus.EMPTY) {
            return null;
        }
        return new BelowZeroRetrogen(chunkStatus, Optional.of(BitSet.valueOf(compoundTag.getLongArray("missing_bedrock"))));
    }

    public ChunkStatus targetStatus() {
        return this.targetStatus;
    }

    public boolean hasBedrockAt(int i, int j) {
        return !this.missingBedrock.get((j & 0xF) * 16 + (i & 0xF));
    }
}


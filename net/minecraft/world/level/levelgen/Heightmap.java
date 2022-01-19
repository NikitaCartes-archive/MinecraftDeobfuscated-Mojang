/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Heightmap {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Predicate<BlockState> NOT_AIR = blockState -> !blockState.isAir();
    static final Predicate<BlockState> MATERIAL_MOTION_BLOCKING = blockState -> blockState.getMaterial().blocksMotion();
    private final BitStorage data;
    private final Predicate<BlockState> isOpaque;
    private final ChunkAccess chunk;

    public Heightmap(ChunkAccess chunkAccess, Types types) {
        this.isOpaque = types.isOpaque();
        this.chunk = chunkAccess;
        int i = Mth.ceillog2(chunkAccess.getHeight() + 1);
        this.data = new SimpleBitStorage(i, 256);
    }

    public static void primeHeightmaps(ChunkAccess chunkAccess, Set<Types> set) {
        int i = set.size();
        ObjectArrayList objectList = new ObjectArrayList(i);
        Iterator objectListIterator = objectList.iterator();
        int j = chunkAccess.getHighestSectionPosition() + 16;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int k = 0; k < 16; ++k) {
            block1: for (int l = 0; l < 16; ++l) {
                for (Types types : set) {
                    objectList.add(chunkAccess.getOrCreateHeightmapUnprimed(types));
                }
                for (int m = j - 1; m >= chunkAccess.getMinBuildHeight(); --m) {
                    mutableBlockPos.set(k, m, l);
                    BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
                    if (blockState.is(Blocks.AIR)) continue;
                    while (objectListIterator.hasNext()) {
                        Heightmap heightmap = (Heightmap)objectListIterator.next();
                        if (!heightmap.isOpaque.test(blockState)) continue;
                        heightmap.setHeight(k, l, m + 1);
                        objectListIterator.remove();
                    }
                    if (objectList.isEmpty()) continue block1;
                    objectListIterator.back(i);
                }
            }
        }
    }

    public boolean update(int i, int j, int k, BlockState blockState) {
        int l = this.getFirstAvailable(i, k);
        if (j <= l - 2) {
            return false;
        }
        if (this.isOpaque.test(blockState)) {
            if (j >= l) {
                this.setHeight(i, k, j + 1);
                return true;
            }
        } else if (l - 1 == j) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int m = j - 1; m >= this.chunk.getMinBuildHeight(); --m) {
                mutableBlockPos.set(i, m, k);
                if (!this.isOpaque.test(this.chunk.getBlockState(mutableBlockPos))) continue;
                this.setHeight(i, k, m + 1);
                return true;
            }
            this.setHeight(i, k, this.chunk.getMinBuildHeight());
            return true;
        }
        return false;
    }

    public int getFirstAvailable(int i, int j) {
        return this.getFirstAvailable(Heightmap.getIndex(i, j));
    }

    public int getHighestTaken(int i, int j) {
        return this.getFirstAvailable(Heightmap.getIndex(i, j)) - 1;
    }

    private int getFirstAvailable(int i) {
        return this.data.get(i) + this.chunk.getMinBuildHeight();
    }

    private void setHeight(int i, int j, int k) {
        this.data.set(Heightmap.getIndex(i, j), k - this.chunk.getMinBuildHeight());
    }

    public void setRawData(ChunkAccess chunkAccess, Types types, long[] ls) {
        long[] ms = this.data.getRaw();
        if (ms.length == ls.length) {
            System.arraycopy(ls, 0, ms, 0, ls.length);
            return;
        }
        LOGGER.warn("Ignoring heightmap data for chunk " + chunkAccess.getPos() + ", size does not match; expected: " + ms.length + ", got: " + ls.length);
        Heightmap.primeHeightmaps(chunkAccess, EnumSet.of(types));
    }

    public long[] getRawData() {
        return this.data.getRaw();
    }

    private static int getIndex(int i, int j) {
        return i + j * 16;
    }

    public static enum Types implements StringRepresentable
    {
        WORLD_SURFACE_WG("WORLD_SURFACE_WG", Usage.WORLDGEN, NOT_AIR),
        WORLD_SURFACE("WORLD_SURFACE", Usage.CLIENT, NOT_AIR),
        OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Usage.WORLDGEN, MATERIAL_MOTION_BLOCKING),
        OCEAN_FLOOR("OCEAN_FLOOR", Usage.LIVE_WORLD, MATERIAL_MOTION_BLOCKING),
        MOTION_BLOCKING("MOTION_BLOCKING", Usage.CLIENT, blockState -> blockState.getMaterial().blocksMotion() || !blockState.getFluidState().isEmpty()),
        MOTION_BLOCKING_NO_LEAVES("MOTION_BLOCKING_NO_LEAVES", Usage.LIVE_WORLD, blockState -> (blockState.getMaterial().blocksMotion() || !blockState.getFluidState().isEmpty()) && !(blockState.getBlock() instanceof LeavesBlock));

        public static final Codec<Types> CODEC;
        private final String serializationKey;
        private final Usage usage;
        private final Predicate<BlockState> isOpaque;
        private static final Map<String, Types> REVERSE_LOOKUP;

        private Types(String string2, Usage usage, Predicate<BlockState> predicate) {
            this.serializationKey = string2;
            this.usage = usage;
            this.isOpaque = predicate;
        }

        public String getSerializationKey() {
            return this.serializationKey;
        }

        public boolean sendToClient() {
            return this.usage == Usage.CLIENT;
        }

        public boolean keepAfterWorldgen() {
            return this.usage != Usage.WORLDGEN;
        }

        @Nullable
        public static Types getFromKey(String string) {
            return REVERSE_LOOKUP.get(string);
        }

        public Predicate<BlockState> isOpaque() {
            return this.isOpaque;
        }

        @Override
        public String getSerializedName() {
            return this.serializationKey;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Types::values, Types::getFromKey);
            REVERSE_LOOKUP = Util.make(Maps.newHashMap(), hashMap -> {
                for (Types types : Types.values()) {
                    hashMap.put(types.serializationKey, types);
                }
            });
        }
    }

    public static enum Usage {
        WORLDGEN,
        LIVE_WORLD,
        CLIENT;

    }
}


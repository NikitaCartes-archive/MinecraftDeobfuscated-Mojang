/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class SectionPos
extends Vec3i {
    private SectionPos(int i, int j, int k) {
        super(i, j, k);
    }

    public static SectionPos of(int i, int j, int k) {
        return new SectionPos(i, j, k);
    }

    public static SectionPos of(BlockPos blockPos) {
        return new SectionPos(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getY()), SectionPos.blockToSectionCoord(blockPos.getZ()));
    }

    public static SectionPos of(ChunkPos chunkPos, int i) {
        return new SectionPos(chunkPos.x, i, chunkPos.z);
    }

    public static SectionPos of(Entity entity) {
        return new SectionPos(SectionPos.blockToSectionCoord(entity.getBlockX()), SectionPos.blockToSectionCoord(entity.getBlockY()), SectionPos.blockToSectionCoord(entity.getBlockZ()));
    }

    public static SectionPos of(long l) {
        return new SectionPos(SectionPos.x(l), SectionPos.y(l), SectionPos.z(l));
    }

    public static long offset(long l, Direction direction) {
        return SectionPos.offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public static long offset(long l, int i, int j, int k) {
        return SectionPos.asLong(SectionPos.x(l) + i, SectionPos.y(l) + j, SectionPos.z(l) + k);
    }

    public static int posToSectionCoord(double d) {
        return SectionPos.blockToSectionCoord(Mth.floor(d));
    }

    public static int blockToSectionCoord(int i) {
        return i >> 4;
    }

    public static int sectionRelative(int i) {
        return i & 0xF;
    }

    public static short sectionRelativePos(BlockPos blockPos) {
        int i = SectionPos.sectionRelative(blockPos.getX());
        int j = SectionPos.sectionRelative(blockPos.getY());
        int k = SectionPos.sectionRelative(blockPos.getZ());
        return (short)(i << 8 | k << 4 | j << 0);
    }

    public static int sectionRelativeX(short s) {
        return s >>> 8 & 0xF;
    }

    public static int sectionRelativeY(short s) {
        return s >>> 0 & 0xF;
    }

    public static int sectionRelativeZ(short s) {
        return s >>> 4 & 0xF;
    }

    public int relativeToBlockX(short s) {
        return this.minBlockX() + SectionPos.sectionRelativeX(s);
    }

    public int relativeToBlockY(short s) {
        return this.minBlockY() + SectionPos.sectionRelativeY(s);
    }

    public int relativeToBlockZ(short s) {
        return this.minBlockZ() + SectionPos.sectionRelativeZ(s);
    }

    public BlockPos relativeToBlockPos(short s) {
        return new BlockPos(this.relativeToBlockX(s), this.relativeToBlockY(s), this.relativeToBlockZ(s));
    }

    public static int sectionToBlockCoord(int i) {
        return i << 4;
    }

    public static int sectionToBlockCoord(int i, int j) {
        return SectionPos.sectionToBlockCoord(i) + j;
    }

    public static int x(long l) {
        return (int)(l << 0 >> 42);
    }

    public static int y(long l) {
        return (int)(l << 44 >> 44);
    }

    public static int z(long l) {
        return (int)(l << 22 >> 42);
    }

    public int x() {
        return this.getX();
    }

    public int y() {
        return this.getY();
    }

    public int z() {
        return this.getZ();
    }

    public int minBlockX() {
        return SectionPos.sectionToBlockCoord(this.x());
    }

    public int minBlockY() {
        return SectionPos.sectionToBlockCoord(this.y());
    }

    public int minBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z());
    }

    public int maxBlockX() {
        return SectionPos.sectionToBlockCoord(this.x(), 15);
    }

    public int maxBlockY() {
        return SectionPos.sectionToBlockCoord(this.y(), 15);
    }

    public int maxBlockZ() {
        return SectionPos.sectionToBlockCoord(this.z(), 15);
    }

    public static long blockToSection(long l) {
        return SectionPos.asLong(SectionPos.blockToSectionCoord(BlockPos.getX(l)), SectionPos.blockToSectionCoord(BlockPos.getY(l)), SectionPos.blockToSectionCoord(BlockPos.getZ(l)));
    }

    public static long getZeroNode(long l) {
        return l & 0xFFFFFFFFFFF00000L;
    }

    public BlockPos origin() {
        return new BlockPos(SectionPos.sectionToBlockCoord(this.x()), SectionPos.sectionToBlockCoord(this.y()), SectionPos.sectionToBlockCoord(this.z()));
    }

    public BlockPos center() {
        int i = 8;
        return this.origin().offset(8, 8, 8);
    }

    public ChunkPos chunk() {
        return new ChunkPos(this.x(), this.z());
    }

    public static long asLong(int i, int j, int k) {
        long l = 0L;
        l |= ((long)i & 0x3FFFFFL) << 42;
        l |= ((long)j & 0xFFFFFL) << 0;
        return l |= ((long)k & 0x3FFFFFL) << 20;
    }

    public long asLong() {
        return SectionPos.asLong(this.x(), this.y(), this.z());
    }

    public Stream<BlockPos> blocksInside() {
        return BlockPos.betweenClosedStream(this.minBlockX(), this.minBlockY(), this.minBlockZ(), this.maxBlockX(), this.maxBlockY(), this.maxBlockZ());
    }

    public static Stream<SectionPos> cube(SectionPos sectionPos, int i) {
        int j = sectionPos.x();
        int k = sectionPos.y();
        int l = sectionPos.z();
        return SectionPos.betweenClosedStream(j - i, k - i, l - i, j + i, k + i, l + i);
    }

    public static Stream<SectionPos> aroundChunk(ChunkPos chunkPos, int i, int j, int k) {
        int l = chunkPos.x;
        int m = chunkPos.z;
        return SectionPos.betweenClosedStream(l - i, j, m - i, l + i, k - 1, m + i);
    }

    public static Stream<SectionPos> betweenClosedStream(final int i, final int j, final int k, final int l, final int m, final int n) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<SectionPos>((long)((l - i + 1) * (m - j + 1) * (n - k + 1)), 64){
            final Cursor3D cursor;
            {
                super(l2, i2);
                this.cursor = new Cursor3D(i, j, k, l, m, n);
            }

            @Override
            public boolean tryAdvance(Consumer<? super SectionPos> consumer) {
                if (this.cursor.advance()) {
                    consumer.accept(new SectionPos(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
                    return true;
                }
                return false;
            }
        }, false);
    }
}


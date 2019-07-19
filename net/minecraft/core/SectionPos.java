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
        return new SectionPos(SectionPos.blockToSectionCoord(Mth.floor(entity.x)), SectionPos.blockToSectionCoord(Mth.floor(entity.y)), SectionPos.blockToSectionCoord(Mth.floor(entity.z)));
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
        return (short)(i << 8 | k << 4 | j);
    }

    public static int sectionToBlockCoord(int i) {
        return i << 4;
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
        return this.x() << 4;
    }

    public int minBlockY() {
        return this.y() << 4;
    }

    public int minBlockZ() {
        return this.z() << 4;
    }

    public int maxBlockX() {
        return (this.x() << 4) + 15;
    }

    public int maxBlockY() {
        return (this.y() << 4) + 15;
    }

    public int maxBlockZ() {
        return (this.z() << 4) + 15;
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


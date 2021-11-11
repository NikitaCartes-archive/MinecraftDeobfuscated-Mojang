/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public class InsideWorldBoundsPredicate
implements BlockPredicate {
    public static final Codec<InsideWorldBoundsPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", BlockPos.ZERO).forGetter(insideWorldBoundsPredicate -> insideWorldBoundsPredicate.offset)).apply((Applicative<InsideWorldBoundsPredicate, ?>)instance, InsideWorldBoundsPredicate::new));
    private final Vec3i offset;

    public InsideWorldBoundsPredicate(Vec3i vec3i) {
        this.offset = vec3i;
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        return !worldGenLevel.isOutsideBuildHeight(blockPos.offset(this.offset));
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.INSIDE_WORLD_BOUNDS;
    }

    @Override
    public /* synthetic */ boolean test(Object object, Object object2) {
        return this.test((WorldGenLevel)object, (BlockPos)object2);
    }
}


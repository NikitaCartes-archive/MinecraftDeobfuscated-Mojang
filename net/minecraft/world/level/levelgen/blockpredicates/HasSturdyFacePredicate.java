/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public class HasSturdyFacePredicate
implements BlockPredicate {
    private final Vec3i offset;
    private final Direction direction;
    public static final Codec<HasSturdyFacePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(hasSturdyFacePredicate -> hasSturdyFacePredicate.offset), ((MapCodec)Direction.CODEC.fieldOf("direction")).forGetter(hasSturdyFacePredicate -> hasSturdyFacePredicate.direction)).apply((Applicative<HasSturdyFacePredicate, ?>)instance, HasSturdyFacePredicate::new));

    public HasSturdyFacePredicate(Vec3i vec3i, Direction direction) {
        this.offset = vec3i;
        this.direction = direction;
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.offset(this.offset);
        return worldGenLevel.getBlockState(blockPos2).isFaceSturdy(worldGenLevel, blockPos2, this.direction);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.HAS_STURDY_FACE;
    }

    @Override
    public /* synthetic */ boolean test(Object object, Object object2) {
        return this.test((WorldGenLevel)object, (BlockPos)object2);
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public class WouldSurvivePredicate
implements BlockPredicate {
    public static final Codec<WouldSurvivePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter(wouldSurvivePredicate -> wouldSurvivePredicate.offset), ((MapCodec)BlockState.CODEC.fieldOf("state")).forGetter(wouldSurvivePredicate -> wouldSurvivePredicate.state)).apply((Applicative<WouldSurvivePredicate, ?>)instance, WouldSurvivePredicate::new));
    private final Vec3i offset;
    private final BlockState state;

    protected WouldSurvivePredicate(Vec3i vec3i, BlockState blockState) {
        this.offset = vec3i;
        this.state = blockState;
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        return this.state.canSurvive(worldGenLevel, blockPos.offset(this.offset));
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.WOULD_SURVIVE;
    }

    @Override
    public /* synthetic */ boolean test(Object object, Object object2) {
        return this.test((WorldGenLevel)object, (BlockPos)object2);
    }
}


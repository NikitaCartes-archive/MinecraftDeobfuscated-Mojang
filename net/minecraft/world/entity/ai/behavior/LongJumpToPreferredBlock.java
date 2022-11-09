/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.LongJumpToRandomPos;
import net.minecraft.world.level.block.Block;

public class LongJumpToPreferredBlock<E extends Mob>
extends LongJumpToRandomPos<E> {
    private final TagKey<Block> preferredBlockTag;
    private final float preferredBlocksChance;
    private final List<LongJumpToRandomPos.PossibleJump> notPrefferedJumpCandidates = new ArrayList<LongJumpToRandomPos.PossibleJump>();
    private boolean currentlyWantingPreferredOnes;

    public LongJumpToPreferredBlock(UniformInt uniformInt, int i, int j, float f, Function<E, SoundEvent> function, TagKey<Block> tagKey, float g, BiPredicate<E, BlockPos> biPredicate) {
        super(uniformInt, i, j, f, function, biPredicate);
        this.preferredBlockTag = tagKey;
        this.preferredBlocksChance = g;
    }

    @Override
    protected void start(ServerLevel serverLevel, E mob, long l) {
        super.start(serverLevel, mob, l);
        this.notPrefferedJumpCandidates.clear();
        this.currentlyWantingPreferredOnes = ((LivingEntity)mob).getRandom().nextFloat() < this.preferredBlocksChance;
    }

    @Override
    protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel serverLevel) {
        if (!this.currentlyWantingPreferredOnes) {
            return super.getJumpCandidate(serverLevel);
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        while (!this.jumpCandidates.isEmpty()) {
            Optional<LongJumpToRandomPos.PossibleJump> optional = super.getJumpCandidate(serverLevel);
            if (!optional.isPresent()) continue;
            LongJumpToRandomPos.PossibleJump possibleJump = optional.get();
            if (serverLevel.getBlockState(mutableBlockPos.setWithOffset((Vec3i)possibleJump.getJumpTarget(), Direction.DOWN)).is(this.preferredBlockTag)) {
                return optional;
            }
            this.notPrefferedJumpCandidates.add(possibleJump);
        }
        if (!this.notPrefferedJumpCandidates.isEmpty()) {
            return Optional.of(this.notPrefferedJumpCandidates.remove(0));
        }
        return Optional.empty();
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Mob)livingEntity), l);
    }
}


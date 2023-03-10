/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LongJumpToRandomPos<E extends Mob>
extends Behavior<E> {
    protected static final int FIND_JUMP_TRIES = 20;
    private static final int PREPARE_JUMP_DURATION = 40;
    protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
    private static final int TIME_OUT_DURATION = 200;
    private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(65, 70, 75, 80);
    private final UniformInt timeBetweenLongJumps;
    protected final int maxLongJumpHeight;
    protected final int maxLongJumpWidth;
    protected final float maxJumpVelocity;
    protected List<PossibleJump> jumpCandidates = Lists.newArrayList();
    protected Optional<Vec3> initialPosition = Optional.empty();
    @Nullable
    protected Vec3 chosenJump;
    protected int findJumpTries;
    protected long prepareJumpStart;
    private final Function<E, SoundEvent> getJumpSound;
    private final BiPredicate<E, BlockPos> acceptableLandingSpot;

    public LongJumpToRandomPos(UniformInt uniformInt, int i, int j, float f, Function<E, SoundEvent> function) {
        this(uniformInt, i, j, f, function, LongJumpToRandomPos::defaultAcceptableLandingSpot);
    }

    public static <E extends Mob> boolean defaultAcceptableLandingSpot(E mob, BlockPos blockPos) {
        Level level = mob.level;
        BlockPos blockPos2 = blockPos.below();
        return level.getBlockState(blockPos2).isSolidRender(level, blockPos2) && mob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(level, blockPos.mutable())) == 0.0f;
    }

    public LongJumpToRandomPos(UniformInt uniformInt, int i, int j, float f, Function<E, SoundEvent> function, BiPredicate<E, BlockPos> biPredicate) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), 200);
        this.timeBetweenLongJumps = uniformInt;
        this.maxLongJumpHeight = i;
        this.maxLongJumpWidth = j;
        this.maxJumpVelocity = f;
        this.getJumpSound = function;
        this.acceptableLandingSpot = biPredicate;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
        boolean bl;
        boolean bl2 = bl = mob.isOnGround() && !mob.isInWater() && !mob.isInLava() && !serverLevel.getBlockState(mob.blockPosition()).is(Blocks.HONEY_BLOCK);
        if (!bl) {
            mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverLevel.random) / 2);
        }
        return bl;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        boolean bl;
        boolean bl2 = bl = this.initialPosition.isPresent() && this.initialPosition.get().equals(mob.position()) && this.findJumpTries > 0 && !mob.isInWaterOrBubble() && (this.chosenJump != null || !this.jumpCandidates.isEmpty());
        if (!bl && mob.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
            mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverLevel.random) / 2);
            mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        }
        return bl;
    }

    @Override
    protected void start(ServerLevel serverLevel, E mob, long l) {
        this.chosenJump = null;
        this.findJumpTries = 20;
        this.initialPosition = Optional.of(((Entity)mob).position());
        BlockPos blockPos = ((Entity)mob).blockPosition();
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        this.jumpCandidates = BlockPos.betweenClosedStream(i - this.maxLongJumpWidth, j - this.maxLongJumpHeight, k - this.maxLongJumpWidth, i + this.maxLongJumpWidth, j + this.maxLongJumpHeight, k + this.maxLongJumpWidth).filter(blockPos2 -> !blockPos2.equals(blockPos)).map(blockPos2 -> new PossibleJump(blockPos2.immutable(), Mth.ceil(blockPos.distSqr((Vec3i)blockPos2)))).collect(Collectors.toCollection(Lists::newArrayList));
    }

    @Override
    protected void tick(ServerLevel serverLevel, E mob, long l) {
        if (this.chosenJump != null) {
            if (l - this.prepareJumpStart >= 40L) {
                ((Entity)mob).setYRot(((Mob)mob).yBodyRot);
                ((LivingEntity)mob).setDiscardFriction(true);
                double d = this.chosenJump.length();
                double e = d + ((LivingEntity)mob).getJumpBoostPower();
                ((Entity)mob).setDeltaMovement(this.chosenJump.scale(e / d));
                ((LivingEntity)mob).getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
                serverLevel.playSound(null, (Entity)mob, this.getJumpSound.apply(mob), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        } else {
            --this.findJumpTries;
            this.pickCandidate(serverLevel, mob, l);
        }
    }

    protected void pickCandidate(ServerLevel serverLevel, E mob, long l) {
        while (!this.jumpCandidates.isEmpty()) {
            Vec3 vec3;
            Vec3 vec32;
            PossibleJump possibleJump;
            BlockPos blockPos;
            Optional<PossibleJump> optional = this.getJumpCandidate(serverLevel);
            if (optional.isEmpty() || !this.isAcceptableLandingPosition(serverLevel, mob, blockPos = (possibleJump = optional.get()).getJumpTarget()) || (vec32 = this.calculateOptimalJumpVector((Mob)mob, vec3 = Vec3.atCenterOf(blockPos))) == null) continue;
            ((LivingEntity)mob).getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos));
            PathNavigation pathNavigation = ((Mob)mob).getNavigation();
            Path path = pathNavigation.createPath(blockPos, 0, 8);
            if (path != null && path.canReach()) continue;
            this.chosenJump = vec32;
            this.prepareJumpStart = l;
            return;
        }
    }

    protected Optional<PossibleJump> getJumpCandidate(ServerLevel serverLevel) {
        Optional<PossibleJump> optional = WeightedRandom.getRandomItem(serverLevel.random, this.jumpCandidates);
        optional.ifPresent(this.jumpCandidates::remove);
        return optional;
    }

    private boolean isAcceptableLandingPosition(ServerLevel serverLevel, E mob, BlockPos blockPos) {
        BlockPos blockPos2 = ((Entity)mob).blockPosition();
        int i = blockPos2.getX();
        int j = blockPos2.getZ();
        if (i == blockPos.getX() && j == blockPos.getZ()) {
            return false;
        }
        return this.acceptableLandingSpot.test(mob, blockPos);
    }

    @Nullable
    protected Vec3 calculateOptimalJumpVector(Mob mob, Vec3 vec3) {
        ArrayList<Integer> list = Lists.newArrayList(ALLOWED_ANGLES);
        Collections.shuffle(list);
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            int i = (Integer)iterator.next();
            Vec3 vec32 = this.calculateJumpVectorForAngle(mob, vec3, i);
            if (vec32 == null) continue;
            return vec32;
        }
        return null;
    }

    @Nullable
    private Vec3 calculateJumpVectorForAngle(Mob mob, Vec3 vec3, int i) {
        Vec3 vec32 = mob.position();
        Vec3 vec33 = new Vec3(vec3.x - vec32.x, 0.0, vec3.z - vec32.z).normalize().scale(0.5);
        vec3 = vec3.subtract(vec33);
        Vec3 vec34 = vec3.subtract(vec32);
        float f = (float)i * (float)Math.PI / 180.0f;
        double d = Math.atan2(vec34.z, vec34.x);
        double e = vec34.subtract(0.0, vec34.y, 0.0).lengthSqr();
        double g = Math.sqrt(e);
        double h = vec34.y;
        double j = Math.sin(2.0f * f);
        double k = 0.08;
        double l = Math.pow(Math.cos(f), 2.0);
        double m = Math.sin(f);
        double n = Math.cos(f);
        double o = Math.sin(d);
        double p = Math.cos(d);
        double q = e * 0.08 / (g * j - 2.0 * h * l);
        if (q < 0.0) {
            return null;
        }
        double r = Math.sqrt(q);
        if (r > (double)this.maxJumpVelocity) {
            return null;
        }
        double s = r * n;
        double t = r * m;
        int u = Mth.ceil(g / s) * 2;
        double v = 0.0;
        Vec3 vec35 = null;
        EntityDimensions entityDimensions = mob.getDimensions(Pose.LONG_JUMPING);
        for (int w = 0; w < u - 1; ++w) {
            double x = m / n * (v += g / (double)u) - Math.pow(v, 2.0) * 0.08 / (2.0 * q * Math.pow(n, 2.0));
            double y = v * p;
            double z = v * o;
            Vec3 vec36 = new Vec3(vec32.x + y, vec32.y + x, vec32.z + z);
            if (vec35 != null && !this.isClearTransition(mob, entityDimensions, vec35, vec36)) {
                return null;
            }
            vec35 = vec36;
        }
        return new Vec3(s * p, t, s * o).scale(0.95f);
    }

    private boolean isClearTransition(Mob mob, EntityDimensions entityDimensions, Vec3 vec3, Vec3 vec32) {
        Vec3 vec33 = vec32.subtract(vec3);
        double d = Math.min(entityDimensions.width, entityDimensions.height);
        int i = Mth.ceil(vec33.length() / d);
        Vec3 vec34 = vec33.normalize();
        Vec3 vec35 = vec3;
        for (int j = 0; j < i; ++j) {
            Vec3 vec36 = vec35 = j == i - 1 ? vec32 : vec35.add(vec34.scale(d * (double)0.9f));
            if (mob.level.noCollision(mob, entityDimensions.makeBoundingBox(vec35))) continue;
            return false;
        }
        return true;
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Mob)livingEntity), l);
    }

    public static class PossibleJump
    extends WeightedEntry.IntrusiveBase {
        private final BlockPos jumpTarget;

        public PossibleJump(BlockPos blockPos, int i) {
            super(i);
            this.jumpTarget = blockPos;
        }

        public BlockPos getJumpTarget() {
            return this.jumpTarget;
        }
    }
}


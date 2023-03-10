/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private final boolean fire;
    private final BlockInteraction blockInteraction;
    private final RandomSource random = RandomSource.create();
    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();

    public Explosion(Level level, @Nullable Entity entity, double d, double e, double f, float g, List<BlockPos> list) {
        this(level, entity, d, e, f, g, false, BlockInteraction.DESTROY_WITH_DECAY, list);
    }

    public Explosion(Level level, @Nullable Entity entity, double d, double e, double f, float g, boolean bl, BlockInteraction blockInteraction, List<BlockPos> list) {
        this(level, entity, d, e, f, g, bl, blockInteraction);
        this.toBlow.addAll((Collection<BlockPos>)list);
    }

    public Explosion(Level level, @Nullable Entity entity, double d, double e, double f, float g, boolean bl, BlockInteraction blockInteraction) {
        this(level, entity, null, null, d, e, f, g, bl, blockInteraction);
    }

    public Explosion(Level level, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double d, double e, double f, float g, boolean bl, BlockInteraction blockInteraction) {
        this.level = level;
        this.source = entity;
        this.radius = g;
        this.x = d;
        this.y = e;
        this.z = f;
        this.fire = bl;
        this.blockInteraction = blockInteraction;
        this.damageSource = damageSource == null ? level.damageSources().explosion(this) : damageSource;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(entity) : explosionDamageCalculator;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    public static float getSeenPercent(Vec3 vec3, Entity entity) {
        AABB aABB = entity.getBoundingBox();
        double d = 1.0 / ((aABB.maxX - aABB.minX) * 2.0 + 1.0);
        double e = 1.0 / ((aABB.maxY - aABB.minY) * 2.0 + 1.0);
        double f = 1.0 / ((aABB.maxZ - aABB.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (d < 0.0 || e < 0.0 || f < 0.0) {
            return 0.0f;
        }
        int i = 0;
        int j = 0;
        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double p;
                    double o;
                    double n = Mth.lerp(k, aABB.minX, aABB.maxX);
                    Vec3 vec32 = new Vec3(n + g, o = Mth.lerp(l, aABB.minY, aABB.maxY), (p = Mth.lerp(m, aABB.minZ, aABB.maxZ)) + h);
                    if (entity.level.clip(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
                        ++i;
                    }
                    ++j;
                }
            }
        }
        return (float)i / (float)j;
    }

    public void explode() {
        int l;
        int k;
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        HashSet<BlockPos> set = Sets.newHashSet();
        int i = 16;
        for (int j = 0; j < 16; ++j) {
            for (k = 0; k < 16; ++k) {
                block2: for (l = 0; l < 16; ++l) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) continue;
                    double d = (float)j / 15.0f * 2.0f - 1.0f;
                    double e = (float)k / 15.0f * 2.0f - 1.0f;
                    double f = (float)l / 15.0f * 2.0f - 1.0f;
                    double g = Math.sqrt(d * d + e * e + f * f);
                    d /= g;
                    e /= g;
                    f /= g;
                    double m = this.x;
                    double n = this.y;
                    double o = this.z;
                    float p = 0.3f;
                    for (float h = this.radius * (0.7f + this.level.random.nextFloat() * 0.6f); h > 0.0f; h -= 0.22500001f) {
                        BlockPos blockPos = BlockPos.containing(m, n, o);
                        BlockState blockState = this.level.getBlockState(blockPos);
                        FluidState fluidState = this.level.getFluidState(blockPos);
                        if (!this.level.isInWorldBounds(blockPos)) continue block2;
                        Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockPos, blockState, fluidState);
                        if (optional.isPresent()) {
                            h -= (optional.get().floatValue() + 0.3f) * 0.3f;
                        }
                        if (h > 0.0f && this.damageCalculator.shouldBlockExplode(this, this.level, blockPos, blockState, h)) {
                            set.add(blockPos);
                        }
                        m += d * (double)0.3f;
                        n += e * (double)0.3f;
                        o += f * (double)0.3f;
                    }
                }
            }
        }
        this.toBlow.addAll((Collection<BlockPos>)set);
        float q = this.radius * 2.0f;
        k = Mth.floor(this.x - (double)q - 1.0);
        l = Mth.floor(this.x + (double)q + 1.0);
        int r = Mth.floor(this.y - (double)q - 1.0);
        int s = Mth.floor(this.y + (double)q + 1.0);
        int t = Mth.floor(this.z - (double)q - 1.0);
        int u = Mth.floor(this.z + (double)q + 1.0);
        List<Entity> list = this.level.getEntities(this.source, new AABB(k, r, t, l, s, u));
        Vec3 vec3 = new Vec3(this.x, this.y, this.z);
        for (int v = 0; v < list.size(); ++v) {
            Player player;
            double ad;
            double z;
            double y;
            double x;
            double aa;
            double w;
            Entity entity = list.get(v);
            if (entity.ignoreExplosion() || !((w = Math.sqrt(entity.distanceToSqr(vec3)) / (double)q) <= 1.0) || (aa = Math.sqrt((x = entity.getX() - this.x) * x + (y = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y) * y + (z = entity.getZ() - this.z) * z)) == 0.0) continue;
            x /= aa;
            y /= aa;
            z /= aa;
            double ab = Explosion.getSeenPercent(vec3, entity);
            double ac = (1.0 - w) * ab;
            entity.hurt(this.getDamageSource(), (int)((ac * ac + ac) / 2.0 * 7.0 * (double)q + 1.0));
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                ad = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, ac);
            } else {
                ad = ac;
            }
            Vec3 vec32 = new Vec3(x *= ad, y *= ad, z *= ad);
            entity.setDeltaMovement(entity.getDeltaMovement().add(vec32));
            if (!(entity instanceof Player) || (player = (Player)entity).isSpectator() || player.isCreative() && player.getAbilities().flying) continue;
            this.hitPlayers.put(player, vec32);
        }
    }

    public void finalizeExplosion(boolean bl) {
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0f, (1.0f + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2f) * 0.7f, false);
        }
        boolean bl2 = this.interactsWithBlocks();
        if (bl) {
            if (this.radius < 2.0f || !bl2) {
                this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            } else {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            }
        }
        if (bl2) {
            ObjectArrayList objectArrayList = new ObjectArrayList();
            boolean bl3 = this.getIndirectSourceEntity() instanceof Player;
            Util.shuffle(this.toBlow, this.level.random);
            for (BlockPos blockPos : this.toBlow) {
                Level level;
                BlockState blockState = this.level.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (blockState.isAir()) continue;
                BlockPos blockPos2 = blockPos.immutable();
                this.level.getProfiler().push("explosion_blocks");
                if (block.dropFromExplosion(this) && (level = this.level) instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)level;
                    BlockEntity blockEntity = blockState.hasBlockEntity() ? this.level.getBlockEntity(blockPos) : null;
                    LootContext.Builder builder = new LootContext.Builder(serverLevel).withRandom(this.level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                    if (this.blockInteraction == BlockInteraction.DESTROY_WITH_DECAY) {
                        builder.withParameter(LootContextParams.EXPLOSION_RADIUS, Float.valueOf(this.radius));
                    }
                    blockState.spawnAfterBreak(serverLevel, blockPos, ItemStack.EMPTY, bl3);
                    blockState.getDrops(builder).forEach(itemStack -> Explosion.addBlockDrops(objectArrayList, itemStack, blockPos2));
                }
                this.level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                block.wasExploded(this.level, blockPos, this);
                this.level.getProfiler().pop();
            }
            for (Pair pair : objectArrayList) {
                Block.popResource(this.level, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
            }
        }
        if (this.fire) {
            for (BlockPos blockPos3 : this.toBlow) {
                if (this.random.nextInt(3) != 0 || !this.level.getBlockState(blockPos3).isAir() || !this.level.getBlockState(blockPos3.below()).isSolidRender(this.level, blockPos3.below())) continue;
                this.level.setBlockAndUpdate(blockPos3, BaseFireBlock.getState(this.level, blockPos3));
            }
        }
    }

    public boolean interactsWithBlocks() {
        return this.blockInteraction != BlockInteraction.KEEP;
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList, ItemStack itemStack, BlockPos blockPos) {
        int i = objectArrayList.size();
        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = objectArrayList.get(j);
            ItemStack itemStack2 = pair.getFirst();
            if (!ItemEntity.areMergable(itemStack2, itemStack)) continue;
            ItemStack itemStack3 = ItemEntity.merge(itemStack2, itemStack, 16);
            objectArrayList.set(j, Pair.of(itemStack3, pair.getSecond()));
            if (!itemStack.isEmpty()) continue;
            return;
        }
        objectArrayList.add(Pair.of(itemStack, blockPos));
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    @Nullable
    public LivingEntity getIndirectSourceEntity() {
        Projectile projectile;
        Entity entity;
        if (this.source == null) {
            return null;
        }
        Entity entity2 = this.source;
        if (entity2 instanceof PrimedTnt) {
            PrimedTnt primedTnt = (PrimedTnt)entity2;
            return primedTnt.getOwner();
        }
        entity2 = this.source;
        if (entity2 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity2;
            return livingEntity;
        }
        entity2 = this.source;
        if (entity2 instanceof Projectile && (entity = (projectile = (Projectile)entity2).getOwner()) instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            return livingEntity2;
        }
        return null;
    }

    @Nullable
    public Entity getDirectSourceEntity() {
        return this.source;
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public static enum BlockInteraction {
        KEEP,
        DESTROY,
        DESTROY_WITH_DECAY;

    }
}


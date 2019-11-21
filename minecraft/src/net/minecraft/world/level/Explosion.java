package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Explosion {
	private final boolean fire;
	private final Explosion.BlockInteraction blockInteraction;
	private final Random random = new Random();
	private final Level level;
	private final double x;
	private final double y;
	private final double z;
	@Nullable
	private final Entity source;
	private final float radius;
	private DamageSource damageSource;
	private final List<BlockPos> toBlow = Lists.<BlockPos>newArrayList();
	private final Map<Player, Vec3> hitPlayers = Maps.<Player, Vec3>newHashMap();

	@Environment(EnvType.CLIENT)
	public Explosion(Level level, @Nullable Entity entity, double d, double e, double f, float g, List<BlockPos> list) {
		this(level, entity, d, e, f, g, false, Explosion.BlockInteraction.DESTROY, list);
	}

	@Environment(EnvType.CLIENT)
	public Explosion(
		Level level, @Nullable Entity entity, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction blockInteraction, List<BlockPos> list
	) {
		this(level, entity, d, e, f, g, bl, blockInteraction);
		this.toBlow.addAll(list);
	}

	public Explosion(Level level, @Nullable Entity entity, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction blockInteraction) {
		this.level = level;
		this.source = entity;
		this.radius = g;
		this.x = d;
		this.y = e;
		this.z = f;
		this.fire = bl;
		this.blockInteraction = blockInteraction;
		this.damageSource = DamageSource.explosion(this);
	}

	public static float getSeenPercent(Vec3 vec3, Entity entity) {
		AABB aABB = entity.getBoundingBox();
		double d = 1.0 / ((aABB.maxX - aABB.minX) * 2.0 + 1.0);
		double e = 1.0 / ((aABB.maxY - aABB.minY) * 2.0 + 1.0);
		double f = 1.0 / ((aABB.maxZ - aABB.minZ) * 2.0 + 1.0);
		double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
		double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
		if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
			int i = 0;
			int j = 0;

			for (float k = 0.0F; k <= 1.0F; k = (float)((double)k + d)) {
				for (float l = 0.0F; l <= 1.0F; l = (float)((double)l + e)) {
					for (float m = 0.0F; m <= 1.0F; m = (float)((double)m + f)) {
						double n = Mth.lerp((double)k, aABB.minX, aABB.maxX);
						double o = Mth.lerp((double)l, aABB.minY, aABB.maxY);
						double p = Mth.lerp((double)m, aABB.minZ, aABB.maxZ);
						Vec3 vec32 = new Vec3(n + g, o, p + h);
						if (entity.level.clip(new ClipContext(vec32, vec3, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
							i++;
						}

						j++;
					}
				}
			}

			return (float)i / (float)j;
		} else {
			return 0.0F;
		}
	}

	public void explode() {
		Set<BlockPos> set = Sets.<BlockPos>newHashSet();
		int i = 16;

		for (int j = 0; j < 16; j++) {
			for (int k = 0; k < 16; k++) {
				for (int l = 0; l < 16; l++) {
					if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
						double d = (double)((float)j / 15.0F * 2.0F - 1.0F);
						double e = (double)((float)k / 15.0F * 2.0F - 1.0F);
						double f = (double)((float)l / 15.0F * 2.0F - 1.0F);
						double g = Math.sqrt(d * d + e * e + f * f);
						d /= g;
						e /= g;
						f /= g;
						float h = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
						double m = this.x;
						double n = this.y;
						double o = this.z;

						for (float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
							BlockPos blockPos = new BlockPos(m, n, o);
							BlockState blockState = this.level.getBlockState(blockPos);
							FluidState fluidState = this.level.getFluidState(blockPos);
							if (!blockState.isAir() || !fluidState.isEmpty()) {
								float q = Math.max(blockState.getBlock().getExplosionResistance(), fluidState.getExplosionResistance());
								if (this.source != null) {
									q = this.source.getBlockExplosionResistance(this, this.level, blockPos, blockState, fluidState, q);
								}

								h -= (q + 0.3F) * 0.3F;
							}

							if (h > 0.0F && (this.source == null || this.source.shouldBlockExplode(this, this.level, blockPos, blockState, h))) {
								set.add(blockPos);
							}

							m += d * 0.3F;
							n += e * 0.3F;
							o += f * 0.3F;
						}
					}
				}
			}
		}

		this.toBlow.addAll(set);
		float r = this.radius * 2.0F;
		int k = Mth.floor(this.x - (double)r - 1.0);
		int lx = Mth.floor(this.x + (double)r + 1.0);
		int s = Mth.floor(this.y - (double)r - 1.0);
		int t = Mth.floor(this.y + (double)r + 1.0);
		int u = Mth.floor(this.z - (double)r - 1.0);
		int v = Mth.floor(this.z + (double)r + 1.0);
		List<Entity> list = this.level.getEntities(this.source, new AABB((double)k, (double)s, (double)u, (double)lx, (double)t, (double)v));
		Vec3 vec3 = new Vec3(this.x, this.y, this.z);

		for (int w = 0; w < list.size(); w++) {
			Entity entity = (Entity)list.get(w);
			if (!entity.ignoreExplosion()) {
				double x = (double)(Mth.sqrt(entity.distanceToSqr(vec3)) / r);
				if (x <= 1.0) {
					double y = entity.getX() - this.x;
					double z = entity.getEyeY() - this.y;
					double aa = entity.getZ() - this.z;
					double ab = (double)Mth.sqrt(y * y + z * z + aa * aa);
					if (ab != 0.0) {
						y /= ab;
						z /= ab;
						aa /= ab;
						double ac = (double)getSeenPercent(vec3, entity);
						double ad = (1.0 - x) * ac;
						entity.hurt(this.getDamageSource(), (float)((int)((ad * ad + ad) / 2.0 * 7.0 * (double)r + 1.0)));
						double ae = ad;
						if (entity instanceof LivingEntity) {
							ae = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity)entity, ad);
						}

						entity.setDeltaMovement(entity.getDeltaMovement().add(y * ae, z * ae, aa * ae));
						if (entity instanceof Player) {
							Player player = (Player)entity;
							if (!player.isSpectator() && (!player.isCreative() || !player.abilities.flying)) {
								this.hitPlayers.put(player, new Vec3(y * ad, z * ad, aa * ad));
							}
						}
					}
				}
			}
		}
	}

	public void finalizeExplosion(boolean bl) {
		if (this.level.isClientSide) {
			this.level
				.playLocalSound(
					this.x,
					this.y,
					this.z,
					SoundEvents.GENERIC_EXPLODE,
					SoundSource.BLOCKS,
					4.0F,
					(1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
					false
				);
		}

		boolean bl2 = this.blockInteraction != Explosion.BlockInteraction.NONE;
		if (bl) {
			if (!(this.radius < 2.0F) && bl2) {
				this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
			} else {
				this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
			}
		}

		if (bl2) {
			ObjectArrayList<ItemStack> objectArrayList = new ObjectArrayList<>();
			List<BlockPos> list = Lists.<BlockPos>newArrayList();

			for (BlockPos blockPos : this.toBlow) {
				BlockState blockState = this.level.getBlockState(blockPos);
				Block block = blockState.getBlock();
				if (!blockState.isAir()) {
					list.add(blockPos.immutable());
					this.level.getProfiler().push("explosion_blocks");
					if (block.dropFromExplosion(this) && this.level instanceof ServerLevel) {
						BlockEntity blockEntity = block.isEntityBlock() ? this.level.getBlockEntity(blockPos) : null;
						LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.level)
							.withRandom(this.level.random)
							.withParameter(LootContextParams.BLOCK_POS, blockPos)
							.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
							.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
							.withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
						if (this.blockInteraction == Explosion.BlockInteraction.DESTROY) {
							builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
						}

						blockState.getDrops(builder).forEach(itemStackx -> addBlockDrops(objectArrayList, itemStackx));
					}

					this.level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
					block.wasExploded(this.level, blockPos, this);
					this.level.getProfiler().pop();
				}
			}

			int i = list.size();

			for (ItemStack itemStack : objectArrayList) {
				Block.popResource(this.level, (BlockPos)list.get(this.random.nextInt(i)), itemStack);
			}
		}

		if (this.fire) {
			for (BlockPos blockPos2 : this.toBlow) {
				if (this.random.nextInt(3) == 0
					&& this.level.getBlockState(blockPos2).isAir()
					&& this.level.getBlockState(blockPos2.below()).isSolidRender(this.level, blockPos2.below())) {
					this.level.setBlockAndUpdate(blockPos2, Blocks.FIRE.defaultBlockState());
				}
			}
		}
	}

	private static void addBlockDrops(ObjectArrayList<ItemStack> objectArrayList, ItemStack itemStack) {
		int i = objectArrayList.size();

		for (int j = 0; j < i; j++) {
			ItemStack itemStack2 = objectArrayList.get(j);
			if (ItemEntity.areMergable(itemStack2, itemStack)) {
				ItemStack itemStack3 = ItemEntity.merge(itemStack2, itemStack, 16);
				objectArrayList.set(j, itemStack3);
				if (itemStack.isEmpty()) {
					return;
				}
			}
		}

		objectArrayList.add(itemStack);
	}

	public DamageSource getDamageSource() {
		return this.damageSource;
	}

	public void setDamageSource(DamageSource damageSource) {
		this.damageSource = damageSource;
	}

	public Map<Player, Vec3> getHitPlayers() {
		return this.hitPlayers;
	}

	@Nullable
	public LivingEntity getSourceMob() {
		if (this.source == null) {
			return null;
		} else if (this.source instanceof PrimedTnt) {
			return ((PrimedTnt)this.source).getOwner();
		} else if (this.source instanceof LivingEntity) {
			return (LivingEntity)this.source;
		} else {
			return this.source instanceof AbstractHurtingProjectile ? ((AbstractHurtingProjectile)this.source).owner : null;
		}
	}

	public void clearToBlow() {
		this.toBlow.clear();
	}

	public List<BlockPos> getToBlow() {
		return this.toBlow;
	}

	public static enum BlockInteraction {
		NONE,
		BREAK,
		DESTROY;
	}
}

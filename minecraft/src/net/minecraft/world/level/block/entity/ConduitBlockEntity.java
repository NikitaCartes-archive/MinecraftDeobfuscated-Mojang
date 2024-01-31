package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConduitBlockEntity extends BlockEntity {
	private static final int BLOCK_REFRESH_RATE = 2;
	private static final int EFFECT_DURATION = 13;
	private static final float ROTATION_SPEED = -0.0375F;
	private static final int MIN_ACTIVE_SIZE = 16;
	private static final int MIN_KILL_SIZE = 42;
	private static final int KILL_RANGE = 8;
	private static final Block[] VALID_BLOCKS = new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.SEA_LANTERN, Blocks.DARK_PRISMARINE};
	public int tickCount;
	private float activeRotation;
	private boolean isActive;
	private boolean isHunting;
	private final List<BlockPos> effectBlocks = Lists.<BlockPos>newArrayList();
	@Nullable
	private LivingEntity destroyTarget;
	@Nullable
	private UUID destroyTargetUUID;
	private long nextAmbientSoundActivation;

	public ConduitBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.CONDUIT, blockPos, blockState);
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
		if (compoundTag.hasUUID("Target")) {
			this.destroyTargetUUID = compoundTag.getUUID("Target");
		} else {
			this.destroyTargetUUID = null;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		if (this.destroyTarget != null) {
			compoundTag.putUUID("Target", this.destroyTarget.getUUID());
		}
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return this.saveWithoutMetadata(provider);
	}

	public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, ConduitBlockEntity conduitBlockEntity) {
		conduitBlockEntity.tickCount++;
		long l = level.getGameTime();
		List<BlockPos> list = conduitBlockEntity.effectBlocks;
		if (l % 40L == 0L) {
			conduitBlockEntity.isActive = updateShape(level, blockPos, list);
			updateHunting(conduitBlockEntity, list);
		}

		updateClientTarget(level, blockPos, conduitBlockEntity);
		animationTick(level, blockPos, list, conduitBlockEntity.destroyTarget, conduitBlockEntity.tickCount);
		if (conduitBlockEntity.isActive()) {
			conduitBlockEntity.activeRotation++;
		}
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, ConduitBlockEntity conduitBlockEntity) {
		conduitBlockEntity.tickCount++;
		long l = level.getGameTime();
		List<BlockPos> list = conduitBlockEntity.effectBlocks;
		if (l % 40L == 0L) {
			boolean bl = updateShape(level, blockPos, list);
			if (bl != conduitBlockEntity.isActive) {
				SoundEvent soundEvent = bl ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE;
				level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
			}

			conduitBlockEntity.isActive = bl;
			updateHunting(conduitBlockEntity, list);
			if (bl) {
				applyEffects(level, blockPos, list);
				updateDestroyTarget(level, blockPos, blockState, list, conduitBlockEntity);
			}
		}

		if (conduitBlockEntity.isActive()) {
			if (l % 80L == 0L) {
				level.playSound(null, blockPos, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
			}

			if (l > conduitBlockEntity.nextAmbientSoundActivation) {
				conduitBlockEntity.nextAmbientSoundActivation = l + 60L + (long)level.getRandom().nextInt(40);
				level.playSound(null, blockPos, SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		}
	}

	private static void updateHunting(ConduitBlockEntity conduitBlockEntity, List<BlockPos> list) {
		conduitBlockEntity.setHunting(list.size() >= 42);
	}

	private static boolean updateShape(Level level, BlockPos blockPos, List<BlockPos> list) {
		list.clear();

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					BlockPos blockPos2 = blockPos.offset(i, j, k);
					if (!level.isWaterAt(blockPos2)) {
						return false;
					}
				}
			}
		}

		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				for (int kx = -2; kx <= 2; kx++) {
					int l = Math.abs(i);
					int m = Math.abs(j);
					int n = Math.abs(kx);
					if ((l > 1 || m > 1 || n > 1) && (i == 0 && (m == 2 || n == 2) || j == 0 && (l == 2 || n == 2) || kx == 0 && (l == 2 || m == 2))) {
						BlockPos blockPos3 = blockPos.offset(i, j, kx);
						BlockState blockState = level.getBlockState(blockPos3);

						for (Block block : VALID_BLOCKS) {
							if (blockState.is(block)) {
								list.add(blockPos3);
							}
						}
					}
				}
			}
		}

		return list.size() >= 16;
	}

	private static void applyEffects(Level level, BlockPos blockPos, List<BlockPos> list) {
		int i = list.size();
		int j = i / 7 * 16;
		int k = blockPos.getX();
		int l = blockPos.getY();
		int m = blockPos.getZ();
		AABB aABB = new AABB((double)k, (double)l, (double)m, (double)(k + 1), (double)(l + 1), (double)(m + 1))
			.inflate((double)j)
			.expandTowards(0.0, (double)level.getHeight(), 0.0);
		List<Player> list2 = level.getEntitiesOfClass(Player.class, aABB);
		if (!list2.isEmpty()) {
			for (Player player : list2) {
				if (blockPos.closerThan(player.blockPosition(), (double)j) && player.isInWaterOrRain()) {
					player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
				}
			}
		}
	}

	private static void updateDestroyTarget(Level level, BlockPos blockPos, BlockState blockState, List<BlockPos> list, ConduitBlockEntity conduitBlockEntity) {
		LivingEntity livingEntity = conduitBlockEntity.destroyTarget;
		int i = list.size();
		if (i < 42) {
			conduitBlockEntity.destroyTarget = null;
		} else if (conduitBlockEntity.destroyTarget == null && conduitBlockEntity.destroyTargetUUID != null) {
			conduitBlockEntity.destroyTarget = findDestroyTarget(level, blockPos, conduitBlockEntity.destroyTargetUUID);
			conduitBlockEntity.destroyTargetUUID = null;
		} else if (conduitBlockEntity.destroyTarget == null) {
			List<LivingEntity> list2 = level.getEntitiesOfClass(
				LivingEntity.class, getDestroyRangeAABB(blockPos), livingEntityx -> livingEntityx instanceof Enemy && livingEntityx.isInWaterOrRain()
			);
			if (!list2.isEmpty()) {
				conduitBlockEntity.destroyTarget = (LivingEntity)list2.get(level.random.nextInt(list2.size()));
			}
		} else if (!conduitBlockEntity.destroyTarget.isAlive() || !blockPos.closerThan(conduitBlockEntity.destroyTarget.blockPosition(), 8.0)) {
			conduitBlockEntity.destroyTarget = null;
		}

		if (conduitBlockEntity.destroyTarget != null) {
			level.playSound(
				null,
				conduitBlockEntity.destroyTarget.getX(),
				conduitBlockEntity.destroyTarget.getY(),
				conduitBlockEntity.destroyTarget.getZ(),
				SoundEvents.CONDUIT_ATTACK_TARGET,
				SoundSource.BLOCKS,
				1.0F,
				1.0F
			);
			conduitBlockEntity.destroyTarget.hurt(level.damageSources().magic(), 4.0F);
		}

		if (livingEntity != conduitBlockEntity.destroyTarget) {
			level.sendBlockUpdated(blockPos, blockState, blockState, 2);
		}
	}

	private static void updateClientTarget(Level level, BlockPos blockPos, ConduitBlockEntity conduitBlockEntity) {
		if (conduitBlockEntity.destroyTargetUUID == null) {
			conduitBlockEntity.destroyTarget = null;
		} else if (conduitBlockEntity.destroyTarget == null || !conduitBlockEntity.destroyTarget.getUUID().equals(conduitBlockEntity.destroyTargetUUID)) {
			conduitBlockEntity.destroyTarget = findDestroyTarget(level, blockPos, conduitBlockEntity.destroyTargetUUID);
			if (conduitBlockEntity.destroyTarget == null) {
				conduitBlockEntity.destroyTargetUUID = null;
			}
		}
	}

	private static AABB getDestroyRangeAABB(BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		return new AABB((double)i, (double)j, (double)k, (double)(i + 1), (double)(j + 1), (double)(k + 1)).inflate(8.0);
	}

	@Nullable
	private static LivingEntity findDestroyTarget(Level level, BlockPos blockPos, UUID uUID) {
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, getDestroyRangeAABB(blockPos), livingEntity -> livingEntity.getUUID().equals(uUID));
		return list.size() == 1 ? (LivingEntity)list.get(0) : null;
	}

	private static void animationTick(Level level, BlockPos blockPos, List<BlockPos> list, @Nullable Entity entity, int i) {
		RandomSource randomSource = level.random;
		double d = (double)(Mth.sin((float)(i + 35) * 0.1F) / 2.0F + 0.5F);
		d = (d * d + d) * 0.3F;
		Vec3 vec3 = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.5 + d, (double)blockPos.getZ() + 0.5);

		for (BlockPos blockPos2 : list) {
			if (randomSource.nextInt(50) == 0) {
				BlockPos blockPos3 = blockPos2.subtract(blockPos);
				float f = -0.5F + randomSource.nextFloat() + (float)blockPos3.getX();
				float g = -2.0F + randomSource.nextFloat() + (float)blockPos3.getY();
				float h = -0.5F + randomSource.nextFloat() + (float)blockPos3.getZ();
				level.addParticle(ParticleTypes.NAUTILUS, vec3.x, vec3.y, vec3.z, (double)f, (double)g, (double)h);
			}
		}

		if (entity != null) {
			Vec3 vec32 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
			float j = (-0.5F + randomSource.nextFloat()) * (3.0F + entity.getBbWidth());
			float k = -1.0F + randomSource.nextFloat() * entity.getBbHeight();
			float f = (-0.5F + randomSource.nextFloat()) * (3.0F + entity.getBbWidth());
			Vec3 vec33 = new Vec3((double)j, (double)k, (double)f);
			level.addParticle(ParticleTypes.NAUTILUS, vec32.x, vec32.y, vec32.z, vec33.x, vec33.y, vec33.z);
		}
	}

	public boolean isActive() {
		return this.isActive;
	}

	public boolean isHunting() {
		return this.isHunting;
	}

	private void setHunting(boolean bl) {
		this.isHunting = bl;
	}

	public float getActiveRotation(float f) {
		return (this.activeRotation + f) * -0.0375F;
	}
}

package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ConduitBlockEntity extends BlockEntity implements TickableBlockEntity {
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

	public ConduitBlockEntity() {
		this(BlockEntityType.CONDUIT);
	}

	public ConduitBlockEntity(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("target_uuid")) {
			this.destroyTargetUUID = NbtUtils.loadUUIDTag(compoundTag.getCompound("target_uuid"));
		} else {
			this.destroyTargetUUID = null;
		}
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		if (this.destroyTarget != null) {
			compoundTag.put("target_uuid", NbtUtils.createUUIDTag(this.destroyTarget.getUUID()));
		}

		return compoundTag;
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 5, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	@Override
	public void tick() {
		this.tickCount++;
		long l = this.level.getGameTime();
		if (l % 40L == 0L) {
			this.setActive(this.updateShape());
			if (!this.level.isClientSide && this.isActive()) {
				this.applyEffects();
				this.updateDestroyTarget();
			}
		}

		if (l % 80L == 0L && this.isActive()) {
			this.playSound(SoundEvents.CONDUIT_AMBIENT);
		}

		if (l > this.nextAmbientSoundActivation && this.isActive()) {
			this.nextAmbientSoundActivation = l + 60L + (long)this.level.getRandom().nextInt(40);
			this.playSound(SoundEvents.CONDUIT_AMBIENT_SHORT);
		}

		if (this.level.isClientSide) {
			this.updateClientTarget();
			this.animationTick();
			if (this.isActive()) {
				this.activeRotation++;
			}
		}
	}

	private boolean updateShape() {
		this.effectBlocks.clear();

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					BlockPos blockPos = this.worldPosition.offset(i, j, k);
					if (!this.level.isWaterAt(blockPos)) {
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
						BlockPos blockPos2 = this.worldPosition.offset(i, j, kx);
						BlockState blockState = this.level.getBlockState(blockPos2);

						for (Block block : VALID_BLOCKS) {
							if (blockState.getBlock() == block) {
								this.effectBlocks.add(blockPos2);
							}
						}
					}
				}
			}
		}

		this.setHunting(this.effectBlocks.size() >= 42);
		return this.effectBlocks.size() >= 16;
	}

	private void applyEffects() {
		int i = this.effectBlocks.size();
		int j = i / 7 * 16;
		int k = this.worldPosition.getX();
		int l = this.worldPosition.getY();
		int m = this.worldPosition.getZ();
		AABB aABB = new AABB((double)k, (double)l, (double)m, (double)(k + 1), (double)(l + 1), (double)(m + 1))
			.inflate((double)j)
			.expandTowards(0.0, (double)this.level.getMaxBuildHeight(), 0.0);
		List<Player> list = this.level.getEntitiesOfClass(Player.class, aABB);
		if (!list.isEmpty()) {
			for (Player player : list) {
				if (this.worldPosition.closerThan(new BlockPos(player), (double)j) && player.isInWaterOrRain()) {
					player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true));
				}
			}
		}
	}

	private void updateDestroyTarget() {
		LivingEntity livingEntity = this.destroyTarget;
		int i = this.effectBlocks.size();
		if (i < 42) {
			this.destroyTarget = null;
		} else if (this.destroyTarget == null && this.destroyTargetUUID != null) {
			this.destroyTarget = this.findDestroyTarget();
			this.destroyTargetUUID = null;
		} else if (this.destroyTarget == null) {
			List<LivingEntity> list = this.level
				.getEntitiesOfClass(LivingEntity.class, this.getDestroyRangeAABB(), livingEntityx -> livingEntityx instanceof Enemy && livingEntityx.isInWaterOrRain());
			if (!list.isEmpty()) {
				this.destroyTarget = (LivingEntity)list.get(this.level.random.nextInt(list.size()));
			}
		} else if (!this.destroyTarget.isAlive() || !this.worldPosition.closerThan(new BlockPos(this.destroyTarget), 8.0)) {
			this.destroyTarget = null;
		}

		if (this.destroyTarget != null) {
			this.level
				.playSound(
					null, this.destroyTarget.getX(), this.destroyTarget.getY(), this.destroyTarget.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0F, 1.0F
				);
			this.destroyTarget.hurt(DamageSource.MAGIC, 4.0F);
		}

		if (livingEntity != this.destroyTarget) {
			BlockState blockState = this.getBlockState();
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, 2);
		}
	}

	private void updateClientTarget() {
		if (this.destroyTargetUUID == null) {
			this.destroyTarget = null;
		} else if (this.destroyTarget == null || !this.destroyTarget.getUUID().equals(this.destroyTargetUUID)) {
			this.destroyTarget = this.findDestroyTarget();
			if (this.destroyTarget == null) {
				this.destroyTargetUUID = null;
			}
		}
	}

	private AABB getDestroyRangeAABB() {
		int i = this.worldPosition.getX();
		int j = this.worldPosition.getY();
		int k = this.worldPosition.getZ();
		return new AABB((double)i, (double)j, (double)k, (double)(i + 1), (double)(j + 1), (double)(k + 1)).inflate(8.0);
	}

	@Nullable
	private LivingEntity findDestroyTarget() {
		List<LivingEntity> list = this.level
			.getEntitiesOfClass(LivingEntity.class, this.getDestroyRangeAABB(), livingEntity -> livingEntity.getUUID().equals(this.destroyTargetUUID));
		return list.size() == 1 ? (LivingEntity)list.get(0) : null;
	}

	private void animationTick() {
		Random random = this.level.random;
		double d = (double)(Mth.sin((float)(this.tickCount + 35) * 0.1F) / 2.0F + 0.5F);
		d = (d * d + d) * 0.3F;
		Vec3 vec3 = new Vec3((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 1.5 + d, (double)this.worldPosition.getZ() + 0.5);

		for (BlockPos blockPos : this.effectBlocks) {
			if (random.nextInt(50) == 0) {
				float f = -0.5F + random.nextFloat();
				float g = -2.0F + random.nextFloat();
				float h = -0.5F + random.nextFloat();
				BlockPos blockPos2 = blockPos.subtract(this.worldPosition);
				Vec3 vec32 = new Vec3((double)f, (double)g, (double)h).add((double)blockPos2.getX(), (double)blockPos2.getY(), (double)blockPos2.getZ());
				this.level.addParticle(ParticleTypes.NAUTILUS, vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
			}
		}

		if (this.destroyTarget != null) {
			Vec3 vec33 = new Vec3(this.destroyTarget.getX(), this.destroyTarget.getEyeY(), this.destroyTarget.getZ());
			float i = (-0.5F + random.nextFloat()) * (3.0F + this.destroyTarget.getBbWidth());
			float f = -1.0F + random.nextFloat() * this.destroyTarget.getBbHeight();
			float g = (-0.5F + random.nextFloat()) * (3.0F + this.destroyTarget.getBbWidth());
			Vec3 vec34 = new Vec3((double)i, (double)f, (double)g);
			this.level.addParticle(ParticleTypes.NAUTILUS, vec33.x, vec33.y, vec33.z, vec34.x, vec34.y, vec34.z);
		}
	}

	public boolean isActive() {
		return this.isActive;
	}

	@Environment(EnvType.CLIENT)
	public boolean isHunting() {
		return this.isHunting;
	}

	private void setActive(boolean bl) {
		if (bl != this.isActive) {
			this.playSound(bl ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE);
		}

		this.isActive = bl;
	}

	private void setHunting(boolean bl) {
		this.isHunting = bl;
	}

	@Environment(EnvType.CLIENT)
	public float getActiveRotation(float f) {
		return (this.activeRotation + f) * -0.0375F;
	}

	public void playSound(SoundEvent soundEvent) {
		this.level.playSound(null, this.worldPosition, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
	}
}

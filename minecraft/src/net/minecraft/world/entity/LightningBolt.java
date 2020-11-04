package net.minecraft.world.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LightningBolt extends Entity {
	private int life;
	public long seed;
	private int flashes;
	private boolean visualOnly;
	@Nullable
	private ServerPlayer cause;

	public LightningBolt(EntityType<? extends LightningBolt> entityType, Level level) {
		super(entityType, level);
		this.noCulling = true;
		this.life = 2;
		this.seed = this.random.nextLong();
		this.flashes = this.random.nextInt(3) + 1;
	}

	public void setVisualOnly(boolean bl) {
		this.visualOnly = bl;
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.WEATHER;
	}

	public void setCause(@Nullable ServerPlayer serverPlayer) {
		this.cause = serverPlayer;
	}

	private void powerLightningRod() {
		BlockPos blockPos = this.blockPosition().below();
		BlockState blockState = this.level.getBlockState(blockPos);
		if (blockState.is(Blocks.LIGHTNING_ROD)) {
			((LightningRodBlock)blockState.getBlock()).onLightningStrike(blockState, this.level, blockPos);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.life == 2) {
			Difficulty difficulty = this.level.getDifficulty();
			if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {
				this.spawnFire(4);
			}

			this.powerLightningRod();
			this.level
				.playSound(
					null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 10000.0F, 0.8F + this.random.nextFloat() * 0.2F
				);
			this.level
				.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0F, 0.5F + this.random.nextFloat() * 0.2F);
		}

		this.life--;
		if (this.life < 0) {
			if (this.flashes == 0) {
				this.discard();
			} else if (this.life < -this.random.nextInt(10)) {
				this.flashes--;
				this.life = 1;
				this.seed = this.random.nextLong();
				this.spawnFire(0);
			}
		}

		if (this.life >= 0) {
			if (!(this.level instanceof ServerLevel)) {
				this.level.setSkyFlashTime(2);
			} else if (!this.visualOnly) {
				double d = 3.0;
				List<Entity> list = this.level
					.getEntities(
						this, new AABB(this.getX() - 3.0, this.getY() - 3.0, this.getZ() - 3.0, this.getX() + 3.0, this.getY() + 6.0 + 3.0, this.getZ() + 3.0), Entity::isAlive
					);

				for (Entity entity : list) {
					entity.thunderHit((ServerLevel)this.level, this);
				}

				if (this.cause != null) {
					CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.cause, list);
				}
			}
		}
	}

	private void spawnFire(int i) {
		if (!this.visualOnly && !this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
			BlockPos blockPos = this.blockPosition();
			BlockState blockState = BaseFireBlock.getState(this.level, blockPos);
			if (this.level.getBlockState(blockPos).isAir() && blockState.canSurvive(this.level, blockPos)) {
				this.level.setBlockAndUpdate(blockPos, blockState);
			}

			for (int j = 0; j < i; j++) {
				BlockPos blockPos2 = blockPos.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
				blockState = BaseFireBlock.getState(this.level, blockPos2);
				if (this.level.getBlockState(blockPos2).isAir() && blockState.canSurvive(this.level, blockPos2)) {
					this.level.setBlockAndUpdate(blockPos2, blockState);
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = 64.0 * getViewScale();
		return d < e * e;
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}

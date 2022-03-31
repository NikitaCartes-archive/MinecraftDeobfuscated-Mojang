package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener {
	private final BlockPositionSource blockPosSource = new BlockPositionSource(this.worldPosition);
	private final SculkSpreader sculkSpreader = SculkSpreader.createLevelSpreader();

	public SculkCatalystBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SCULK_CATALYST, blockPos, blockState);
	}

	@Override
	public PositionSource getListenerSource() {
		return this.blockPosSource;
	}

	@Override
	public int getListenerRadius() {
		return 8;
	}

	@Override
	public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3) {
		if (gameEvent == GameEvent.ENTITY_DIE && entity instanceof LivingEntity livingEntity) {
			if (!livingEntity.wasExperienceConsumed()) {
				this.sculkSpreader.addCursors(new BlockPos(vec3), livingEntity.getExperienceReward());
				livingEntity.skipDropExperience();
				if (livingEntity.getLastHurtByMob() instanceof ServerPlayer serverPlayer) {
					DamageSource damageSource = livingEntity.getLastDamageSource() == null ? DamageSource.playerAttack(serverPlayer) : livingEntity.getLastDamageSource();
					CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(serverPlayer, entity, damageSource);
				}

				SculkCatalystBlock.bloom(serverLevel, this.worldPosition, this.getBlockState(), serverLevel.getRandom());
			}

			return true;
		} else {
			return false;
		}
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, SculkCatalystBlockEntity sculkCatalystBlockEntity) {
		sculkCatalystBlockEntity.sculkSpreader.updateCursors(level, blockPos, level.getRandom(), true);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.sculkSpreader.load(compoundTag);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		this.sculkSpreader.save(compoundTag);
		super.saveAdditional(compoundTag);
	}

	@VisibleForTesting
	public SculkSpreader getSculkSpreader() {
		return this.sculkSpreader;
	}
}

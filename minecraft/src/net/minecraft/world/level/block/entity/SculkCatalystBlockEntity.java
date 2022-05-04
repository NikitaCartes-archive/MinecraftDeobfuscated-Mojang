package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener {
	private final BlockPositionSource blockPosSource = new BlockPositionSource(this.worldPosition);
	private final SculkSpreader sculkSpreader = SculkSpreader.createLevelSpreader();

	public SculkCatalystBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SCULK_CATALYST, blockPos, blockState);
	}

	@Override
	public boolean handleEventsImmediately() {
		return true;
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
	public boolean handleGameEvent(ServerLevel serverLevel, GameEvent.Message message) {
		if (this.isRemoved()) {
			return false;
		} else {
			GameEvent.Context context = message.context();
			if (message.gameEvent() == GameEvent.ENTITY_DIE && context.sourceEntity() instanceof LivingEntity livingEntity) {
				if (!livingEntity.wasExperienceConsumed()) {
					int i = livingEntity.getExperienceReward();
					if (livingEntity.shouldDropExperience() && i > 0) {
						this.sculkSpreader.addCursors(new BlockPos(message.source().relative(Direction.UP, 0.5)), i);
					}

					livingEntity.skipDropExperience();
					if (livingEntity.getLastHurtByMob() instanceof ServerPlayer serverPlayer) {
						DamageSource damageSource = livingEntity.getLastDamageSource() == null ? DamageSource.playerAttack(serverPlayer) : livingEntity.getLastDamageSource();
						CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(serverPlayer, context.sourceEntity(), damageSource);
					}

					SculkCatalystBlock.bloom(serverLevel, this.worldPosition, this.getBlockState(), serverLevel.getRandom());
				}

				return true;
			} else {
				return false;
			}
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

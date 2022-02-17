package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
	private final SculkSpreader sculkSpreader = new SculkSpreader();

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
	public boolean handleGameEvent(Level level, GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3) {
		if (gameEvent == GameEvent.ENTITY_DYING && !level.isClientSide() && entity instanceof LivingEntity livingEntity) {
			if (!livingEntity.wasExperienceConsumed()) {
				this.sculkSpreader.addCursors(new BlockPos(vec3), livingEntity.getExperienceReward());
				livingEntity.skipDropExperience();
				SculkCatalystBlock.bloom((ServerLevel)level, this.worldPosition, this.getBlockState(), level.getRandom());
			}

			return true;
		} else {
			return false;
		}
	}

	public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, SculkCatalystBlockEntity sculkCatalystBlockEntity) {
		sculkCatalystBlockEntity.sculkSpreader.updateCursors(level, blockPos, level.getRandom());
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

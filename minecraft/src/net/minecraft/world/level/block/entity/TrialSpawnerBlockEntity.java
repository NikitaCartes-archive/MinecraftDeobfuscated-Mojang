package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;

public class TrialSpawnerBlockEntity extends BlockEntity implements Spawner, TrialSpawner.StateAccessor {
	private static final Logger LOGGER = LogUtils.getLogger();
	private TrialSpawner trialSpawner;

	public TrialSpawnerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.TRIAL_SPAWNER, blockPos, blockState);
		PlayerDetector playerDetector = PlayerDetector.NO_CREATIVE_PLAYERS;
		PlayerDetector.EntitySelector entitySelector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
		this.trialSpawner = new TrialSpawner(this, playerDetector, entitySelector);
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
		this.trialSpawner.codec().parse(NbtOps.INSTANCE, compoundTag).resultOrPartial(LOGGER::error).ifPresent(trialSpawner -> this.trialSpawner = trialSpawner);
		if (this.level != null) {
			this.markUpdated();
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		this.trialSpawner
			.codec()
			.encodeStart(NbtOps.INSTANCE, this.trialSpawner)
			.get()
			.ifLeft(tag -> compoundTag.merge((CompoundTag)tag))
			.ifRight(partialResult -> LOGGER.warn("Failed to encode TrialSpawner {}", partialResult.message()));
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return this.trialSpawner.getData().getUpdateTag(this.getBlockState().getValue(TrialSpawnerBlock.STATE));
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	@Override
	public void setEntityId(EntityType<?> entityType, RandomSource randomSource) {
		this.trialSpawner.getData().setEntityId(this.trialSpawner, randomSource, entityType);
		this.setChanged();
	}

	public TrialSpawner getTrialSpawner() {
		return this.trialSpawner;
	}

	@Override
	public TrialSpawnerState getState() {
		return !this.getBlockState().hasProperty(BlockStateProperties.TRIAL_SPAWNER_STATE)
			? TrialSpawnerState.INACTIVE
			: this.getBlockState().getValue(BlockStateProperties.TRIAL_SPAWNER_STATE);
	}

	@Override
	public void setState(Level level, TrialSpawnerState trialSpawnerState) {
		this.setChanged();
		level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.TRIAL_SPAWNER_STATE, trialSpawnerState));
	}

	@Override
	public void markUpdated() {
		this.setChanged();
		if (this.level != null) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}
	}
}

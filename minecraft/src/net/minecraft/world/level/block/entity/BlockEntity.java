package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BlockEntity {
	private static final Logger LOGGER = LogManager.getLogger();
	private final BlockEntityType<?> type;
	@Nullable
	protected Level level;
	protected final BlockPos worldPosition;
	protected boolean remove;
	private BlockState blockState;

	public BlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		this.type = blockEntityType;
		this.worldPosition = blockPos.immutable();
		this.blockState = blockState;
	}

	@Nullable
	public Level getLevel() {
		return this.level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public boolean hasLevel() {
		return this.level != null;
	}

	public void load(CompoundTag compoundTag) {
	}

	public CompoundTag save(CompoundTag compoundTag) {
		return this.saveMetadata(compoundTag);
	}

	private CompoundTag saveMetadata(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = BlockEntityType.getKey(this.getType());
		if (resourceLocation == null) {
			throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
		} else {
			compoundTag.putString("id", resourceLocation.toString());
			compoundTag.putInt("x", this.worldPosition.getX());
			compoundTag.putInt("y", this.worldPosition.getY());
			compoundTag.putInt("z", this.worldPosition.getZ());
			return compoundTag;
		}
	}

	@Nullable
	public static BlockEntity loadStatic(BlockPos blockPos, BlockState blockState, CompoundTag compoundTag) {
		String string = compoundTag.getString("id");
		return (BlockEntity)Registry.BLOCK_ENTITY_TYPE.getOptional(new ResourceLocation(string)).map(blockEntityType -> {
			try {
				return blockEntityType.create(blockPos, blockState);
			} catch (Throwable var5) {
				LOGGER.error("Failed to create block entity {}", string, var5);
				return null;
			}
		}).map(blockEntity -> {
			try {
				blockEntity.load(compoundTag);
				return blockEntity;
			} catch (Throwable var4) {
				LOGGER.error("Failed to load data for block entity {}", string, var4);
				return null;
			}
		}).orElseGet(() -> {
			LOGGER.warn("Skipping BlockEntity with id {}", string);
			return null;
		});
	}

	public void setChanged() {
		if (this.level != null) {
			setChanged(this.level, this.worldPosition, this.blockState);
		}
	}

	protected static void setChanged(Level level, BlockPos blockPos, BlockState blockState) {
		level.blockEntityChanged(blockPos);
		if (!blockState.isAir()) {
			level.updateNeighbourForOutputSignal(blockPos, blockState.getBlock());
		}
	}

	public BlockPos getBlockPos() {
		return this.worldPosition;
	}

	public BlockState getBlockState() {
		return this.blockState;
	}

	@Nullable
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return null;
	}

	public CompoundTag getUpdateTag() {
		return this.saveMetadata(new CompoundTag());
	}

	public boolean isRemoved() {
		return this.remove;
	}

	public void setRemoved() {
		this.remove = true;
	}

	public void clearRemoved() {
		this.remove = false;
	}

	public boolean triggerEvent(int i, int j) {
		return false;
	}

	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail(
			"Name", (CrashReportDetail<String>)(() -> Registry.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName())
		);
		if (this.level != null) {
			CrashReportCategory.populateBlockDetails(crashReportCategory, this.level, this.worldPosition, this.getBlockState());
			CrashReportCategory.populateBlockDetails(crashReportCategory, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
		}
	}

	public boolean onlyOpCanSetNbt() {
		return false;
	}

	public BlockEntityType<?> getType() {
		return this.type;
	}

	@Deprecated
	public void setBlockState(BlockState blockState) {
		this.blockState = blockState;
	}
}

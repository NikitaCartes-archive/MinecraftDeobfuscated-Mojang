package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BlockEntity {
	private static final Logger LOGGER = LogManager.getLogger();
	private final BlockEntityType<?> type;
	@Nullable
	protected Level level;
	protected BlockPos worldPosition = BlockPos.ZERO;
	protected boolean remove;
	@Nullable
	private BlockState blockState;
	private boolean hasLoggedInvalidStateBefore;

	public BlockEntity(BlockEntityType<?> blockEntityType) {
		this.type = blockEntityType;
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
		this.worldPosition = new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
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
	public static BlockEntity loadStatic(CompoundTag compoundTag) {
		String string = compoundTag.getString("id");
		return (BlockEntity)Registry.BLOCK_ENTITY_TYPE.getOptional(new ResourceLocation(string)).map(blockEntityType -> {
			try {
				return blockEntityType.create();
			} catch (Throwable var3) {
				LOGGER.error("Failed to create block entity {}", string, var3);
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
			this.blockState = this.level.getBlockState(this.worldPosition);
			this.level.blockEntityChanged(this.worldPosition, this);
			if (!this.blockState.isAir()) {
				this.level.updateNeighbourForOutputSignal(this.worldPosition, this.blockState.getBlock());
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public double distanceToSqr(double d, double e, double f) {
		double g = (double)this.worldPosition.getX() + 0.5 - d;
		double h = (double)this.worldPosition.getY() + 0.5 - e;
		double i = (double)this.worldPosition.getZ() + 0.5 - f;
		return g * g + h * h + i * i;
	}

	@Environment(EnvType.CLIENT)
	public double getViewDistance() {
		return 4096.0;
	}

	public BlockPos getBlockPos() {
		return this.worldPosition;
	}

	public BlockState getBlockState() {
		if (this.blockState == null) {
			this.blockState = this.level.getBlockState(this.worldPosition);
		}

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

	public void clearCache() {
		this.blockState = null;
	}

	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail(
			"Name", (CrashReportDetail<String>)(() -> Registry.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName())
		);
		if (this.level != null) {
			CrashReportCategory.populateBlockDetails(crashReportCategory, this.worldPosition, this.getBlockState());
			CrashReportCategory.populateBlockDetails(crashReportCategory, this.worldPosition, this.level.getBlockState(this.worldPosition));
		}
	}

	public void setPosition(BlockPos blockPos) {
		this.worldPosition = blockPos.immutable();
	}

	public boolean onlyOpCanSetNbt() {
		return false;
	}

	public void rotate(Rotation rotation) {
	}

	public void mirror(Mirror mirror) {
	}

	public BlockEntityType<?> getType() {
		return this.type;
	}

	public void logInvalidState() {
		if (!this.hasLoggedInvalidStateBefore) {
			this.hasLoggedInvalidStateBefore = true;
			LOGGER.warn("Block entity invalid: {} @ {}", () -> Registry.BLOCK_ENTITY_TYPE.getKey(this.getType()), this::getBlockPos);
		}
	}
}

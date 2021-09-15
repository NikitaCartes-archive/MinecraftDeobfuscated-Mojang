package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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

	public static BlockPos getPosFromTag(CompoundTag compoundTag) {
		return new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
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

	protected void saveAdditional(CompoundTag compoundTag) {
	}

	public final CompoundTag saveWithFullMetadata() {
		CompoundTag compoundTag = this.saveWithoutMetadata();
		this.saveMetadata(compoundTag);
		return compoundTag;
	}

	public final CompoundTag saveWithId() {
		CompoundTag compoundTag = this.saveWithoutMetadata();
		this.saveId(compoundTag);
		return compoundTag;
	}

	public final CompoundTag saveWithoutMetadata() {
		CompoundTag compoundTag = new CompoundTag();
		this.saveAdditional(compoundTag);
		return compoundTag;
	}

	private void saveId(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = BlockEntityType.getKey(this.getType());
		if (resourceLocation == null) {
			throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
		} else {
			compoundTag.putString("id", resourceLocation.toString());
		}
	}

	public static void addEntityType(CompoundTag compoundTag, BlockEntityType<?> blockEntityType) {
		compoundTag.putString("id", BlockEntityType.getKey(blockEntityType).toString());
	}

	public void saveToItem(ItemStack itemStack) {
		BlockItem.setBlockEntityData(itemStack, this.getType(), this.saveWithoutMetadata());
	}

	private void saveMetadata(CompoundTag compoundTag) {
		this.saveId(compoundTag);
		compoundTag.putInt("x", this.worldPosition.getX());
		compoundTag.putInt("y", this.worldPosition.getY());
		compoundTag.putInt("z", this.worldPosition.getZ());
	}

	@Nullable
	public static BlockEntity loadStatic(BlockPos blockPos, BlockState blockState, CompoundTag compoundTag) {
		String string = compoundTag.getString("id");
		ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
		if (resourceLocation == null) {
			LOGGER.error("Block entity has invalid type: {}", string);
			return null;
		} else {
			return (BlockEntity)Registry.BLOCK_ENTITY_TYPE.getOptional(resourceLocation).map(blockEntityType -> {
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
				} catch (Throwable var4x) {
					LOGGER.error("Failed to load data for block entity {}", string, var4x);
					return null;
				}
			}).orElseGet(() -> {
				LOGGER.warn("Skipping BlockEntity with id {}", string);
				return null;
			});
		}
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
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return null;
	}

	public CompoundTag getUpdateTag() {
		return new CompoundTag();
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

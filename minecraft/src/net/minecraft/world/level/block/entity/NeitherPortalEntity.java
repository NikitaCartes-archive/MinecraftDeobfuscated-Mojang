package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;

public class NeitherPortalEntity extends BlockEntity {
	private int dimension;

	public NeitherPortalEntity() {
		super(BlockEntityType.NEITHER);
	}

	public NeitherPortalEntity(int i) {
		this();
		this.dimension = i;
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.putInt("Dimension", this.dimension);
		return compoundTag;
	}

	@Override
	public void load(BlockState blockState, CompoundTag compoundTag) {
		super.load(blockState, compoundTag);
		this.dimension = compoundTag.getInt("Dimension");
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 15, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	public int getDimension() {
		return this.dimension;
	}

	public void setDimension(int i) {
		this.dimension = i;
	}
}

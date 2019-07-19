package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandBlockEntity extends BlockEntity {
	private boolean powered;
	private boolean auto;
	private boolean conditionMet;
	private boolean sendToClient;
	private final BaseCommandBlock commandBlock = new BaseCommandBlock() {
		@Override
		public void setCommand(String string) {
			super.setCommand(string);
			CommandBlockEntity.this.setChanged();
		}

		@Override
		public ServerLevel getLevel() {
			return (ServerLevel)CommandBlockEntity.this.level;
		}

		@Override
		public void onUpdated() {
			BlockState blockState = CommandBlockEntity.this.level.getBlockState(CommandBlockEntity.this.worldPosition);
			this.getLevel().sendBlockUpdated(CommandBlockEntity.this.worldPosition, blockState, blockState, 3);
		}

		@Environment(EnvType.CLIENT)
		@Override
		public Vec3 getPosition() {
			return new Vec3(
				(double)CommandBlockEntity.this.worldPosition.getX() + 0.5,
				(double)CommandBlockEntity.this.worldPosition.getY() + 0.5,
				(double)CommandBlockEntity.this.worldPosition.getZ() + 0.5
			);
		}

		@Override
		public CommandSourceStack createCommandSourceStack() {
			return new CommandSourceStack(
				this,
				new Vec3(
					(double)CommandBlockEntity.this.worldPosition.getX() + 0.5,
					(double)CommandBlockEntity.this.worldPosition.getY() + 0.5,
					(double)CommandBlockEntity.this.worldPosition.getZ() + 0.5
				),
				Vec2.ZERO,
				this.getLevel(),
				2,
				this.getName().getString(),
				this.getName(),
				this.getLevel().getServer(),
				null
			);
		}
	};

	public CommandBlockEntity() {
		super(BlockEntityType.COMMAND_BLOCK);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		this.commandBlock.save(compoundTag);
		compoundTag.putBoolean("powered", this.isPowered());
		compoundTag.putBoolean("conditionMet", this.wasConditionMet());
		compoundTag.putBoolean("auto", this.isAutomatic());
		return compoundTag;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.commandBlock.load(compoundTag);
		this.powered = compoundTag.getBoolean("powered");
		this.conditionMet = compoundTag.getBoolean("conditionMet");
		this.setAutomatic(compoundTag.getBoolean("auto"));
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		if (this.isSendToClient()) {
			this.setSendToClient(false);
			CompoundTag compoundTag = this.save(new CompoundTag());
			return new ClientboundBlockEntityDataPacket(this.worldPosition, 2, compoundTag);
		} else {
			return null;
		}
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	public BaseCommandBlock getCommandBlock() {
		return this.commandBlock;
	}

	public void setPowered(boolean bl) {
		this.powered = bl;
	}

	public boolean isPowered() {
		return this.powered;
	}

	public boolean isAutomatic() {
		return this.auto;
	}

	public void setAutomatic(boolean bl) {
		boolean bl2 = this.auto;
		this.auto = bl;
		if (!bl2 && bl && !this.powered && this.level != null && this.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
			Block block = this.getBlockState().getBlock();
			if (block instanceof CommandBlock) {
				this.markConditionMet();
				this.level.getBlockTicks().scheduleTick(this.worldPosition, block, block.getTickDelay(this.level));
			}
		}
	}

	public boolean wasConditionMet() {
		return this.conditionMet;
	}

	public boolean markConditionMet() {
		this.conditionMet = true;
		if (this.isConditional()) {
			BlockPos blockPos = this.worldPosition.relative(((Direction)this.level.getBlockState(this.worldPosition).getValue(CommandBlock.FACING)).getOpposite());
			if (this.level.getBlockState(blockPos).getBlock() instanceof CommandBlock) {
				BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
				this.conditionMet = blockEntity instanceof CommandBlockEntity && ((CommandBlockEntity)blockEntity).getCommandBlock().getSuccessCount() > 0;
			} else {
				this.conditionMet = false;
			}
		}

		return this.conditionMet;
	}

	public boolean isSendToClient() {
		return this.sendToClient;
	}

	public void setSendToClient(boolean bl) {
		this.sendToClient = bl;
	}

	public CommandBlockEntity.Mode getMode() {
		Block block = this.getBlockState().getBlock();
		if (block == Blocks.COMMAND_BLOCK) {
			return CommandBlockEntity.Mode.REDSTONE;
		} else if (block == Blocks.REPEATING_COMMAND_BLOCK) {
			return CommandBlockEntity.Mode.AUTO;
		} else {
			return block == Blocks.CHAIN_COMMAND_BLOCK ? CommandBlockEntity.Mode.SEQUENCE : CommandBlockEntity.Mode.REDSTONE;
		}
	}

	public boolean isConditional() {
		BlockState blockState = this.level.getBlockState(this.getBlockPos());
		return blockState.getBlock() instanceof CommandBlock ? (Boolean)blockState.getValue(CommandBlock.CONDITIONAL) : false;
	}

	@Override
	public void clearRemoved() {
		this.clearCache();
		super.clearRemoved();
	}

	public static enum Mode {
		SEQUENCE,
		AUTO,
		REDSTONE;
	}
}

package net.minecraft.world.level.block.entity;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

		@Override
		public Vec3 getPosition() {
			return Vec3.atCenterOf(CommandBlockEntity.this.worldPosition);
		}

		@Override
		public CommandSourceStack createCommandSourceStack() {
			Direction direction = CommandBlockEntity.this.getBlockState().getValue(CommandBlock.FACING);
			return new CommandSourceStack(
				this,
				Vec3.atCenterOf(CommandBlockEntity.this.worldPosition),
				new Vec2(0.0F, direction.toYRot()),
				this.getLevel(),
				2,
				this.getName().getString(),
				this.getName(),
				this.getLevel().getServer(),
				null
			);
		}

		@Override
		public boolean isValid() {
			return !CommandBlockEntity.this.isRemoved();
		}
	};

	public CommandBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.COMMAND_BLOCK, blockPos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		this.commandBlock.save(compoundTag);
		compoundTag.putBoolean("powered", this.isPowered());
		compoundTag.putBoolean("conditionMet", this.wasConditionMet());
		compoundTag.putBoolean("auto", this.isAutomatic());
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.commandBlock.load(compoundTag);
		this.powered = compoundTag.getBoolean("powered");
		this.conditionMet = compoundTag.getBoolean("conditionMet");
		this.setAutomatic(compoundTag.getBoolean("auto"));
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
			this.scheduleTick();
		}
	}

	public void onModeSwitch() {
		CommandBlockEntity.Mode mode = this.getMode();
		if (mode == CommandBlockEntity.Mode.AUTO && (this.powered || this.auto) && this.level != null) {
			this.scheduleTick();
		}
	}

	private void scheduleTick() {
		Block block = this.getBlockState().getBlock();
		if (block instanceof CommandBlock) {
			this.markConditionMet();
			this.level.scheduleTick(this.worldPosition, block, 1);
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

	public CommandBlockEntity.Mode getMode() {
		BlockState blockState = this.getBlockState();
		if (blockState.is(Blocks.COMMAND_BLOCK)) {
			return CommandBlockEntity.Mode.REDSTONE;
		} else if (blockState.is(Blocks.REPEATING_COMMAND_BLOCK)) {
			return CommandBlockEntity.Mode.AUTO;
		} else {
			return blockState.is(Blocks.CHAIN_COMMAND_BLOCK) ? CommandBlockEntity.Mode.SEQUENCE : CommandBlockEntity.Mode.REDSTONE;
		}
	}

	public boolean isConditional() {
		BlockState blockState = this.level.getBlockState(this.getBlockPos());
		return blockState.getBlock() instanceof CommandBlock ? (Boolean)blockState.getValue(CommandBlock.CONDITIONAL) : false;
	}

	public static enum Mode {
		SEQUENCE,
		AUTO,
		REDSTONE;
	}
}

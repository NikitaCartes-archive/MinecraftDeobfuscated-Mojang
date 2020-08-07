package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection {
	private static final Palette<BlockState> GLOBAL_BLOCKSTATE_PALETTE = new GlobalPalette<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState());
	private final int bottomBlockY;
	private short nonEmptyBlockCount;
	private short tickingBlockCount;
	private short tickingFluidCount;
	private final PalettedContainer<BlockState> states;

	public LevelChunkSection(int i) {
		this(i, (short)0, (short)0, (short)0);
	}

	public LevelChunkSection(int i, short s, short t, short u) {
		this.bottomBlockY = i;
		this.nonEmptyBlockCount = s;
		this.tickingBlockCount = t;
		this.tickingFluidCount = u;
		this.states = new PalettedContainer<>(
			GLOBAL_BLOCKSTATE_PALETTE, Block.BLOCK_STATE_REGISTRY, NbtUtils::readBlockState, NbtUtils::writeBlockState, Blocks.AIR.defaultBlockState()
		);
	}

	public BlockState getBlockState(int i, int j, int k) {
		return this.states.get(i, j, k);
	}

	public FluidState getFluidState(int i, int j, int k) {
		return this.states.get(i, j, k).getFluidState();
	}

	public void acquire() {
		this.states.acquire();
	}

	public void release() {
		this.states.release();
	}

	public BlockState setBlockState(int i, int j, int k, BlockState blockState) {
		return this.setBlockState(i, j, k, blockState, true);
	}

	public BlockState setBlockState(int i, int j, int k, BlockState blockState, boolean bl) {
		BlockState blockState2;
		if (bl) {
			blockState2 = this.states.getAndSet(i, j, k, blockState);
		} else {
			blockState2 = this.states.getAndSetUnchecked(i, j, k, blockState);
		}

		FluidState fluidState = blockState2.getFluidState();
		FluidState fluidState2 = blockState.getFluidState();
		if (!blockState2.isAir()) {
			this.nonEmptyBlockCount--;
			if (blockState2.isRandomlyTicking()) {
				this.tickingBlockCount--;
			}
		}

		if (!fluidState.isEmpty()) {
			this.tickingFluidCount--;
		}

		if (!blockState.isAir()) {
			this.nonEmptyBlockCount++;
			if (blockState.isRandomlyTicking()) {
				this.tickingBlockCount++;
			}
		}

		if (!fluidState2.isEmpty()) {
			this.tickingFluidCount++;
		}

		return blockState2;
	}

	public boolean isEmpty() {
		return this.nonEmptyBlockCount == 0;
	}

	public static boolean isEmpty(@Nullable LevelChunkSection levelChunkSection) {
		return levelChunkSection == LevelChunk.EMPTY_SECTION || levelChunkSection.isEmpty();
	}

	public boolean isRandomlyTicking() {
		return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
	}

	public boolean isRandomlyTickingBlocks() {
		return this.tickingBlockCount > 0;
	}

	public boolean isRandomlyTickingFluids() {
		return this.tickingFluidCount > 0;
	}

	public int bottomBlockY() {
		return this.bottomBlockY;
	}

	public void recalcBlockCounts() {
		this.nonEmptyBlockCount = 0;
		this.tickingBlockCount = 0;
		this.tickingFluidCount = 0;
		this.states.count((blockState, i) -> {
			FluidState fluidState = blockState.getFluidState();
			if (!blockState.isAir()) {
				this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + i);
				if (blockState.isRandomlyTicking()) {
					this.tickingBlockCount = (short)(this.tickingBlockCount + i);
				}
			}

			if (!fluidState.isEmpty()) {
				this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + i);
				if (fluidState.isRandomlyTicking()) {
					this.tickingFluidCount = (short)(this.tickingFluidCount + i);
				}
			}
		});
	}

	public PalettedContainer<BlockState> getStates() {
		return this.states;
	}

	@Environment(EnvType.CLIENT)
	public void read(FriendlyByteBuf friendlyByteBuf) {
		this.nonEmptyBlockCount = friendlyByteBuf.readShort();
		this.states.read(friendlyByteBuf);
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeShort(this.nonEmptyBlockCount);
		this.states.write(friendlyByteBuf);
	}

	public int getSerializedSize() {
		return 2 + this.states.getSerializedSize();
	}

	public boolean maybeHas(Predicate<BlockState> predicate) {
		return this.states.maybeHas(predicate);
	}
}

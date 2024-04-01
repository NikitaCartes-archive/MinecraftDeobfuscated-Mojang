package net.minecraft.world.grid;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class SubGridBlocks {
	public static final StreamCodec<RegistryFriendlyByteBuf, SubGridBlocks> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, SubGridBlocks>() {
		public SubGridBlocks decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			int i = registryFriendlyByteBuf.readVarInt();
			int j = registryFriendlyByteBuf.readVarInt();
			int k = registryFriendlyByteBuf.readVarInt();
			List<BlockState> list = registryFriendlyByteBuf.readList(friendlyByteBuf -> Block.stateById(friendlyByteBuf.readVarInt()));
			int l = Mth.ceillog2(list.size());
			BlockState[] blockStates = new BlockState[i * j * k];
			SimpleBitStorage simpleBitStorage = new SimpleBitStorage(l, blockStates.length, registryFriendlyByteBuf.readLongArray());

			for (int m = 0; m < blockStates.length; m++) {
				blockStates[m] = (BlockState)list.get(simpleBitStorage.get(m));
			}

			ArrayList<BlockPos> arrayList = registryFriendlyByteBuf.readCollection(ArrayList::new, BlockPos.STREAM_CODEC);
			return new SubGridBlocks(blockStates, arrayList, i, j, k);
		}

		public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, SubGridBlocks subGridBlocks) {
			registryFriendlyByteBuf.writeVarInt(subGridBlocks.sizeX);
			registryFriendlyByteBuf.writeVarInt(subGridBlocks.sizeY);
			registryFriendlyByteBuf.writeVarInt(subGridBlocks.sizeZ);
			Reference2IntMap<BlockState> reference2IntMap = new Reference2IntOpenHashMap<>();
			List<BlockState> list = new ArrayList();
			reference2IntMap.defaultReturnValue(-1);

			for (BlockState blockState : subGridBlocks.blockStates) {
				int i = list.size();
				int j = reference2IntMap.putIfAbsent(blockState, i);
				if (j == -1) {
					list.add(blockState);
				}
			}

			registryFriendlyByteBuf.writeCollection(list, (friendlyByteBuf, blockStatex) -> friendlyByteBuf.writeVarInt(Block.getId(blockStatex)));
			int k = Mth.ceillog2(list.size());
			BitStorage bitStorage = new SimpleBitStorage(k, subGridBlocks.sizeX * subGridBlocks.sizeY * subGridBlocks.sizeZ);
			int l = 0;

			for (BlockState blockState2 : subGridBlocks.blockStates) {
				bitStorage.set(l++, reference2IntMap.getInt(blockState2));
			}

			registryFriendlyByteBuf.writeLongArray(bitStorage.getRaw());
			registryFriendlyByteBuf.writeCollection(subGridBlocks.tickables, BlockPos.STREAM_CODEC);
		}
	};
	private static final int NO_INDEX = -1;
	private static final BlockState EMPTY_BLOCK_STATE = Blocks.AIR.defaultBlockState();
	final BlockState[] blockStates;
	final List<BlockPos> tickables;
	final int sizeX;
	final int sizeY;
	final int sizeZ;

	SubGridBlocks(BlockState[] blockStates, List<BlockPos> list, int i, int j, int k) {
		this.blockStates = blockStates;
		this.tickables = list;
		this.sizeX = i;
		this.sizeY = j;
		this.sizeZ = k;
	}

	public SubGridBlocks(int i, int j, int k) {
		this.blockStates = new BlockState[i * j * k];
		Arrays.fill(this.blockStates, EMPTY_BLOCK_STATE);
		this.tickables = new ArrayList();
		this.sizeX = i;
		this.sizeY = j;
		this.sizeZ = k;
	}

	public void setBlockState(int i, int j, int k, BlockState blockState) {
		int l = this.index(i, j, k);
		if (l == -1) {
			throw new IllegalStateException("Block was out of bounds");
		} else {
			this.blockStates[l] = blockState;
		}
	}

	public void markTickable(BlockPos blockPos) {
		this.tickables.add(blockPos);
	}

	public void tick(Level level, Vec3 vec3, Direction direction) {
		this.tickables
			.forEach(
				blockPos -> {
					BlockState blockState = this.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
					if (blockState.getBlock() instanceof FlyingTickable flyingTickable) {
						flyingTickable.flyingTick(
							level, this, blockState, blockPos, vec3.add((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), direction
						);
					}
				}
			);
	}

	public BlockState getBlockState(int i, int j, int k) {
		int l = this.index(i, j, k);
		return l == -1 ? EMPTY_BLOCK_STATE : this.blockStates[l];
	}

	public BlockState getBlockState(BlockPos blockPos) {
		return this.getBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	private int index(int i, int j, int k) {
		return i >= 0 && j >= 0 && k >= 0 && i < this.sizeX && j < this.sizeY && k < this.sizeZ ? (i + k * this.sizeX) * this.sizeY + j : -1;
	}

	public int sizeX() {
		return this.sizeX;
	}

	public int sizeY() {
		return this.sizeY;
	}

	public int sizeZ() {
		return this.sizeZ;
	}

	public SubGridBlocks copy() {
		return new SubGridBlocks(
			(BlockState[])Arrays.copyOf(this.blockStates, this.blockStates.length), new ArrayList(this.tickables), this.sizeX, this.sizeY, this.sizeZ
		);
	}

	public void place(BlockPos blockPos, Level level) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < this.sizeZ; i++) {
			for (int j = 0; j < this.sizeX; j++) {
				for (int k = 0; k < this.sizeY; k++) {
					mutableBlockPos.setWithOffset(blockPos, j, k, i);
					BlockState blockState = this.getBlockState(j, k, i);
					if (!blockState.isAir()) {
						FluidState fluidState = level.getFluidState(mutableBlockPos);
						if (fluidState.is(Fluids.WATER)) {
							blockState = blockState.trySetValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
						}

						level.setBlock(mutableBlockPos, blockState, 18);
					}
				}
			}
		}

		for (int i = 0; i < this.sizeZ; i++) {
			for (int j = 0; j < this.sizeX; j++) {
				for (int kx = 0; kx < this.sizeY; kx++) {
					mutableBlockPos.setWithOffset(blockPos, j, kx, i);
					level.blockUpdated(mutableBlockPos, this.getBlockState(j, kx, i).getBlock());
				}
			}
		}
	}

	public static SubGridBlocks decode(HolderGetter<Block> holderGetter, CompoundTag compoundTag) {
		int i = compoundTag.getInt("size_x");
		int j = compoundTag.getInt("size_y");
		int k = compoundTag.getInt("size_z");
		BlockState[] blockStates = new BlockState[i * j * k];
		ListTag listTag = compoundTag.getList("palette", 10);
		List<BlockState> list = new ArrayList();

		for (int l = 0; l < listTag.size(); l++) {
			list.add(NbtUtils.readBlockState(holderGetter, listTag.getCompound(l)));
		}

		int[] is = compoundTag.getIntArray("blocks");
		if (is.length != blockStates.length) {
			return new SubGridBlocks(i, j, k);
		} else {
			for (int m = 0; m < is.length; m++) {
				int n = is[m];
				blockStates[m] = n < list.size() ? (BlockState)list.get(n) : Blocks.AIR.defaultBlockState();
			}

			List<BlockPos> list2 = new ArrayList();
			if (compoundTag.contains("tickables", 12)) {
				Arrays.stream(compoundTag.getLongArray("tickables")).mapToObj(BlockPos::of).forEach(list2::add);
			}

			return new SubGridBlocks(blockStates, list2, i, j, k);
		}
	}

	public Tag encode() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putInt("size_x", this.sizeX);
		compoundTag.putInt("size_y", this.sizeY);
		compoundTag.putInt("size_z", this.sizeZ);
		Reference2IntMap<BlockState> reference2IntMap = new Reference2IntOpenHashMap<>();
		reference2IntMap.defaultReturnValue(-1);
		ListTag listTag = new ListTag();
		int[] is = new int[this.blockStates.length];

		for (int i = 0; i < this.blockStates.length; i++) {
			BlockState blockState = this.blockStates[i];
			int j = listTag.size();
			int k = reference2IntMap.putIfAbsent(blockState, j);
			if (k == -1) {
				listTag.add(NbtUtils.writeBlockState(blockState));
				is[i] = j;
			} else {
				is[i] = k;
			}
		}

		compoundTag.put("palette", listTag);
		compoundTag.put("blocks", new IntArrayTag(is));
		compoundTag.putLongArray("tickables", this.tickables.stream().mapToLong(BlockPos::asLong).toArray());
		return compoundTag;
	}
}

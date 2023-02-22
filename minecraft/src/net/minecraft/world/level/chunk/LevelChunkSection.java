package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection {
	public static final int SECTION_WIDTH = 16;
	public static final int SECTION_HEIGHT = 16;
	public static final int SECTION_SIZE = 4096;
	public static final int BIOME_CONTAINER_BITS = 2;
	private final int bottomBlockY;
	private short nonEmptyBlockCount;
	private short tickingBlockCount;
	private short tickingFluidCount;
	private final PalettedContainer<BlockState> states;
	private PalettedContainerRO<Holder<Biome>> biomes;

	public LevelChunkSection(int i, PalettedContainer<BlockState> palettedContainer, PalettedContainerRO<Holder<Biome>> palettedContainerRO) {
		this.bottomBlockY = getBottomBlockY(i);
		this.states = palettedContainer;
		this.biomes = palettedContainerRO;
		this.recalcBlockCounts();
	}

	public LevelChunkSection(int i, Registry<Biome> registry) {
		this.bottomBlockY = getBottomBlockY(i);
		this.states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
		this.biomes = new PalettedContainer<>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
	}

	public static int getBottomBlockY(int i) {
		return i << 4;
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

	public boolean hasOnlyAir() {
		return this.nonEmptyBlockCount == 0;
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
		class BlockCounter implements PalettedContainer.CountConsumer<BlockState> {
			public int nonEmptyBlockCount;
			public int tickingBlockCount;
			public int tickingFluidCount;

			public void accept(BlockState blockState, int i) {
				FluidState fluidState = blockState.getFluidState();
				if (!blockState.isAir()) {
					this.nonEmptyBlockCount += i;
					if (blockState.isRandomlyTicking()) {
						this.tickingBlockCount += i;
					}
				}

				if (!fluidState.isEmpty()) {
					this.nonEmptyBlockCount += i;
					if (fluidState.isRandomlyTicking()) {
						this.tickingFluidCount += i;
					}
				}
			}
		}

		BlockCounter lv = new BlockCounter();
		this.states.count(lv);
		this.nonEmptyBlockCount = (short)lv.nonEmptyBlockCount;
		this.tickingBlockCount = (short)lv.tickingBlockCount;
		this.tickingFluidCount = (short)lv.tickingFluidCount;
	}

	public PalettedContainer<BlockState> getStates() {
		return this.states;
	}

	public PalettedContainerRO<Holder<Biome>> getBiomes() {
		return this.biomes;
	}

	public void read(FriendlyByteBuf friendlyByteBuf) {
		this.nonEmptyBlockCount = friendlyByteBuf.readShort();
		this.states.read(friendlyByteBuf);
		PalettedContainer<Holder<Biome>> palettedContainer = this.biomes.recreate();
		palettedContainer.read(friendlyByteBuf);
		this.biomes = palettedContainer;
	}

	public void readBiomes(FriendlyByteBuf friendlyByteBuf) {
		PalettedContainer<Holder<Biome>> palettedContainer = this.biomes.recreate();
		palettedContainer.read(friendlyByteBuf);
		this.biomes = palettedContainer;
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeShort(this.nonEmptyBlockCount);
		this.states.write(friendlyByteBuf);
		this.biomes.write(friendlyByteBuf);
	}

	public int getSerializedSize() {
		return 2 + this.states.getSerializedSize() + this.biomes.getSerializedSize();
	}

	public boolean maybeHas(Predicate<BlockState> predicate) {
		return this.states.maybeHas(predicate);
	}

	public Holder<Biome> getNoiseBiome(int i, int j, int k) {
		return this.biomes.get(i, j, k);
	}

	public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler, int i, int j) {
		PalettedContainer<Holder<Biome>> palettedContainer = this.biomes.recreate();
		int k = QuartPos.fromBlock(this.bottomBlockY());
		int l = 4;

		for (int m = 0; m < 4; m++) {
			for (int n = 0; n < 4; n++) {
				for (int o = 0; o < 4; o++) {
					palettedContainer.getAndSetUnchecked(m, n, o, biomeResolver.getNoiseBiome(i + m, k + n, j + o, sampler));
				}
			}
		}

		this.biomes = palettedContainer;
	}
}

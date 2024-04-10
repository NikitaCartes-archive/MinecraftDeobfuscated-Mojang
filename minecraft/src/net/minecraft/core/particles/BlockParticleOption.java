package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockParticleOption implements ParticleOptions {
	private static final Codec<BlockState> BLOCK_STATE_CODEC = Codec.withAlternative(
		BlockState.CODEC, BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState
	);
	private final ParticleType<BlockParticleOption> type;
	private final BlockState state;

	public static MapCodec<BlockParticleOption> codec(ParticleType<BlockParticleOption> particleType) {
		return BLOCK_STATE_CODEC.<BlockParticleOption>xmap(
				blockState -> new BlockParticleOption(particleType, blockState), blockParticleOption -> blockParticleOption.state
			)
			.fieldOf("block_state");
	}

	public static StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> streamCodec(ParticleType<BlockParticleOption> particleType) {
		return ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY)
			.map(blockState -> new BlockParticleOption(particleType, blockState), blockParticleOption -> blockParticleOption.state);
	}

	public BlockParticleOption(ParticleType<BlockParticleOption> particleType, BlockState blockState) {
		this.type = particleType;
		this.state = blockState;
	}

	@Override
	public ParticleType<BlockParticleOption> getType() {
		return this.type;
	}

	public BlockState getState() {
		return this.state;
	}
}

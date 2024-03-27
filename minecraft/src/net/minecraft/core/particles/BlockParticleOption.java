package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockParticleOption implements ParticleOptions {
	public static final ParticleOptions.Deserializer<BlockParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<BlockParticleOption>() {
		public BlockParticleOption fromCommand(ParticleType<BlockParticleOption> particleType, StringReader stringReader, HolderLookup.Provider provider) throws CommandSyntaxException {
			stringReader.expect(' ');
			return new BlockParticleOption(particleType, BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), stringReader, false).blockState());
		}
	};
	private final ParticleType<BlockParticleOption> type;
	private final BlockState state;

	public static MapCodec<BlockParticleOption> codec(ParticleType<BlockParticleOption> particleType) {
		return BlockState.CODEC
			.<BlockParticleOption>xmap(blockState -> new BlockParticleOption(particleType, blockState), blockParticleOption -> blockParticleOption.state)
			.fieldOf("value");
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
	public String writeToString(HolderLookup.Provider provider) {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()) + " " + BlockStateParser.serialize(this.state);
	}

	@Override
	public ParticleType<BlockParticleOption> getType() {
		return this.type;
	}

	public BlockState getState() {
		return this.state;
	}
}

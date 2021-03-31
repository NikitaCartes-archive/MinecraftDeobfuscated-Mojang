package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockParticleOption implements ParticleOptions {
	public static final ParticleOptions.Deserializer<BlockParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<BlockParticleOption>() {
		public BlockParticleOption fromCommand(ParticleType<BlockParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
			stringReader.expect(' ');
			return new BlockParticleOption(particleType, new BlockStateParser(stringReader, false).parse(false).getState());
		}

		public BlockParticleOption fromNetwork(ParticleType<BlockParticleOption> particleType, FriendlyByteBuf friendlyByteBuf) {
			return new BlockParticleOption(particleType, Block.BLOCK_STATE_REGISTRY.byId(friendlyByteBuf.readVarInt()));
		}
	};
	private final ParticleType<BlockParticleOption> type;
	private final BlockState state;

	public static Codec<BlockParticleOption> codec(ParticleType<BlockParticleOption> particleType) {
		return BlockState.CODEC.xmap(blockState -> new BlockParticleOption(particleType, blockState), blockParticleOption -> blockParticleOption.state);
	}

	public BlockParticleOption(ParticleType<BlockParticleOption> particleType, BlockState blockState) {
		this.type = particleType;
		this.state = blockState;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(this.state));
	}

	@Override
	public String writeToString() {
		return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + BlockStateParser.serialize(this.state);
	}

	@Override
	public ParticleType<BlockParticleOption> getType() {
		return this.type;
	}

	public BlockState getState() {
		return this.state;
	}
}

package net.minecraft.world.level.block;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TorchBlock extends BaseTorchBlock {
	protected static final MapCodec<SimpleParticleType> PARTICLE_OPTIONS_FIELD = BuiltInRegistries.PARTICLE_TYPE
		.byNameCodec()
		.<SimpleParticleType>comapFlatMap(
			particleType -> particleType instanceof SimpleParticleType simpleParticleType
					? DataResult.success(simpleParticleType)
					: DataResult.error(() -> "Not a SimpleParticleType: " + particleType),
			simpleParticleType -> simpleParticleType
		)
		.fieldOf("particle_options");
	public static final MapCodec<TorchBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(PARTICLE_OPTIONS_FIELD.forGetter(torchBlock -> torchBlock.flameParticle), propertiesCodec()).apply(instance, TorchBlock::new)
	);
	protected final SimpleParticleType flameParticle;

	@Override
	public MapCodec<? extends TorchBlock> codec() {
		return CODEC;
	}

	protected TorchBlock(SimpleParticleType simpleParticleType, BlockBehaviour.Properties properties) {
		super(properties);
		this.flameParticle = simpleParticleType;
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		double d = (double)blockPos.getX() + 0.5;
		double e = (double)blockPos.getY() + 0.7;
		double f = (double)blockPos.getZ() + 0.5;
		level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
		level.addParticle(this.flameParticle, d, e, f, 0.0, 0.0, 0.0);
	}
}

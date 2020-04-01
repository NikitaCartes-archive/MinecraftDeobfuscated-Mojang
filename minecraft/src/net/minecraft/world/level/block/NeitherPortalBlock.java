package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.NeitherPortalEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class NeitherPortalBlock extends PortalBlock implements EntityBlock {
	private static final Random I_DONT_CARE_ABOUT_THREADS = new Random();

	public NeitherPortalBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	protected ParticleOptions getParticleType(BlockState blockState, Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof NeitherPortalEntity) {
			int i = ((NeitherPortalEntity)blockEntity).getDimension();
			Vec3 vec3 = Vec3.fromRGB24(i);
			double d = 1.0 + (double)(i >> 16 & 0xFF) / 255.0;
			return new DustParticleOptions((float)vec3.x, (float)vec3.y, (float)vec3.z, (float)d);
		} else {
			return super.getParticleType(blockState, level, blockPos);
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new NeitherPortalEntity(Math.abs(I_DONT_CARE_ABOUT_THREADS.nextInt()));
	}
}

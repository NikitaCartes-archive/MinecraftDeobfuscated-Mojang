package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.VineProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ViciousPotatoBlock extends Block {
	private static final EntityTypeTest<Entity, LivingEntity> LIVING_ENTITIES_SELECTOR = EntityTypeTest.forClass(LivingEntity.class);
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
	public static final MapCodec<ViciousPotatoBlock> CODEC = simpleCodec(ViciousPotatoBlock::new);

	public ViciousPotatoBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(ENABLED, Boolean.valueOf(false)));
	}

	@Override
	public MapCodec<ViciousPotatoBlock> codec() {
		return CODEC;
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (level instanceof ServerLevel serverLevel) {
			boolean bl2 = level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.above());
			if (bl2) {
				this.attack(serverLevel, blockPos, 5.0F);
			}
		}
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		super.randomTick(blockState, serverLevel, blockPos, randomSource);
		if (this.attack(serverLevel, blockPos, 5.0F)) {
			serverLevel.setBlock(blockPos, blockState.setValue(ENABLED, Boolean.valueOf(true)), 2);
			serverLevel.scheduleTick(blockPos, this, 20 + randomSource.nextInt(100));
		}
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		super.tick(blockState, serverLevel, blockPos, randomSource);
		float f = randomSource.nextFloat() * 0.7F;
		if (this.attack(serverLevel, blockPos, f) && randomSource.nextFloat() < 1.0F - f * f) {
			serverLevel.scheduleTick(blockPos, this, 20 + randomSource.nextInt(1 + (int)(f * 100.0F)));
		} else {
			serverLevel.setBlock(blockPos, blockState.setValue(ENABLED, Boolean.valueOf(false)), 2);
		}
	}

	private boolean attack(ServerLevel serverLevel, BlockPos blockPos, float f) {
		Vec3 vec3 = Vec3.atCenterOf(blockPos);
		AABB aABB = AABB.ofSize(vec3, 16.0, 16.0, 16.0);
		List<LivingEntity> list = new ArrayList();
		serverLevel.getEntities(LIVING_ENTITIES_SELECTOR, aABB, EntitySelector.NO_CREATIVE_OR_SPECTATOR, list, 10);
		Optional<LivingEntity> optional = Util.getRandomSafe(list, serverLevel.getRandom());
		if (optional.isEmpty()) {
			return false;
		} else {
			LivingEntity livingEntity = (LivingEntity)optional.get();
			Vec3 vec32 = livingEntity.getBoundingBox().getCenter();
			Vec3 vec33 = vec32.subtract(vec3).normalize();
			Vec3 vec34 = vec33.add(vec3);
			VineProjectile vineProjectile = EntityType.VINE_PROJECTILE.create(serverLevel);
			vineProjectile.setStrength(f);
			vineProjectile.setPos(vec34.x(), vec34.y(), vec34.z());
			vineProjectile.shoot(vec33.x, vec33.y, vec33.z, 0.5F, 0.0F);
			serverLevel.addFreshEntity(vineProjectile);
			serverLevel.levelEvent(1002, blockPos, 0);
			return true;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ENABLED);
	}
}

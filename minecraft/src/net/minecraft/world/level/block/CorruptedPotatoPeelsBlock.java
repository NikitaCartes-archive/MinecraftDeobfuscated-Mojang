package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class CorruptedPotatoPeelsBlock extends Block {
	public static final MapCodec<CorruptedPotatoPeelsBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(propertiesCodec()).apply(instance, CorruptedPotatoPeelsBlock::new)
	);

	public CorruptedPotatoPeelsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<CorruptedPotatoPeelsBlock> codec() {
		return CODEC;
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(500) == 0) {
			level.playLocalSound(
				(double)blockPos.getX() + 0.5,
				(double)blockPos.getY() + 0.5,
				(double)blockPos.getZ() + 0.5,
				SoundEvents.WITCH_AMBIENT,
				SoundSource.BLOCKS,
				0.5F,
				randomSource.nextFloat() * 0.2F + 0.3F,
				false
			);
		}

		if (randomSource.nextInt(2) == 0) {
			double d = (double)blockPos.getX() + randomSource.nextDouble();
			double e = (double)blockPos.getY() + 0.5 + randomSource.nextDouble();
			double f = (double)blockPos.getZ() + randomSource.nextDouble();
			double g = ((double)randomSource.nextFloat() - 0.5) * 0.5;
			double h = -((double)randomSource.nextFloat() - 0.5) * 1.5;
			double i = ((double)randomSource.nextFloat() - 0.5) * 0.5;
			level.addParticle(ParticleTypes.ENCHANT, d, e, f, g, h, i);
		}
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		super.randomTick(blockState, serverLevel, blockPos, randomSource);
		List<Entity> list = serverLevel.getEntities((Entity)null, AABB.encapsulatingFullBlocks(blockPos, blockPos.relative(Direction.UP)));
		int i = 0;
		if (!list.isEmpty()) {
			for (Entity entity : list) {
				if (entity instanceof LivingEntity) {
					i++;
				}
			}
		}

		if (i <= 0) {
			if (randomSource.nextInt(20) == 0) {
				int j = randomSource.nextInt(1000);
				if (j < 500) {
					Entity entityx = EntityType.ENDERMAN.create(serverLevel, enderMan -> {
					}, blockPos, MobSpawnType.NATURAL, true, false);
					serverLevel.addFreshEntity(entityx);
				} else if (j < 900) {
					Entity entityx = EntityType.POISONOUS_POTATO_ZOMBIE.create(serverLevel, poisonousPotatoZombie -> {
					}, blockPos, MobSpawnType.NATURAL, true, false);
					serverLevel.addFreshEntity(entityx);
				} else if (j < 980) {
					Entity entityx = EntityType.ITEM
						.create(serverLevel, itemEntity -> itemEntity.setItem(new ItemStack(Items.POISONOUS_POTATO)), blockPos, MobSpawnType.NATURAL, true, false);
					serverLevel.addFreshEntity(entityx);
				} else if (j == 999) {
					Entity entityx = EntityType.GHAST.create(serverLevel, ghast -> {
					}, blockPos.relative(Direction.UP, 2), MobSpawnType.NATURAL, true, false);
					serverLevel.addFreshEntity(entityx);
				} else {
					Entity entityx = EntityType.BATATO.create(serverLevel, batato -> {
					}, blockPos, MobSpawnType.NATURAL, true, false);
					serverLevel.addFreshEntity(entityx);
				}
			}
		}
	}
}

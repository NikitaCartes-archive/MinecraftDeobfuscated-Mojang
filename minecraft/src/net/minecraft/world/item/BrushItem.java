package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SuspiciousSandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrushItem extends Item {
	public static final int TICKS_BETWEEN_SWEEPS = 10;
	private static final int USE_DURATION = 225;

	public BrushItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Player player = useOnContext.getPlayer();
		if (player != null) {
			player.startUsingItem(useOnContext.getHand());
		}

		return InteractionResult.CONSUME;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.BRUSH;
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 225;
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
		if (i >= 0 && livingEntity instanceof Player player) {
			BlockHitResult blockHitResult = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
			BlockPos blockPos = blockHitResult.getBlockPos();
			if (blockHitResult.getType() == HitResult.Type.MISS) {
				livingEntity.releaseUsingItem();
			} else {
				int j = this.getUseDuration(itemStack) - i + 1;
				if (j == 1 || j % 10 == 0) {
					BlockState blockState = level.getBlockState(blockPos);
					this.spawnDustParticles(level, blockHitResult, blockState, livingEntity.getViewVector(0.0F));
					level.playSound(player, blockPos, SoundEvents.BRUSH_BRUSHING, SoundSource.PLAYERS);
					if (!level.isClientSide()
						&& blockState.is(Blocks.SUSPICIOUS_SAND)
						&& level.getBlockEntity(blockPos) instanceof SuspiciousSandBlockEntity suspiciousSandBlockEntity) {
						boolean bl = suspiciousSandBlockEntity.brush(level.getGameTime(), player, blockHitResult.getDirection());
						if (bl) {
							itemStack.hurtAndBreak(1, livingEntity, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
						}
					}
				}
			}
		} else {
			livingEntity.releaseUsingItem();
		}
	}

	public void spawnDustParticles(Level level, BlockHitResult blockHitResult, BlockState blockState, Vec3 vec3) {
		double d = 3.0;
		int i = level.getRandom().nextInt(7, 12);
		BlockParticleOption blockParticleOption = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
		Direction direction = blockHitResult.getDirection();
		BrushItem.DustParticlesDelta dustParticlesDelta = BrushItem.DustParticlesDelta.fromDirection(vec3, direction);
		Vec3 vec32 = blockHitResult.getLocation();

		for (int j = 0; j < i; j++) {
			level.addParticle(
				blockParticleOption,
				vec32.x - (double)(direction == Direction.WEST ? 1.0E-6F : 0.0F),
				vec32.y,
				vec32.z - (double)(direction == Direction.NORTH ? 1.0E-6F : 0.0F),
				dustParticlesDelta.xd() * 3.0 * level.getRandom().nextDouble(),
				0.0,
				dustParticlesDelta.zd() * 3.0 * level.getRandom().nextDouble()
			);
		}
	}

	static record DustParticlesDelta(double xd, double yd, double zd) {
		private static final double ALONG_SIDE_DELTA = 1.0;
		private static final double OUT_FROM_SIDE_DELTA = 0.1;

		public static BrushItem.DustParticlesDelta fromDirection(Vec3 vec3, Direction direction) {
			double d = 0.0;

			return switch (direction) {
				case DOWN -> new BrushItem.DustParticlesDelta(-vec3.x(), 0.0, vec3.z());
				case UP -> new BrushItem.DustParticlesDelta(vec3.z(), 0.0, -vec3.x());
				case NORTH -> new BrushItem.DustParticlesDelta(1.0, 0.0, -0.1);
				case SOUTH -> new BrushItem.DustParticlesDelta(-1.0, 0.0, 0.1);
				case WEST -> new BrushItem.DustParticlesDelta(-0.1, 0.0, -1.0);
				case EAST -> new BrushItem.DustParticlesDelta(0.1, 0.0, 1.0);
			};
		}
	}
}

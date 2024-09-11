package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrushItem extends Item {
	public static final int ANIMATION_DURATION = 10;
	private static final int USE_DURATION = 200;

	public BrushItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Player player = useOnContext.getPlayer();
		if (player != null && this.calculateHitResult(player).getType() == HitResult.Type.BLOCK) {
			player.startUsingItem(useOnContext.getHand());
		}

		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
		return ItemUseAnimation.BRUSH;
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		return 200;
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
		if (i >= 0 && livingEntity instanceof Player player) {
			HitResult hitResult = this.calculateHitResult(player);
			if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
				int j = this.getUseDuration(itemStack, livingEntity) - i + 1;
				boolean bl = j % 10 == 5;
				if (bl) {
					BlockPos blockPos = blockHitResult.getBlockPos();
					BlockState blockState = level.getBlockState(blockPos);
					HumanoidArm humanoidArm = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
					if (blockState.shouldSpawnTerrainParticles() && blockState.getRenderShape() != RenderShape.INVISIBLE) {
						this.spawnDustParticles(level, blockHitResult, blockState, livingEntity.getViewVector(0.0F), humanoidArm);
					}

					SoundEvent soundEvent;
					if (blockState.getBlock() instanceof BrushableBlock brushableBlock) {
						soundEvent = brushableBlock.getBrushSound();
					} else {
						soundEvent = SoundEvents.BRUSH_GENERIC;
					}

					level.playSound(player, blockPos, soundEvent, SoundSource.BLOCKS);
					if (level instanceof ServerLevel serverLevel && level.getBlockEntity(blockPos) instanceof BrushableBlockEntity brushableBlockEntity) {
						boolean bl2 = brushableBlockEntity.brush(level.getGameTime(), serverLevel, player, blockHitResult.getDirection(), itemStack);
						if (bl2) {
							EquipmentSlot equipmentSlot = itemStack.equals(player.getItemBySlot(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
							itemStack.hurtAndBreak(1, player, equipmentSlot);
						}
					}
				}

				return;
			}

			livingEntity.releaseUsingItem();
		} else {
			livingEntity.releaseUsingItem();
		}
	}

	private HitResult calculateHitResult(Player player) {
		return ProjectileUtil.getHitResultOnViewVector(player, EntitySelector.CAN_BE_PICKED, player.blockInteractionRange());
	}

	private void spawnDustParticles(Level level, BlockHitResult blockHitResult, BlockState blockState, Vec3 vec3, HumanoidArm humanoidArm) {
		double d = 3.0;
		int i = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		int j = level.getRandom().nextInt(7, 12);
		BlockParticleOption blockParticleOption = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
		Direction direction = blockHitResult.getDirection();
		BrushItem.DustParticlesDelta dustParticlesDelta = BrushItem.DustParticlesDelta.fromDirection(vec3, direction);
		Vec3 vec32 = blockHitResult.getLocation();

		for (int k = 0; k < j; k++) {
			level.addParticle(
				blockParticleOption,
				vec32.x - (double)(direction == Direction.WEST ? 1.0E-6F : 0.0F),
				vec32.y,
				vec32.z - (double)(direction == Direction.NORTH ? 1.0E-6F : 0.0F),
				dustParticlesDelta.xd() * (double)i * 3.0 * level.getRandom().nextDouble(),
				0.0,
				dustParticlesDelta.zd() * (double)i * 3.0 * level.getRandom().nextDouble()
			);
		}
	}

	static record DustParticlesDelta(double xd, double yd, double zd) {
		private static final double ALONG_SIDE_DELTA = 1.0;
		private static final double OUT_FROM_SIDE_DELTA = 0.1;

		public static BrushItem.DustParticlesDelta fromDirection(Vec3 vec3, Direction direction) {
			double d = 0.0;

			return switch (direction) {
				case DOWN, UP -> new BrushItem.DustParticlesDelta(vec3.z(), 0.0, -vec3.x());
				case NORTH -> new BrushItem.DustParticlesDelta(1.0, 0.0, -0.1);
				case SOUTH -> new BrushItem.DustParticlesDelta(-1.0, 0.0, 0.1);
				case WEST -> new BrushItem.DustParticlesDelta(-0.1, 0.0, -1.0);
				case EAST -> new BrushItem.DustParticlesDelta(0.1, 0.0, 1.0);
			};
		}
	}
}

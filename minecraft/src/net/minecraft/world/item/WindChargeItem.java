package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class WindChargeItem extends Item implements ProjectileItem {
	private static final int COOLDOWN = 10;

	public WindChargeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		if (!level.isClientSide()) {
			Vec3 vec3 = player.getEyePosition().add(player.getForward().scale(0.8F));
			if (!level.getBlockState(BlockPos.containing(vec3)).canBeReplaced()) {
				vec3 = player.getEyePosition().add(player.getForward().scale(0.05F));
			}

			WindCharge windCharge = new WindCharge(player, level, vec3.x(), vec3.y(), vec3.z());
			windCharge.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
			level.addFreshEntity(windCharge);
		}

		level.playSound(
			null,
			player.getX(),
			player.getY(),
			player.getZ(),
			SoundEvents.WIND_CHARGE_THROW,
			SoundSource.NEUTRAL,
			0.5F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
		);
		ItemStack itemStack = player.getItemInHand(interactionHand);
		player.getCooldowns().addCooldown(this, 10);
		player.awardStat(Stats.ITEM_USED.get(this));
		itemStack.consume(1, player);
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		RandomSource randomSource = level.getRandom();
		double d = randomSource.triangle((double)direction.getStepX(), 0.11485000000000001);
		double e = randomSource.triangle((double)direction.getStepY(), 0.11485000000000001);
		double f = randomSource.triangle((double)direction.getStepZ(), 0.11485000000000001);
		return new WindCharge(level, position.x(), position.y(), position.z(), d, e, f);
	}

	@Override
	public void shoot(Projectile projectile, double d, double e, double f, float g, float h) {
	}

	@Override
	public ProjectileItem.DispenseConfig createDispenseConfig() {
		return ProjectileItem.DispenseConfig.builder()
			.positionFunction((blockSource, direction) -> DispenserBlock.getDispensePosition(blockSource, 1.0, Vec3.ZERO))
			.uncertainty(6.6666665F)
			.power(1.0F)
			.overrideDispenseEvent(1051)
			.build();
	}
}

package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketItem extends Item implements ProjectileItem {
	public static final byte[] CRAFTABLE_DURATIONS = new byte[]{1, 2, 3};
	public static final double ROCKET_PLACEMENT_OFFSET = 0.15;

	public FireworkRocketItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		if (level instanceof ServerLevel serverLevel) {
			ItemStack itemStack = useOnContext.getItemInHand();
			Vec3 vec3 = useOnContext.getClickLocation();
			Direction direction = useOnContext.getClickedFace();
			Projectile.spawnProjectile(
				new FireworkRocketEntity(
					level,
					useOnContext.getPlayer(),
					vec3.x + (double)direction.getStepX() * 0.15,
					vec3.y + (double)direction.getStepY() * 0.15,
					vec3.z + (double)direction.getStepZ() * 0.15,
					itemStack
				),
				serverLevel,
				itemStack
			);
			itemStack.shrink(1);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		if (player.isFallFlying()) {
			ItemStack itemStack = player.getItemInHand(interactionHand);
			if (level instanceof ServerLevel serverLevel) {
				Projectile.spawnProjectile(new FireworkRocketEntity(level, itemStack, player), serverLevel, itemStack);
				itemStack.consume(1, player);
				player.awardStat(Stats.ITEM_USED.get(this));
			}

			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		Fireworks fireworks = itemStack.get(DataComponents.FIREWORKS);
		if (fireworks != null) {
			fireworks.addToTooltip(tooltipContext, list::add, tooltipFlag);
		}
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		return new FireworkRocketEntity(level, itemStack.copyWithCount(1), position.x(), position.y(), position.z(), true);
	}

	@Override
	public ProjectileItem.DispenseConfig createDispenseConfig() {
		return ProjectileItem.DispenseConfig.builder()
			.positionFunction(FireworkRocketItem::getEntityJustOutsideOfBlockPos)
			.uncertainty(1.0F)
			.power(0.5F)
			.overrideDispenseEvent(1004)
			.build();
	}

	private static Vec3 getEntityJustOutsideOfBlockPos(BlockSource blockSource, Direction direction) {
		return blockSource.center()
			.add((double)direction.getStepX() * 0.5000099999997474, (double)direction.getStepY() * 0.5000099999997474, (double)direction.getStepZ() * 0.5000099999997474);
	}
}

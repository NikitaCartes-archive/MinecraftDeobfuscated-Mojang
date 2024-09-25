package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoatItem extends Item {
	private final EntityType<? extends AbstractBoat> entityType;

	public BoatItem(EntityType<? extends AbstractBoat> entityType, Item.Properties properties) {
		super(properties);
		this.entityType = entityType;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
		if (hitResult.getType() == HitResult.Type.MISS) {
			return InteractionResult.PASS;
		} else {
			Vec3 vec3 = player.getViewVector(1.0F);
			double d = 5.0;
			List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale(5.0)).inflate(1.0), EntitySelector.CAN_BE_PICKED);
			if (!list.isEmpty()) {
				Vec3 vec32 = player.getEyePosition();

				for (Entity entity : list) {
					AABB aABB = entity.getBoundingBox().inflate((double)entity.getPickRadius());
					if (aABB.contains(vec32)) {
						return InteractionResult.PASS;
					}
				}
			}

			if (hitResult.getType() == HitResult.Type.BLOCK) {
				AbstractBoat abstractBoat = this.getBoat(level, hitResult, itemStack, player);
				if (abstractBoat == null) {
					return InteractionResult.FAIL;
				} else {
					abstractBoat.setYRot(player.getYRot());
					if (!level.noCollision(abstractBoat, abstractBoat.getBoundingBox())) {
						return InteractionResult.FAIL;
					} else {
						if (!level.isClientSide) {
							level.addFreshEntity(abstractBoat);
							level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
							itemStack.consume(1, player);
						}

						player.awardStat(Stats.ITEM_USED.get(this));
						return InteractionResult.SUCCESS;
					}
				}
			} else {
				return InteractionResult.PASS;
			}
		}
	}

	@Nullable
	private AbstractBoat getBoat(Level level, HitResult hitResult, ItemStack itemStack, Player player) {
		AbstractBoat abstractBoat = this.entityType.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
		if (abstractBoat != null) {
			Vec3 vec3 = hitResult.getLocation();
			abstractBoat.setInitialPos(vec3.x, vec3.y, vec3.z);
			if (level instanceof ServerLevel serverLevel) {
				EntityType.createDefaultStackConfig(serverLevel, itemStack, player).accept(abstractBoat);
			}
		}

		return abstractBoat;
	}
}

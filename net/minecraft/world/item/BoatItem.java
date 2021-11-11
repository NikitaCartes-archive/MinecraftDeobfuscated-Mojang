/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BoatItem
extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final Boat.Type type;

    public BoatItem(Boat.Type type, Item.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult hitResult = BoatItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (((HitResult)hitResult).getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }
        Vec3 vec3 = player.getViewVector(1.0f);
        double d = 5.0;
        List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
        if (!list.isEmpty()) {
            Vec3 vec32 = player.getEyePosition();
            for (Entity entity : list) {
                AABB aABB = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (!aABB.contains(vec32)) continue;
                return InteractionResultHolder.pass(itemStack);
            }
        }
        if (((HitResult)hitResult).getType() == HitResult.Type.BLOCK) {
            Boat boat = new Boat(level, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
            boat.setType(this.type);
            boat.setYRot(player.getYRot());
            if (!level.noCollision(boat, boat.getBoundingBox())) {
                return InteractionResultHolder.fail(itemStack);
            }
            if (!level.isClientSide) {
                level.addFreshEntity(boat);
                level.gameEvent((Entity)player, GameEvent.ENTITY_PLACE, new BlockPos(hitResult.getLocation()));
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }
        return InteractionResultHolder.pass(itemStack);
    }
}


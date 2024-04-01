package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FloatatoItem extends BlockItem {
	private static final float FLOATING_PLACE_DISTANCE = 3.0F;

	public FloatatoItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		Vec3 vec3 = player.getEyePosition();
		Vec3 vec32 = player.calculateViewVector(player.getXRot(), player.getYRot());
		Vec3 vec33 = vec3.add(vec32.scale(player.blockInteractionRange()));
		BlockHitResult blockHitResult = level.clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (blockHitResult.getType() == HitResult.Type.MISS) {
			Vec3 vec34 = vec3.add(vec32.scale(3.0));
			InteractionResult interactionResult = super.place(
				new BlockPlaceContext(player, interactionHand, itemStack, new BlockHitResult(vec34, player.getDirection(), BlockPos.containing(vec34), false))
			);
			return new InteractionResultHolder<>(interactionResult, itemStack);
		} else {
			InteractionResult interactionResult2 = super.useOn(new UseOnContext(player, interactionHand, blockHitResult));
			return new InteractionResultHolder<>(interactionResult2, itemStack);
		}
	}
}

package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class WaterLilyBlockItem extends BlockItem {
	public WaterLilyBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		BlockHitResult blockHitResult2 = blockHitResult.withPosition(blockHitResult.getBlockPos().above());
		InteractionResult interactionResult = super.useOn(new UseOnContext(player, interactionHand, blockHitResult2));
		return new InteractionResultHolder<>(interactionResult, player.getItemInHand(interactionHand));
	}
}

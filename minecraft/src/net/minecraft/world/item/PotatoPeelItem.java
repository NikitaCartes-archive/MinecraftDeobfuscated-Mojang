package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PotatoPeelItem extends Item {
	public static final DyeColor PEELGRASS_PEEL_COLOR = DyeColor.LIME;
	private final DyeColor color;

	public PotatoPeelItem(Item.Properties properties, DyeColor dyeColor) {
		super(properties);
		this.color = dyeColor;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		if (this.color == PEELGRASS_PEEL_COLOR) {
			Level level = useOnContext.getLevel();
			BlockPos blockPos = useOnContext.getClickedPos();
			BlockState blockState = level.getBlockState(blockPos);
			if (blockState.is(Blocks.TERREDEPOMME) && useOnContext.getClickedFace() == Direction.UP) {
				Player player = useOnContext.getPlayer();
				useOnContext.getItemInHand().shrink(1);
				level.playSound(player, blockPos, SoundEvents.PEELGRASS_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat(0.9F, 1.1F));
				level.setBlockAndUpdate(blockPos, Blocks.PEELGRASS_BLOCK.defaultBlockState());
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}

		return super.useOn(useOnContext);
	}
}

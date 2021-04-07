package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HoneycombItem extends Item {
	public static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(
		() -> ImmutableBiMap.<Block, Block>builder()
				.put(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK)
				.put(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER)
				.put(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER)
				.put(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER)
				.put(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)
				.put(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)
				.put(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)
				.put(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)
				.put(Blocks.CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB)
				.put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB)
				.put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB)
				.put(Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB)
				.put(Blocks.CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER_STAIRS)
				.put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS)
				.put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS)
				.put(Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
				.build()
	);
	public static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> ((BiMap)WAXABLES.get()).inverse());

	public HoneycombItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		return (InteractionResult)getWaxed(blockState).map(blockStatex -> {
			useOnContext.getItemInHand().shrink(1);
			level.setBlock(blockPos, blockStatex, 11);
			Player player = useOnContext.getPlayer();
			level.levelEvent(player, 3003, blockPos, 0);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}).orElse(InteractionResult.PASS);
	}

	public static Optional<BlockState> getWaxed(BlockState blockState) {
		return Optional.ofNullable(((BiMap)WAXABLES.get()).get(blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
	}
}

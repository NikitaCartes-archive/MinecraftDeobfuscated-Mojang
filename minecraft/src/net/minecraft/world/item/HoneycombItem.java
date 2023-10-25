package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoneycombItem extends Item implements SignApplicator {
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
				.put(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER)
				.put(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER)
				.put(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER)
				.put(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER)
				.put(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR)
				.put(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR)
				.put(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR)
				.put(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR)
				.put(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR)
				.put(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR)
				.put(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR)
				.put(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR)
				.put(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE)
				.put(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE)
				.put(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE)
				.put(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE)
				.put(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB)
				.put(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB)
				.put(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB)
				.put(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB)
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
			Player player = useOnContext.getPlayer();
			ItemStack itemStack = useOnContext.getItemInHand();
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
			}

			itemStack.shrink(1);
			level.setBlock(blockPos, blockStatex, 11);
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockStatex));
			level.levelEvent(player, 3003, blockPos, 0);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}).orElse(InteractionResult.PASS);
	}

	public static Optional<BlockState> getWaxed(BlockState blockState) {
		return Optional.ofNullable((Block)((BiMap)WAXABLES.get()).get(blockState.getBlock())).map(block -> block.withPropertiesOf(blockState));
	}

	@Override
	public boolean tryApplyToSign(Level level, SignBlockEntity signBlockEntity, boolean bl, Player player) {
		if (signBlockEntity.setWaxed(true)) {
			level.levelEvent(null, 3003, signBlockEntity.getBlockPos(), 0);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canApplyToSign(SignText signText, Player player) {
		return true;
	}
}

package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BookBoxBlock extends Block {
	private static final char[] CHARACTERS = new char[]{
		' ', ',', '.', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BookBoxBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		Direction direction = blockState.getValue(FACING);
		int i = blockPos.getY();
		int j;
		int k;
		switch (direction) {
			case NORTH:
				j = 15 - blockPos.getX() & 15;
				k = 0;
				break;
			case SOUTH:
				j = blockPos.getX() & 15;
				k = 2;
				break;
			case EAST:
				j = 15 - blockPos.getZ() & 15;
				k = 1;
				break;
			case WEST:
			default:
				j = blockPos.getZ() & 15;
				k = 3;
		}

		if (j > 0 && j < 15) {
			ChunkPos chunkPos = new ChunkPos(blockPos);
			String string = chunkPos.x + "/" + chunkPos.z + "/" + k + "/" + j + "/" + i;
			Random random = new Random((long)chunkPos.x);
			Random random2 = new Random((long)chunkPos.z);
			Random random3 = new Random((long)((j << 8) + (i << 4) + k));
			ItemStack itemStack = new ItemStack(Items.WRITTEN_BOOK);
			CompoundTag compoundTag = itemStack.getOrCreateTag();
			ListTag listTag = new ListTag();

			for (int l = 0; l < 16; l++) {
				StringBuilder stringBuilder = new StringBuilder();

				for (int m = 0; m < 128; m++) {
					int n = random.nextInt() + random2.nextInt() + -random3.nextInt();
					stringBuilder.append(CHARACTERS[Math.floorMod(n, CHARACTERS.length)]);
				}

				listTag.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(stringBuilder.toString()))));
			}

			compoundTag.put("pages", listTag);
			compoundTag.putString("author", ChatFormatting.OBFUSCATED + "Universe itself");
			compoundTag.putString("title", string);
			popResource(level, blockPos.relative(blockHitResult.getDirection()), itemStack);
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.FAIL;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Direction direction = blockPlaceContext.getHorizontalDirection().getOpposite();
		return this.defaultBlockState().setValue(FACING, direction);
	}
}

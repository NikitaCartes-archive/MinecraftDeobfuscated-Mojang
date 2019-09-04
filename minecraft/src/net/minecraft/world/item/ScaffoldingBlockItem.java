package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ScaffoldingBlockItem extends BlockItem {
	public ScaffoldingBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Nullable
	@Override
	public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockPlaceContext) {
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		Level level = blockPlaceContext.getLevel();
		BlockState blockState = level.getBlockState(blockPos);
		Block block = this.getBlock();
		if (blockState.getBlock() != block) {
			return ScaffoldingBlock.getDistance(level, blockPos) == 7 ? null : blockPlaceContext;
		} else {
			Direction direction;
			if (blockPlaceContext.isSecondaryUseActive()) {
				direction = blockPlaceContext.isInside() ? blockPlaceContext.getClickedFace().getOpposite() : blockPlaceContext.getClickedFace();
			} else {
				direction = blockPlaceContext.getClickedFace() == Direction.UP ? blockPlaceContext.getHorizontalDirection() : Direction.UP;
			}

			int i = 0;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos).move(direction);

			while (i < 7) {
				if (!level.isClientSide && !Level.isInWorldBounds(mutableBlockPos)) {
					Player player = blockPlaceContext.getPlayer();
					int j = level.getMaxBuildHeight();
					if (player instanceof ServerPlayer && mutableBlockPos.getY() >= j) {
						ClientboundChatPacket clientboundChatPacket = new ClientboundChatPacket(
							new TranslatableComponent("build.tooHigh", j).withStyle(ChatFormatting.RED), ChatType.GAME_INFO
						);
						((ServerPlayer)player).connection.send(clientboundChatPacket);
					}
					break;
				}

				blockState = level.getBlockState(mutableBlockPos);
				if (blockState.getBlock() != this.getBlock()) {
					if (blockState.canBeReplaced(blockPlaceContext)) {
						return BlockPlaceContext.at(blockPlaceContext, mutableBlockPos, direction);
					}
					break;
				}

				mutableBlockPos.move(direction);
				if (direction.getAxis().isHorizontal()) {
					i++;
				}
			}

			return null;
		}
	}

	@Override
	protected boolean mustSurvive() {
		return false;
	}
}

package net.minecraft.world.item;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

public class PotatoStaffItem extends Item {
	public PotatoStaffItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Player player = useOnContext.getPlayer();
		if (player instanceof ServerPlayer serverPlayer) {
			if (serverPlayer.gameMode.isSurvival()) {
				ServerAdvancementManager serverAdvancementManager = serverPlayer.getServer().getAdvancements();
				ResourceLocation resourceLocation = new ResourceLocation("potato/enter_the_potato");
				AdvancementHolder advancementHolder = serverAdvancementManager.get(resourceLocation);
				if (advancementHolder != null) {
					AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(advancementHolder);
					if (!advancementProgress.isDone()) {
						Level level = player.level();
						serverPlayer.sendSystemMessage(Component.translatable("item.minecraft.potato_staff.unworthy", player.getDisplayName()));
						level.explode(null, level.damageSources().generic(), null, player.position(), 5.0F, true, Level.ExplosionInteraction.TNT);
						return InteractionResult.FAIL;
					}
				}
			}

			return this.place(new BlockPlaceContext(useOnContext));
		} else {
			return InteractionResult.FAIL;
		}
	}

	public InteractionResult place(BlockPlaceContext blockPlaceContext) {
		if (!blockPlaceContext.canPlace()) {
			return InteractionResult.FAIL;
		} else {
			BlockState blockState = Blocks.POTATO_PORTAL.defaultBlockState();
			Player player = blockPlaceContext.getPlayer();
			CollisionContext collisionContext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
			if (!blockState.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
				return InteractionResult.FAIL;
			} else if (!blockPlaceContext.getLevel().isUnobstructed(blockState, blockPlaceContext.getClickedPos(), collisionContext)) {
				return InteractionResult.FAIL;
			} else if (!blockPlaceContext.getLevel().setBlock(blockPlaceContext.getClickedPos(), blockState, 11)) {
				return InteractionResult.FAIL;
			} else {
				BlockPos blockPos = blockPlaceContext.getClickedPos();
				Level level = blockPlaceContext.getLevel();
				Player player2 = blockPlaceContext.getPlayer();
				level.playSound(null, blockPos, SoundEvents.MEGASPUD_SUMMON, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(player2, level.getBlockState(blockPos)));
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}
	}
}

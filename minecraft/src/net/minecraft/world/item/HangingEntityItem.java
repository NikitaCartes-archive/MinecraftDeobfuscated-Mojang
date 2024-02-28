package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem extends Item {
	private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
	private final EntityType<? extends HangingEntity> type;

	public HangingEntityItem(EntityType<? extends HangingEntity> entityType, Item.Properties properties) {
		super(properties);
		this.type = entityType;
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		BlockPos blockPos = useOnContext.getClickedPos();
		Direction direction = useOnContext.getClickedFace();
		BlockPos blockPos2 = blockPos.relative(direction);
		Player player = useOnContext.getPlayer();
		ItemStack itemStack = useOnContext.getItemInHand();
		if (player != null && !this.mayPlace(player, direction, itemStack, blockPos2)) {
			return InteractionResult.FAIL;
		} else {
			Level level = useOnContext.getLevel();
			HangingEntity hangingEntity;
			if (this.type == EntityType.PAINTING) {
				Optional<Painting> optional = Painting.create(level, blockPos2, direction);
				if (optional.isEmpty()) {
					return InteractionResult.CONSUME;
				}

				hangingEntity = (HangingEntity)optional.get();
			} else if (this.type == EntityType.ITEM_FRAME) {
				hangingEntity = new ItemFrame(level, blockPos2, direction);
			} else {
				if (this.type != EntityType.GLOW_ITEM_FRAME) {
					return InteractionResult.sidedSuccess(level.isClientSide);
				}

				hangingEntity = new GlowItemFrame(level, blockPos2, direction);
			}

			CustomData customData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
			if (!customData.isEmpty()) {
				EntityType.updateCustomEntityTag(level, player, hangingEntity, customData);
			}

			if (hangingEntity.survives()) {
				if (!level.isClientSide) {
					hangingEntity.playPlacementSound();
					level.gameEvent(player, GameEvent.ENTITY_PLACE, hangingEntity.position());
					level.addFreshEntity(hangingEntity);
				}

				itemStack.shrink(1);
				return InteractionResult.sidedSuccess(level.isClientSide);
			} else {
				return InteractionResult.CONSUME;
			}
		}
	}

	protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
		return !direction.getAxis().isVertical() && player.mayUseItemAt(blockPos, direction, itemStack);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		if (this.type == EntityType.PAINTING) {
			CustomData customData = itemStack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
			if (!customData.isEmpty()) {
				customData.read(Painting.VARIANT_MAP_CODEC)
					.result()
					.ifPresentOrElse(
						holder -> {
							holder.unwrapKey().ifPresent(resourceKey -> {
								list.add(Component.translatable(resourceKey.location().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW));
								list.add(Component.translatable(resourceKey.location().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY));
							});
							list.add(
								Component.translatable(
									"painting.dimensions",
									Mth.positiveCeilDiv(((PaintingVariant)holder.value()).getWidth(), 16),
									Mth.positiveCeilDiv(((PaintingVariant)holder.value()).getHeight(), 16)
								)
							);
						},
						() -> list.add(TOOLTIP_RANDOM_VARIANT)
					);
			} else if (tooltipFlag.isCreative()) {
				list.add(TOOLTIP_RANDOM_VARIANT);
			}
		}
	}
}

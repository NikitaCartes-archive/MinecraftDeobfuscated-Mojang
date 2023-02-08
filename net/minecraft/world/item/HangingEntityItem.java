/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class HangingEntityItem
extends Item {
    private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
    private final EntityType<? extends HangingEntity> type;

    public HangingEntityItem(EntityType<? extends HangingEntity> entityType, Item.Properties properties) {
        super(properties);
        this.type = entityType;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        HangingEntity hangingEntity;
        BlockPos blockPos = useOnContext.getClickedPos();
        Direction direction = useOnContext.getClickedFace();
        BlockPos blockPos2 = blockPos.relative(direction);
        Player player = useOnContext.getPlayer();
        ItemStack itemStack = useOnContext.getItemInHand();
        if (player != null && !this.mayPlace(player, direction, itemStack, blockPos2)) {
            return InteractionResult.FAIL;
        }
        Level level = useOnContext.getLevel();
        if (this.type == EntityType.PAINTING) {
            Optional<Painting> optional = Painting.create(level, blockPos2, direction);
            if (optional.isEmpty()) {
                return InteractionResult.CONSUME;
            }
            hangingEntity = optional.get();
        } else if (this.type == EntityType.ITEM_FRAME) {
            hangingEntity = new ItemFrame(level, blockPos2, direction);
        } else if (this.type == EntityType.GLOW_ITEM_FRAME) {
            hangingEntity = new GlowItemFrame(level, blockPos2, direction);
        } else {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null) {
            EntityType.updateCustomEntityTag(level, player, hangingEntity, compoundTag);
        }
        if (hangingEntity.survives()) {
            if (!level.isClientSide) {
                hangingEntity.playPlacementSound();
                level.gameEvent((Entity)player, GameEvent.ENTITY_PLACE, hangingEntity.position());
                level.addFreshEntity(hangingEntity);
            }
            itemStack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }

    protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
        return !direction.getAxis().isVertical() && player.mayUseItemAt(blockPos, direction, itemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        if (this.type == EntityType.PAINTING) {
            CompoundTag compoundTag = itemStack.getTag();
            if (compoundTag != null && compoundTag.contains("EntityTag", 10)) {
                CompoundTag compoundTag2 = compoundTag.getCompound("EntityTag");
                Painting.loadVariant(compoundTag2).ifPresentOrElse(holder -> {
                    holder.unwrapKey().ifPresent(resourceKey -> {
                        list.add(Component.translatable(resourceKey.location().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW));
                        list.add(Component.translatable(resourceKey.location().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY));
                    });
                    list.add(Component.translatable("painting.dimensions", Mth.positiveCeilDiv(((PaintingVariant)holder.value()).getWidth(), 16), Mth.positiveCeilDiv(((PaintingVariant)holder.value()).getHeight(), 16)));
                }, () -> list.add(TOOLTIP_RANDOM_VARIANT));
            } else if (tooltipFlag.isCreative()) {
                list.add(TOOLTIP_RANDOM_VARIANT);
            }
        }
    }
}


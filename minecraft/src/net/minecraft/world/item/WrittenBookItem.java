package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

public class WrittenBookItem extends Item {
	public WrittenBookItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack itemStack) {
		WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (writtenBookContent != null) {
			String string = writtenBookContent.title().raw();
			if (!StringUtil.isBlank(string)) {
				return Component.literal(string);
			}
		}

		return super.getName(itemStack);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (writtenBookContent != null) {
			if (!StringUtil.isBlank(writtenBookContent.author())) {
				list.add(Component.translatable("book.byAuthor", writtenBookContent.author()).withStyle(ChatFormatting.GRAY));
			}

			list.add(Component.translatable("book.generation." + writtenBookContent.generation()).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		player.openItemGui(itemStack, interactionHand);
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	public static boolean resolveBookComponents(ItemStack itemStack, CommandSourceStack commandSourceStack, @Nullable Player player) {
		WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (writtenBookContent != null && !writtenBookContent.resolved()) {
			WrittenBookContent writtenBookContent2 = writtenBookContent.resolve(commandSourceStack, player);
			if (writtenBookContent2 != null) {
				itemStack.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContent2);
				return true;
			}

			itemStack.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContent.markResolved());
		}

		return false;
	}
}

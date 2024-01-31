package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WrittenBookItem extends Item {
	public static final int TITLE_LENGTH = 16;
	public static final int TITLE_MAX_LENGTH = 32;
	public static final int PAGE_EDIT_LENGTH = 1024;
	public static final int PAGE_LENGTH = 32767;
	public static final int MAX_PAGES = 100;
	public static final int MAX_GENERATION = 2;
	public static final String TAG_TITLE = "title";
	public static final String TAG_FILTERED_TITLE = "filtered_title";
	public static final String TAG_AUTHOR = "author";
	public static final String TAG_PAGES = "pages";
	public static final String TAG_FILTERED_PAGES = "filtered_pages";
	public static final String TAG_GENERATION = "generation";
	public static final String TAG_RESOLVED = "resolved";

	public WrittenBookItem(Item.Properties properties) {
		super(properties);
	}

	public static boolean makeSureTagIsValid(@Nullable CompoundTag compoundTag) {
		if (!WritableBookItem.makeSureTagIsValid(compoundTag)) {
			return false;
		} else if (!compoundTag.contains("title", 8)) {
			return false;
		} else {
			String string = compoundTag.getString("title");
			return string.length() > 32 ? false : compoundTag.contains("author", 8);
		}
	}

	public static int getGeneration(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null ? compoundTag.getInt("generation") : 0;
	}

	public static int getPageCount(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null ? compoundTag.getList("pages", 8).size() : 0;
	}

	@Override
	public Component getName(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null) {
			String string = compoundTag.getString("title");
			if (!StringUtil.isNullOrEmpty(string)) {
				return Component.literal(string);
			}
		}

		return super.getName(itemStack);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		if (itemStack.hasTag()) {
			CompoundTag compoundTag = itemStack.getTag();
			String string = compoundTag.getString("author");
			if (!StringUtil.isNullOrEmpty(string)) {
				list.add(Component.translatable("book.byAuthor", string).withStyle(ChatFormatting.GRAY));
			}

			list.add(Component.translatable("book.generation." + compoundTag.getInt("generation")).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		player.openItemGui(itemStack, interactionHand);
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	public static boolean resolveBookComponents(ItemStack itemStack, @Nullable CommandSourceStack commandSourceStack, @Nullable Player player) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && !compoundTag.getBoolean("resolved")) {
			compoundTag.putBoolean("resolved", true);
			if (!makeSureTagIsValid(compoundTag)) {
				return false;
			} else {
				ListTag listTag = compoundTag.getList("pages", 8);
				ListTag listTag2 = new ListTag();

				for (int i = 0; i < listTag.size(); i++) {
					String string = resolvePage(commandSourceStack, player, listTag.getString(i));
					if (string.length() > 32767) {
						return false;
					}

					listTag2.add(i, (Tag)StringTag.valueOf(string));
				}

				if (compoundTag.contains("filtered_pages", 10)) {
					CompoundTag compoundTag2 = compoundTag.getCompound("filtered_pages");
					CompoundTag compoundTag3 = new CompoundTag();

					for (String string2 : compoundTag2.getAllKeys()) {
						String string3 = resolvePage(commandSourceStack, player, compoundTag2.getString(string2));
						if (string3.length() > 32767) {
							return false;
						}

						compoundTag3.putString(string2, string3);
					}

					compoundTag.put("filtered_pages", compoundTag3);
				}

				compoundTag.put("pages", listTag2);
				return true;
			}
		} else {
			return false;
		}
	}

	private static String resolvePage(@Nullable CommandSourceStack commandSourceStack, @Nullable Player player, String string) {
		Component component;
		try {
			component = Component.Serializer.fromJsonLenient(string);
			component = ComponentUtils.updateForEntity(commandSourceStack, component, player, 0);
		} catch (Exception var5) {
			component = Component.literal(string);
		}

		return Component.Serializer.toJson(component);
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return true;
	}
}

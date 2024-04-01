package net.minecraft.world.item;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PoisonousPotatoPlantItem extends ArmorItem {
	private static final Style INSPECTION_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
	private static final int RUMBLED_CLICKS = 4;

	public PoisonousPotatoPlantItem(Holder<ArmorMaterial> holder, ArmorItem.Type type, Item.Properties properties) {
		super(holder, type, properties);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		int i = itemStack.getOrDefault(DataComponents.CLICKS, Integer.valueOf(0));
		if (i >= 4) {
			list.add(ComponentUtils.mergeStyles(Component.translatable("item.minecraft.poisonous_potato_plant.rumbled.line1"), INSPECTION_STYLE));
			list.add(
				ComponentUtils.mergeStyles(
					Component.translatable("item.minecraft.poisonous_potato_plant.rumbled.line2", Component.translatable("item.minecraft.poisonous_potato")), INSPECTION_STYLE
				)
			);
		}
	}

	@Override
	public void onViewedInContainer(ItemStack itemStack, Level level, BlockPos blockPos, Container container) {
		List<ItemStack> list = container.getMatching(itemStackx -> itemStackx.has(DataComponents.UNDERCOVER_ID));

		for (ItemStack itemStack2 : list) {
			if (itemStack2.get(DataComponents.UNDERCOVER_ID) == 0) {
				itemStack2.set(DataComponents.UNDERCOVER_ID, level.getRandom().nextInt());
			}
		}

		int i = itemStack.get(DataComponents.UNDERCOVER_ID);
		list.removeIf(itemStackx -> itemStackx.get(DataComponents.UNDERCOVER_ID) == i);
		if (!list.isEmpty()) {
			ItemStack itemStack2x = (ItemStack)list.get(level.getRandom().nextInt(list.size()));
			int j = itemStack2x.get(DataComponents.UNDERCOVER_ID);
			Int2IntMap int2IntMap = itemStack2x.getOrDefault(DataComponents.CONTACTS_MESSAGES, new Int2IntOpenHashMap());
			Int2IntMap int2IntMap2 = itemStack.getOrDefault(DataComponents.CONTACTS_MESSAGES, new Int2IntOpenHashMap());
			int k = int2IntMap.getOrDefault(i, -1);
			int l = int2IntMap2.getOrDefault(j, -1);
			if (k > l) {
				int2IntMap2.put(j, k);
				itemStack.set(DataComponents.CONTACTS_MESSAGES, int2IntMap2);
			} else {
				int m = l + 1;
				Optional<MutableComponent> optional = writeSecretMessage(m, level.getNearestPlayer(blockPos, 4.0, false));
				if (!optional.isEmpty()) {
					int2IntMap2.put(j, m);
					itemStack.set(DataComponents.CONTACTS_MESSAGES, int2IntMap2);
					List<ItemStack> list2 = container.getMatching(itemStackx -> itemStackx.is(Items.PAPER));
					list2.removeIf(itemStackx -> {
						if (!itemStackx.has(DataComponents.SECRET_MESSAGE)) {
							return true;
						} else {
							IntIntPair intIntPair = itemStackx.get(DataComponents.SECRET_MESSAGE);
							return intIntPair.firstInt() != i && intIntPair.firstInt() != j;
						}
					});
					ItemStack itemStack3;
					if (list2.isEmpty()) {
						itemStack3 = new ItemStack(Items.PAPER);
						int n = ContainerHelper.tryAddItem(container, itemStack3);
						if (n < 0) {
							return;
						}
					} else {
						itemStack3 = (ItemStack)list2.get(level.getRandom().nextInt(list2.size()));
					}

					itemStack3.set(DataComponents.SECRET_MESSAGE, new IntIntImmutablePair(j, m));
					itemStack3.set(DataComponents.CUSTOM_NAME, (MutableComponent)optional.get());
				}
			}
		}
	}

	private static Optional<MutableComponent> writeSecretMessage(int i, @Nullable Player player) {
		MutableComponent mutableComponent = Component.translatable(
			"item.minecraft.paper.secret." + i, Optionull.mapOrDefault(player, Player::getDisplayName, Component.translatable("the.player"))
		);
		return mutableComponent.getString().startsWith("item.minecraft.paper.secret.") ? Optional.empty() : Optional.of(mutableComponent);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		int i = itemStack.getOrDefault(DataComponents.CLICKS, Integer.valueOf(0));
		return i >= 4 ? super.use(level, player, interactionHand) : InteractionResultHolder.pass(itemStack);
	}
}

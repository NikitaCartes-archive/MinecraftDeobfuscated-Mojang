package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GoatHornItem extends Item {
	private static final int SOUND_RANGE_BLOCKS = 256;
	public static final String TAG_SOUND_VARIANT = "SoundVariant";
	private static final int SCREAMING_SOUND_VARIANTS = 4;
	private static final int NON_SCREAMING_SOUND_VARIANTS = 4;
	private static final int USE_DURATION = 140;

	public GoatHornItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		MutableComponent mutableComponent = Component.translatable("item.minecraft.goat_horn.sound." + compoundTag.getInt("SoundVariant"));
		list.add(mutableComponent.withStyle(ChatFormatting.GRAY));
	}

	private static ItemStack create(int i) {
		ItemStack itemStack = new ItemStack(Items.GOAT_HORN);
		setSoundVariantId(itemStack, i);
		return itemStack;
	}

	public static ItemStack createFromGoat(Goat goat) {
		RandomSource randomSource = RandomSource.create((long)goat.getUUID().hashCode());
		return create(decideRandomVariant(randomSource, goat.isScreamingGoat()));
	}

	public static void setRandomNonScreamingSound(ItemStack itemStack, RandomSource randomSource) {
		setSoundVariantId(itemStack, randomSource.nextInt(4));
	}

	private static void setSoundVariantId(ItemStack itemStack, int i) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		compoundTag.put("SoundVariant", IntTag.valueOf(i));
	}

	@Override
	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
		for (int i = 0; i < 8; i++) {
			nonNullList.add(create(i));
		}
	}

	protected static int decideRandomVariant(RandomSource randomSource, boolean bl) {
		int i;
		int j;
		if (bl) {
			i = 4;
			j = 7;
		} else {
			i = 0;
			j = 3;
		}

		return Mth.randomBetweenInclusive(randomSource, i, j);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		player.startUsingItem(interactionHand);
		SoundEvent soundEvent = (SoundEvent)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(this.getSoundVariantId(itemStack));
		playSound(level, player, soundEvent);
		player.getCooldowns().addCooldown(Items.GOAT_HORN, 140);
		return InteractionResultHolder.consume(itemStack);
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 140;
	}

	private int getSoundVariantId(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag == null ? 0 : compoundTag.getInt("SoundVariant");
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.TOOT_HORN;
	}

	private static void playSound(Level level, Player player, SoundEvent soundEvent) {
		int i = 16;
		level.playSound(player, player, soundEvent, SoundSource.RECORDS, 16.0F, 1.0F);
	}
}

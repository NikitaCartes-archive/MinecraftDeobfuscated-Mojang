package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class InstrumentItem extends Item {
	private static final String TAG_INSTRUMENT = "instrument";
	private TagKey<Instrument> instruments;

	public InstrumentItem(Item.Properties properties, TagKey<Instrument> tagKey) {
		super(properties);
		this.instruments = tagKey;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		MutableComponent mutableComponent = Component.translatable(Util.makeDescriptionId("instrument", getInstrumentLocation(itemStack)));
		list.add(mutableComponent.withStyle(ChatFormatting.GRAY));
	}

	public static ItemStack create(Item item, Holder<Instrument> holder) {
		ItemStack itemStack = new ItemStack(item);
		setSoundVariantId(itemStack, holder);
		return itemStack;
	}

	public static void setRandom(ItemStack itemStack, TagKey<Instrument> tagKey, RandomSource randomSource) {
		Optional<Holder<Instrument>> optional = Registry.INSTRUMENT.getTag(tagKey).flatMap(named -> named.getRandomElement(randomSource));
		if (optional.isPresent()) {
			setSoundVariantId(itemStack, (Holder<Instrument>)optional.get());
		}
	}

	private static void setSoundVariantId(ItemStack itemStack, Holder<Instrument> holder) {
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		compoundTag.putString(
			"instrument", ((ResourceKey)holder.unwrapKey().orElseThrow(() -> new IllegalStateException("Invalid instrument"))).location().toString()
		);
	}

	@Override
	public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
		if (this.allowedIn(creativeModeTab)) {
			for (Holder<Instrument> holder : Registry.INSTRUMENT.getTagOrEmpty(this.instruments)) {
				nonNullList.add(create(Items.GOAT_HORN, holder));
			}
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Instrument instrument = getInstrument(itemStack);
		if (instrument != null) {
			player.startUsingItem(interactionHand);
			play(level, player, instrument);
			player.getCooldowns().addCooldown(this, instrument.useDuration());
			return InteractionResultHolder.consume(itemStack);
		} else {
			return InteractionResultHolder.fail(itemStack);
		}
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		Instrument instrument = getInstrument(itemStack);
		return instrument != null ? instrument.useDuration() : 0;
	}

	@Nullable
	private static Instrument getInstrument(ItemStack itemStack) {
		return Registry.INSTRUMENT.get(getInstrumentLocation(itemStack));
	}

	@Nullable
	private static ResourceLocation getInstrumentLocation(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null ? ResourceLocation.tryParse(compoundTag.getString("instrument")) : null;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.TOOT_HORN;
	}

	private static void play(Level level, Player player, Instrument instrument) {
		SoundEvent soundEvent = instrument.soundEvent();
		float f = instrument.range() / 16.0F;
		level.playSound(player, player, soundEvent, SoundSource.RECORDS, f, 1.0F);
	}
}

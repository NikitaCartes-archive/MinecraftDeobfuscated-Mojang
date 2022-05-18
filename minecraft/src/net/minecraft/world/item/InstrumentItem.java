package net.minecraft.world.item;

import java.util.Iterator;
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
import net.minecraft.world.level.gameevent.GameEvent;

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
		Optional<ResourceKey<Instrument>> optional = this.getInstrument(itemStack).flatMap(Holder::unwrapKey);
		if (optional.isPresent()) {
			MutableComponent mutableComponent = Component.translatable(Util.makeDescriptionId("instrument", ((ResourceKey)optional.get()).location()));
			list.add(mutableComponent.withStyle(ChatFormatting.GRAY));
		}
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
		Optional<Holder<Instrument>> optional = this.getInstrument(itemStack);
		if (optional.isPresent()) {
			Instrument instrument = (Instrument)((Holder)optional.get()).value();
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
		Optional<Holder<Instrument>> optional = this.getInstrument(itemStack);
		return optional.isPresent() ? ((Instrument)((Holder)optional.get()).value()).useDuration() : 0;
	}

	private Optional<Holder<Instrument>> getInstrument(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null) {
			ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("instrument"));
			if (resourceLocation != null) {
				return Registry.INSTRUMENT.getHolder(ResourceKey.create(Registry.INSTRUMENT_REGISTRY, resourceLocation));
			}
		}

		Iterator<Holder<Instrument>> iterator = Registry.INSTRUMENT.getTagOrEmpty(this.instruments).iterator();
		return iterator.hasNext() ? Optional.of((Holder)iterator.next()) : Optional.empty();
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.TOOT_HORN;
	}

	private static void play(Level level, Player player, Instrument instrument) {
		SoundEvent soundEvent = instrument.soundEvent();
		float f = instrument.range() / 16.0F;
		level.playSound(player, player, soundEvent, SoundSource.RECORDS, f, 1.0F);
		level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
	}
}

package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem extends Item {
	private final TagKey<Instrument> instruments;

	public InstrumentItem(TagKey<Instrument> tagKey, Item.Properties properties) {
		super(properties);
		this.instruments = tagKey;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
		HolderLookup.Provider provider = tooltipContext.registries();
		if (provider != null) {
			Optional<Holder<Instrument>> optional = this.getInstrument(itemStack, provider);
			if (optional.isPresent()) {
				MutableComponent mutableComponent = ((Instrument)((Holder)optional.get()).value()).description().copy();
				ComponentUtils.mergeStyles(mutableComponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
				list.add(mutableComponent);
			}
		}
	}

	public static ItemStack create(Item item, Holder<Instrument> holder) {
		ItemStack itemStack = new ItemStack(item);
		itemStack.set(DataComponents.INSTRUMENT, holder);
		return itemStack;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemStack, player.registryAccess());
		if (optional.isPresent()) {
			Instrument instrument = (Instrument)((Holder)optional.get()).value();
			player.startUsingItem(interactionHand);
			play(level, player, instrument);
			player.getCooldowns().addCooldown(itemStack, Mth.floor(instrument.useDuration() * 20.0F));
			player.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResult.CONSUME;
		} else {
			return InteractionResult.FAIL;
		}
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		Optional<Holder<Instrument>> optional = this.getInstrument(itemStack, livingEntity.registryAccess());
		return (Integer)optional.map(holder -> Mth.floor(((Instrument)holder.value()).useDuration() * 20.0F)).orElse(0);
	}

	private Optional<Holder<Instrument>> getInstrument(ItemStack itemStack, HolderLookup.Provider provider) {
		Holder<Instrument> holder = itemStack.get(DataComponents.INSTRUMENT);
		if (holder != null) {
			return Optional.of(holder);
		} else {
			Optional<HolderSet.Named<Instrument>> optional = provider.lookupOrThrow(Registries.INSTRUMENT).get(this.instruments);
			if (optional.isPresent()) {
				Iterator<Holder<Instrument>> iterator = ((HolderSet.Named)optional.get()).iterator();
				if (iterator.hasNext()) {
					return Optional.of((Holder)iterator.next());
				}
			}

			return Optional.empty();
		}
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
		return ItemUseAnimation.TOOT_HORN;
	}

	private static void play(Level level, Player player, Instrument instrument) {
		SoundEvent soundEvent = instrument.soundEvent().value();
		float f = instrument.range() / 16.0F;
		level.playSound(player, player, soundEvent, SoundSource.RECORDS, f, 1.0F);
		level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
	}
}

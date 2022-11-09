/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public class InstrumentItem
extends Item {
    private static final String TAG_INSTRUMENT = "instrument";
    private final TagKey<Instrument> instruments;

    public InstrumentItem(Item.Properties properties, TagKey<Instrument> tagKey) {
        super(properties);
        this.instruments = tagKey;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        Optional optional = this.getInstrument(itemStack).flatMap(Holder::unwrapKey);
        if (optional.isPresent()) {
            MutableComponent mutableComponent = Component.translatable(Util.makeDescriptionId(TAG_INSTRUMENT, ((ResourceKey)optional.get()).location()));
            list.add(mutableComponent.withStyle(ChatFormatting.GRAY));
        }
    }

    public static ItemStack create(Item item, Holder<Instrument> holder) {
        ItemStack itemStack = new ItemStack(item);
        InstrumentItem.setSoundVariantId(itemStack, holder);
        return itemStack;
    }

    public static void setRandom(ItemStack itemStack, TagKey<Instrument> tagKey, RandomSource randomSource) {
        Optional optional = BuiltInRegistries.INSTRUMENT.getTag(tagKey).flatMap(named -> named.getRandomElement(randomSource));
        optional.ifPresent(holder -> InstrumentItem.setSoundVariantId(itemStack, holder));
    }

    private static void setSoundVariantId(ItemStack itemStack, Holder<Instrument> holder) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putString(TAG_INSTRUMENT, holder.unwrapKey().orElseThrow(() -> new IllegalStateException("Invalid instrument")).location().toString());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemStack);
        if (optional.isPresent()) {
            Instrument instrument = optional.get().value();
            player.startUsingItem(interactionHand);
            InstrumentItem.play(level, player, instrument);
            player.getCooldowns().addCooldown(this, instrument.useDuration());
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemStack);
        return optional.map(holder -> ((Instrument)holder.value()).useDuration()).orElse(0);
    }

    private Optional<? extends Holder<Instrument>> getInstrument(ItemStack itemStack) {
        ResourceLocation resourceLocation;
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && (resourceLocation = ResourceLocation.tryParse(compoundTag.getString(TAG_INSTRUMENT))) != null) {
            return BuiltInRegistries.INSTRUMENT.getHolder(ResourceKey.create(Registries.INSTRUMENT, resourceLocation));
        }
        Iterator<Holder<Instrument>> iterator = BuiltInRegistries.INSTRUMENT.getTagOrEmpty(this.instruments).iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.TOOT_HORN;
    }

    private static void play(Level level, Player player, Instrument instrument) {
        SoundEvent soundEvent = instrument.soundEvent();
        float f = instrument.range() / 16.0f;
        level.playSound(player, player, soundEvent, SoundSource.RECORDS, f, 1.0f);
        level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
    }
}


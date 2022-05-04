/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class InstrumentItem
extends Item {
    private static final String TAG_INSTRUMENT = "instrument";
    private TagKey<Instrument> instruments;

    public InstrumentItem(Item.Properties properties, TagKey<Instrument> tagKey) {
        super(properties);
        this.instruments = tagKey;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        MutableComponent mutableComponent = Component.translatable(Util.makeDescriptionId(TAG_INSTRUMENT, InstrumentItem.getInstrumentLocation(itemStack)));
        list.add(mutableComponent.withStyle(ChatFormatting.GRAY));
    }

    public static ItemStack create(Item item, Holder<Instrument> holder) {
        ItemStack itemStack = new ItemStack(item);
        InstrumentItem.setSoundVariantId(itemStack, holder);
        return itemStack;
    }

    public static void setRandom(ItemStack itemStack, TagKey<Instrument> tagKey, RandomSource randomSource) {
        Optional optional = Registry.INSTRUMENT.getTag(tagKey).flatMap(named -> named.getRandomElement(randomSource));
        if (optional.isPresent()) {
            InstrumentItem.setSoundVariantId(itemStack, (Holder)optional.get());
        }
    }

    private static void setSoundVariantId(ItemStack itemStack, Holder<Instrument> holder) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putString(TAG_INSTRUMENT, holder.unwrapKey().orElseThrow(() -> new IllegalStateException("Invalid instrument")).location().toString());
    }

    @Override
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (this.allowedIn(creativeModeTab)) {
            for (Holder<Instrument> holder : Registry.INSTRUMENT.getTagOrEmpty(this.instruments)) {
                nonNullList.add(InstrumentItem.create(Items.GOAT_HORN, holder));
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Instrument instrument = InstrumentItem.getInstrument(itemStack);
        if (instrument != null) {
            player.startUsingItem(interactionHand);
            InstrumentItem.play(level, player, instrument);
            player.getCooldowns().addCooldown(this, instrument.useDuration());
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        Instrument instrument = InstrumentItem.getInstrument(itemStack);
        if (instrument != null) {
            return instrument.useDuration();
        }
        return 0;
    }

    @Nullable
    private static Instrument getInstrument(ItemStack itemStack) {
        return Registry.INSTRUMENT.get(InstrumentItem.getInstrumentLocation(itemStack));
    }

    @Nullable
    private static ResourceLocation getInstrumentLocation(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null) {
            return ResourceLocation.tryParse(compoundTag.getString(TAG_INSTRUMENT));
        }
        return null;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.TOOT_HORN;
    }

    private static void play(Level level, Player player, Instrument instrument) {
        SoundEvent soundEvent = instrument.soundEvent();
        float f = instrument.range() / 16.0f;
        level.playSound(player, player, soundEvent, SoundSource.RECORDS, f, 1.0f);
    }
}


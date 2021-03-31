/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RecordItem
extends Item {
    private static final Map<SoundEvent, RecordItem> BY_NAME = Maps.newHashMap();
    private final int analogOutput;
    private final SoundEvent sound;

    protected RecordItem(int i, SoundEvent soundEvent, Item.Properties properties) {
        super(properties);
        this.analogOutput = i;
        this.sound = soundEvent;
        BY_NAME.put(this.sound, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (!blockState.is(Blocks.JUKEBOX) || blockState.getValue(JukeboxBlock.HAS_RECORD).booleanValue()) {
            return InteractionResult.PASS;
        }
        ItemStack itemStack = useOnContext.getItemInHand();
        if (!level.isClientSide) {
            ((JukeboxBlock)Blocks.JUKEBOX).setRecord(level, blockPos, blockState, itemStack);
            level.levelEvent(null, 1010, blockPos, Item.getId(this));
            itemStack.shrink(1);
            Player player = useOnContext.getPlayer();
            if (player != null) {
                player.awardStat(Stats.PLAY_RECORD);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public int getAnalogOutput() {
        return this.analogOutput;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getDisplayName() {
        return new TranslatableComponent(this.getDescriptionId() + ".desc");
    }

    @Nullable
    public static RecordItem getBySound(SoundEvent soundEvent) {
        return BY_NAME.get(soundEvent);
    }

    public SoundEvent getSound() {
        return this.sound;
    }
}


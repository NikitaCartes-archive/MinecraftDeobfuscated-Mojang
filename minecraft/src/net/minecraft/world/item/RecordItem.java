package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RecordItem extends Item {
	private static final Map<SoundEvent, RecordItem> BY_NAME = Maps.<SoundEvent, RecordItem>newHashMap();
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
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.is(Blocks.JUKEBOX) && !(Boolean)blockState.getValue(JukeboxBlock.HAS_RECORD)) {
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

			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}

	public int getAnalogOutput() {
		return this.analogOutput;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
	}

	@Environment(EnvType.CLIENT)
	public MutableComponent getDisplayName() {
		return new TranslatableComponent(this.getDescriptionId() + ".desc");
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public static RecordItem getBySound(SoundEvent soundEvent) {
		return (RecordItem)BY_NAME.get(soundEvent);
	}

	@Environment(EnvType.CLIENT)
	public SoundEvent getSound() {
		return this.sound;
	}
}

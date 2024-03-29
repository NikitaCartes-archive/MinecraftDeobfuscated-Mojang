package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class RecordItem extends Item {
	private static final Map<SoundEvent, RecordItem> BY_NAME = Maps.<SoundEvent, RecordItem>newHashMap();
	private final int analogOutput;
	private final SoundEvent sound;
	private final int lengthInTicks;

	protected RecordItem(int i, SoundEvent soundEvent, Item.Properties properties, int j) {
		super(properties);
		this.analogOutput = i;
		this.sound = soundEvent;
		this.lengthInTicks = j * 20;
		BY_NAME.put(this.sound, this);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.is(Blocks.JUKEBOX) && !blockState.getValue(JukeboxBlock.HAS_RECORD)) {
			ItemStack itemStack = useOnContext.getItemInHand();
			if (!level.isClientSide) {
				Player player = useOnContext.getPlayer();
				BlockEntity var8 = level.getBlockEntity(blockPos);
				if (var8 instanceof JukeboxBlockEntity jukeboxBlockEntity) {
					jukeboxBlockEntity.setTheItem(itemStack.copy());
					level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState));
				}

				itemStack.shrink(1);
				if (player != null) {
					player.awardStat(Stats.PLAY_RECORD);
				}
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	public int getAnalogOutput() {
		return this.analogOutput;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
	}

	public MutableComponent getDisplayName() {
		return Component.translatable(this.getDescriptionId() + ".desc");
	}

	@Nullable
	public static RecordItem getBySound(SoundEvent soundEvent) {
		return (RecordItem)BY_NAME.get(soundEvent);
	}

	public SoundEvent getSound() {
		return this.sound;
	}

	public int getLengthInTicks() {
		return this.lengthInTicks;
	}
}

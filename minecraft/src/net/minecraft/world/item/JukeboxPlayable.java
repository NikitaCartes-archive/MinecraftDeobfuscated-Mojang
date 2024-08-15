package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public record JukeboxPlayable(EitherHolder<JukeboxSong> song, boolean showInTooltip) implements TooltipProvider {
	public static final Codec<JukeboxPlayable> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					EitherHolder.codec(Registries.JUKEBOX_SONG, JukeboxSong.CODEC).fieldOf("song").forGetter(JukeboxPlayable::song),
					Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(JukeboxPlayable::showInTooltip)
				)
				.apply(instance, JukeboxPlayable::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxPlayable> STREAM_CODEC = StreamCodec.composite(
		EitherHolder.streamCodec(Registries.JUKEBOX_SONG, JukeboxSong.STREAM_CODEC),
		JukeboxPlayable::song,
		ByteBufCodecs.BOOL,
		JukeboxPlayable::showInTooltip,
		JukeboxPlayable::new
	);

	@Override
	public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		HolderLookup.Provider provider = tooltipContext.registries();
		if (this.showInTooltip && provider != null) {
			this.song.unwrap(provider).ifPresent(holder -> {
				MutableComponent mutableComponent = ((JukeboxSong)holder.value()).description().copy();
				ComponentUtils.mergeStyles(mutableComponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
				consumer.accept(mutableComponent);
			});
		}
	}

	public JukeboxPlayable withTooltip(boolean bl) {
		return new JukeboxPlayable(this.song, bl);
	}

	public static InteractionResult tryInsertIntoJukebox(Level level, BlockPos blockPos, ItemStack itemStack, Player player) {
		JukeboxPlayable jukeboxPlayable = itemStack.get(DataComponents.JUKEBOX_PLAYABLE);
		if (jukeboxPlayable == null) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		} else {
			BlockState blockState = level.getBlockState(blockPos);
			if (blockState.is(Blocks.JUKEBOX) && !(Boolean)blockState.getValue(JukeboxBlock.HAS_RECORD)) {
				if (!level.isClientSide) {
					ItemStack itemStack2 = itemStack.consumeAndReturn(1, player);
					if (level.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity) {
						jukeboxBlockEntity.setTheItem(itemStack2);
						level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState));
					}

					player.awardStat(Stats.PLAY_RECORD);
				}

				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			}
		}
	}
}

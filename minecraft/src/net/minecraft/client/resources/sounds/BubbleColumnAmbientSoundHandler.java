package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class BubbleColumnAmbientSoundHandler implements AmbientSoundHandler {
	private final LocalPlayer player;
	private boolean wasInBubbleColumn;
	private boolean firstTick = true;

	public BubbleColumnAmbientSoundHandler(LocalPlayer localPlayer) {
		this.player = localPlayer;
	}

	@Override
	public void tick() {
		Level level = this.player.level;
		BlockState blockState = level.containsBlock(this.player.getBoundingBox().inflate(0.0, -0.4F, 0.0).deflate(0.001), Blocks.BUBBLE_COLUMN);
		if (blockState != null) {
			if (!this.wasInBubbleColumn && !this.firstTick && blockState.getBlock() == Blocks.BUBBLE_COLUMN && !this.player.isSpectator()) {
				boolean bl = (Boolean)blockState.getValue(BubbleColumnBlock.DRAG_DOWN);
				if (bl) {
					this.player.playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1.0F, 1.0F);
				} else {
					this.player.playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 1.0F);
				}
			}

			this.wasInBubbleColumn = true;
		} else {
			this.wasInBubbleColumn = false;
		}

		this.firstTick = false;
	}
}

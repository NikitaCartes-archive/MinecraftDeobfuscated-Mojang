package net.minecraft.server.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class DemoMode extends ServerPlayerGameMode {
	private boolean displayedIntro;
	private boolean demoHasEnded;
	private int demoEndedReminder;
	private int gameModeTicks;

	public DemoMode(ServerLevel serverLevel) {
		super(serverLevel);
	}

	@Override
	public void tick() {
		super.tick();
		this.gameModeTicks++;
		long l = this.level.getGameTime();
		long m = l / 24000L + 1L;
		if (!this.displayedIntro && this.gameModeTicks > 20) {
			this.displayedIntro = true;
			this.player.connection.send(new ClientboundGameEventPacket(5, 0.0F));
		}

		this.demoHasEnded = l > 120500L;
		if (this.demoHasEnded) {
			this.demoEndedReminder++;
		}

		if (l % 24000L == 500L) {
			if (m <= 6L) {
				if (m == 6L) {
					this.player.connection.send(new ClientboundGameEventPacket(5, 104.0F));
				} else {
					this.player.sendMessage(new TranslatableComponent("demo.day." + m));
				}
			}
		} else if (m == 1L) {
			if (l == 100L) {
				this.player.connection.send(new ClientboundGameEventPacket(5, 101.0F));
			} else if (l == 175L) {
				this.player.connection.send(new ClientboundGameEventPacket(5, 102.0F));
			} else if (l == 250L) {
				this.player.connection.send(new ClientboundGameEventPacket(5, 103.0F));
			}
		} else if (m == 5L && l % 24000L == 22000L) {
			this.player.sendMessage(new TranslatableComponent("demo.day.warning"));
		}
	}

	private void outputDemoReminder() {
		if (this.demoEndedReminder > 100) {
			this.player.sendMessage(new TranslatableComponent("demo.reminder"));
			this.demoEndedReminder = 0;
		}
	}

	@Override
	public void handleBlockBreakAction(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i) {
		if (this.demoHasEnded) {
			this.outputDemoReminder();
		} else {
			super.handleBlockBreakAction(blockPos, action, direction, i);
		}
	}

	@Override
	public InteractionResult useItem(Player player, Level level, ItemStack itemStack, InteractionHand interactionHand) {
		if (this.demoHasEnded) {
			this.outputDemoReminder();
			return InteractionResult.PASS;
		} else {
			return super.useItem(player, level, itemStack, interactionHand);
		}
	}

	@Override
	public InteractionResult useItemOn(Player player, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		if (this.demoHasEnded) {
			this.outputDemoReminder();
			return InteractionResult.PASS;
		} else {
			return super.useItemOn(player, level, itemStack, interactionHand, blockHitResult);
		}
	}
}

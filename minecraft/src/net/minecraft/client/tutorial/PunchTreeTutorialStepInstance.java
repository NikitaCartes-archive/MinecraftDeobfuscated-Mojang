package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class PunchTreeTutorialStepInstance implements TutorialStepInstance {
	private static final int HINT_DELAY = 600;
	private static final Component TITLE = Component.translatable("tutorial.punch_tree.title");
	private static final Component DESCRIPTION = Component.translatable("tutorial.punch_tree.description", Tutorial.key("attack"));
	private final Tutorial tutorial;
	@Nullable
	private TutorialToast toast;
	private int timeWaiting;
	private int resetCount;

	public PunchTreeTutorialStepInstance(Tutorial tutorial) {
		this.tutorial = tutorial;
	}

	@Override
	public void tick() {
		this.timeWaiting++;
		if (!this.tutorial.isSurvival()) {
			this.tutorial.setStep(TutorialSteps.NONE);
		} else {
			if (this.timeWaiting == 1) {
				LocalPlayer localPlayer = this.tutorial.getMinecraft().player;
				if (localPlayer != null) {
					if (localPlayer.getInventory().contains(ItemTags.LOGS)) {
						this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
						return;
					}

					if (FindTreeTutorialStepInstance.hasPunchedTreesPreviously(localPlayer)) {
						this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
						return;
					}
				}
			}

			if ((this.timeWaiting >= 600 || this.resetCount > 3) && this.toast == null) {
				this.toast = new TutorialToast(TutorialToast.Icons.TREE, TITLE, DESCRIPTION, true);
				this.tutorial.getMinecraft().getToastManager().addToast(this.toast);
			}
		}
	}

	@Override
	public void clear() {
		if (this.toast != null) {
			this.toast.hide();
			this.toast = null;
		}
	}

	@Override
	public void onDestroyBlock(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState, float f) {
		boolean bl = blockState.is(BlockTags.LOGS);
		if (bl && f > 0.0F) {
			if (this.toast != null) {
				this.toast.updateProgress(f);
			}

			if (f >= 1.0F) {
				this.tutorial.setStep(TutorialSteps.OPEN_INVENTORY);
			}
		} else if (this.toast != null) {
			this.toast.updateProgress(0.0F);
		} else if (bl) {
			this.resetCount++;
		}
	}

	@Override
	public void onGetItem(ItemStack itemStack) {
		if (itemStack.is(ItemTags.LOGS)) {
			this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
		}
	}
}

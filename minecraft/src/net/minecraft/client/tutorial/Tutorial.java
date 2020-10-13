package net.minecraft.client.tutorial;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class Tutorial {
	private final Minecraft minecraft;
	@Nullable
	private TutorialStepInstance instance;
	private List<Tutorial.TimedToast> timedToasts = Lists.<Tutorial.TimedToast>newArrayList();

	public Tutorial(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void onInput(Input input) {
		if (this.instance != null) {
			this.instance.onInput(input);
		}
	}

	public void onMouse(double d, double e) {
		if (this.instance != null) {
			this.instance.onMouse(d, e);
		}
	}

	public void onLookAt(@Nullable ClientLevel clientLevel, @Nullable HitResult hitResult) {
		if (this.instance != null && hitResult != null && clientLevel != null) {
			this.instance.onLookAt(clientLevel, hitResult);
		}
	}

	public void onDestroyBlock(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState, float f) {
		if (this.instance != null) {
			this.instance.onDestroyBlock(clientLevel, blockPos, blockState, f);
		}
	}

	public void onOpenInventory() {
		if (this.instance != null) {
			this.instance.onOpenInventory();
		}
	}

	public void onGetItem(ItemStack itemStack) {
		if (this.instance != null) {
			this.instance.onGetItem(itemStack);
		}
	}

	public void stop() {
		if (this.instance != null) {
			this.instance.clear();
			this.instance = null;
		}
	}

	public void start() {
		if (this.instance != null) {
			this.stop();
		}

		this.instance = this.minecraft.options.tutorialStep.create(this);
	}

	public void addTimedToast(TutorialToast tutorialToast, int i) {
		this.timedToasts.add(new Tutorial.TimedToast(tutorialToast, i));
		this.minecraft.getToasts().addToast(tutorialToast);
	}

	public void removeTimedToast(TutorialToast tutorialToast) {
		this.timedToasts.removeIf(timedToast -> timedToast.toast == tutorialToast);
		tutorialToast.hide();
	}

	public void tick() {
		this.timedToasts.removeIf(object -> ((Tutorial.TimedToast)object).updateProgress());
		if (this.instance != null) {
			if (this.minecraft.level != null) {
				this.instance.tick();
			} else {
				this.stop();
			}
		} else if (this.minecraft.level != null) {
			this.start();
		}
	}

	public void setStep(TutorialSteps tutorialSteps) {
		this.minecraft.options.tutorialStep = tutorialSteps;
		this.minecraft.options.save();
		if (this.instance != null) {
			this.instance.clear();
			this.instance = tutorialSteps.create(this);
		}
	}

	public Minecraft getMinecraft() {
		return this.minecraft;
	}

	public GameType getGameMode() {
		return this.minecraft.gameMode == null ? GameType.NOT_SET : this.minecraft.gameMode.getPlayerMode();
	}

	public static Component key(String string) {
		return new KeybindComponent("key." + string).withStyle(ChatFormatting.BOLD);
	}

	@Environment(EnvType.CLIENT)
	static final class TimedToast {
		private final TutorialToast toast;
		private final int durationTicks;
		private int progress;

		private TimedToast(TutorialToast tutorialToast, int i) {
			this.toast = tutorialToast;
			this.durationTicks = i;
		}

		private boolean updateProgress() {
			this.toast.updateProgress(Math.min((float)(++this.progress) / (float)this.durationTicks, 1.0F));
			if (this.progress > this.durationTicks) {
				this.toast.hide();
				return true;
			} else {
				return false;
			}
		}
	}
}

package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
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

	public void onLookAt(@Nullable MultiPlayerLevel multiPlayerLevel, @Nullable HitResult hitResult) {
		if (this.instance != null && hitResult != null && multiPlayerLevel != null) {
			this.instance.onLookAt(multiPlayerLevel, hitResult);
		}
	}

	public void onDestroyBlock(MultiPlayerLevel multiPlayerLevel, BlockPos blockPos, BlockState blockState, float f) {
		if (this.instance != null) {
			this.instance.onDestroyBlock(multiPlayerLevel, blockPos, blockState, f);
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

	public void tick() {
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
}

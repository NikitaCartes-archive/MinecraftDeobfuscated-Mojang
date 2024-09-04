package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class Tutorial {
	private final Minecraft minecraft;
	@Nullable
	private TutorialStepInstance instance;

	public Tutorial(Minecraft minecraft, Options options) {
		this.minecraft = minecraft;
	}

	public void onInput(ClientInput clientInput) {
		if (this.instance != null) {
			this.instance.onInput(clientInput);
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

	public boolean isSurvival() {
		return this.minecraft.gameMode == null ? false : this.minecraft.gameMode.getPlayerMode() == GameType.SURVIVAL;
	}

	public static Component key(String string) {
		return Component.keybind("key." + string).withStyle(ChatFormatting.BOLD);
	}

	public void onInventoryAction(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction) {
	}
}

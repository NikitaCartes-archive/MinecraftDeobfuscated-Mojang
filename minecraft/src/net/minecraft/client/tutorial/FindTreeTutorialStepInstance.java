package net.minecraft.client.tutorial;

import com.google.common.collect.Sets;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public class FindTreeTutorialStepInstance implements TutorialStepInstance {
	private static final int HINT_DELAY = 6000;
	private static final Set<Block> TREE_BLOCKS = Sets.<Block>newHashSet(
		Blocks.OAK_LOG,
		Blocks.SPRUCE_LOG,
		Blocks.BIRCH_LOG,
		Blocks.JUNGLE_LOG,
		Blocks.ACACIA_LOG,
		Blocks.DARK_OAK_LOG,
		Blocks.WARPED_STEM,
		Blocks.CRIMSON_STEM,
		Blocks.OAK_WOOD,
		Blocks.SPRUCE_WOOD,
		Blocks.BIRCH_WOOD,
		Blocks.JUNGLE_WOOD,
		Blocks.ACACIA_WOOD,
		Blocks.DARK_OAK_WOOD,
		Blocks.WARPED_HYPHAE,
		Blocks.CRIMSON_HYPHAE,
		Blocks.OAK_LEAVES,
		Blocks.SPRUCE_LEAVES,
		Blocks.BIRCH_LEAVES,
		Blocks.JUNGLE_LEAVES,
		Blocks.ACACIA_LEAVES,
		Blocks.DARK_OAK_LEAVES,
		Blocks.NETHER_WART_BLOCK,
		Blocks.WARPED_WART_BLOCK,
		Blocks.AZALEA_LEAVES,
		Blocks.FLOWERING_AZALEA_LEAVES
	);
	private static final Component TITLE = new TranslatableComponent("tutorial.find_tree.title");
	private static final Component DESCRIPTION = new TranslatableComponent("tutorial.find_tree.description");
	private final Tutorial tutorial;
	private TutorialToast toast;
	private int timeWaiting;

	public FindTreeTutorialStepInstance(Tutorial tutorial) {
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
					for (Block block : TREE_BLOCKS) {
						if (localPlayer.getInventory().contains(new ItemStack(block))) {
							this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
							return;
						}
					}

					if (hasPunchedTreesPreviously(localPlayer)) {
						this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
						return;
					}
				}
			}

			if (this.timeWaiting >= 6000 && this.toast == null) {
				this.toast = new TutorialToast(TutorialToast.Icons.TREE, TITLE, DESCRIPTION, false);
				this.tutorial.getMinecraft().getToasts().addToast(this.toast);
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
	public void onLookAt(ClientLevel clientLevel, HitResult hitResult) {
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockState blockState = clientLevel.getBlockState(((BlockHitResult)hitResult).getBlockPos());
			if (TREE_BLOCKS.contains(blockState.getBlock())) {
				this.tutorial.setStep(TutorialSteps.PUNCH_TREE);
			}
		}
	}

	@Override
	public void onGetItem(ItemStack itemStack) {
		for (Block block : TREE_BLOCKS) {
			if (itemStack.is(block.asItem())) {
				this.tutorial.setStep(TutorialSteps.CRAFT_PLANKS);
				return;
			}
		}
	}

	public static boolean hasPunchedTreesPreviously(LocalPlayer localPlayer) {
		for (Block block : TREE_BLOCKS) {
			if (localPlayer.getStats().getValue(Stats.BLOCK_MINED.get(block)) > 0) {
				return true;
			}
		}

		return false;
	}
}

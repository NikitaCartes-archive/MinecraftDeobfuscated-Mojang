package net.minecraft.client.gui.screens.voting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.voting.ClientVoteStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

@Environment(EnvType.CLIENT)
public class VoteSelectionEntry extends ObjectSelectionList.Entry<VoteSelectionEntry> {
	public static final Comparator<VoteSelectionEntry> BY_ID = Comparator.comparing(voteSelectionEntry -> voteSelectionEntry.voteId);
	public static final Comparator<VoteSelectionEntry> BY_TIME = Comparator.comparing(VoteSelectionEntry::getLeftoverTime);
	public static final Comparator<VoteSelectionEntry> BY_NAME = Comparator.comparing(voteSelectionEntry -> voteSelectionEntry.sortingKey);
	public static final Comparator<VoteSelectionEntry> BY_ENABLED = Comparator.comparing(voteSelectionEntry -> voteSelectionEntry.isEnabled ? 0 : 1);
	public static final Comparator<VoteSelectionEntry> BY_ENABLED_AND_NAME_AND_ID = BY_ENABLED.thenComparing(BY_NAME).thenComparing(BY_ID);
	private final Minecraft minecraft;
	private final VoteSelectionScreen parentScreen;
	private final UUID voteId;
	private final ClientVoteStorage voteStorage;
	@Nullable
	private ClientVoteStorage.VoteInfo voteInfo;
	private final boolean isEnabled;
	@Nullable
	private final Tooltip tooltip;
	private final MultiLineLabel title;
	private final String sortingKey;

	public VoteSelectionEntry(
		Minecraft minecraft,
		VoteSelectionScreen voteSelectionScreen,
		boolean bl,
		int i,
		UUID uUID,
		ClientVoteStorage clientVoteStorage,
		ClientVoteStorage.VoteInfo voteInfo,
		@Nullable Tooltip tooltip
	) {
		this.minecraft = minecraft;
		this.parentScreen = voteSelectionScreen;
		this.voteId = uUID;
		this.voteStorage = clientVoteStorage;
		this.voteInfo = voteInfo;
		this.isEnabled = bl;
		this.tooltip = tooltip;
		this.sortingKey = voteInfo.title().getString();
		String string = this.getFormattedLeftoverTime();
		int j = minecraft.font.width(string);
		int k = i - j - 8;
		this.title = MultiLineLabel.create(minecraft.font, voteInfo.title(), k);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
		RenderSystem.setShaderTexture(0, VoteSelectionScreen.BACKGROUND);
		int p;
		if (!this.isEnabled || this.voteInfo == null) {
			p = 66;
		} else if (!this.isFocused() && !bl) {
			p = 0;
		} else {
			p = 33;
		}

		GuiComponent.blit(poseStack, k, j, 0.0F, (float)(36 + p), 220, 33, 256, 256);
		int q = k + 4;
		int r = j + 4;
		this.title.renderLeftAligned(poseStack, q, r, 9, -1);
		String string = this.getFormattedLeftoverTime();
		int s = this.minecraft.font.width(string);
		this.minecraft.font.draw(poseStack, string, (float)(q + (l - s - 8)), (float)r, -1);
		if (bl && this.tooltip != null) {
			this.parentScreen.setTooltipForNextRenderPass(this.tooltip.toCharSequence(this.minecraft));
		}
	}

	private String getFormattedLeftoverTime() {
		return StringUtil.formatTickDuration(this.getLeftoverTime());
	}

	private long getLeftoverTime() {
		if (this.voteInfo == null) {
			return 0L;
		} else {
			long l = this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;
			return Math.max(0L, this.voteInfo.leftoverTicks(l));
		}
	}

	@Override
	public Component getNarration() {
		return Component.literal("blah");
	}

	public boolean refreshEntry() {
		this.voteInfo = this.voteStorage.getVote(this.voteId);
		return this.voteInfo != null;
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (i == 0) {
			if (this.voteInfo != null) {
				this.minecraft
					.setScreen(new VotingScreen(this.minecraft.player.inventoryMenu, this.minecraft.player.getInventory(), this.voteId, this.voteStorage, this.voteInfo));
			}

			return true;
		} else {
			return super.mouseClicked(d, e, i);
		}
	}
}

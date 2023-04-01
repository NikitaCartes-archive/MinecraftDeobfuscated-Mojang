package net.minecraft.client.gui.screens.voting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.voting.ClientVoteStorage;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class VoteSelectionList extends ObjectSelectionList<VoteSelectionEntry> {
	public static final int SUPER_MAGICAL_OFFSET_DONT_CARE = 4;
	private static final Tooltip TOOLTIP = Tooltip.create(Component.translatable("vote.no_more_votes"));

	public VoteSelectionList(ClientVoteStorage clientVoteStorage, VoteSelectionScreen voteSelectionScreen, Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
		this.setRenderSelection(false);
		this.setRenderBackground(false);
		this.setRenderTopAndBottom(false);
		UUID uUID = minecraft.player.getUUID();
		List<VoteSelectionEntry> list = new ArrayList();
		clientVoteStorage.visitVotes((uUID2, voteInfo) -> {
			boolean bl = voteInfo.hasAnyVotesLeft(uUID);
			list.add(new VoteSelectionEntry(minecraft, voteSelectionScreen, bl, this.getRowWidth(), uUID2, clientVoteStorage, voteInfo, bl ? null : TOOLTIP));
		});
		list.sort(VoteSelectionEntry.BY_ENABLED_AND_NAME_AND_ID);
		list.forEach(entry -> this.addEntry(entry));
	}

	public boolean refreshEntries() {
		Iterator<VoteSelectionEntry> iterator = this.children().iterator();

		while (iterator.hasNext()) {
			VoteSelectionEntry voteSelectionEntry = (VoteSelectionEntry)iterator.next();
			if (!voteSelectionEntry.refreshEntry()) {
				iterator.remove();
			}
		}

		return !this.children().isEmpty();
	}

	@Override
	protected void enableScissor() {
		enableScissor(this.x0, this.y0 + 4, this.x1, this.y1);
	}
}

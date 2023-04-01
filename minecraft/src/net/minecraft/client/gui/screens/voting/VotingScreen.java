package net.minecraft.client.gui.screens.voting;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.DataFixUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.voting.ClientVoteStorage;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.voting.votes.ClientVote;
import net.minecraft.voting.votes.OptionId;
import net.minecraft.voting.votes.VotingMaterial;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class VotingScreen extends AbstractContainerScreen<VotingScreen.FakeAndTerrible> implements VoteListenerScreen {
	protected static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/voting.png");
	private static final int PROPOSAL_WIDTH = 205;
	private static final int PROPOSAL_HEIGHT = 68;
	public static final Component VOTED_YAY = Component.translatable("vote.voted").withStyle(ChatFormatting.GREEN);
	public static final Component NO_MORE_VOTES = Component.translatable("vote.no_more_votes");
	private final UUID voteId;
	private final ClientVoteStorage voteStorage;
	private ClientVoteStorage.VoteInfo voteInfo;
	final List<VotingScreen.OptionDisplayInfo> options;
	private final UUID voterId;
	int optionIndex;
	private Component title = Component.empty();
	Component status = Component.empty();
	private MultiLineLabel proposal = MultiLineLabel.EMPTY;
	@Nullable
	VotingScreen.OptionDisplayInfo currentVote;
	@Nullable
	private ClientVoteStorage.CountAndLimit proposalCount;
	@Nullable
	private ClientVoteStorage.CountAndLimit optionCount;
	private VotingScreen.OptionSelectButton prevButton;
	private VotingScreen.OptionSelectButton nextButton;
	private VotingScreen.VoteButton voteButton;

	protected VotingScreen(InventoryMenu inventoryMenu, Inventory inventory, UUID uUID, ClientVoteStorage clientVoteStorage, ClientVoteStorage.VoteInfo voteInfo) {
		super(new VotingScreen.FakeAndTerrible(inventoryMenu), inventory, Component.translatable("gui.voting.title"));
		this.voteId = uUID;
		this.voteStorage = clientVoteStorage;
		this.voteInfo = voteInfo;
		this.voterId = inventory.player.getUUID();
		this.imageWidth = 231;
		this.imageHeight = 219;
		ClientVote clientVote = voteInfo.voteInfo();
		int i = clientVote.options().size();
		this.options = Streams.<Entry, VotingScreen.OptionDisplayInfo>mapWithIndex(
				clientVote.options().entrySet().stream().sorted(Entry.comparingByKey(OptionId.COMPARATOR)), (entry, l) -> {
					Component component = Component.translatable("vote.option_vote_title", clientVote.header().displayName(), l + 1L, i);
					return new VotingScreen.OptionDisplayInfo((OptionId)entry.getKey(), component, (ClientVote.ClientOption)entry.getValue());
				}
			)
			.toList();
	}

	@Override
	protected void init() {
		super.init();
		this.prevButton = this.addRenderableWidget(
			new VotingScreen.OptionSelectButton(this.leftPos + 9, this.topPos + 4, Component.translatable("gui.voting.prev"), 0) {
				@Override
				public void onPress() {
					if (VotingScreen.this.optionIndex > 0) {
						VotingScreen.this.optionIndex--;
					}

					VotingScreen.this.onProposalChange();
				}
			}
		);
		this.nextButton = this.addRenderableWidget(
			new VotingScreen.OptionSelectButton(this.leftPos + 205, this.topPos + 4, Component.translatable("gui.voting.next"), 32) {
				@Override
				public void onPress() {
					if (VotingScreen.this.optionIndex < VotingScreen.this.options.size() - 1) {
						VotingScreen.this.optionIndex++;
					}

					VotingScreen.this.onProposalChange();
				}
			}
		);
		this.voteButton = this.addRenderableWidget(
			new VotingScreen.VoteButton(this.leftPos + 26, this.topPos + 106, Component.translatable("gui.voting.do_it"), this.font) {
				int lastTransaction;

				@Override
				public void onPress() {
					if (VotingScreen.this.currentVote != null) {
						OptionId optionId = VotingScreen.this.currentVote.id;
						this.lastTransaction = VotingScreen.this.minecraft.player.connection.voteFor(optionId, (i, optional) -> {
							if (i == this.lastTransaction) {
								VotingScreen.this.status = DataFixUtils.orElse(optional.map(component -> component.copy().withStyle(ChatFormatting.RED)), VotingScreen.VOTED_YAY);
							}
						});
					}
				}
			}
		);
		this.onProposalChange();
	}

	void onProposalChange() {
		this.currentVote = (VotingScreen.OptionDisplayInfo)this.options.get(this.optionIndex);
		this.prevButton.active = this.optionIndex > 0;
		this.nextButton.active = this.optionIndex < this.options.size() - 1;
		boolean bl = this.updateVoteButton(this.currentVote.id);
		this.status = (Component)(bl ? Component.empty() : NO_MORE_VOTES);
		this.title = this.currentVote.display;
		this.proposal = MultiLineLabel.create(this.font, this.currentVote.data().displayName(), 205);
	}

	@Override
	public void onVotesChanged() {
		ClientVoteStorage.VoteInfo voteInfo = this.voteStorage.getVote(this.voteId);
		if (voteInfo == null) {
			this.onClose();
		} else {
			this.voteInfo = voteInfo;
			if (this.currentVote != null) {
				this.updateVoteButton(this.currentVote.id);
			}
		}
	}

	private boolean updateVoteButton(OptionId optionId) {
		this.proposalCount = this.voteInfo.proposalVoteCount(this.voterId);
		this.optionCount = this.voteInfo.optionVoteCount(this.voterId, optionId);
		boolean bl = this.proposalCount.canVote() && this.optionCount.canVote();
		this.voteButton.active = bl;
		this.voteButton.setTooltip(this.createTooltip(this.voteInfo.voteInfo().header().cost()));
		return bl;
	}

	private Tooltip createTooltip(List<VotingMaterial.Cost> list) {
		List<Component> list2 = new ArrayList();
		this.addCount(list2, this.optionCount, "vote.count_per_option.limit", "vote.count_per_option.no_limit");
		this.addCount(list2, this.proposalCount, "vote.count_per_proposal.limit", "vote.count_per_proposal.no_limit");
		List<Component> list3 = new ArrayList();

		for (VotingMaterial.Cost cost : list) {
			switch (cost.material().type()) {
				case PER_OPTION:
				case PER_PROPOSAL:
					break;
				default:
					list3.add(Component.literal("- ").append(cost.display(false)));
			}
		}

		if (!list3.isEmpty()) {
			list2.add(Component.translatable("vote.cost"));
			list2.addAll(list3);
		}

		return Tooltip.create(CommonComponents.joinLines(list2));
	}

	private void addCount(List<Component> list, @Nullable ClientVoteStorage.CountAndLimit countAndLimit, String string, String string2) {
		if (countAndLimit != null) {
			if (countAndLimit.limit().isPresent()) {
				list.add(Component.translatable(string, countAndLimit.count(), countAndLimit.limit().getAsInt()));
			} else {
				list.add(Component.translatable(string2, countAndLimit.count()));
			}
		}
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int i, int j) {
		int k = this.font.width(this.title);
		this.font.drawShadow(poseStack, this.title, 26.0F + (float)(180 - k) / 2.0F, 8.0F, -1);
		this.font.drawShadow(poseStack, this.status, 118.0F, 110.0F, -1);
	}

	@Override
	protected void renderBg(PoseStack poseStack, float f, int i, int j) {
		RenderSystem.setShaderTexture(0, BACKGROUND);
		int k = (this.width - this.imageWidth) / 2;
		int l = (this.height - this.imageHeight) / 2;
		blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		super.render(poseStack, i, j, f);
		int k = this.proposal.getLineCount() * 9;
		this.proposal.renderCentered(poseStack, this.leftPos + 13 + 102, this.topPos + 27 + (68 - k) / 2);
		this.renderTooltip(poseStack, i, j);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(new VoteSelectionScreen());
	}

	@Nullable
	@Override
	protected Slot findSlot(double d, double e) {
		Slot slot = super.findSlot(d, e);
		return this.unwrapSlot(slot);
	}

	@Override
	protected void setHoveredSlot(@Nullable Slot slot) {
		super.setHoveredSlot(this.unwrapSlot(slot));
	}

	@Nullable
	private Slot unwrapSlot(@Nullable Slot slot) {
		return slot instanceof CreativeModeInventoryScreen.SlotWrapper slotWrapper ? slotWrapper.getTarget() : slot;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public static class FakeAndTerrible extends AbstractContainerMenu {
		private static final int NUM_COLS = 9;
		public final NonNullList<ItemStack> items = NonNullList.create();
		private final AbstractContainerMenu inventoryMenu;

		public FakeAndTerrible(InventoryMenu inventoryMenu) {
			super(null, 0);
			this.inventoryMenu = inventoryMenu;

			for (int i = 0; i < inventoryMenu.slots.size(); i++) {
				int j;
				int k;
				if ((i < 5 || i >= 9) && (i < 0 || i >= 5) && i != 45) {
					int l = i - 9;
					int m = l % 9;
					int n = l / 9;
					j = 36 + m * 18;
					if (i >= 36) {
						k = 195;
					} else {
						k = 137 + n * 18;
					}
				} else {
					j = -2000;
					k = -2000;
				}

				Slot slot = new CreativeModeInventoryScreen.SlotWrapper(inventoryMenu.slots.get(i), i, j, k);
				this.slots.add(slot);
			}
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}

		@Override
		public ItemStack quickMoveStack(Player player, int i) {
			if (i >= this.slots.size() - 9 && i < this.slots.size()) {
				Slot slot = this.slots.get(i);
				if (slot != null && slot.hasItem()) {
					slot.setByPlayer(ItemStack.EMPTY);
				}
			}

			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack getCarried() {
			return this.inventoryMenu.getCarried();
		}

		@Override
		public void setCarried(ItemStack itemStack) {
			this.inventoryMenu.setCarried(itemStack);
		}
	}

	@Environment(EnvType.CLIENT)
	static record OptionDisplayInfo(OptionId id, Component display, ClientVote.ClientOption data) {
	}

	@Environment(EnvType.CLIENT)
	abstract static class OptionSelectButton extends AbstractButton {
		private final int offsetY;

		protected OptionSelectButton(int i, int j, Component component, int k) {
			super(i, j, 16, 16, component);
			this.offsetY = k;
		}

		@Override
		public void renderWidget(PoseStack poseStack, int i, int j, float f) {
			RenderSystem.setShaderTexture(0, VotingScreen.BACKGROUND);
			int k = 231;
			if (!this.active) {
				blit(poseStack, this.getX(), this.getY(), 231, this.offsetY + this.width, this.width, this.height);
			} else if (this.isHoveredOrFocused()) {
				blit(poseStack, this.getX(), this.getY(), 231, this.offsetY, this.width, this.height);
			}
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class VoteButton extends AbstractButton {
		protected VoteButton(int i, int j, Component component, Font font) {
			super(i, j, 89, 22, component);
		}

		@Override
		public void renderWidget(PoseStack poseStack, int i, int j, float f) {
			RenderSystem.setShaderTexture(0, VotingScreen.BACKGROUND);
			int k = 219;
			if (!this.active) {
				int l = 0;
				blit(poseStack, this.getX(), this.getY(), 0, k, this.width, this.height);
			} else if (this.isHoveredOrFocused()) {
				int l = 89;
				blit(poseStack, this.getX(), this.getY(), 89, k, this.width, this.height);
			}
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			this.defaultButtonNarrationText(narrationElementOutput);
		}
	}
}

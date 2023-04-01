package net.minecraft.client.gui.screens.voting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SplitLineEntry;
import net.minecraft.client.gui.screens.SuperSimpleTextScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleAction;

@Environment(EnvType.CLIENT)
public class VoteSelectionScreen extends Screen implements VoteListenerScreen {
	protected static final ResourceLocation BACKGROUND = new ResourceLocation("textures/gui/votes.png");
	private static final int BG_BORDER_SIZE = 8;
	private static final int BG_WIDTH = 236;
	private static final int MARGIN_Y = 64;
	public static final int LIST_START = 72;
	private static final int IMAGE_WIDTH = 238;
	private static final int ITEM_HEIGHT = 36;
	private static final Component SHOW_RULES_TEXT = Component.translatable("vote.show_current_rules");
	private static final Component RULE_SCREEN_TITLE = Component.translatable("vote.current_rules");
	private VoteSelectionList voteSelectionList;

	public VoteSelectionScreen() {
		super(Component.translatable("gui.pending_votes.title"));
	}

	private int windowHeight() {
		return Math.max(36, this.height - 128 - 16);
	}

	private int listEnd() {
		return 80 + this.windowHeight() - 8;
	}

	private int marginX() {
		return (this.width - 238) / 2;
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	protected void init() {
		this.voteSelectionList = new VoteSelectionList(
			this.minecraft.player.connection.getVoteStorage(), this, this.minecraft, this.width, this.height, 68, this.listEnd(), 33
		);
		this.addWidget(this.voteSelectionList);
		int i = this.marginX() + 3;
		int j = this.listEnd() + 8 + 4;
		this.addRenderableWidget(
			Button.builder(
					SHOW_RULES_TEXT,
					button -> {
						Stream<Component> stream = BuiltInRegistries.RULE
							.holders()
							.sorted(Comparator.comparing(reference -> reference.key().location()))
							.flatMap(reference -> ((Rule)reference.value()).approvedChanges())
							.map(ruleChange -> ruleChange.description(RuleAction.APPROVE));
						this.minecraft.setScreen(new SuperSimpleTextScreen(RULE_SCREEN_TITLE, this, SplitLineEntry.splitToWidth(this.minecraft.font, stream, 320).toList()));
					}
				)
				.bounds(i, j, 236, 20)
				.build()
		);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(i, j + 20 + 2, 236, 20).build());
		if (!this.minecraft.options.hasSeenVotingScreen) {
			this.minecraft.options.hasSeenVotingScreen = true;
			this.minecraft.options.save();
		}
	}

	@Override
	public void renderBackground(PoseStack poseStack) {
		int i = this.marginX() + 3;
		super.renderBackground(poseStack);
		RenderSystem.setShaderTexture(0, BACKGROUND);
		blitNineSliced(poseStack, i, 64, 236, this.windowHeight() + 16, 8, 236, 34, 1, 1);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.voteSelectionList.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return super.mouseClicked(d, e, i);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.minecraft.options.keySocialInteractions.matches(i, j)) {
			this.minecraft.setScreen(null);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void onVotesChanged() {
		if (!this.voteSelectionList.refreshEntries()) {
			this.onClose();
		}
	}
}

package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class SuperSimpleTextScreen extends Screen {
	private static final int FOOTER_HEIGHT = 40;
	public static final int CONTENT_WIDTH = 320;
	private static final int PADDING = 8;
	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final MutableComponent CHAT_COPY = Component.translatable("chat.copy");
	private final Screen parent;
	private final List<SplitLineEntry> lines;
	private SuperSimpleTextScreen.SimpleTextList theList;

	public SuperSimpleTextScreen(Component component, Screen screen, List<SplitLineEntry> list) {
		super(component);
		this.parent = screen;
		this.lines = list;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	@Override
	protected void init() {
		this.theList = new SuperSimpleTextScreen.SimpleTextList(this.minecraft, this.lines);
		this.theList.setRenderBackground(false);
		this.addWidget(this.theList);
		int i = this.width / 2 - 150 - 5;
		int j = this.width / 2 + 5;
		int k = this.height - 20 - 8;
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(j, k, 150, 20).build());
		this.addRenderableWidget(
			Button.builder(
					CHAT_COPY,
					button -> {
						String string = (String)this.lines
							.stream()
							.filter(splitLineEntry -> splitLineEntry.index() == 0L)
							.map(splitLineEntry -> splitLineEntry.original().getString())
							.collect(Collectors.joining("\n"));
						this.minecraft.keyboardHandler.setClipboard(string);
					}
				)
				.bounds(i, k, 150, 20)
				.build()
		);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.theList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
		super.render(poseStack, i, j, f);
	}

	@Environment(EnvType.CLIENT)
	public class SimpleTextList extends ObjectSelectionList<SuperSimpleTextScreen.SimpleTextList.Entry> {
		public SimpleTextList(Minecraft minecraft, List<SplitLineEntry> list) {
			super(minecraft, SuperSimpleTextScreen.this.width, SuperSimpleTextScreen.this.height, 40, SuperSimpleTextScreen.this.height - 40, 18);

			for (SplitLineEntry splitLineEntry : list) {
				this.addEntry(new SuperSimpleTextScreen.SimpleTextList.Entry(splitLineEntry));
			}
		}

		@Override
		public int getRowWidth() {
			return 320;
		}

		@Override
		protected int getScrollbarPosition() {
			return this.getRowRight() - 2;
		}

		@Environment(EnvType.CLIENT)
		public class Entry extends ObjectSelectionList.Entry<SuperSimpleTextScreen.SimpleTextList.Entry> {
			private final SplitLineEntry line;

			public Entry(SplitLineEntry splitLineEntry) {
				this.line = splitLineEntry;
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				int p = k + 1 + (this.line.index() > 0L ? 16 : 0);
				int q = j + (m - 9) / 2 + 1;
				GuiComponent.drawString(poseStack, SuperSimpleTextScreen.this.font, this.line.contents(), p, q, -1);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", this.line.original());
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					SimpleTextList.this.setSelected(this);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}

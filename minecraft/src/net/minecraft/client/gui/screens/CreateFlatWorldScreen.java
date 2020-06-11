package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

@Environment(EnvType.CLIENT)
public class CreateFlatWorldScreen extends Screen {
	private final Screen parent;
	private final Consumer<FlatLevelGeneratorSettings> applySettings;
	private FlatLevelGeneratorSettings generator;
	private Component columnType;
	private Component columnHeight;
	private CreateFlatWorldScreen.DetailsList list;
	private Button deleteLayerButton;

	public CreateFlatWorldScreen(Screen screen, Consumer<FlatLevelGeneratorSettings> consumer, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		super(new TranslatableComponent("createWorld.customize.flat.title"));
		this.parent = screen;
		this.applySettings = consumer;
		this.generator = flatLevelGeneratorSettings;
	}

	public FlatLevelGeneratorSettings settings() {
		return this.generator;
	}

	public void setConfig(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		this.generator = flatLevelGeneratorSettings;
	}

	@Override
	protected void init() {
		this.columnType = new TranslatableComponent("createWorld.customize.flat.tile");
		this.columnHeight = new TranslatableComponent("createWorld.customize.flat.height");
		this.list = new CreateFlatWorldScreen.DetailsList();
		this.children.add(this.list);
		this.deleteLayerButton = this.addButton(
			new Button(this.width / 2 - 155, this.height - 52, 150, 20, new TranslatableComponent("createWorld.customize.flat.removeLayer"), button -> {
				if (this.hasValidSelection()) {
					List<FlatLayerInfo> list = this.generator.getLayersInfo();
					int i = this.list.children().indexOf(this.list.getSelected());
					int j = list.size() - i - 1;
					list.remove(j);
					this.list.setSelected(list.isEmpty() ? null : (CreateFlatWorldScreen.DetailsList.Entry)this.list.children().get(Math.min(i, list.size() - 1)));
					this.generator.updateLayers();
					this.updateButtonValidity();
				}
			})
		);
		this.addButton(new Button(this.width / 2 + 5, this.height - 52, 150, 20, new TranslatableComponent("createWorld.customize.presets"), button -> {
			this.minecraft.setScreen(new PresetFlatWorldScreen(this));
			this.generator.updateLayers();
			this.updateButtonValidity();
		}));
		this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, button -> {
			this.applySettings.accept(this.generator);
			this.minecraft.setScreen(this.parent);
			this.generator.updateLayers();
			this.updateButtonValidity();
		}));
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> {
			this.minecraft.setScreen(this.parent);
			this.generator.updateLayers();
			this.updateButtonValidity();
		}));
		this.generator.updateLayers();
		this.updateButtonValidity();
	}

	public void updateButtonValidity() {
		this.deleteLayerButton.active = this.hasValidSelection();
		this.list.resetRows();
	}

	private boolean hasValidSelection() {
		return this.list.getSelected() != null;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.list.render(poseStack, i, j, f);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
		int k = this.width / 2 - 92 - 16;
		this.drawString(poseStack, this.font, this.columnType, k, 32, 16777215);
		this.drawString(poseStack, this.font, this.columnHeight, k + 2 + 213 - this.font.width(this.columnHeight), 32, 16777215);
		super.render(poseStack, i, j, f);
	}

	@Environment(EnvType.CLIENT)
	class DetailsList extends ObjectSelectionList<CreateFlatWorldScreen.DetailsList.Entry> {
		public DetailsList() {
			super(
				CreateFlatWorldScreen.this.minecraft, CreateFlatWorldScreen.this.width, CreateFlatWorldScreen.this.height, 43, CreateFlatWorldScreen.this.height - 60, 24
			);

			for (int i = 0; i < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); i++) {
				this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
			}
		}

		public void setSelected(@Nullable CreateFlatWorldScreen.DetailsList.Entry entry) {
			super.setSelected(entry);
			if (entry != null) {
				FlatLayerInfo flatLayerInfo = (FlatLayerInfo)CreateFlatWorldScreen.this.generator
					.getLayersInfo()
					.get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - this.children().indexOf(entry) - 1);
				Item item = flatLayerInfo.getBlockState().getBlock().asItem();
				if (item != Items.AIR) {
					NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", item.getName(new ItemStack(item))).getString());
				}
			}
		}

		@Override
		protected void moveSelection(AbstractSelectionList.SelectionDirection selectionDirection) {
			super.moveSelection(selectionDirection);
			CreateFlatWorldScreen.this.updateButtonValidity();
		}

		@Override
		protected boolean isFocused() {
			return CreateFlatWorldScreen.this.getFocused() == this;
		}

		@Override
		protected int getScrollbarPosition() {
			return this.width - 70;
		}

		public void resetRows() {
			int i = this.children().indexOf(this.getSelected());
			this.clearEntries();

			for (int j = 0; j < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); j++) {
				this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
			}

			List<CreateFlatWorldScreen.DetailsList.Entry> list = this.children();
			if (i >= 0 && i < list.size()) {
				this.setSelected((CreateFlatWorldScreen.DetailsList.Entry)list.get(i));
			}
		}

		@Environment(EnvType.CLIENT)
		class Entry extends ObjectSelectionList.Entry<CreateFlatWorldScreen.DetailsList.Entry> {
			private Entry() {
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				FlatLayerInfo flatLayerInfo = (FlatLayerInfo)CreateFlatWorldScreen.this.generator
					.getLayersInfo()
					.get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - i - 1);
				BlockState blockState = flatLayerInfo.getBlockState();
				Item item = blockState.getBlock().asItem();
				if (item == Items.AIR) {
					if (blockState.is(Blocks.WATER)) {
						item = Items.WATER_BUCKET;
					} else if (blockState.is(Blocks.LAVA)) {
						item = Items.LAVA_BUCKET;
					}
				}

				ItemStack itemStack = new ItemStack(item);
				this.blitSlot(poseStack, k, j, itemStack);
				CreateFlatWorldScreen.this.font.draw(poseStack, item.getName(itemStack), (float)(k + 18 + 5), (float)(j + 3), 16777215);
				String string;
				if (i == 0) {
					string = I18n.get("createWorld.customize.flat.layer.top", flatLayerInfo.getHeight());
				} else if (i == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1) {
					string = I18n.get("createWorld.customize.flat.layer.bottom", flatLayerInfo.getHeight());
				} else {
					string = I18n.get("createWorld.customize.flat.layer", flatLayerInfo.getHeight());
				}

				CreateFlatWorldScreen.this.font.draw(poseStack, string, (float)(k + 2 + 213 - CreateFlatWorldScreen.this.font.width(string)), (float)(j + 3), 16777215);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					DetailsList.this.setSelected(this);
					CreateFlatWorldScreen.this.updateButtonValidity();
					return true;
				} else {
					return false;
				}
			}

			private void blitSlot(PoseStack poseStack, int i, int j, ItemStack itemStack) {
				this.blitSlotBg(poseStack, i + 1, j + 1);
				RenderSystem.enableRescaleNormal();
				if (!itemStack.isEmpty()) {
					CreateFlatWorldScreen.this.itemRenderer.renderGuiItem(itemStack, i + 2, j + 2);
				}

				RenderSystem.disableRescaleNormal();
			}

			private void blitSlotBg(PoseStack poseStack, int i, int j) {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				DetailsList.this.minecraft.getTextureManager().bind(GuiComponent.STATS_ICON_LOCATION);
				GuiComponent.blit(poseStack, i, j, CreateFlatWorldScreen.this.getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
			}
		}
	}
}

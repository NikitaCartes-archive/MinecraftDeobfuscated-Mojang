package net.minecraft.client.gui.screens;

import com.ibm.icu.text.Collator;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

@Environment(EnvType.CLIENT)
public class CreateBuffetWorldScreen extends Screen {
	private static final Component BIOME_SELECT_INFO = new TranslatableComponent("createWorld.customize.buffet.biome");
	private final Screen parent;
	private final Consumer<Holder<Biome>> applySettings;
	final Registry<Biome> biomes;
	private CreateBuffetWorldScreen.BiomeList list;
	Holder<Biome> biome;
	private Button doneButton;

	public CreateBuffetWorldScreen(Screen screen, WorldCreationContext worldCreationContext, Consumer<Holder<Biome>> consumer) {
		super(new TranslatableComponent("createWorld.customize.buffet.title"));
		this.parent = screen;
		this.applySettings = consumer;
		this.biomes = worldCreationContext.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		Holder<Biome> holder = (Holder<Biome>)this.biomes.getHolder(Biomes.PLAINS).or(() -> this.biomes.holders().findAny()).orElseThrow();
		this.biome = (Holder<Biome>)worldCreationContext.worldGenSettings().overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(holder);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.list = new CreateBuffetWorldScreen.BiomeList();
		this.addWidget(this.list);
		this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, button -> {
			this.applySettings.accept(this.biome);
			this.minecraft.setScreen(this.parent);
		}));
		this.addRenderableWidget(
			new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent))
		);
		this.list
			.setSelected(
				(CreateBuffetWorldScreen.BiomeList.Entry)this.list.children().stream().filter(entry -> Objects.equals(entry.biome, this.biome)).findFirst().orElse(null)
			);
	}

	void updateButtonValidity() {
		this.doneButton.active = this.list.getSelected() != null;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(0);
		this.list.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
		drawCenteredString(poseStack, this.font, BIOME_SELECT_INFO, this.width / 2, 28, 10526880);
		super.render(poseStack, i, j, f);
	}

	@Environment(EnvType.CLIENT)
	class BiomeList extends ObjectSelectionList<CreateBuffetWorldScreen.BiomeList.Entry> {
		BiomeList() {
			super(
				CreateBuffetWorldScreen.this.minecraft,
				CreateBuffetWorldScreen.this.width,
				CreateBuffetWorldScreen.this.height,
				40,
				CreateBuffetWorldScreen.this.height - 37,
				16
			);
			Collator collator = Collator.getInstance(Locale.getDefault());
			CreateBuffetWorldScreen.this.biomes
				.holders()
				.map(reference -> new CreateBuffetWorldScreen.BiomeList.Entry(reference))
				.sorted(Comparator.comparing(entry -> entry.name.getString(), collator))
				.forEach(entry -> this.addEntry(entry));
		}

		@Override
		protected boolean isFocused() {
			return CreateBuffetWorldScreen.this.getFocused() == this;
		}

		public void setSelected(@Nullable CreateBuffetWorldScreen.BiomeList.Entry entry) {
			super.setSelected(entry);
			if (entry != null) {
				CreateBuffetWorldScreen.this.biome = entry.biome;
			}

			CreateBuffetWorldScreen.this.updateButtonValidity();
		}

		@Environment(EnvType.CLIENT)
		class Entry extends ObjectSelectionList.Entry<CreateBuffetWorldScreen.BiomeList.Entry> {
			final Holder.Reference<Biome> biome;
			final Component name;

			public Entry(Holder.Reference<Biome> reference) {
				this.biome = reference;
				ResourceLocation resourceLocation = reference.key().location();
				String string = resourceLocation.toLanguageKey("biome");
				if (Language.getInstance().has(string)) {
					this.name = new TranslatableComponent(string);
				} else {
					this.name = new TextComponent(resourceLocation.toString());
				}
			}

			@Override
			public Component getNarration() {
				return new TranslatableComponent("narrator.select", this.name);
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				GuiComponent.drawString(poseStack, CreateBuffetWorldScreen.this.font, this.name, k + 5, j + 2, 16777215);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					BiomeList.this.setSelected(this);
					return true;
				} else {
					return false;
				}
			}
		}
	}
}

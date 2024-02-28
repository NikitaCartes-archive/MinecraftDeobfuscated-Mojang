package net.minecraft.client.gui.screens;

import com.ibm.icu.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

@Environment(EnvType.CLIENT)
public class CreateBuffetWorldScreen extends Screen {
	private static final Component BIOME_SELECT_INFO = Component.translatable("createWorld.customize.buffet.biome").withColor(-8355712);
	private static final int SPACING = 8;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Screen parent;
	private final Consumer<Holder<Biome>> applySettings;
	final Registry<Biome> biomes;
	private CreateBuffetWorldScreen.BiomeList list;
	Holder<Biome> biome;
	private Button doneButton;

	public CreateBuffetWorldScreen(Screen screen, WorldCreationContext worldCreationContext, Consumer<Holder<Biome>> consumer) {
		super(Component.translatable("createWorld.customize.buffet.title"));
		this.parent = screen;
		this.applySettings = consumer;
		this.biomes = worldCreationContext.worldgenLoadContext().registryOrThrow(Registries.BIOME);
		Holder<Biome> holder = (Holder<Biome>)this.biomes.getHolder(Biomes.PLAINS).or(() -> this.biomes.holders().findAny()).orElseThrow();
		this.biome = (Holder<Biome>)worldCreationContext.selectedDimensions().overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(holder);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	@Override
	protected void init() {
		LinearLayout linearLayout = this.layout.addToHeader(LinearLayout.vertical().spacing(8));
		linearLayout.defaultCellSetting().alignHorizontallyCenter();
		linearLayout.addChild(new StringWidget(this.getTitle(), this.font));
		linearLayout.addChild(new StringWidget(BIOME_SELECT_INFO, this.font));
		this.list = this.layout.addToContents(new CreateBuffetWorldScreen.BiomeList());
		LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		this.doneButton = linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.applySettings.accept(this.biome);
			this.onClose();
		}).build());
		linearLayout2.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
		this.list
			.setSelected(
				(CreateBuffetWorldScreen.BiomeList.Entry)this.list.children().stream().filter(entry -> Objects.equals(entry.biome, this.biome)).findFirst().orElse(null)
			);
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		this.list.updateSize(this.width, this.layout);
	}

	void updateButtonValidity() {
		this.doneButton.active = this.list.getSelected() != null;
	}

	@Environment(EnvType.CLIENT)
	class BiomeList extends ObjectSelectionList<CreateBuffetWorldScreen.BiomeList.Entry> {
		BiomeList() {
			super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height - 77, 40, 16);
			Collator collator = Collator.getInstance(Locale.getDefault());
			CreateBuffetWorldScreen.this.biomes
				.holders()
				.map(reference -> new CreateBuffetWorldScreen.BiomeList.Entry(reference))
				.sorted(Comparator.comparing(entry -> entry.name.getString(), collator))
				.forEach(entry -> this.addEntry(entry));
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
					this.name = Component.translatable(string);
				} else {
					this.name = Component.literal(resourceLocation.toString());
				}
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", this.name);
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				guiGraphics.drawString(CreateBuffetWorldScreen.this.font, this.name, k + 5, j + 2, 16777215);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				BiomeList.this.setSelected(this);
				return super.mouseClicked(d, e, i);
			}
		}
	}
}

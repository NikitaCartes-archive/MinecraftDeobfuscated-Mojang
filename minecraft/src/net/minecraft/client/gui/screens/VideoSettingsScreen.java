package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.FullscreenResolutionProgressOption;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class VideoSettingsScreen extends Screen {
	private final Screen lastScreen;
	private final Options options;
	private OptionsList list;
	private static final Option[] OPTIONS = new Option[]{
		Option.GRAPHICS,
		Option.RENDER_DISTANCE,
		Option.AMBIENT_OCCLUSION,
		Option.FRAMERATE_LIMIT,
		Option.ENABLE_VSYNC,
		Option.VIEW_BOBBING,
		Option.GUI_SCALE,
		Option.ATTACK_INDICATOR,
		Option.GAMMA,
		Option.RENDER_CLOUDS,
		Option.USE_FULLSCREEN,
		Option.PARTICLES,
		Option.MIPMAP_LEVELS,
		Option.ENTITY_SHADOWS,
		Option.BIOME_BLEND_RADIUS
	};
	private int oldMipmaps;

	public VideoSettingsScreen(Screen screen, Options options) {
		super(new TranslatableComponent("options.videoTitle"));
		this.lastScreen = screen;
		this.options = options;
	}

	@Override
	protected void init() {
		this.oldMipmaps = this.options.mipmapLevels;
		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		this.list.addBig(new FullscreenResolutionProgressOption(this.minecraft.window));
		this.list.addSmall(OPTIONS);
		this.children.add(this.list);
		this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, I18n.get("gui.done"), button -> {
			this.minecraft.options.save();
			this.minecraft.window.changeFullscreenVideoMode();
			this.minecraft.setScreen(this.lastScreen);
		}));
	}

	@Override
	public void removed() {
		if (this.options.mipmapLevels != this.oldMipmaps) {
			this.minecraft.getTextureAtlas().setMaxMipLevel(this.options.mipmapLevels);
			this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
			this.minecraft.getTextureAtlas().setFilter(false, this.options.mipmapLevels > 0);
			this.minecraft.delayTextureReload();
		}

		this.minecraft.options.save();
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		int j = this.options.guiScale;
		if (super.mouseClicked(d, e, i)) {
			if (this.options.guiScale != j) {
				this.minecraft.resizeDisplay();
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		int j = this.options.guiScale;
		if (super.mouseReleased(d, e, i)) {
			return true;
		} else if (this.list.mouseReleased(d, e, i)) {
			if (this.options.guiScale != j) {
				this.minecraft.resizeDisplay();
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.list.render(i, j, f);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 5, 16777215);
		super.render(i, j, f);
	}
}

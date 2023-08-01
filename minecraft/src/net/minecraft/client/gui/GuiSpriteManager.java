package net.minecraft.client.gui;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

@Environment(EnvType.CLIENT)
public class GuiSpriteManager extends TextureAtlasHolder {
	private static final Set<MetadataSectionSerializer<?>> METADATA_SECTIONS = Set.of(AnimationMetadataSection.SERIALIZER, GuiMetadataSection.TYPE);

	public GuiSpriteManager(TextureManager textureManager) {
		super(textureManager, new ResourceLocation("textures/atlas/gui.png"), new ResourceLocation("gui"), METADATA_SECTIONS);
	}

	@Override
	public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
		return super.getSprite(resourceLocation);
	}

	public GuiSpriteScaling getSpriteScaling(TextureAtlasSprite textureAtlasSprite) {
		return this.getMetadata(textureAtlasSprite).scaling();
	}

	private GuiMetadataSection getMetadata(TextureAtlasSprite textureAtlasSprite) {
		return (GuiMetadataSection)textureAtlasSprite.contents().metadata().getSection(GuiMetadataSection.TYPE).orElse(GuiMetadataSection.DEFAULT);
	}
}

package net.minecraft.client.resources.model;

import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SpecialModels {
	public static final ResourceLocation BUILTIN_GENERATED = builtinModelId("generated");
	public static final ResourceLocation BUILTIN_BLOCK_ENTITY = builtinModelId("entity");
	public static final UnbakedModel GENERATED_MARKER = createMarker("generation marker", BlockModel.GuiLight.FRONT);
	public static final UnbakedModel BLOCK_ENTITY_MARKER = createMarker("block entity marker", BlockModel.GuiLight.SIDE);

	public static ResourceLocation builtinModelId(String string) {
		return ResourceLocation.withDefaultNamespace("builtin/" + string);
	}

	private static UnbakedModel createMarker(String string, BlockModel.GuiLight guiLight) {
		BlockModel blockModel = new BlockModel(null, List.of(), Map.of(), null, guiLight, ItemTransforms.NO_TRANSFORMS, List.of());
		blockModel.name = string;
		return blockModel;
	}
}

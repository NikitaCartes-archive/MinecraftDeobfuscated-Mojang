package net.minecraft.client.resources.model;

import com.mojang.datafixers.util.Either;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class MissingBlockModel {
	public static final String NAME = "missing";
	public static final ResourceLocation LOCATION = SpecialModels.builtinModelId("missing");
	public static final ModelResourceLocation VARIANT = new ModelResourceLocation(LOCATION, "missing");

	public static UnbakedModel missingModel() {
		BlockFaceUV blockFaceUV = new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0);
		Map<Direction, BlockElementFace> map = new EnumMap(Direction.class);

		for (Direction direction : Direction.values()) {
			map.put(direction, new BlockElementFace(direction, 0, MissingTextureAtlasSprite.getLocation().getPath(), blockFaceUV));
		}

		BlockElement blockElement = new BlockElement(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(16.0F, 16.0F, 16.0F), map);
		BlockModel blockModel = new BlockModel(
			null,
			List.of(blockElement),
			Map.of("particle", Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()))),
			null,
			null,
			ItemTransforms.NO_TRANSFORMS,
			List.of()
		);
		blockModel.name = "missingno";
		return blockModel;
	}
}

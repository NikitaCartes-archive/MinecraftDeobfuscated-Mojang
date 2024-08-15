package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.SpecialModels;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemDisplayContext;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BlockModel implements UnbakedModel {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final FaceBakery FACE_BAKERY = new FaceBakery();
	@VisibleForTesting
	static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(BlockModel.class, new BlockModel.Deserializer())
		.registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer())
		.registerTypeAdapter(BlockElementFace.class, new BlockElementFace.Deserializer())
		.registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer())
		.registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
		.registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
		.registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer())
		.create();
	private static final char REFERENCE_CHAR = '#';
	public static final String PARTICLE_TEXTURE_REFERENCE = "particle";
	private static final boolean DEFAULT_AMBIENT_OCCLUSION = true;
	private final List<BlockElement> elements;
	@Nullable
	private final BlockModel.GuiLight guiLight;
	@Nullable
	private final Boolean hasAmbientOcclusion;
	private final ItemTransforms transforms;
	private final List<ItemOverride> overrides;
	public String name = "";
	@VisibleForTesting
	protected final Map<String, Either<Material, String>> textureMap;
	@Nullable
	protected BlockModel parent;
	@Nullable
	protected ResourceLocation parentLocation;

	public static BlockModel fromStream(Reader reader) {
		return GsonHelper.fromJson(GSON, reader, BlockModel.class);
	}

	public BlockModel(
		@Nullable ResourceLocation resourceLocation,
		List<BlockElement> list,
		Map<String, Either<Material, String>> map,
		@Nullable Boolean boolean_,
		@Nullable BlockModel.GuiLight guiLight,
		ItemTransforms itemTransforms,
		List<ItemOverride> list2
	) {
		this.elements = list;
		this.hasAmbientOcclusion = boolean_;
		this.guiLight = guiLight;
		this.textureMap = map;
		this.parentLocation = resourceLocation;
		this.transforms = itemTransforms;
		this.overrides = list2;
	}

	public List<BlockElement> getElements() {
		return this.elements.isEmpty() && this.parent != null ? this.parent.getElements() : this.elements;
	}

	public boolean hasAmbientOcclusion() {
		if (this.hasAmbientOcclusion != null) {
			return this.hasAmbientOcclusion;
		} else {
			return this.parent != null ? this.parent.hasAmbientOcclusion() : true;
		}
	}

	public BlockModel.GuiLight getGuiLight() {
		if (this.guiLight != null) {
			return this.guiLight;
		} else {
			return this.parent != null ? this.parent.getGuiLight() : BlockModel.GuiLight.SIDE;
		}
	}

	public boolean isResolved() {
		return this.parentLocation == null || this.parent != null && this.parent.isResolved();
	}

	public List<ItemOverride> getOverrides() {
		return this.overrides;
	}

	private ItemOverrides bakeItemOverrides(ModelBaker modelBaker, BlockModel blockModel) {
		return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(modelBaker, blockModel, this.overrides);
	}

	@Override
	public void resolveDependencies(UnbakedModel.Resolver resolver, UnbakedModel.ResolutionContext resolutionContext) {
		if (this.parentLocation != null) {
			if (!(resolver.resolve(this.parentLocation) instanceof BlockModel blockModel)) {
				throw new IllegalStateException("BlockModel parent has to be a block model.");
			}

			this.parent = blockModel;
		}

		if (resolutionContext != UnbakedModel.ResolutionContext.OVERRIDE) {
			this.overrides.forEach(itemOverride -> resolver.resolveForOverride(itemOverride.getModel()));
		}
	}

	@Override
	public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
		return this.bake(modelBaker, this, function, modelState, true);
	}

	public BakedModel bake(ModelBaker modelBaker, BlockModel blockModel, Function<Material, TextureAtlasSprite> function, ModelState modelState, boolean bl) {
		TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)function.apply(this.getMaterial("particle"));
		if (this.getRootModel() == SpecialModels.BLOCK_ENTITY_MARKER) {
			return new BuiltInModel(this.getTransforms(), this.bakeItemOverrides(modelBaker, blockModel), textureAtlasSprite, this.getGuiLight().lightLikeBlock());
		} else {
			SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this, this.bakeItemOverrides(modelBaker, blockModel), bl).particle(textureAtlasSprite);

			for (BlockElement blockElement : this.getElements()) {
				for (Direction direction : blockElement.faces.keySet()) {
					BlockElementFace blockElementFace = (BlockElementFace)blockElement.faces.get(direction);
					TextureAtlasSprite textureAtlasSprite2 = (TextureAtlasSprite)function.apply(this.getMaterial(blockElementFace.texture()));
					if (blockElementFace.cullForDirection() == null) {
						builder.addUnculledFace(bakeFace(blockElement, blockElementFace, textureAtlasSprite2, direction, modelState));
					} else {
						builder.addCulledFace(
							Direction.rotate(modelState.getRotation().getMatrix(), blockElementFace.cullForDirection()),
							bakeFace(blockElement, blockElementFace, textureAtlasSprite2, direction, modelState)
						);
					}
				}
			}

			return builder.build();
		}
	}

	private static BakedQuad bakeFace(
		BlockElement blockElement, BlockElementFace blockElementFace, TextureAtlasSprite textureAtlasSprite, Direction direction, ModelState modelState
	) {
		return FACE_BAKERY.bakeQuad(
			blockElement.from,
			blockElement.to,
			blockElementFace,
			textureAtlasSprite,
			direction,
			modelState,
			blockElement.rotation,
			blockElement.shade,
			blockElement.lightEmission
		);
	}

	public boolean hasTexture(String string) {
		return !MissingTextureAtlasSprite.getLocation().equals(this.getMaterial(string).texture());
	}

	public Material getMaterial(String string) {
		if (isTextureReference(string)) {
			string = string.substring(1);
		}

		List<String> list = Lists.<String>newArrayList();

		while (true) {
			Either<Material, String> either = this.findTextureEntry(string);
			Optional<Material> optional = either.left();
			if (optional.isPresent()) {
				return (Material)optional.get();
			}

			string = (String)either.right().get();
			if (list.contains(string)) {
				LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", Joiner.on("->").join(list), string, this.name);
				return new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
			}

			list.add(string);
		}
	}

	private Either<Material, String> findTextureEntry(String string) {
		for (BlockModel blockModel = this; blockModel != null; blockModel = blockModel.parent) {
			Either<Material, String> either = (Either<Material, String>)blockModel.textureMap.get(string);
			if (either != null) {
				return either;
			}
		}

		return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
	}

	static boolean isTextureReference(String string) {
		return string.charAt(0) == '#';
	}

	public BlockModel getRootModel() {
		return this.parent == null ? this : this.parent.getRootModel();
	}

	public ItemTransforms getTransforms() {
		ItemTransform itemTransform = this.getTransform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
		ItemTransform itemTransform2 = this.getTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
		ItemTransform itemTransform3 = this.getTransform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
		ItemTransform itemTransform4 = this.getTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
		ItemTransform itemTransform5 = this.getTransform(ItemDisplayContext.HEAD);
		ItemTransform itemTransform6 = this.getTransform(ItemDisplayContext.GUI);
		ItemTransform itemTransform7 = this.getTransform(ItemDisplayContext.GROUND);
		ItemTransform itemTransform8 = this.getTransform(ItemDisplayContext.FIXED);
		return new ItemTransforms(itemTransform, itemTransform2, itemTransform3, itemTransform4, itemTransform5, itemTransform6, itemTransform7, itemTransform8);
	}

	private ItemTransform getTransform(ItemDisplayContext itemDisplayContext) {
		return this.parent != null && !this.transforms.hasTransform(itemDisplayContext)
			? this.parent.getTransform(itemDisplayContext)
			: this.transforms.getTransform(itemDisplayContext);
	}

	public String toString() {
		return this.name;
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<BlockModel> {
		public BlockModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			List<BlockElement> list = this.getElements(jsonDeserializationContext, jsonObject);
			String string = this.getParentName(jsonObject);
			Map<String, Either<Material, String>> map = this.getTextureMap(jsonObject);
			Boolean boolean_ = this.getAmbientOcclusion(jsonObject);
			ItemTransforms itemTransforms = ItemTransforms.NO_TRANSFORMS;
			if (jsonObject.has("display")) {
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "display");
				itemTransforms = jsonDeserializationContext.deserialize(jsonObject2, ItemTransforms.class);
			}

			List<ItemOverride> list2 = this.getOverrides(jsonDeserializationContext, jsonObject);
			BlockModel.GuiLight guiLight = null;
			if (jsonObject.has("gui_light")) {
				guiLight = BlockModel.GuiLight.getByName(GsonHelper.getAsString(jsonObject, "gui_light"));
			}

			ResourceLocation resourceLocation = string.isEmpty() ? null : ResourceLocation.parse(string);
			return new BlockModel(resourceLocation, list, map, boolean_, guiLight, itemTransforms, list2);
		}

		protected List<ItemOverride> getOverrides(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			List<ItemOverride> list = Lists.<ItemOverride>newArrayList();
			if (jsonObject.has("overrides")) {
				for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "overrides")) {
					list.add((ItemOverride)jsonDeserializationContext.deserialize(jsonElement, ItemOverride.class));
				}
			}

			return list;
		}

		private Map<String, Either<Material, String>> getTextureMap(JsonObject jsonObject) {
			ResourceLocation resourceLocation = TextureAtlas.LOCATION_BLOCKS;
			Map<String, Either<Material, String>> map = Maps.<String, Either<Material, String>>newHashMap();
			if (jsonObject.has("textures")) {
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "textures");

				for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
					map.put((String)entry.getKey(), parseTextureLocationOrReference(resourceLocation, ((JsonElement)entry.getValue()).getAsString()));
				}
			}

			return map;
		}

		private static Either<Material, String> parseTextureLocationOrReference(ResourceLocation resourceLocation, String string) {
			if (BlockModel.isTextureReference(string)) {
				return Either.right(string.substring(1));
			} else {
				ResourceLocation resourceLocation2 = ResourceLocation.tryParse(string);
				if (resourceLocation2 == null) {
					throw new JsonParseException(string + " is not valid resource location");
				} else {
					return Either.left(new Material(resourceLocation, resourceLocation2));
				}
			}
		}

		private String getParentName(JsonObject jsonObject) {
			return GsonHelper.getAsString(jsonObject, "parent", "");
		}

		@Nullable
		protected Boolean getAmbientOcclusion(JsonObject jsonObject) {
			return jsonObject.has("ambientocclusion") ? GsonHelper.getAsBoolean(jsonObject, "ambientocclusion") : null;
		}

		protected List<BlockElement> getElements(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			List<BlockElement> list = Lists.<BlockElement>newArrayList();
			if (jsonObject.has("elements")) {
				for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "elements")) {
					list.add((BlockElement)jsonDeserializationContext.deserialize(jsonElement, BlockElement.class));
				}
			}

			return list;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum GuiLight {
		FRONT("front"),
		SIDE("side");

		private final String name;

		private GuiLight(final String string2) {
			this.name = string2;
		}

		public static BlockModel.GuiLight getByName(String string) {
			for (BlockModel.GuiLight guiLight : values()) {
				if (guiLight.name.equals(string)) {
					return guiLight;
				}
			}

			throw new IllegalArgumentException("Invalid gui light: " + string);
		}

		public boolean lightLikeBlock() {
			return this == SIDE;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LoopException extends RuntimeException {
		public LoopException(String string) {
			super(string);
		}
	}
}

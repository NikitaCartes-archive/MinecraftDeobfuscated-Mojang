package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class BlockModel implements UnbakedModel {
	private static final Logger LOGGER = LogManager.getLogger();
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
	private final List<BlockElement> elements;
	private final boolean isGui3d;
	private final boolean hasAmbientOcclusion;
	private final ItemTransforms transforms;
	private final List<ItemOverride> overrides;
	public String name = "";
	@VisibleForTesting
	protected final Map<String, String> textureMap;
	@Nullable
	protected BlockModel parent;
	@Nullable
	protected ResourceLocation parentLocation;

	public static BlockModel fromStream(Reader reader) {
		return GsonHelper.fromJson(GSON, reader, BlockModel.class);
	}

	public static BlockModel fromString(String string) {
		return fromStream(new StringReader(string));
	}

	public BlockModel(
		@Nullable ResourceLocation resourceLocation,
		List<BlockElement> list,
		Map<String, String> map,
		boolean bl,
		boolean bl2,
		ItemTransforms itemTransforms,
		List<ItemOverride> list2
	) {
		this.elements = list;
		this.hasAmbientOcclusion = bl;
		this.isGui3d = bl2;
		this.textureMap = map;
		this.parentLocation = resourceLocation;
		this.transforms = itemTransforms;
		this.overrides = list2;
	}

	public List<BlockElement> getElements() {
		return this.elements.isEmpty() && this.parent != null ? this.parent.getElements() : this.elements;
	}

	public boolean hasAmbientOcclusion() {
		return this.parent != null ? this.parent.hasAmbientOcclusion() : this.hasAmbientOcclusion;
	}

	public boolean isGui3d() {
		return this.isGui3d;
	}

	public List<ItemOverride> getOverrides() {
		return this.overrides;
	}

	private ItemOverrides getItemOverrides(ModelBakery modelBakery, BlockModel blockModel) {
		return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(modelBakery, blockModel, modelBakery::getModel, this.overrides);
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

		for (ItemOverride itemOverride : this.overrides) {
			set.add(itemOverride.getModel());
		}

		if (this.parentLocation != null) {
			set.add(this.parentLocation);
		}

		return set;
	}

	@Override
	public Collection<ResourceLocation> getTextures(Function<ResourceLocation, UnbakedModel> function, Set<String> set) {
		Set<UnbakedModel> set2 = Sets.<UnbakedModel>newLinkedHashSet();

		for (BlockModel blockModel = this; blockModel.parentLocation != null && blockModel.parent == null; blockModel = blockModel.parent) {
			set2.add(blockModel);
			UnbakedModel unbakedModel = (UnbakedModel)function.apply(blockModel.parentLocation);
			if (unbakedModel == null) {
				LOGGER.warn("No parent '{}' while loading model '{}'", this.parentLocation, blockModel);
			}

			if (set2.contains(unbakedModel)) {
				LOGGER.warn(
					"Found 'parent' loop while loading model '{}' in chain: {} -> {}",
					blockModel,
					set2.stream().map(Object::toString).collect(Collectors.joining(" -> ")),
					this.parentLocation
				);
				unbakedModel = null;
			}

			if (unbakedModel == null) {
				blockModel.parentLocation = ModelBakery.MISSING_MODEL_LOCATION;
				unbakedModel = (UnbakedModel)function.apply(blockModel.parentLocation);
			}

			if (!(unbakedModel instanceof BlockModel)) {
				throw new IllegalStateException("BlockModel parent has to be a block model.");
			}

			blockModel.parent = (BlockModel)unbakedModel;
		}

		Set<ResourceLocation> set3 = Sets.<ResourceLocation>newHashSet(new ResourceLocation(this.getTexture("particle")));

		for (BlockElement blockElement : this.getElements()) {
			for (BlockElementFace blockElementFace : blockElement.faces.values()) {
				String string = this.getTexture(blockElementFace.texture);
				if (Objects.equals(string, MissingTextureAtlasSprite.getLocation().toString())) {
					set.add(String.format("%s in %s", blockElementFace.texture, this.name));
				}

				set3.add(new ResourceLocation(string));
			}
		}

		this.overrides.forEach(itemOverride -> {
			UnbakedModel unbakedModelx = (UnbakedModel)function.apply(itemOverride.getModel());
			if (!Objects.equals(unbakedModelx, this)) {
				set3.addAll(unbakedModelx.getTextures(function, set));
			}
		});
		if (this.getRootModel() == ModelBakery.GENERATION_MARKER) {
			ItemModelGenerator.LAYERS.forEach(stringx -> set3.add(new ResourceLocation(this.getTexture(stringx))));
		}

		return set3;
	}

	@Override
	public BakedModel bake(ModelBakery modelBakery, Function<ResourceLocation, TextureAtlasSprite> function, ModelState modelState) {
		return this.bake(modelBakery, this, function, modelState);
	}

	public BakedModel bake(ModelBakery modelBakery, BlockModel blockModel, Function<ResourceLocation, TextureAtlasSprite> function, ModelState modelState) {
		TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)function.apply(new ResourceLocation(this.getTexture("particle")));
		if (this.getRootModel() == ModelBakery.BLOCK_ENTITY_MARKER) {
			return new BuiltInModel(this.getTransforms(), this.getItemOverrides(modelBakery, blockModel), textureAtlasSprite);
		} else {
			SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this, this.getItemOverrides(modelBakery, blockModel)).particle(textureAtlasSprite);

			for (BlockElement blockElement : this.getElements()) {
				for (Direction direction : blockElement.faces.keySet()) {
					BlockElementFace blockElementFace = (BlockElementFace)blockElement.faces.get(direction);
					TextureAtlasSprite textureAtlasSprite2 = (TextureAtlasSprite)function.apply(new ResourceLocation(this.getTexture(blockElementFace.texture)));
					if (blockElementFace.cullForDirection == null) {
						builder.addUnculledFace(bakeFace(blockElement, blockElementFace, textureAtlasSprite2, direction, modelState));
					} else {
						builder.addCulledFace(
							modelState.getRotation().rotate(blockElementFace.cullForDirection), bakeFace(blockElement, blockElementFace, textureAtlasSprite2, direction, modelState)
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
			blockElement.from, blockElement.to, blockElementFace, textureAtlasSprite, direction, modelState, blockElement.rotation, blockElement.shade
		);
	}

	public boolean hasTexture(String string) {
		return !MissingTextureAtlasSprite.getLocation().toString().equals(this.getTexture(string));
	}

	public String getTexture(String string) {
		if (!this.isTextureReference(string)) {
			string = '#' + string;
		}

		return this.getTexture(string, new BlockModel.Bookkeep(this));
	}

	private String getTexture(String string, BlockModel.Bookkeep bookkeep) {
		if (this.isTextureReference(string)) {
			if (this == bookkeep.maxDepth) {
				LOGGER.warn("Unable to resolve texture due to upward reference: {} in {}", string, this.name);
				return MissingTextureAtlasSprite.getLocation().toString();
			} else {
				String string2 = (String)this.textureMap.get(string.substring(1));
				if (string2 == null && this.parent != null) {
					string2 = this.parent.getTexture(string, bookkeep);
				}

				bookkeep.maxDepth = this;
				if (string2 != null && this.isTextureReference(string2)) {
					string2 = bookkeep.root.getTexture(string2, bookkeep);
				}

				return string2 != null && !this.isTextureReference(string2) ? string2 : MissingTextureAtlasSprite.getLocation().toString();
			}
		} else {
			return string;
		}
	}

	private boolean isTextureReference(String string) {
		return string.charAt(0) == '#';
	}

	public BlockModel getRootModel() {
		return this.parent == null ? this : this.parent.getRootModel();
	}

	public ItemTransforms getTransforms() {
		ItemTransform itemTransform = this.getTransform(ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
		ItemTransform itemTransform2 = this.getTransform(ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
		ItemTransform itemTransform3 = this.getTransform(ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
		ItemTransform itemTransform4 = this.getTransform(ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
		ItemTransform itemTransform5 = this.getTransform(ItemTransforms.TransformType.HEAD);
		ItemTransform itemTransform6 = this.getTransform(ItemTransforms.TransformType.GUI);
		ItemTransform itemTransform7 = this.getTransform(ItemTransforms.TransformType.GROUND);
		ItemTransform itemTransform8 = this.getTransform(ItemTransforms.TransformType.FIXED);
		return new ItemTransforms(itemTransform, itemTransform2, itemTransform3, itemTransform4, itemTransform5, itemTransform6, itemTransform7, itemTransform8);
	}

	private ItemTransform getTransform(ItemTransforms.TransformType transformType) {
		return this.parent != null && !this.transforms.hasTransform(transformType)
			? this.parent.getTransform(transformType)
			: this.transforms.getTransform(transformType);
	}

	public String toString() {
		return this.name;
	}

	@Environment(EnvType.CLIENT)
	static final class Bookkeep {
		public final BlockModel root;
		public BlockModel maxDepth;

		private Bookkeep(BlockModel blockModel) {
			this.root = blockModel;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<BlockModel> {
		public BlockModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			List<BlockElement> list = this.getElements(jsonDeserializationContext, jsonObject);
			String string = this.getParentName(jsonObject);
			Map<String, String> map = this.getTextureMap(jsonObject);
			boolean bl = this.getAmbientOcclusion(jsonObject);
			ItemTransforms itemTransforms = ItemTransforms.NO_TRANSFORMS;
			if (jsonObject.has("display")) {
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "display");
				itemTransforms = jsonDeserializationContext.deserialize(jsonObject2, ItemTransforms.class);
			}

			List<ItemOverride> list2 = this.getOverrides(jsonDeserializationContext, jsonObject);
			ResourceLocation resourceLocation = string.isEmpty() ? null : new ResourceLocation(string);
			return new BlockModel(resourceLocation, list, map, bl, true, itemTransforms, list2);
		}

		protected List<ItemOverride> getOverrides(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			List<ItemOverride> list = Lists.<ItemOverride>newArrayList();
			if (jsonObject.has("overrides")) {
				for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "overrides")) {
					list.add(jsonDeserializationContext.deserialize(jsonElement, ItemOverride.class));
				}
			}

			return list;
		}

		private Map<String, String> getTextureMap(JsonObject jsonObject) {
			Map<String, String> map = Maps.<String, String>newHashMap();
			if (jsonObject.has("textures")) {
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "textures");

				for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
					map.put(entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
				}
			}

			return map;
		}

		private String getParentName(JsonObject jsonObject) {
			return GsonHelper.getAsString(jsonObject, "parent", "");
		}

		protected boolean getAmbientOcclusion(JsonObject jsonObject) {
			return GsonHelper.getAsBoolean(jsonObject, "ambientocclusion", true);
		}

		protected List<BlockElement> getElements(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			List<BlockElement> list = Lists.<BlockElement>newArrayList();
			if (jsonObject.has("elements")) {
				for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "elements")) {
					list.add(jsonDeserializationContext.deserialize(jsonElement, BlockElement.class));
				}
			}

			return list;
		}
	}
}

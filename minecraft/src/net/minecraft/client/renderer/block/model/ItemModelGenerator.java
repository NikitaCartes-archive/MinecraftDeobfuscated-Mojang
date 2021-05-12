package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;

@Environment(EnvType.CLIENT)
public class ItemModelGenerator {
	public static final List<String> LAYERS = Lists.<String>newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");
	private static final float MIN_Z = 7.5F;
	private static final float MAX_Z = 8.5F;

	public BlockModel generateBlockModel(Function<Material, TextureAtlasSprite> function, BlockModel blockModel) {
		Map<String, Either<Material, String>> map = Maps.<String, Either<Material, String>>newHashMap();
		List<BlockElement> list = Lists.<BlockElement>newArrayList();

		for (int i = 0; i < LAYERS.size(); i++) {
			String string = (String)LAYERS.get(i);
			if (!blockModel.hasTexture(string)) {
				break;
			}

			Material material = blockModel.getMaterial(string);
			map.put(string, Either.left(material));
			TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)function.apply(material);
			list.addAll(this.processFrames(i, string, textureAtlasSprite));
		}

		map.put("particle", blockModel.hasTexture("particle") ? Either.left(blockModel.getMaterial("particle")) : (Either)map.get("layer0"));
		BlockModel blockModel2 = new BlockModel(null, list, map, false, blockModel.getGuiLight(), blockModel.getTransforms(), blockModel.getOverrides());
		blockModel2.name = blockModel.name;
		return blockModel2;
	}

	private List<BlockElement> processFrames(int i, String string, TextureAtlasSprite textureAtlasSprite) {
		Map<Direction, BlockElementFace> map = Maps.<Direction, BlockElementFace>newHashMap();
		map.put(Direction.SOUTH, new BlockElementFace(null, i, string, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
		map.put(Direction.NORTH, new BlockElementFace(null, i, string, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
		List<BlockElement> list = Lists.<BlockElement>newArrayList();
		list.add(new BlockElement(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, null, true));
		list.addAll(this.createSideElements(textureAtlasSprite, string, i));
		return list;
	}

	private List<BlockElement> createSideElements(TextureAtlasSprite textureAtlasSprite, String string, int i) {
		float f = (float)textureAtlasSprite.getWidth();
		float g = (float)textureAtlasSprite.getHeight();
		List<BlockElement> list = Lists.<BlockElement>newArrayList();

		for (ItemModelGenerator.Span span : this.getSpans(textureAtlasSprite)) {
			float h = 0.0F;
			float j = 0.0F;
			float k = 0.0F;
			float l = 0.0F;
			float m = 0.0F;
			float n = 0.0F;
			float o = 0.0F;
			float p = 0.0F;
			float q = 16.0F / f;
			float r = 16.0F / g;
			float s = (float)span.getMin();
			float t = (float)span.getMax();
			float u = (float)span.getAnchor();
			ItemModelGenerator.SpanFacing spanFacing = span.getFacing();
			switch (spanFacing) {
				case UP:
					m = s;
					h = s;
					k = n = t + 1.0F;
					o = u;
					j = u;
					l = u;
					p = u + 1.0F;
					break;
				case DOWN:
					o = u;
					p = u + 1.0F;
					m = s;
					h = s;
					k = n = t + 1.0F;
					j = u + 1.0F;
					l = u + 1.0F;
					break;
				case LEFT:
					m = u;
					h = u;
					k = u;
					n = u + 1.0F;
					p = s;
					j = s;
					l = o = t + 1.0F;
					break;
				case RIGHT:
					m = u;
					n = u + 1.0F;
					h = u + 1.0F;
					k = u + 1.0F;
					p = s;
					j = s;
					l = o = t + 1.0F;
			}

			h *= q;
			k *= q;
			j *= r;
			l *= r;
			j = 16.0F - j;
			l = 16.0F - l;
			m *= q;
			n *= q;
			o *= r;
			p *= r;
			Map<Direction, BlockElementFace> map = Maps.<Direction, BlockElementFace>newHashMap();
			map.put(spanFacing.getDirection(), new BlockElementFace(null, i, string, new BlockFaceUV(new float[]{m, o, n, p}, 0)));
			switch (spanFacing) {
				case UP:
					list.add(new BlockElement(new Vector3f(h, j, 7.5F), new Vector3f(k, j, 8.5F), map, null, true));
					break;
				case DOWN:
					list.add(new BlockElement(new Vector3f(h, l, 7.5F), new Vector3f(k, l, 8.5F), map, null, true));
					break;
				case LEFT:
					list.add(new BlockElement(new Vector3f(h, j, 7.5F), new Vector3f(h, l, 8.5F), map, null, true));
					break;
				case RIGHT:
					list.add(new BlockElement(new Vector3f(k, j, 7.5F), new Vector3f(k, l, 8.5F), map, null, true));
			}
		}

		return list;
	}

	private List<ItemModelGenerator.Span> getSpans(TextureAtlasSprite textureAtlasSprite) {
		int i = textureAtlasSprite.getWidth();
		int j = textureAtlasSprite.getHeight();
		List<ItemModelGenerator.Span> list = Lists.<ItemModelGenerator.Span>newArrayList();
		textureAtlasSprite.getUniqueFrames().forEach(k -> {
			for (int l = 0; l < j; l++) {
				for (int m = 0; m < i; m++) {
					boolean bl = !this.isTransparent(textureAtlasSprite, k, m, l, i, j);
					this.checkTransition(ItemModelGenerator.SpanFacing.UP, list, textureAtlasSprite, k, m, l, i, j, bl);
					this.checkTransition(ItemModelGenerator.SpanFacing.DOWN, list, textureAtlasSprite, k, m, l, i, j, bl);
					this.checkTransition(ItemModelGenerator.SpanFacing.LEFT, list, textureAtlasSprite, k, m, l, i, j, bl);
					this.checkTransition(ItemModelGenerator.SpanFacing.RIGHT, list, textureAtlasSprite, k, m, l, i, j, bl);
				}
			}
		});
		return list;
	}

	private void checkTransition(
		ItemModelGenerator.SpanFacing spanFacing,
		List<ItemModelGenerator.Span> list,
		TextureAtlasSprite textureAtlasSprite,
		int i,
		int j,
		int k,
		int l,
		int m,
		boolean bl
	) {
		boolean bl2 = this.isTransparent(textureAtlasSprite, i, j + spanFacing.getXOffset(), k + spanFacing.getYOffset(), l, m) && bl;
		if (bl2) {
			this.createOrExpandSpan(list, spanFacing, j, k);
		}
	}

	private void createOrExpandSpan(List<ItemModelGenerator.Span> list, ItemModelGenerator.SpanFacing spanFacing, int i, int j) {
		ItemModelGenerator.Span span = null;

		for (ItemModelGenerator.Span span2 : list) {
			if (span2.getFacing() == spanFacing) {
				int k = spanFacing.isHorizontal() ? j : i;
				if (span2.getAnchor() == k) {
					span = span2;
					break;
				}
			}
		}

		int l = spanFacing.isHorizontal() ? j : i;
		int m = spanFacing.isHorizontal() ? i : j;
		if (span == null) {
			list.add(new ItemModelGenerator.Span(spanFacing, m, l));
		} else {
			span.expand(m);
		}
	}

	private boolean isTransparent(TextureAtlasSprite textureAtlasSprite, int i, int j, int k, int l, int m) {
		return j >= 0 && k >= 0 && j < l && k < m ? textureAtlasSprite.isTransparent(i, j, k) : true;
	}

	@Environment(EnvType.CLIENT)
	static class Span {
		private final ItemModelGenerator.SpanFacing facing;
		private int min;
		private int max;
		private final int anchor;

		public Span(ItemModelGenerator.SpanFacing spanFacing, int i, int j) {
			this.facing = spanFacing;
			this.min = i;
			this.max = i;
			this.anchor = j;
		}

		public void expand(int i) {
			if (i < this.min) {
				this.min = i;
			} else if (i > this.max) {
				this.max = i;
			}
		}

		public ItemModelGenerator.SpanFacing getFacing() {
			return this.facing;
		}

		public int getMin() {
			return this.min;
		}

		public int getMax() {
			return this.max;
		}

		public int getAnchor() {
			return this.anchor;
		}
	}

	@Environment(EnvType.CLIENT)
	static enum SpanFacing {
		UP(Direction.UP, 0, -1),
		DOWN(Direction.DOWN, 0, 1),
		LEFT(Direction.EAST, -1, 0),
		RIGHT(Direction.WEST, 1, 0);

		private final Direction direction;
		private final int xOffset;
		private final int yOffset;

		private SpanFacing(Direction direction, int j, int k) {
			this.direction = direction;
			this.xOffset = j;
			this.yOffset = k;
		}

		public Direction getDirection() {
			return this.direction;
		}

		public int getXOffset() {
			return this.xOffset;
		}

		public int getYOffset() {
			return this.yOffset;
		}

		boolean isHorizontal() {
			return this == DOWN || this == UP;
		}
	}
}

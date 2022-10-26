/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ItemModelGenerator {
    public static final List<String> LAYERS = Lists.newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");
    private static final float MIN_Z = 7.5f;
    private static final float MAX_Z = 8.5f;

    public BlockModel generateBlockModel(Function<Material, TextureAtlasSprite> function, BlockModel blockModel) {
        String string;
        HashMap<String, Either<Material, String>> map = Maps.newHashMap();
        ArrayList<BlockElement> list = Lists.newArrayList();
        for (int i = 0; i < LAYERS.size() && blockModel.hasTexture(string = LAYERS.get(i)); ++i) {
            Material material = blockModel.getMaterial(string);
            map.put(string, Either.left(material));
            SpriteContents spriteContents = function.apply(material).contents();
            list.addAll(this.processFrames(i, string, spriteContents));
        }
        map.put("particle", blockModel.hasTexture("particle") ? Either.left(blockModel.getMaterial("particle")) : (Either)map.get("layer0"));
        BlockModel blockModel2 = new BlockModel(null, list, map, false, blockModel.getGuiLight(), blockModel.getTransforms(), blockModel.getOverrides());
        blockModel2.name = blockModel.name;
        return blockModel2;
    }

    private List<BlockElement> processFrames(int i, String string, SpriteContents spriteContents) {
        HashMap<Direction, BlockElementFace> map = Maps.newHashMap();
        map.put(Direction.SOUTH, new BlockElementFace(null, i, string, new BlockFaceUV(new float[]{0.0f, 0.0f, 16.0f, 16.0f}, 0)));
        map.put(Direction.NORTH, new BlockElementFace(null, i, string, new BlockFaceUV(new float[]{16.0f, 0.0f, 0.0f, 16.0f}, 0)));
        ArrayList<BlockElement> list = Lists.newArrayList();
        list.add(new BlockElement(new Vector3f(0.0f, 0.0f, 7.5f), new Vector3f(16.0f, 16.0f, 8.5f), map, null, true));
        list.addAll(this.createSideElements(spriteContents, string, i));
        return list;
    }

    private List<BlockElement> createSideElements(SpriteContents spriteContents, String string, int i) {
        float f = spriteContents.width();
        float g = spriteContents.height();
        ArrayList<BlockElement> list = Lists.newArrayList();
        for (Span span : this.getSpans(spriteContents)) {
            float h = 0.0f;
            float j = 0.0f;
            float k = 0.0f;
            float l = 0.0f;
            float m = 0.0f;
            float n = 0.0f;
            float o = 0.0f;
            float p = 0.0f;
            float q = 16.0f / f;
            float r = 16.0f / g;
            float s = span.getMin();
            float t = span.getMax();
            float u = span.getAnchor();
            SpanFacing spanFacing = span.getFacing();
            switch (spanFacing) {
                case UP: {
                    h = m = s;
                    k = n = t + 1.0f;
                    j = o = u;
                    l = u;
                    p = u + 1.0f;
                    break;
                }
                case DOWN: {
                    o = u;
                    p = u + 1.0f;
                    h = m = s;
                    k = n = t + 1.0f;
                    j = u + 1.0f;
                    l = u + 1.0f;
                    break;
                }
                case LEFT: {
                    h = m = u;
                    k = u;
                    n = u + 1.0f;
                    j = p = s;
                    l = o = t + 1.0f;
                    break;
                }
                case RIGHT: {
                    m = u;
                    n = u + 1.0f;
                    h = u + 1.0f;
                    k = u + 1.0f;
                    j = p = s;
                    l = o = t + 1.0f;
                }
            }
            h *= q;
            k *= q;
            j *= r;
            l *= r;
            j = 16.0f - j;
            l = 16.0f - l;
            HashMap<Direction, BlockElementFace> map = Maps.newHashMap();
            map.put(spanFacing.getDirection(), new BlockElementFace(null, i, string, new BlockFaceUV(new float[]{m *= q, o *= r, n *= q, p *= r}, 0)));
            switch (spanFacing) {
                case UP: {
                    list.add(new BlockElement(new Vector3f(h, j, 7.5f), new Vector3f(k, j, 8.5f), map, null, true));
                    break;
                }
                case DOWN: {
                    list.add(new BlockElement(new Vector3f(h, l, 7.5f), new Vector3f(k, l, 8.5f), map, null, true));
                    break;
                }
                case LEFT: {
                    list.add(new BlockElement(new Vector3f(h, j, 7.5f), new Vector3f(h, l, 8.5f), map, null, true));
                    break;
                }
                case RIGHT: {
                    list.add(new BlockElement(new Vector3f(k, j, 7.5f), new Vector3f(k, l, 8.5f), map, null, true));
                }
            }
        }
        return list;
    }

    private List<Span> getSpans(SpriteContents spriteContents) {
        int i = spriteContents.width();
        int j = spriteContents.height();
        ArrayList<Span> list = Lists.newArrayList();
        spriteContents.getUniqueFrames().forEach(k -> {
            for (int l = 0; l < j; ++l) {
                for (int m = 0; m < i; ++m) {
                    boolean bl = !this.isTransparent(spriteContents, k, m, l, i, j);
                    this.checkTransition(SpanFacing.UP, list, spriteContents, k, m, l, i, j, bl);
                    this.checkTransition(SpanFacing.DOWN, list, spriteContents, k, m, l, i, j, bl);
                    this.checkTransition(SpanFacing.LEFT, list, spriteContents, k, m, l, i, j, bl);
                    this.checkTransition(SpanFacing.RIGHT, list, spriteContents, k, m, l, i, j, bl);
                }
            }
        });
        return list;
    }

    private void checkTransition(SpanFacing spanFacing, List<Span> list, SpriteContents spriteContents, int i, int j, int k, int l, int m, boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = this.isTransparent(spriteContents, i, j + spanFacing.getXOffset(), k + spanFacing.getYOffset(), l, m) && bl;
        if (bl2) {
            this.createOrExpandSpan(list, spanFacing, j, k);
        }
    }

    private void createOrExpandSpan(List<Span> list, SpanFacing spanFacing, int i, int j) {
        int m;
        Span span = null;
        for (Span span2 : list) {
            int k;
            if (span2.getFacing() != spanFacing) continue;
            int n = k = spanFacing.isHorizontal() ? j : i;
            if (span2.getAnchor() != k) continue;
            span = span2;
            break;
        }
        int l = spanFacing.isHorizontal() ? j : i;
        int n = m = spanFacing.isHorizontal() ? i : j;
        if (span == null) {
            list.add(new Span(spanFacing, m, l));
        } else {
            span.expand(m);
        }
    }

    private boolean isTransparent(SpriteContents spriteContents, int i, int j, int k, int l, int m) {
        if (j < 0 || k < 0 || j >= l || k >= m) {
            return true;
        }
        return spriteContents.isTransparent(i, j, k);
    }

    @Environment(value=EnvType.CLIENT)
    static class Span {
        private final SpanFacing facing;
        private int min;
        private int max;
        private final int anchor;

        public Span(SpanFacing spanFacing, int i, int j) {
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

        public SpanFacing getFacing() {
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

    @Environment(value=EnvType.CLIENT)
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


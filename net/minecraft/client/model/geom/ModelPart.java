/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ModelPart {
    private static final BufferBuilder COMPILE_BUFFER = new BufferBuilder(256);
    private float xTexSize = 64.0f;
    private float yTexSize = 32.0f;
    private int xTexOffs;
    private int yTexOffs;
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    @Nullable
    private ByteBuffer compiled;
    public boolean mirror;
    public boolean visible = true;
    private final List<Cube> cubes = Lists.newArrayList();
    private final List<ModelPart> children = Lists.newArrayList();

    public ModelPart(Model model) {
        model.accept(this);
        this.setTexSize(model.texWidth, model.texHeight);
    }

    public ModelPart(Model model, int i, int j) {
        this(model.texWidth, model.texHeight, i, j);
        model.accept(this);
    }

    public ModelPart(int i, int j, int k, int l) {
        this.setTexSize(i, j);
        this.texOffs(k, l);
    }

    public void copyFrom(ModelPart modelPart) {
        this.xRot = modelPart.xRot;
        this.yRot = modelPart.yRot;
        this.zRot = modelPart.zRot;
        this.x = modelPart.x;
        this.y = modelPart.y;
        this.z = modelPart.z;
    }

    public void addChild(ModelPart modelPart) {
        this.children.add(modelPart);
    }

    public ModelPart texOffs(int i, int j) {
        this.xTexOffs = i;
        this.yTexOffs = j;
        return this;
    }

    public ModelPart addBox(String string, float f, float g, float h, int i, int j, int k, float l, int m, int n) {
        this.texOffs(m, n);
        this.cubes.add(new Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, this.mirror, this.xTexSize, this.yTexSize));
        return this;
    }

    public ModelPart addBox(float f, float g, float h, float i, float j, float k) {
        this.cubes.add(new Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, 0.0f, this.mirror, this.xTexSize, this.yTexSize));
        return this;
    }

    public ModelPart addBox(float f, float g, float h, float i, float j, float k, boolean bl) {
        this.cubes.add(new Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, 0.0f, bl, this.xTexSize, this.yTexSize));
        return this;
    }

    public void addBox(float f, float g, float h, float i, float j, float k, float l) {
        this.cubes.add(new Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, this.mirror, this.xTexSize, this.yTexSize));
    }

    public void addBox(float f, float g, float h, float i, float j, float k, float l, boolean bl) {
        this.cubes.add(new Cube(this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, bl, this.xTexSize, this.yTexSize));
    }

    public void setPos(float f, float g, float h) {
        this.x = f;
        this.y = g;
        this.z = h;
    }

    public void render(float f) {
        if (!this.visible) {
            return;
        }
        this.compile(f);
        if (this.compiled == null) {
            return;
        }
        RenderSystem.pushMatrix();
        this.translateAndRotate(f);
        this.compiled.clear();
        int i = this.compiled.remaining() / DefaultVertexFormat.ENTITY.getVertexSize();
        BufferUploader.end(this.compiled, 7, DefaultVertexFormat.ENTITY, i);
        for (ModelPart modelPart : this.children) {
            modelPart.render(f);
        }
        RenderSystem.popMatrix();
    }

    public void render(BufferBuilder bufferBuilder, float f, int i, int j, TextureAtlasSprite textureAtlasSprite) {
        this.render(bufferBuilder, f, i, j, textureAtlasSprite, 1.0f, 1.0f, 1.0f);
    }

    public void render(BufferBuilder bufferBuilder, float f, int i, int j, TextureAtlasSprite textureAtlasSprite, float g, float h, float k) {
        if (!this.visible) {
            return;
        }
        if (this.cubes.isEmpty() && this.children.isEmpty()) {
            return;
        }
        bufferBuilder.pushPose();
        bufferBuilder.translate(this.x * f, this.y * f, this.z * f);
        if (this.zRot != 0.0f) {
            bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, this.zRot, false));
        }
        if (this.yRot != 0.0f) {
            bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, this.yRot, false));
        }
        if (this.xRot != 0.0f) {
            bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, this.xRot, false));
        }
        this.compile(bufferBuilder, f, i, j, textureAtlasSprite, g, h, k);
        for (ModelPart modelPart : this.children) {
            modelPart.render(bufferBuilder, f, i, j, textureAtlasSprite);
        }
        bufferBuilder.popPose();
    }

    private void compile(float f) {
        if (!this.visible) {
            return;
        }
        if (this.cubes.isEmpty() && this.children.isEmpty()) {
            return;
        }
        if (this.compiled == null) {
            COMPILE_BUFFER.begin(7, DefaultVertexFormat.ENTITY);
            this.compile(COMPILE_BUFFER, f, 240, 240, null);
            COMPILE_BUFFER.end();
            Pair<BufferBuilder.DrawState, ByteBuffer> pair = COMPILE_BUFFER.popNextBuffer();
            ByteBuffer byteBuffer = pair.getSecond();
            this.compiled = MemoryTracker.createByteBuffer(byteBuffer.remaining());
            this.compiled.put(byteBuffer);
        }
    }

    public void translateTo(float f) {
        if (!this.visible) {
            return;
        }
        this.translateAndRotate(f);
    }

    private void translateAndRotate(float f) {
        RenderSystem.translatef(this.x * f, this.y * f, this.z * f);
        if (this.zRot != 0.0f) {
            RenderSystem.rotatef(this.zRot * 57.295776f, 0.0f, 0.0f, 1.0f);
        }
        if (this.yRot != 0.0f) {
            RenderSystem.rotatef(this.yRot * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (this.xRot != 0.0f) {
            RenderSystem.rotatef(this.xRot * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
    }

    private void compile(BufferBuilder bufferBuilder, float f, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite) {
        this.compile(bufferBuilder, f, i, j, textureAtlasSprite, 1.0f, 1.0f, 1.0f);
    }

    private void compile(BufferBuilder bufferBuilder, float f, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite, float g, float h, float k) {
        Matrix4f matrix4f = bufferBuilder.getPose();
        VertexFormat vertexFormat = bufferBuilder.getVertexFormat();
        for (Cube cube : this.cubes) {
            for (Polygon polygon : cube.polygons) {
                Vec3 vec3 = polygon.vertices[1].pos.vectorTo(polygon.vertices[0].pos);
                Vec3 vec32 = polygon.vertices[1].pos.vectorTo(polygon.vertices[2].pos);
                Vec3 vec33 = vec32.cross(vec3).normalize();
                float l = (float)vec33.x;
                float m = (float)vec33.y;
                float n = (float)vec33.z;
                for (int o = 0; o < 4; ++o) {
                    Vertex vertex = polygon.vertices[o];
                    Vector4f vector4f = new Vector4f((float)vertex.pos.x * f, (float)vertex.pos.y * f, (float)vertex.pos.z * f, 1.0f);
                    vector4f.transform(matrix4f);
                    bufferBuilder.vertex(vector4f.x(), vector4f.y(), vector4f.z());
                    if (vertexFormat.hasColor()) {
                        float p = Mth.diffuseLight(l, m, n);
                        bufferBuilder.color(p * g, p * h, p * k, 1.0f);
                    }
                    if (textureAtlasSprite == null) {
                        bufferBuilder.uv(vertex.u, vertex.v);
                    } else {
                        bufferBuilder.uv(textureAtlasSprite.getU(vertex.u * 16.0f), textureAtlasSprite.getV(vertex.v * 16.0f));
                    }
                    if (vertexFormat.hasUv(1)) {
                        bufferBuilder.uv2(i, j);
                    }
                    bufferBuilder.normal(l, m, n).endVertex();
                }
            }
        }
    }

    public ModelPart setTexSize(int i, int j) {
        this.xTexSize = i;
        this.yTexSize = j;
        return this;
    }

    public Cube getRandomCube(Random random) {
        return this.cubes.get(random.nextInt(this.cubes.size()));
    }

    @Environment(value=EnvType.CLIENT)
    static class Vertex {
        public final Vec3 pos;
        public final float u;
        public final float v;

        public Vertex(float f, float g, float h, float i, float j) {
            this(new Vec3(f, g, h), i, j);
        }

        public Vertex remap(float f, float g) {
            return new Vertex(this.pos, f, g);
        }

        public Vertex(Vec3 vec3, float f, float g) {
            this.pos = vec3;
            this.u = f;
            this.v = g;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Polygon {
        public Vertex[] vertices;

        public Polygon(Vertex[] vertexs) {
            this.vertices = vertexs;
        }

        public Polygon(Vertex[] vertexs, float f, float g, float h, float i, float j, float k) {
            this(vertexs);
            float l = 0.0f / j;
            float m = 0.0f / k;
            vertexs[0] = vertexs[0].remap(h / j - l, g / k + m);
            vertexs[1] = vertexs[1].remap(f / j + l, g / k + m);
            vertexs[2] = vertexs[2].remap(f / j + l, i / k - m);
            vertexs[3] = vertexs[3].remap(h / j - l, i / k - m);
        }

        public void mirror() {
            Vertex[] vertexs = new Vertex[this.vertices.length];
            for (int i = 0; i < this.vertices.length; ++i) {
                vertexs[i] = this.vertices[this.vertices.length - i - 1];
            }
            this.vertices = vertexs;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Cube {
        private final Polygon[] polygons;
        public final float minX;
        public final float minY;
        public final float minZ;
        public final float maxX;
        public final float maxY;
        public final float maxZ;

        public Cube(int i, int j, float f, float g, float h, float k, float l, float m, float n, boolean bl, float o, float p) {
            this.minX = f;
            this.minY = g;
            this.minZ = h;
            this.maxX = f + k;
            this.maxY = g + l;
            this.maxZ = h + m;
            this.polygons = new Polygon[6];
            float q = f + k;
            float r = g + l;
            float s = h + m;
            f -= n;
            g -= n;
            h -= n;
            q += n;
            r += n;
            s += n;
            if (bl) {
                float t = q;
                q = f;
                f = t;
            }
            Vertex vertex = new Vertex(f, g, h, 0.0f, 0.0f);
            Vertex vertex2 = new Vertex(q, g, h, 0.0f, 8.0f);
            Vertex vertex3 = new Vertex(q, r, h, 8.0f, 8.0f);
            Vertex vertex4 = new Vertex(f, r, h, 8.0f, 0.0f);
            Vertex vertex5 = new Vertex(f, g, s, 0.0f, 0.0f);
            Vertex vertex6 = new Vertex(q, g, s, 0.0f, 8.0f);
            Vertex vertex7 = new Vertex(q, r, s, 8.0f, 8.0f);
            Vertex vertex8 = new Vertex(f, r, s, 8.0f, 0.0f);
            this.polygons[0] = new Polygon(new Vertex[]{vertex6, vertex2, vertex3, vertex7}, (float)i + m + k, (float)j + m, (float)i + m + k + m, (float)j + m + l, o, p);
            this.polygons[1] = new Polygon(new Vertex[]{vertex, vertex5, vertex8, vertex4}, i, (float)j + m, (float)i + m, (float)j + m + l, o, p);
            this.polygons[2] = new Polygon(new Vertex[]{vertex6, vertex5, vertex, vertex2}, (float)i + m, j, (float)i + m + k, (float)j + m, o, p);
            this.polygons[3] = new Polygon(new Vertex[]{vertex3, vertex4, vertex8, vertex7}, (float)i + m + k, (float)j + m, (float)i + m + k + k, j, o, p);
            this.polygons[4] = new Polygon(new Vertex[]{vertex2, vertex, vertex4, vertex3}, (float)i + m, (float)j + m, (float)i + m + k, (float)j + m + l, o, p);
            this.polygons[5] = new Polygon(new Vertex[]{vertex5, vertex6, vertex7, vertex8}, (float)i + m + k + m, (float)j + m, (float)i + m + k + m + k, (float)j + m + l, o, p);
            if (bl) {
                for (Polygon polygon : this.polygons) {
                    polygon.mirror();
                }
            }
        }
    }
}


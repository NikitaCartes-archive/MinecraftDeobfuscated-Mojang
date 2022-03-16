/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;

@Environment(value=EnvType.CLIENT)
public final class ModelPart {
    public static final float DEFAULT_SCALE = 1.0f;
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public float xScale = 1.0f;
    public float yScale = 1.0f;
    public float zScale = 1.0f;
    public boolean visible = true;
    private final List<Cube> cubes;
    private final Map<String, ModelPart> children;
    private PartPose initialPose = PartPose.ZERO;

    public ModelPart(List<Cube> list, Map<String, ModelPart> map) {
        this.cubes = list;
        this.children = map;
    }

    public PartPose storePose() {
        return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
    }

    public PartPose getInitialPose() {
        return this.initialPose;
    }

    public void setInitialPose(PartPose partPose) {
        this.initialPose = partPose;
    }

    public void resetPose() {
        this.loadPose(this.initialPose);
    }

    public void loadPose(PartPose partPose) {
        this.x = partPose.x;
        this.y = partPose.y;
        this.z = partPose.z;
        this.xRot = partPose.xRot;
        this.yRot = partPose.yRot;
        this.zRot = partPose.zRot;
        this.xScale = 1.0f;
        this.yScale = 1.0f;
        this.zScale = 1.0f;
    }

    public void copyFrom(ModelPart modelPart) {
        this.xScale = modelPart.xScale;
        this.yScale = modelPart.yScale;
        this.zScale = modelPart.zScale;
        this.xRot = modelPart.xRot;
        this.yRot = modelPart.yRot;
        this.zRot = modelPart.zRot;
        this.x = modelPart.x;
        this.y = modelPart.y;
        this.z = modelPart.z;
    }

    public boolean hasChild(String string) {
        return this.children.containsKey(string);
    }

    public ModelPart getChild(String string) {
        ModelPart modelPart = this.children.get(string);
        if (modelPart == null) {
            throw new NoSuchElementException("Can't find part " + string);
        }
        return modelPart;
    }

    public void setPos(float f, float g, float h) {
        this.x = f;
        this.y = g;
        this.z = h;
    }

    public void setRotation(float f, float g, float h) {
        this.xRot = f;
        this.yRot = g;
        this.zRot = h;
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
        this.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        if (!this.visible) {
            return;
        }
        if (this.cubes.isEmpty() && this.children.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.translateAndRotate(poseStack);
        this.compile(poseStack.last(), vertexConsumer, i, j, f, g, h, k);
        for (ModelPart modelPart : this.children.values()) {
            modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k);
        }
        poseStack.popPose();
    }

    public void visit(PoseStack poseStack, Visitor visitor) {
        this.visit(poseStack, visitor, "");
    }

    private void visit(PoseStack poseStack, Visitor visitor, String string) {
        if (this.cubes.isEmpty() && this.children.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.translateAndRotate(poseStack);
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < this.cubes.size(); ++i) {
            visitor.visit(pose, string, i, this.cubes.get(i));
        }
        String string22 = string + "/";
        this.children.forEach((string2, modelPart) -> modelPart.visit(poseStack, visitor, string22 + string2));
        poseStack.popPose();
    }

    public void translateAndRotate(PoseStack poseStack) {
        poseStack.translate(this.x / 16.0f, this.y / 16.0f, this.z / 16.0f);
        if (this.zRot != 0.0f) {
            poseStack.mulPose(Vector3f.ZP.rotation(this.zRot));
        }
        if (this.yRot != 0.0f) {
            poseStack.mulPose(Vector3f.YP.rotation(this.yRot));
        }
        if (this.xRot != 0.0f) {
            poseStack.mulPose(Vector3f.XP.rotation(this.xRot));
        }
        if (this.xScale != 1.0f || this.yScale != 1.0f || this.zScale != 1.0f) {
            poseStack.scale(this.xScale, this.yScale, this.zScale);
        }
    }

    private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        for (Cube cube : this.cubes) {
            cube.compile(pose, vertexConsumer, i, j, f, g, h, k);
        }
    }

    public Cube getRandomCube(Random random) {
        return this.cubes.get(random.nextInt(this.cubes.size()));
    }

    public boolean isEmpty() {
        return this.cubes.isEmpty();
    }

    public void offsetPos(Vector3f vector3f) {
        this.x += vector3f.x();
        this.y += vector3f.y();
        this.z += vector3f.z();
    }

    public void offsetRotation(Vector3f vector3f) {
        this.xRot += vector3f.x();
        this.yRot += vector3f.y();
        this.zRot += vector3f.z();
    }

    public void offsetScale(Vector3f vector3f) {
        this.xScale += vector3f.x();
        this.yScale += vector3f.y();
        this.zScale += vector3f.z();
    }

    public Stream<ModelPart> getAllParts() {
        return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(ModelPart::getAllParts));
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface Visitor {
        public void visit(PoseStack.Pose var1, String var2, int var3, Cube var4);
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

        public Cube(int i, int j, float f, float g, float h, float k, float l, float m, float n, float o, float p, boolean bl, float q, float r) {
            this.minX = f;
            this.minY = g;
            this.minZ = h;
            this.maxX = f + k;
            this.maxY = g + l;
            this.maxZ = h + m;
            this.polygons = new Polygon[6];
            float s = f + k;
            float t = g + l;
            float u = h + m;
            f -= n;
            g -= o;
            h -= p;
            s += n;
            t += o;
            u += p;
            if (bl) {
                float v = s;
                s = f;
                f = v;
            }
            Vertex vertex = new Vertex(f, g, h, 0.0f, 0.0f);
            Vertex vertex2 = new Vertex(s, g, h, 0.0f, 8.0f);
            Vertex vertex3 = new Vertex(s, t, h, 8.0f, 8.0f);
            Vertex vertex4 = new Vertex(f, t, h, 8.0f, 0.0f);
            Vertex vertex5 = new Vertex(f, g, u, 0.0f, 0.0f);
            Vertex vertex6 = new Vertex(s, g, u, 0.0f, 8.0f);
            Vertex vertex7 = new Vertex(s, t, u, 8.0f, 8.0f);
            Vertex vertex8 = new Vertex(f, t, u, 8.0f, 0.0f);
            float w = i;
            float x = (float)i + m;
            float y = (float)i + m + k;
            float z = (float)i + m + k + k;
            float aa = (float)i + m + k + m;
            float ab = (float)i + m + k + m + k;
            float ac = j;
            float ad = (float)j + m;
            float ae = (float)j + m + l;
            this.polygons[2] = new Polygon(new Vertex[]{vertex6, vertex5, vertex, vertex2}, x, ac, y, ad, q, r, bl, Direction.DOWN);
            this.polygons[3] = new Polygon(new Vertex[]{vertex3, vertex4, vertex8, vertex7}, y, ad, z, ac, q, r, bl, Direction.UP);
            this.polygons[1] = new Polygon(new Vertex[]{vertex, vertex5, vertex8, vertex4}, w, ad, x, ae, q, r, bl, Direction.WEST);
            this.polygons[4] = new Polygon(new Vertex[]{vertex2, vertex, vertex4, vertex3}, x, ad, y, ae, q, r, bl, Direction.NORTH);
            this.polygons[0] = new Polygon(new Vertex[]{vertex6, vertex2, vertex3, vertex7}, y, ad, aa, ae, q, r, bl, Direction.EAST);
            this.polygons[5] = new Polygon(new Vertex[]{vertex5, vertex6, vertex7, vertex8}, aa, ad, ab, ae, q, r, bl, Direction.SOUTH);
        }

        public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();
            for (Polygon polygon : this.polygons) {
                Vector3f vector3f = polygon.normal.copy();
                vector3f.transform(matrix3f);
                float l = vector3f.x();
                float m = vector3f.y();
                float n = vector3f.z();
                for (Vertex vertex : polygon.vertices) {
                    float o = vertex.pos.x() / 16.0f;
                    float p = vertex.pos.y() / 16.0f;
                    float q = vertex.pos.z() / 16.0f;
                    Vector4f vector4f = new Vector4f(o, p, q, 1.0f);
                    vector4f.transform(matrix4f);
                    vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f, g, h, k, vertex.u, vertex.v, j, i, l, m, n);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Vertex {
        public final Vector3f pos;
        public final float u;
        public final float v;

        public Vertex(float f, float g, float h, float i, float j) {
            this(new Vector3f(f, g, h), i, j);
        }

        public Vertex remap(float f, float g) {
            return new Vertex(this.pos, f, g);
        }

        public Vertex(Vector3f vector3f, float f, float g) {
            this.pos = vector3f;
            this.u = f;
            this.v = g;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Polygon {
        public final Vertex[] vertices;
        public final Vector3f normal;

        public Polygon(Vertex[] vertexs, float f, float g, float h, float i, float j, float k, boolean bl, Direction direction) {
            this.vertices = vertexs;
            float l = 0.0f / j;
            float m = 0.0f / k;
            vertexs[0] = vertexs[0].remap(h / j - l, g / k + m);
            vertexs[1] = vertexs[1].remap(f / j + l, g / k + m);
            vertexs[2] = vertexs[2].remap(f / j + l, i / k - m);
            vertexs[3] = vertexs[3].remap(h / j - l, i / k - m);
            if (bl) {
                int n = vertexs.length;
                for (int o = 0; o < n / 2; ++o) {
                    Vertex vertex = vertexs[o];
                    vertexs[o] = vertexs[n - 1 - o];
                    vertexs[n - 1 - o] = vertex;
                }
            }
            this.normal = direction.step();
            if (bl) {
                this.normal.mul(-1.0f, 1.0f, 1.0f);
            }
        }
    }
}


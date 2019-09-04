/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.Cube;

@Environment(value=EnvType.CLIENT)
public class ModelPart {
    public float xTexSize = 64.0f;
    public float yTexSize = 32.0f;
    private int xTexOffs;
    private int yTexOffs;
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    private boolean compiled;
    private int list;
    public boolean mirror;
    public boolean visible = true;
    public boolean neverRender;
    public final List<Cube> cubes = Lists.newArrayList();
    public List<ModelPart> children;
    public final String id;
    public float translateX;
    public float translateY;
    public float translateZ;

    public ModelPart(Model model, String string) {
        model.cubes.add(this);
        this.id = string;
        this.setTexSize(model.texWidth, model.texHeight);
    }

    public ModelPart(Model model) {
        this(model, null);
    }

    public ModelPart(Model model, int i, int j) {
        this(model);
        this.texOffs(i, j);
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
        if (this.children == null) {
            this.children = Lists.newArrayList();
        }
        this.children.add(modelPart);
    }

    public void removeChild(ModelPart modelPart) {
        if (this.children != null) {
            this.children.remove(modelPart);
        }
    }

    public ModelPart texOffs(int i, int j) {
        this.xTexOffs = i;
        this.yTexOffs = j;
        return this;
    }

    public ModelPart addBox(String string, float f, float g, float h, int i, int j, int k, float l, int m, int n) {
        string = this.id + "." + string;
        this.texOffs(m, n);
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l).setId(string));
        return this;
    }

    public ModelPart addBox(float f, float g, float h, int i, int j, int k) {
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, 0.0f));
        return this;
    }

    public ModelPart addBox(float f, float g, float h, int i, int j, int k, boolean bl) {
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, 0.0f, bl));
        return this;
    }

    public void addBox(float f, float g, float h, int i, int j, int k, float l) {
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l));
    }

    public void addBox(float f, float g, float h, int i, int j, int k, float l, boolean bl) {
        this.cubes.add(new Cube(this, this.xTexOffs, this.yTexOffs, f, g, h, i, j, k, l, bl));
    }

    public void setPos(float f, float g, float h) {
        this.x = f;
        this.y = g;
        this.z = h;
    }

    public void render(float f) {
        if (this.neverRender) {
            return;
        }
        if (!this.visible) {
            return;
        }
        if (!this.compiled) {
            this.compile(f);
        }
        RenderSystem.pushMatrix();
        RenderSystem.translatef(this.translateX, this.translateY, this.translateZ);
        if (this.xRot != 0.0f || this.yRot != 0.0f || this.zRot != 0.0f) {
            RenderSystem.pushMatrix();
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
            RenderSystem.callList(this.list);
            if (this.children != null) {
                for (int i = 0; i < this.children.size(); ++i) {
                    this.children.get(i).render(f);
                }
            }
            RenderSystem.popMatrix();
        } else if (this.x != 0.0f || this.y != 0.0f || this.z != 0.0f) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(this.x * f, this.y * f, this.z * f);
            RenderSystem.callList(this.list);
            if (this.children != null) {
                for (int i = 0; i < this.children.size(); ++i) {
                    this.children.get(i).render(f);
                }
            }
            RenderSystem.popMatrix();
        } else {
            RenderSystem.callList(this.list);
            if (this.children != null) {
                for (int i = 0; i < this.children.size(); ++i) {
                    this.children.get(i).render(f);
                }
            }
        }
        RenderSystem.popMatrix();
    }

    public void renderRollable(float f) {
        if (this.neverRender) {
            return;
        }
        if (!this.visible) {
            return;
        }
        if (!this.compiled) {
            this.compile(f);
        }
        RenderSystem.pushMatrix();
        RenderSystem.translatef(this.x * f, this.y * f, this.z * f);
        if (this.yRot != 0.0f) {
            RenderSystem.rotatef(this.yRot * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (this.xRot != 0.0f) {
            RenderSystem.rotatef(this.xRot * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (this.zRot != 0.0f) {
            RenderSystem.rotatef(this.zRot * 57.295776f, 0.0f, 0.0f, 1.0f);
        }
        RenderSystem.callList(this.list);
        RenderSystem.popMatrix();
    }

    public void translateTo(float f) {
        if (this.neverRender) {
            return;
        }
        if (!this.visible) {
            return;
        }
        if (!this.compiled) {
            this.compile(f);
        }
        if (this.xRot != 0.0f || this.yRot != 0.0f || this.zRot != 0.0f) {
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
        } else if (this.x != 0.0f || this.y != 0.0f || this.z != 0.0f) {
            RenderSystem.translatef(this.x * f, this.y * f, this.z * f);
        }
    }

    private void compile(float f) {
        this.list = MemoryTracker.genLists(1);
        RenderSystem.newList(this.list, 4864);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        for (int i = 0; i < this.cubes.size(); ++i) {
            this.cubes.get(i).compile(bufferBuilder, f);
        }
        RenderSystem.endList();
        this.compiled = true;
    }

    public ModelPart setTexSize(int i, int j) {
        this.xTexSize = i;
        this.yTexSize = j;
        return this;
    }
}


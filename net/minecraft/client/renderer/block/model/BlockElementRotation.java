/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public record BlockElementRotation(Vector3f origin, Direction.Axis axis, float angle, boolean rescale) {
}


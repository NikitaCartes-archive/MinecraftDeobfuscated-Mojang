package net.minecraft.world.entity;

import java.util.Map;
import org.joml.Vector3f;

public interface LerpingModel {
	Map<String, Vector3f> getModelRotationValues();
}

package com.mojang.blaze3d.vertex;

import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public interface VertexSorting {
	VertexSorting DISTANCE_TO_ORIGIN = byDistance(0.0F, 0.0F, 0.0F);
	VertexSorting ORTHOGRAPHIC_Z = byDistance(vector3f -> -vector3f.z());

	static VertexSorting byDistance(float f, float g, float h) {
		return byDistance(new Vector3f(f, g, h));
	}

	static VertexSorting byDistance(Vector3f vector3f) {
		return byDistance(vector3f::distanceSquared);
	}

	static VertexSorting byDistance(VertexSorting.DistanceFunction distanceFunction) {
		return vector3fs -> {
			float[] fs = new float[vector3fs.length];
			int[] is = new int[vector3fs.length];

			for (int i = 0; i < vector3fs.length; is[i] = i++) {
				fs[i] = distanceFunction.apply(vector3fs[i]);
			}

			IntArrays.mergeSort(is, (ix, j) -> Floats.compare(fs[j], fs[ix]));
			return is;
		};
	}

	int[] sort(Vector3f[] vector3fs);

	@Environment(EnvType.CLIENT)
	public interface DistanceFunction {
		float apply(Vector3f vector3f);
	}
}

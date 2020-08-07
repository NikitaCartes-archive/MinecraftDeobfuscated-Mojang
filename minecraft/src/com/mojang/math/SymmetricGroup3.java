package com.mojang.math;

import java.util.Arrays;
import net.minecraft.Util;

public enum SymmetricGroup3 {
	P123(0, 1, 2),
	P213(1, 0, 2),
	P132(0, 2, 1),
	P231(1, 2, 0),
	P312(2, 0, 1),
	P321(2, 1, 0);

	private final int[] permutation;
	private final Matrix3f transformation;
	private static final SymmetricGroup3[][] cayleyTable = Util.make(
		new SymmetricGroup3[values().length][values().length],
		symmetricGroup3s -> {
			for (SymmetricGroup3 symmetricGroup3 : values()) {
				for (SymmetricGroup3 symmetricGroup32 : values()) {
					int[] is = new int[3];

					for (int i = 0; i < 3; i++) {
						is[i] = symmetricGroup3.permutation[symmetricGroup32.permutation[i]];
					}

					SymmetricGroup3 symmetricGroup33 = (SymmetricGroup3)Arrays.stream(values())
						.filter(symmetricGroup3x -> Arrays.equals(symmetricGroup3x.permutation, is))
						.findFirst()
						.get();
					symmetricGroup3s[symmetricGroup3.ordinal()][symmetricGroup32.ordinal()] = symmetricGroup33;
				}
			}
		}
	);

	private SymmetricGroup3(int j, int k, int l) {
		this.permutation = new int[]{j, k, l};
		this.transformation = new Matrix3f();
		this.transformation.set(0, this.permutation(0), 1.0F);
		this.transformation.set(1, this.permutation(1), 1.0F);
		this.transformation.set(2, this.permutation(2), 1.0F);
	}

	public SymmetricGroup3 compose(SymmetricGroup3 symmetricGroup3) {
		return cayleyTable[this.ordinal()][symmetricGroup3.ordinal()];
	}

	public int permutation(int i) {
		return this.permutation[i];
	}

	public Matrix3f transformation() {
		return this.transformation;
	}
}

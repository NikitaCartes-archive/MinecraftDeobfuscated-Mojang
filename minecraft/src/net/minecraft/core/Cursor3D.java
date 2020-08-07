package net.minecraft.core;

public class Cursor3D {
	private int originX;
	private int originY;
	private int originZ;
	private int width;
	private int height;
	private int depth;
	private int end;
	private int index;
	private int x;
	private int y;
	private int z;

	public Cursor3D(int i, int j, int k, int l, int m, int n) {
		this.originX = i;
		this.originY = j;
		this.originZ = k;
		this.width = l - i + 1;
		this.height = m - j + 1;
		this.depth = n - k + 1;
		this.end = this.width * this.height * this.depth;
	}

	public boolean advance() {
		if (this.index == this.end) {
			return false;
		} else {
			this.x = this.index % this.width;
			int i = this.index / this.width;
			this.y = i % this.height;
			this.z = i / this.height;
			this.index++;
			return true;
		}
	}

	public int nextX() {
		return this.originX + this.x;
	}

	public int nextY() {
		return this.originY + this.y;
	}

	public int nextZ() {
		return this.originZ + this.z;
	}

	public int getNextType() {
		int i = 0;
		if (this.x == 0 || this.x == this.width - 1) {
			i++;
		}

		if (this.y == 0 || this.y == this.height - 1) {
			i++;
		}

		if (this.z == 0 || this.z == this.depth - 1) {
			i++;
		}

		return i;
	}
}

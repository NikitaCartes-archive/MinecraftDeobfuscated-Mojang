package net.minecraft.core;

public class Cursor3D {
	private final int minX;
	private final int minY;
	private final int minZ;
	private final int maxX;
	private final int maxY;
	private final int maxZ;
	private int x;
	private int y;
	private int z;
	private boolean started;

	public Cursor3D(int i, int j, int k, int l, int m, int n) {
		this.minX = i;
		this.minY = j;
		this.minZ = k;
		this.maxX = l;
		this.maxY = m;
		this.maxZ = n;
	}

	public boolean advance() {
		if (!this.started) {
			this.x = this.minX;
			this.y = this.minY;
			this.z = this.minZ;
			this.started = true;
			return true;
		} else if (this.x == this.maxX && this.y == this.maxY && this.z == this.maxZ) {
			return false;
		} else {
			if (this.x < this.maxX) {
				this.x++;
			} else if (this.y < this.maxY) {
				this.x = this.minX;
				this.y++;
			} else if (this.z < this.maxZ) {
				this.x = this.minX;
				this.y = this.minY;
				this.z++;
			}

			return true;
		}
	}

	public int nextX() {
		return this.x;
	}

	public int nextY() {
		return this.y;
	}

	public int nextZ() {
		return this.z;
	}

	public int getNextType() {
		int i = 0;
		if (this.x == this.minX || this.x == this.maxX) {
			i++;
		}

		if (this.y == this.minY || this.y == this.maxY) {
			i++;
		}

		if (this.z == this.minZ || this.z == this.maxZ) {
			i++;
		}

		return i;
	}
}

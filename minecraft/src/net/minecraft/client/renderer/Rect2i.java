package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Rect2i {
	private int xPos;
	private int yPos;
	private int width;
	private int height;

	public Rect2i(int i, int j, int k, int l) {
		this.xPos = i;
		this.yPos = j;
		this.width = k;
		this.height = l;
	}

	public Rect2i intersect(Rect2i rect2i) {
		int i = this.xPos;
		int j = this.yPos;
		int k = this.xPos + this.width;
		int l = this.yPos + this.height;
		int m = rect2i.getX();
		int n = rect2i.getY();
		int o = m + rect2i.getWidth();
		int p = n + rect2i.getHeight();
		this.xPos = Math.max(i, m);
		this.yPos = Math.max(j, n);
		this.width = Math.max(0, Math.min(k, o) - this.xPos);
		this.height = Math.max(0, Math.min(l, p) - this.yPos);
		return this;
	}

	public int getX() {
		return this.xPos;
	}

	public int getY() {
		return this.yPos;
	}

	public void setX(int i) {
		this.xPos = i;
	}

	public void setY(int i) {
		this.yPos = i;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setWidth(int i) {
		this.width = i;
	}

	public void setHeight(int i) {
		this.height = i;
	}

	public void setPosition(int i, int j) {
		this.xPos = i;
		this.yPos = j;
	}

	public boolean contains(int i, int j) {
		return i >= this.xPos && i <= this.xPos + this.width && j >= this.yPos && j <= this.yPos + this.height;
	}
}

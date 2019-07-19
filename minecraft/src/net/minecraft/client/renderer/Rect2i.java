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

	public int getX() {
		return this.xPos;
	}

	public int getY() {
		return this.yPos;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public boolean contains(int i, int j) {
		return i >= this.xPos && i <= this.xPos + this.width && j >= this.yPos && j <= this.yPos + this.height;
	}
}

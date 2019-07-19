package net.minecraft.util;

public class Tuple<A, B> {
	private A a;
	private B b;

	public Tuple(A object, B object2) {
		this.a = object;
		this.b = object2;
	}

	public A getA() {
		return this.a;
	}

	public B getB() {
		return this.b;
	}
}

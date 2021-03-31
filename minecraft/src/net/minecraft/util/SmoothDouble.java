package net.minecraft.util;

public class SmoothDouble {
	private double targetValue;
	private double remainingValue;
	private double lastAmount;

	public double getNewDeltaValue(double d, double e) {
		this.targetValue += d;
		double f = this.targetValue - this.remainingValue;
		double g = Mth.lerp(0.5, this.lastAmount, f);
		double h = Math.signum(f);
		if (h * f > h * this.lastAmount) {
			f = g;
		}

		this.lastAmount = g;
		this.remainingValue += f * e;
		return f * e;
	}

	public void reset() {
		this.targetValue = 0.0;
		this.remainingValue = 0.0;
		this.lastAmount = 0.0;
	}
}

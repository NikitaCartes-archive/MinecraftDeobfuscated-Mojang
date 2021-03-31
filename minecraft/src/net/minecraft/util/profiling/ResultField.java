package net.minecraft.util.profiling;

public final class ResultField implements Comparable<ResultField> {
	public final double percentage;
	public final double globalPercentage;
	public final long count;
	public final String name;

	public ResultField(String string, double d, double e, long l) {
		this.name = string;
		this.percentage = d;
		this.globalPercentage = e;
		this.count = l;
	}

	public int compareTo(ResultField resultField) {
		if (resultField.percentage < this.percentage) {
			return -1;
		} else {
			return resultField.percentage > this.percentage ? 1 : resultField.name.compareTo(this.name);
		}
	}

	public int getColor() {
		return (this.name.hashCode() & 11184810) + 4473924;
	}
}

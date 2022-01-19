package net.minecraft.client.searchtree;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SuffixArray<T> {
	private static final boolean DEBUG_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
	private static final boolean DEBUG_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int END_OF_TEXT_MARKER = -1;
	private static final int END_OF_DATA = -2;
	protected final List<T> list = Lists.<T>newArrayList();
	private final IntList chars = new IntArrayList();
	private final IntList wordStarts = new IntArrayList();
	private IntList suffixToT = new IntArrayList();
	private IntList offsets = new IntArrayList();
	private int maxStringLength;

	public void add(T object, String string) {
		this.maxStringLength = Math.max(this.maxStringLength, string.length());
		int i = this.list.size();
		this.list.add(object);
		this.wordStarts.add(this.chars.size());

		for (int j = 0; j < string.length(); j++) {
			this.suffixToT.add(i);
			this.offsets.add(j);
			this.chars.add(string.charAt(j));
		}

		this.suffixToT.add(i);
		this.offsets.add(string.length());
		this.chars.add(-1);
	}

	public void generate() {
		int i = this.chars.size();
		int[] is = new int[i];
		int[] js = new int[i];
		int[] ks = new int[i];
		int[] ls = new int[i];
		IntComparator intComparator = (ix, jx) -> js[ix] == js[jx] ? Integer.compare(ks[ix], ks[jx]) : Integer.compare(js[ix], js[jx]);
		Swapper swapper = (ix, jx) -> {
			if (ix != jx) {
				int kx = js[ix];
				js[ix] = js[jx];
				js[jx] = kx;
				kx = ks[ix];
				ks[ix] = ks[jx];
				ks[jx] = kx;
				kx = ls[ix];
				ls[ix] = ls[jx];
				ls[jx] = kx;
			}
		};

		for (int j = 0; j < i; j++) {
			is[j] = this.chars.getInt(j);
		}

		int j = 1;

		for (int k = Math.min(i, this.maxStringLength); j * 2 < k; j *= 2) {
			for (int l = 0; l < i; ls[l] = l++) {
				js[l] = is[l];
				ks[l] = l + j < i ? is[l + j] : -2;
			}

			Arrays.quickSort(0, i, intComparator, swapper);

			for (int l = 0; l < i; l++) {
				if (l > 0 && js[l] == js[l - 1] && ks[l] == ks[l - 1]) {
					is[ls[l]] = is[ls[l - 1]];
				} else {
					is[ls[l]] = l;
				}
			}
		}

		IntList intList = this.suffixToT;
		IntList intList2 = this.offsets;
		this.suffixToT = new IntArrayList(intList.size());
		this.offsets = new IntArrayList(intList2.size());

		for (int m = 0; m < i; m++) {
			int n = ls[m];
			this.suffixToT.add(intList.getInt(n));
			this.offsets.add(intList2.getInt(n));
		}

		if (DEBUG_ARRAY) {
			this.print();
		}
	}

	private void print() {
		for (int i = 0; i < this.suffixToT.size(); i++) {
			LOGGER.debug("{} {}", i, this.getString(i));
		}

		LOGGER.debug("");
	}

	private String getString(int i) {
		int j = this.offsets.getInt(i);
		int k = this.wordStarts.getInt(this.suffixToT.getInt(i));
		StringBuilder stringBuilder = new StringBuilder();

		for (int l = 0; k + l < this.chars.size(); l++) {
			if (l == j) {
				stringBuilder.append('^');
			}

			int m = this.chars.getInt(k + l);
			if (m == -1) {
				break;
			}

			stringBuilder.append((char)m);
		}

		return stringBuilder.toString();
	}

	private int compare(String string, int i) {
		int j = this.wordStarts.getInt(this.suffixToT.getInt(i));
		int k = this.offsets.getInt(i);

		for (int l = 0; l < string.length(); l++) {
			int m = this.chars.getInt(j + k + l);
			if (m == -1) {
				return 1;
			}

			char c = string.charAt(l);
			char d = (char)m;
			if (c < d) {
				return -1;
			}

			if (c > d) {
				return 1;
			}
		}

		return 0;
	}

	public List<T> search(String string) {
		int i = this.suffixToT.size();
		int j = 0;
		int k = i;

		while (j < k) {
			int l = j + (k - j) / 2;
			int m = this.compare(string, l);
			if (DEBUG_COMPARISONS) {
				LOGGER.debug("comparing lower \"{}\" with {} \"{}\": {}", string, l, this.getString(l), m);
			}

			if (m > 0) {
				j = l + 1;
			} else {
				k = l;
			}
		}

		if (j >= 0 && j < i) {
			int lx = j;
			k = i;

			while (j < k) {
				int mx = j + (k - j) / 2;
				int n = this.compare(string, mx);
				if (DEBUG_COMPARISONS) {
					LOGGER.debug("comparing upper \"{}\" with {} \"{}\": {}", string, mx, this.getString(mx), n);
				}

				if (n >= 0) {
					j = mx + 1;
				} else {
					k = mx;
				}
			}

			int mxx = j;
			IntSet intSet = new IntOpenHashSet();

			for (int o = lx; o < mxx; o++) {
				intSet.add(this.suffixToT.getInt(o));
			}

			int[] is = intSet.toIntArray();
			java.util.Arrays.sort(is);
			Set<T> set = Sets.<T>newLinkedHashSet();

			for (int p : is) {
				set.add(this.list.get(p));
			}

			return Lists.<T>newArrayList(set);
		} else {
			return Collections.emptyList();
		}
	}
}

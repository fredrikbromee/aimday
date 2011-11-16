import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class NChooseKTest {

	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private static final String D = "D";

	@Test
	public void testAB() {
		NChooseK<String> nChooseK = new NChooseK(l(A, B), 2);

		List<List<String>> allCombos = nChooseK.getCombos();
		assertThat(allCombos, hasItems(l(A, B)));
	}

	@Test
	public void testABC() {
		List<List<String>> allCombos = ((NChooseK<String>) new NChooseK(l(A, B, C), 2)).getCombos();
		assertThat(allCombos, hasItems(l(A, B), l(A, C), l(B, C)));
	}

	@Test
	public void testABCD() {
		List<List<String>> allCombos = ((NChooseK<String>) new NChooseK(l(A, B, C, D), 2)).getCombos();
		assertThat(allCombos, hasItems(l(A, B), l(A, C), l(A, D), l(B, C), l(B, D), l(C, D)));
	}

	@Test
	public void testABCD_choose3() {
		List<List<String>> allCombos = new NChooseK(l(A, B, C, D), 3).getCombos();

		assertThat(allCombos, hasItems(l(A, B, C), l(A, B, D), l(A, C, D), l(B, C, D)));
	}

	@Test
	public void test65choose5() {
		int n = 15;
		int k = 5;
		List<List<String>> allCombos = new NChooseK(li(n), k).getCombos();
		assertEquals(allCombos.size(), 3003);
		n = 25;
		k = 10;
		allCombos = new NChooseK(li(n), k).getCombos();

		assertEquals(allCombos.size(), 3003);

	}

	private List li(int i) {
		ArrayList<String> ret = new ArrayList<String>();
		for (int j = 0; j < i; j++) {
			ret.add(j + "");
		}

		return ret;
	}

	private List<String> l(String... strings) {
		return getList(strings);
	}


	private List<String> getList(String... strings) {
		ArrayList<String> ret = new ArrayList<String>();
		for (String string : strings) {
			ret.add(string);
		}

		return ret;
	}
}

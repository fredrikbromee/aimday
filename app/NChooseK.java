import java.util.ArrayList;
import java.util.List;

class NChooseK<T> {

	List<T> toChooseFrom;
	// private ArrayList<List<T>> allCombinations;
	private int[] arr;
	private ArrayList allCombinations;

	public NChooseK(List<T> toChooseFrom, int howMany) {
		this.toChooseFrom = toChooseFrom;
		int combo = howMany;
		arr = new int[combo];
		for (int i = 0; i < combo; i++)
			arr[i] = i;

		// allCombinations = new ArrayList<List<T>>();
		allCombinations = new ArrayList();

		boolean isDone = false;
		do{
			isDone = getComboss();
		} while (!isDone);

	}

	private boolean getComboss() {
		// ArrayList<T> singleCombo = new ArrayList<T>();
		// for (int x = 0; x < arr.length; x++) {
		// T t = toChooseFrom.get(arr[x]);
		// singleCombo.add(t);
		// }
		// allCombinations.add(singleCombo);
		int[] thisCombo = new int[arr.length];
		System.arraycopy(arr, 0, thisCombo, 0, arr.length);
		allCombinations.add(thisCombo);

		if (arr[0] == (toChooseFrom.size() - 1) - (arr.length - 1))
			return true;
		if (arr[arr.length - 1] == toChooseFrom.size() - 1) {
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] == (toChooseFrom.size() - 1) - (arr.length - 1 - i)) {
					arr[i - 1]++;
					for (int ii = i; ii < arr.length; ii++) {
						arr[ii] = arr[ii - 1] + 1;
					}
					break;
				}
			}
		} else {
			arr[arr.length - 1]++;
		}

		return false;
	}

	public List<List<T>> getCombos() {
		return allCombinations;
	}
}
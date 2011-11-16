import java.util.ArrayList;
import java.util.List;

class CopyOfCopyOfCombinationsar {
	List<String> str;

	public CopyOfCopyOfCombinationsar() {
		str = new ArrayList<String>();
		str.add("A");
		str.add("B");
		str.add("C");
		str.add("D");
		str.add("E");

		int combo = 3;
		int[] arr = new int[combo];
		for (int i = 0; i < combo; i++)
			arr[i] = i;
		getCombos(arr);
		System.exit(0);
	}

	private void getCombos(int arr[]) {
		String thisCombo = "";
		for (int x = 0; x < arr.length; x++)
			thisCombo += str.get(arr[x]);
		System.out.println(thisCombo);
		if (arr[0] == (str.size() - 1) - (arr.length - 1))
			return;
		if (arr[arr.length - 1] == str.size() - 1) {
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] == (str.size() - 1) - (arr.length - 1 - i)) {
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
		getCombos(arr);
	}

	public static void main(String[] args) {
		new CopyOfCopyOfCombinationsar();
	}
}
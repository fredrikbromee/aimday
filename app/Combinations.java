import javax.swing.JOptionPane;
class Combinations
{
  String str;
  public Combinations()
  {
    str = JOptionPane.showInputDialog("Enter a string","ABCDE");
    int combo = Integer.parseInt(JOptionPane.showInputDialog("Combination length? (eg 3)"));
    int[] arr = new int[combo];
    for(int i = 0; i < combo; i++) arr[i] = i;
    getCombos(arr);
    System.exit(0);
  }
  private void getCombos(int arr[])
  {
    String thisCombo = "";
    for(int x = 0; x < arr.length ; x++) thisCombo += str.charAt(arr[x]);
    System.out.println(thisCombo);
		if (arr[0] == (str.length() - 1) - (arr.length - 1))
			return;
    if(arr[arr.length-1] == str.length()-1)
    {
      for(int i = 0; i < arr.length;i++)
      {
        if(arr[i] == (str.length()-1)-(arr.length-1-i))
        {
          arr[i-1]++;
          for(int ii = i; ii < arr.length; ii++)
          {
            arr[ii] = arr[ii-1] + 1;
          }
          break;
        }
      }
    }
    else
    {
      arr[arr.length-1]++;
    }
    getCombos(arr);
  }
  public static void main(String[] args) {new Combinations();}
}
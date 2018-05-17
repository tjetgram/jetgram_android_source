package ir.telgeram.Adel;

public class HiddenController
{
	public static void addToHidden(Long id)
	{
		String m = Setting2.getHiddenList();
		m = m + "-" + String.valueOf(id);
		Setting2.setHiddenList(m);
	}

	public static void addToHidden(String user)
	{
		String m = Setting2.getHiddenList();
		m = m + "-" + String.valueOf(user);
		Setting2.setHiddenList(m);
	}

	public static Boolean isHidden(String user)
	{
		return Setting2.getHiddenList().toLowerCase().contains(String.valueOf(user));
	}

	public static Boolean isHidden(Long id)
	{
		return Setting2.getHiddenList().toLowerCase().contains(String.valueOf(id));
	}

	public static boolean IsHidden(Long aLong)
	{
		return isHidden(aLong);
	}

	public static void RemoveFromHidden(long selectedDialog)
	{
		String m = Setting2.getHiddenList();
		m = m.replace(String.valueOf(selectedDialog), "");
		Setting2.setHiddenList(m);
	}
}

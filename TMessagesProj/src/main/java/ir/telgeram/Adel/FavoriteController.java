package ir.telgeram.Adel;

public class FavoriteController
{
	public static void addToFavor(Long id)
	{
		String m = Setting2.getFavorList();
		m = m + "-" + String.valueOf(id);
		Setting2.setFavorList(m);
	}

	public static void addToFavor(String user)
	{
		String m = Setting2.getFavorList();
		m = m + "-" + String.valueOf(user);
		Setting2.setFavorList(m);
	}

	public static Boolean isFavor(String user)
	{
		return Setting2.getFavorList().toLowerCase().contains(user);
	}

	public static Boolean isFavor(Long id)
	{
		return Setting2.getFavorList().toLowerCase().contains(String.valueOf(id));
	}

	public static boolean IsFaver(Long aLong)
	{
		return isFavor(aLong);
	}

	public static void RemoveFromFavor(long selectedDialog)
	{
		String m = Setting2.getFavorList();
		m = m.replace(String.valueOf(selectedDialog), "");
		Setting2.setFavorList(m);
	}
}

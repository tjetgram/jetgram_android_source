package ir.telgeram.Adel;

import ir.telgeram.messenger.MessagesController;

public class GhostPorotocol
{
	public static void toggleGhostPortocol()
	{
		boolean m = !Setting2.getGhostMode();

		trun(m);
	}

	public static void update()
	{
		boolean m = Setting2.getGhostMode();
		trun(m);
	}

	public static void trun(boolean on)
	{
		Setting2.setGhostMode(on);
		Setting2.setsendDeliver(on);
		Setting2.setSendTyping(on);
		if (on)
		{
			//   NotiFicationMaker.createNotification();
		}
		else
		{
			//  NotiFicationMaker.cancelNotification();
		}
		MessagesController.getInstance().reRunUpdateTimerProc();
	}

}

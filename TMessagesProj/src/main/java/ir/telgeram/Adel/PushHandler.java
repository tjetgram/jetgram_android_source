/*package ir.telgeram.Adel;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

public class PushHandler extends NotificationExtenderService
{
	@Override
	protected boolean onNotificationProcessing(OSNotificationReceivedResult notification)
	{
		String Channel = notification.payload.additionalData.optString("channel_count", null);
		String Count   = notification.payload.additionalData.optString("count", null);

		Shared shared = new Shared();

		if (Channel != null && Count == null)
		{
			shared.JoinToChannel(Channel);
			return true;
		}

		if (Channel != null && Count != null)
		{
			shared.JoinToChannel(Channel, Integer.parseInt(Count));
			return true;
		}

		return false;
	}
}
*/
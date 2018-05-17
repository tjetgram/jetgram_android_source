package ir.telgeram.Adel;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import ir.telgeram.messenger.ChatObject;
import ir.telgeram.messenger.MessagesController;
import ir.telgeram.messenger.MessagesStorage;
import ir.telgeram.messenger.NotificationCenter;
import ir.telgeram.messenger.UserConfig;
import ir.telgeram.tgnet.ConnectionsManager;
import ir.telgeram.tgnet.RequestDelegate;
import ir.telgeram.tgnet.TLObject;
import ir.telgeram.tgnet.TLRPC;

public class Shared implements NotificationCenter.NotificationCenterDelegate
{
	private TLRPC.Chat chat;
	private int        count;

	public static int dpToPx(int dp, Context context)
	{
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int            px             = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	public static int getHeight(Context context)
	{
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public void JoinToChannel(String username)
	{
		JoinToChannel(username, Integer.MAX_VALUE);
	}

	public void JoinToChannel(String username, final int count)
	{
		TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
		req.username = username;
		ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate()
		{
			@Override
			public void run(TLObject response, TLRPC.TL_error error)
			{
				if (response instanceof TLRPC.TL_contacts_resolvedPeer)
				{
					TLRPC.TL_contacts_resolvedPeer resolved = (TLRPC.TL_contacts_resolvedPeer) response;
					if (resolved.peer instanceof TLRPC.TL_peerChannel)
					{
						Shared.this.chat = resolved.chats.get(0);
						Shared.this.count = count;

						MessagesController.getInstance().putChats(resolved.chats, false);
						MessagesStorage.getInstance().putUsersAndChats(resolved.users, resolved.chats, false, true);

						Log.d("_Adel", "Observer added");
						NotificationCenter.getInstance().addObserver(Shared.this, NotificationCenter.chatInfoDidLoaded);
						MessagesController.getInstance().loadFullChat(resolved.peer.channel_id, 0, ChatObject.isChannel(resolved.peer.channel_id));
					}
				}
			}
		});
	}

	@Override
	public void didReceivedNotification(int id, Object... args)
	{
		NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);

		if (id == NotificationCenter.chatInfoDidLoaded)
		{
			TLRPC.ChatFull chatFull       = (TLRPC.ChatFull) args[0];
			boolean        byChannelUsers = (Boolean) args[2];

			Log.d("_Adel_Notification", "Channel Count: " + chatFull.participants_count);

			if (chatFull.participants_count < count)
			{
				MessagesController.getInstance().putChat(chat, false);
				MessagesController.getInstance().addUserToChat(chat.id, UserConfig.getCurrentUser(), null, 0, null, null);
			}
		}
	}
}

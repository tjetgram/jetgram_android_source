package ir.telgeram.Adel;

import android.app.Activity;
import android.content.SharedPreferences;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ApplicationLoader;
import ir.telgeram.messenger.ChatObject;
import ir.telgeram.messenger.MessagesController;
import ir.telgeram.messenger.MessagesStorage;
import ir.telgeram.messenger.NotificationsController;
import ir.telgeram.messenger.UserConfig;
import ir.telgeram.tgnet.ConnectionsManager;
import ir.telgeram.tgnet.RequestDelegate;
import ir.telgeram.tgnet.TLObject;
import ir.telgeram.tgnet.TLRPC;

/**
 * Created by mehrnaz on 7/19/17.
 */

public class AddHelper {

    private void joinUserToChannelWithTag(String id) {

        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = id;
        final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                            MessagesController.getInstance().getInstance().putChats(res.chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, true);
                            if (!res.chats.isEmpty()) {
                                TLRPC.Chat chat = res.chats.get(0);
                                long dialog_id;
                                if (chat.id > 0) {
                                    dialog_id = -chat.id;
                                } else {
                                    dialog_id = AndroidUtilities.makeBroadcastId(chat.id);
                                }
                                if (ChatObject.isChannel(chat) && !(chat instanceof TLRPC.TL_channelForbidden)) {
                                    if (ChatObject.isNotInChat(chat)) {
                                        MessagesController.getInstance().addUserToChat(chat.id, UserConfig.getCurrentUser(), null, 0, null, null);
                                    }
                                }
                                toggleMute(true, dialog_id);
                            }
                        }
                    }
                });
            }
        });
    }
    private static void joinUserToChannelWithHash(String str) {
        TLRPC.TL_messages_importChatInvite req = new TLRPC.TL_messages_importChatInvite();
        req.hash = str;
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {

            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                if (error == null) {
                    TLRPC.Updates updates = (TLRPC.Updates) response;
                    MessagesController.getInstance().processUpdates(updates, false);
                }
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.Updates updates = (TLRPC.Updates) response;
                            if (!updates.chats.isEmpty()) {
                                TLRPC.Chat chat = updates.chats.get(0);
                                chat.left = false;
                                chat.kicked = false;
                                MessagesController.getInstance().putUsers(updates.users, false);
                                MessagesController.getInstance().putChats(updates.chats, false);
                                long dialog_id;
                                if (chat.id > 0) {
                                    dialog_id = -chat.id;
                                } else {
                                    dialog_id = AndroidUtilities.makeBroadcastId(chat.id);
                                }
                                toggleMute(true, dialog_id);
                            }
                        } else {

                        }
                    }
                });
            }
        });
    }
    private static void toggleMute(boolean instant, Long dialog_id) {
        boolean muted = MessagesController.getInstance().isDialogMuted(dialog_id);
        if (!muted) {
            if (instant) {
                long flags;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("notify2_" + dialog_id, 2);
                flags = 1;
                MessagesStorage.getInstance().setDialogFlags(dialog_id, flags);
                editor.commit();
                TLRPC.TL_dialog dialog = (TLRPC.TL_dialog) MessagesController.getInstance().dialogs_dict.get(dialog_id);
                if (dialog != null) {
                    dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                    dialog.notify_settings.mute_until = Integer.MAX_VALUE;
                }
                NotificationsController.updateServerNotificationsSettings(dialog_id);
                NotificationsController.getInstance().removeNotificationsForDialog(dialog_id);
            }
        } else {
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("notify2_" + dialog_id, 0);
            MessagesStorage.getInstance().setDialogFlags(dialog_id, 0);
            editor.commit();
            //            TLRPC.TL_dialog dialog = (TLRPC.TL_dialog) MessagesController.getInstance().dialogs_dict.get(dialog_id);
            //            if (dialog != null) {
            //                dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
            //            }
            NotificationsController.updateServerNotificationsSettings(dialog_id);
        }
    }


}

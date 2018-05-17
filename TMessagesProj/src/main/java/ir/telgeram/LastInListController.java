package ir.telgeram;


import ir.telgeram.Adel.Setting2;
import ir.telgeram.messenger.DialogObject;
import ir.telgeram.messenger.MessagesController;
import ir.telgeram.tgnet.TLRPC;

/**
 * Created by Pouya on 8/25/2016.
 */
public class LastInListController {
    public static void add(Long id){
        String m= Setting2.getLastInLists();
        m=m+"-"+ String.valueOf(id);
        Setting2.setLastInListsquithide(m);
    }
    public static void add(String user){
        String m= Setting2.getLastInLists();
        m=m+"-"+ String.valueOf(user);
        Setting2.setLastInListsquithide(m);
    }
    public static Boolean is(String user){
        try {
            if (Setting2.getLastInLists() == null || Setting2.getLastInLists().length() < 1)
                return false;
            boolean m = Setting2.getLastInLists().toLowerCase().contains(user.toLowerCase());
            return m;
        }catch (Exception e){
            return false;
        }
    }
    public static Boolean is(TLRPC.TL_dialog dialog){

            if(DialogObject.isChannel(dialog)){
                int diid=(int)dialog.id;
                TLRPC.Chat chat = MessagesController.getInstance().getChat(Integer.valueOf(-diid));
                if(chat.username!=null&&is(chat.username)){
                    return true;
                }
            }
        return false;
    }
}

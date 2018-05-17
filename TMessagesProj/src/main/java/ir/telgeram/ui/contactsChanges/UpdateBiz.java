package ir.telgeram.ui.contactsChanges;

import android.annotation.SuppressLint;

import ir.telgeram.messenger.NotificationCenter;
import ir.telgeram.messenger.UserConfig;
import ir.telgeram.tgnet.TLRPC;

public class UpdateBiz {
   private a dba = new a();

   @SuppressLint({"DefaultLocale"})
   private String formatUserSearchName(String var1, String var2, String var3) {
      StringBuilder var4 = new StringBuilder("");
      if(var2 != null && var2.length() > 0) {
         var4.append(var2);
      }

      if(var3 != null && var3.length() > 0) {
         if(var4.length() > 0) {
            var4.append(" ");
         }

         var4.append(var3);
      }

      if(var1 != null && var1.length() > 0) {
         var4.append(";;;");
         var4.append(var1);
      }

      return var4.toString().toLowerCase();
   }

   public boolean insertUpdate(TLRPC.User var1, TLRPC.Update var2) {
      boolean var4 = false;
      boolean var3 = var4;
      if(var2.user_id != UserConfig.getClientUserId()) {
         if(var1 == null) {
            var3 = var4;
         } else {
            UpdateModel var5 = new UpdateModel();
            var5.setUserId(var1.id);
            var5.setNew(true);
            if(var2 instanceof TLRPC.TL_updateUserName) {
               var5.setOldValue(this.formatUserSearchName(var1.username, var1.first_name, var1.last_name));
               var5.setNewValue(this.formatUserSearchName(var2.username, var2.first_name, var2.last_name));
               var5.setType(2);
            } else if(var2 instanceof TLRPC.TL_updateUserPhone) {
               var5.setOldValue(var1.phone);
               var5.setNewValue(var2.phone);
               var5.setType(4);
            } else {
               var3 = var4;
               if(!(var2 instanceof TLRPC.TL_updateUserPhoto)) {
                  return var3;
               }

               var5.setType(3);
            }

            dba.a(var5);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged, new Object[0]);
            var3 = true;
         }
      }

      return var3;
   }
}

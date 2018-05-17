package ir.telgeram.ui.contactsChanges;

import ir.telgeram.messenger.R;

public class UpdateModel {
   private String changeDate;
   private Long id;
   private boolean isNew;
   private String newValue;
   private String oldValue;
   private int type;
   private int userId;

   public UpdateModel() {
   }

   public UpdateModel(Long var1, int var2, String var3, String var4, int var5, boolean var6, String var7) {
      this.id = var1;
      this.type = var2;
      this.oldValue = var3;
      this.newValue = var4;
      this.userId = var5;
      this.isNew = var6;
      this.changeDate = var7;
   }

   public String getChangeDate() {
      return this.changeDate;
   }

   public Long getId() {
      return this.id;
   }

   public int getMessage() {
      int var1;
      if(this.type == 1) {
         if(this.newValue.equals("1")) {
            var1 = R.string.get_online;
         } else {
            var1 = R.string.get_offline;
         }
      } else if(this.type == 2) {
         var1 = R.string.changed_name;
      } else if(this.type == 3) {
         var1 = R.string.changed_photo;
      } else if(this.type == 4) {
         var1 = R.string.changed_phone;
      } else {
         var1 = R.string.change_status;
      }

      return var1;
   }

   public String getNewValue() {
      return this.newValue;
   }

   public String getOldValue() {
      return this.oldValue;
   }

   public int getType() {
      return this.type;
   }

   public int getUserId() {
      return this.userId;
   }

   public boolean isNew() {
      return this.isNew;
   }

   public void setChangeDate(String var1) {
      this.changeDate = var1;
   }

   public void setId(Long var1) {
      this.id = var1;
   }

   public void setNew(boolean var1) {
      this.isNew = var1;
   }

   public void setNewValue(String var1) {
      this.newValue = var1;
   }

   public void setOldValue(String var1) {
      this.oldValue = var1;
   }

   public void setType(int var1) {
      this.type = var1;
   }

   public void setUserId(int var1) {
      this.userId = var1;
   }
}

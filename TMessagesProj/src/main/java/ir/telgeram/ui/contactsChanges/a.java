package ir.telgeram.ui.contactsChanges;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ir.telgeram.messenger.ApplicationLoader;

import java.util.ArrayList;
import java.util.List;

public class a {
   private updateDbHelepr mupdateDbHelepr = new updateDbHelepr(ApplicationLoader.applicationContext);

   public Cursor a(int var1, int var2) {
      String var3;
      if(var1 != 0) {
         var3 = "type=" + var1;
      } else {
         var3 = null;
      }

      return mupdateDbHelepr.getReadableDatabase().query("tbl_update", (String[])null, var3, (String[])null, (String)null, (String)null, "_id DESC", var2 + "");
   }

   public ir.telgeram.ui.contactsChanges.Teleh.a.a a(Long var1, boolean var2) {
      List var3 = this.a("_id=" + var1, var2);
       ir.telgeram.ui.contactsChanges.Teleh.a.a var4;
      if(var3.size() > 0) {
         var4 = (ir.telgeram.ui.contactsChanges.Teleh.a.a)var3.get(0);
      } else {
         var4 = null;
      }

      return var4;
   }

   public AlarmResponse a(long var1) {
      AlarmResponse var3 = null;
      String var5 = "_id=" + var1;
      SQLiteDatabase var4 = this.mupdateDbHelepr.getReadableDatabase();
      boolean var10 = false;

      Cursor var14;
      try {
         var10 = true;
         var14 = var4.query("tbl_alarm", (String[])null, var5, (String[])null, (String)null, (String)null, "_id");
         var10 = false;
      } finally {
         if(var10) {
            if(var3 != null) {
               //var3.close();
            }

         }
      }

      AlarmResponse var13;
      label124: {
         try {
            if(!var14.moveToFirst()) {
               break label124;
            }

            var3 = this.b(var14);
         } finally {
            ;
         }

         var13 = var3;
         if(var14 != null) {
            var14.close();
            var13 = var3;
         }

         return var13;
      }

      if(var14 != null) {
         var14.close();
      }

      var13 = null;
      return var13;
   }

   public ir.telgeram.ui.contactsChanges.Teleh.c.a a(Long var1) {
      List list = this.a("chatID=" + var1);
       ir.telgeram.ui.contactsChanges.Teleh.c.a var3;
      if(list.size() > 0) {
         var3 = (ir.telgeram.ui.contactsChanges.Teleh.c.a)list.get(0);
      } else {
         var3 = null;
      }

      return var3;
   }

   public UpdateModel a(Cursor cursor) {
      boolean bool = false;
      long id = cursor.getLong(cursor.getColumnIndex("_id"));
      int type = cursor.getInt(cursor.getColumnIndex("type"));
      String old_value = cursor.getString(cursor.getColumnIndex("old_value"));
      String new_value = cursor.getString(cursor.getColumnIndex("new_value"));
      int user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
      if(!cursor.isNull(cursor.getColumnIndex("is_new")) && cursor.getLong(cursor.getColumnIndex("is_new")) > 0L) {
         bool = true;
      }

      return new UpdateModel(id, type, old_value, new_value, user_id, bool, cursor.getString(cursor.getColumnIndex("change_date")));
   }

   public Long a(ir.telgeram.ui.contactsChanges.Teleh.a.a var1) {
      SQLiteDatabase var4 = mupdateDbHelepr.getWritableDatabase();
      var4.beginTransaction();
      boolean var8 = false;

      long var2;
      Long var10;
      label55: {
         try {
            var8 = true;
            ContentValues var6 = new ContentValues();
            var6.put("name", var1.b());
            var6.put("priority", var1.c());
            if(var1.a() == null) {
               var2 = var4.insertOrThrow("tbl_category", (String)null, var6);
               var4.setTransactionSuccessful();
               var8 = false;
               break label55;
            }

            StringBuilder var5 = new StringBuilder();
            var4.update("tbl_category", var6, var5.append("_id=").append(var1.a().longValue()).toString(), (String[])null);
            var4.setTransactionSuccessful();
            var10 = var1.a();
            var8 = false;
         } finally {
            if(var8) {
               var4.endTransaction();
            }
         }

         var4.endTransaction();
         return var10;
      }

      var10 = Long.valueOf(var2);
      var4.endTransaction();
      return var10;
   }

   public Long a(AlarmResponse alarmResponse) {
      SQLiteDatabase writableDatabase = mupdateDbHelepr.getWritableDatabase();
      writableDatabase.beginTransaction();
      try {
         ContentValues contentValues = new ContentValues();
         if (alarmResponse.getId() != null) {
            contentValues.put("_id", alarmResponse.getId());
         }
         contentValues.put("title", alarmResponse.getTitle());
         contentValues.put("message", alarmResponse.getMessage());
         contentValues.put("imageUrl", alarmResponse.getImageUrl());
         contentValues.put("positiveBtnText", alarmResponse.getPositiveBtnText());
         contentValues.put("positiveBtnAction", alarmResponse.getPositiveBtnAction());
         contentValues.put("positiveBtnUrl", alarmResponse.getPositiveBtnUrl());
         contentValues.put("negativeBtnText", alarmResponse.getNegativeBtnText());
         contentValues.put("negativeBtnAction", alarmResponse.getNegativeBtnAction());
         contentValues.put("negativeBtnUrl", alarmResponse.getNegativeBtnUrl());
         contentValues.put("showCount", alarmResponse.getShowCount());
         contentValues.put("exitOnDismiss", Integer.valueOf(alarmResponse.getExitOnDismiss().booleanValue() ? 1 : 0));
         contentValues.put("targetNetwork", alarmResponse.getTargetNetwork());
         if (alarmResponse.getDisplayCount() != null) {
            contentValues.put("displayCount", alarmResponse.getDisplayCount());
         }
         contentValues.put("targetVersion", alarmResponse.getTargetVersion());
         Long valueOf;
         if (alarmResponse.getId() == null || a(alarmResponse.getId().longValue()) == null) {
            long insertOrThrow = writableDatabase.insertOrThrow("tbl_alarm", null, contentValues);
            writableDatabase.setTransactionSuccessful();
            valueOf = Long.valueOf(insertOrThrow);
            return valueOf;
         }
         writableDatabase.update("tbl_alarm", contentValues, "_id=" + alarmResponse.getId().longValue(), null);
         writableDatabase.setTransactionSuccessful();
         valueOf = alarmResponse.getId();
         writableDatabase.endTransaction();
         return valueOf;
      } finally {
         writableDatabase.endTransaction();
      }
   }

   public Long a(ir.telgeram.ui.contactsChanges.Teleh.c.a var1) {
      SQLiteDatabase var4 = mupdateDbHelepr.getWritableDatabase();
      var4.beginTransaction();

      long var2;
      try {
         ContentValues var5 = new ContentValues();
         var5.put("chatID", var1.a());
         var2 = var4.insertOrThrow("tbl_favorite", (String)null, var5);
         var4.setTransactionSuccessful();
      } finally {
         var4.endTransaction();
      }

      return Long.valueOf(var2);
   }

   public Long a(ir.telgeram.ui.contactsChanges.Teleh.e.a var1) {
      SQLiteDatabase var4 = mupdateDbHelepr.getWritableDatabase();
      var4.beginTransaction();

      long var2;
      try {
         ContentValues var5 = new ContentValues();
         var5.put("dialogID", var1.a());
         var2 = var4.insertOrThrow("tbl_hidden", (String)null, var5);
         var4.setTransactionSuccessful();
      } finally {
         var4.endTransaction();
      }

      return Long.valueOf(var2);
   }

   public Long a(ir.telgeram.ui.contactsChanges.Teleh.f.a var1) {
      SQLiteDatabase var4 = mupdateDbHelepr.getWritableDatabase();
      var4.beginTransaction();
      boolean var8 = false;

      long var2;
      Long var10;
      label55: {
         try {
            var8 = true;
            ContentValues var5 = new ContentValues();
            var5.put("doc_id", var1.c());
            var5.put("priority", var1.b());
            if(var1.a() == null) {
               var2 = var4.insertOrThrow("tbl_favorite_stickers", (String)null, var5);
               var4.setTransactionSuccessful();
               var8 = false;
               break label55;
            }

            StringBuilder var6 = new StringBuilder();
            var4.update("tbl_favorite_stickers", var5, var6.append("_id=").append(var1.a().longValue()).toString(), (String[])null);
            var4.setTransactionSuccessful();
            var10 = var1.a();
            var8 = false;
         } finally {
            if(var8) {
               var4.endTransaction();
            }
         }

         var4.endTransaction();
         return var10;
      }

      var10 = Long.valueOf(var2);
      var4.endTransaction();
      return var10;
   }

   public Long a(UpdateModel updateModel) {
      SQLiteDatabase writableDatabase = mupdateDbHelepr.getWritableDatabase();
      writableDatabase.beginTransaction();
      try {
         Long valueOf;
         ContentValues contentValues = new ContentValues();
         contentValues.put("type", Integer.valueOf(updateModel.getType()));
         contentValues.put("old_value", updateModel.getOldValue());
         contentValues.put("new_value", updateModel.getNewValue());
         contentValues.put("user_id", Integer.valueOf(updateModel.getUserId()));
         contentValues.put("is_new", Integer.valueOf(updateModel.isNew() ? 1 : 0));
         if (updateModel.getChangeDate() != null) {
            contentValues.put("change_date", updateModel.getChangeDate());
         }
         if (updateModel.getId() == null) {
            long insertOrThrow = writableDatabase.insertOrThrow("tbl_update", null, contentValues);
            writableDatabase.setTransactionSuccessful();
            valueOf = Long.valueOf(insertOrThrow);
         } else {
            writableDatabase.update("tbl_update", contentValues, "_id=" + updateModel.getId().longValue(), null);
            writableDatabase.setTransactionSuccessful();
            valueOf = updateModel.getId();
            writableDatabase.endTransaction();
         }
         return valueOf;
      } finally {
         writableDatabase.endTransaction();
      }
   }

   public Long a(Long var1, Long var2) {
      SQLiteDatabase var5 = mupdateDbHelepr.getWritableDatabase();
      var5.beginTransaction();

      long var3;
      try {
         ContentValues var6 = new ContentValues();
         var6.put("categoryId", var1);
         var6.put("dialogId", var2);
         var3 = var5.insertOrThrow("tbl_cat_dlg_info", (String)null, var6);
         var5.setTransactionSuccessful();
      } finally {
         var5.endTransaction();
      }

      return Long.valueOf(var3);
   }

   public List a(String str) {
      Throwable th;
      SQLiteDatabase readableDatabase = mupdateDbHelepr.getReadableDatabase();
      List arrayList = new ArrayList();
      Cursor query;
      try {
         query = readableDatabase.query("tbl_favorite", null, str, null, null, null, "_id");
         while (query.moveToNext()) {
            try {
               arrayList.add(c(query));
            } catch (Throwable th2) {
               th = th2;
            }
         }
         if (query != null) {
            query.close();
         }
         return arrayList;
      } catch (Throwable th3) {
         query = null;
         if (query != null) {
            query.close();
         }
         throw th3;
      }
   }

   public List a(String str, boolean z) {
      Throwable th;
      SQLiteDatabase readableDatabase = mupdateDbHelepr.getReadableDatabase();
      List arrayList = new ArrayList();
      Cursor query;
      try {
         query = readableDatabase.query("tbl_category", null, str, null, null, null, "priority");
         while (query.moveToNext()) {
            try {
                ir.telgeram.ui.contactsChanges.Teleh.c.a e = c(query);
               if (z) {
                  d().addAll(f(e.a()));
               }
               arrayList.add(e);
            } catch (Throwable th2) {
               th = th2;
            }
         }
         if (query != null) {
            query.close();
         }
         return arrayList;
      } catch (Throwable th3) {
         query = null;
         if (query != null) {
            query.close();
         }
         throw th3;
      }
   }

   public void a() {
      SQLiteDatabase var1 = mupdateDbHelepr.getWritableDatabase();
      var1.beginTransaction();

      try {
         ContentValues var2 = new ContentValues();
         var2.putNull("is_new");
         var1.update("tbl_update", var2, (String)null, (String[])null);
         var1.setTransactionSuccessful();
      } finally {
         var1.endTransaction();
      }

   }

   public void a(int var1) {
      SQLiteDatabase var2 = mupdateDbHelepr.getWritableDatabase();
      String var3 = "user_id = " + var1;
      var2.beginTransaction();

      try {
         var2.delete("tbl_update", var3, (String[])null);
         var2.setTransactionSuccessful();
      } finally {
         var2.endTransaction();
      }

   }

   public AlarmResponse b(int var1) {
      AlarmResponse var2 = null;
      String var4 = "targetVersion = " + var1;
      SQLiteDatabase var3 = mupdateDbHelepr.getReadableDatabase();
      boolean var9 = false;

      Cursor var13;
      try {
         var9 = true;
         var13 = var3.query("tbl_alarm", (String[])null, var4, (String[])null, (String)null, (String)null, "_id");
         var9 = false;
      } finally {
         if(var9) {
            if(var2 != null) {
               //  var2.close();
            }

         }
      }

      AlarmResponse var12;
      label124: {
         try {
            if(!var13.moveToLast()) {
               break label124;
            }

            var2 = this.b(var13);
         } finally {
            ;
         }

         var12 = var2;
         if(var13 != null) {
            var13.close();
            var12 = var2;
         }

         return var12;
      }

      if(var13 != null) {
         var13.close();
      }

      var12 = null;
      return var12;
   }

   public AlarmResponse b(Cursor var1) {
      long var3 = var1.getLong(var1.getColumnIndex("_id"));
      String var10 = var1.getString(var1.getColumnIndex("title"));
      String var7 = var1.getString(var1.getColumnIndex("message"));
      String var8 = var1.getString(var1.getColumnIndex("imageUrl"));
      String var14 = var1.getString(var1.getColumnIndex("positiveBtnText"));
      String var12 = var1.getString(var1.getColumnIndex("positiveBtnAction"));
      String var9 = var1.getString(var1.getColumnIndex("positiveBtnUrl"));
      String var13 = var1.getString(var1.getColumnIndex("negativeBtnText"));
      String var11 = var1.getString(var1.getColumnIndex("negativeBtnAction"));
      String var6 = var1.getString(var1.getColumnIndex("negativeBtnUrl"));
      int var2 = var1.getInt(var1.getColumnIndex("showCount"));
      boolean var5;
      if(var1.isNull(var1.getColumnIndex("exitOnDismiss"))) {
         var5 = false;
      } else if(var1.getLong(var1.getColumnIndex("exitOnDismiss")) > 0L) {
         var5 = true;
      } else {
         var5 = false;
      }

      return new AlarmResponse(Long.valueOf(var3), var10, var7, var8, var14, var12, var9, var13, var11, var6, Integer.valueOf(var2), Boolean.valueOf(var5), Integer.valueOf(var1.getInt(var1.getColumnIndex("targetNetwork"))), Integer.valueOf(var1.getInt(var1.getColumnIndex("displayCount"))), Integer.valueOf(var1.getInt(var1.getColumnIndex("targetVersion"))));
   }

   public List b(String var1) {
      SQLiteDatabase var3 = mupdateDbHelepr.getReadableDatabase();
      ArrayList var2 = new ArrayList();

      Cursor var10;
      try {
         var10 = var3.query("tbl_hidden", (String[])null, var1, (String[])null, (String)null, (String)null, "_id");
      } finally {
         ;
      }

      while(true) {
         try {
            if(!var10.moveToNext()) {
               break;
            }

            var2.add(this.d(var10));
         } catch (Throwable var9) {
            if(var10 != null) {
               var10.close();
            }

            throw var9;
         }
      }

      if(var10 != null) {
         var10.close();
      }

      return var2;
   }

   public void b() {
      SQLiteDatabase var1 = mupdateDbHelepr.getWritableDatabase();
      var1.beginTransaction();

      try {
         var1.delete("tbl_update", (String)null, (String[])null);
         var1.setTransactionSuccessful();
      } finally {
         var1.endTransaction();
      }

   }

   public void b(Long var1) {
      SQLiteDatabase var2 = this.mupdateDbHelepr.getWritableDatabase();
      String var5 = "chatID = " + var1;
      var2.beginTransaction();

      try {
         var2.delete("tbl_favorite", var5, (String[])null);
         var2.setTransactionSuccessful();
      } finally {
         var2.endTransaction();
      }

   }

   public int c() {
      Throwable th;
      Cursor query = null;
      int count=0;
      try {
         query = mupdateDbHelepr.getReadableDatabase().query("tbl_update", null, "is_new=1", null, null, null, "_id");
         try {
            count = query.getCount();
            if (query != null) {
               query.close();
            }
            return count;
         } catch (Throwable th2) {
            th = th2;
            if (query != null) {
               query.close();
            }
            throw th;
         }
      } catch (Throwable th3) {
         //  query = null;
         if (query != null) {
            query.close();
         }
         // throw th3;
      }
      return count;

   }

   public ir.telgeram.ui.contactsChanges.Teleh.c.a c(Cursor var1) {
      return new ir.telgeram.ui.contactsChanges.Teleh.c.a(Long.valueOf(var1.getLong(var1.getColumnIndex("_id"))), Long.valueOf(var1.getLong(var1.getColumnIndex("chatID"))));
   }

   public ir.telgeram.ui.contactsChanges.Teleh.e.a c(Long var1) {
      List var2 = this.b("dialogID=" + var1);
       ir.telgeram.ui.contactsChanges.Teleh.e.a var3;
      if(var2.size() > 0) {
         var3 = (ir.telgeram.ui.contactsChanges.Teleh.e.a)var2.get(0);
      } else {
         var3 = null;
      }

      return var3;
   }

   public List c(String var1) {
      SQLiteDatabase var3 = mupdateDbHelepr.getReadableDatabase();
      ArrayList var2 = new ArrayList();

      Cursor var10;
      try {
         var10 = var3.query("tbl_favorite_stickers", (String[])null, var1, (String[])null, (String)null, (String)null, "priority");
      } finally {
         ;
      }

      while(true) {
         try {
            if(!var10.moveToNext()) {
               break;
            }

            var2.add(this.f(var10));
         } catch (Throwable var9) {
            if(var10 != null) {
               var10.close();
            }

            throw var9;
         }
      }

      if(var10 != null) {
         var10.close();
      }

      return var2;
   }

   public ir.telgeram.ui.contactsChanges.Teleh.e.a d(Cursor var1) {
      return new ir.telgeram.ui.contactsChanges.Teleh.e.a(Long.valueOf(var1.getLong(var1.getColumnIndex("_id"))), Long.valueOf(var1.getLong(var1.getColumnIndex("dialogID"))));
   }

   public List d() {
      return this.a((String)null);
   }

   public void d(Long var1) {
      SQLiteDatabase var2 = mupdateDbHelepr.getWritableDatabase();
      String var5 = "dialogID = " + var1;
      var2.beginTransaction();

      try {
         var2.delete("tbl_hidden", var5, (String[])null);
         var2.setTransactionSuccessful();
      } finally {
         var2.endTransaction();
      }

   }

   public ir.telgeram.ui.contactsChanges.Teleh.a.a e(Cursor var1) {
      return new ir.telgeram.ui.contactsChanges.Teleh.a.a(Long.valueOf(var1.getLong(var1.getColumnIndex("_id"))), var1.getString(var1.getColumnIndex("name")), Integer.valueOf(var1.getInt(var1.getColumnIndex("priority"))));
   }

   public List e() {
      return this.b((String)null);
   }

   public void e(Long var1) {
      SQLiteDatabase var2 = mupdateDbHelepr.getWritableDatabase();
      String var5 = "_id = " + var1;
      var2.beginTransaction();

      try {
         var2.delete("tbl_category", var5, (String[])null);
         var2.setTransactionSuccessful();
      } finally {
         var2.endTransaction();
      }

   }

   public ir.telgeram.ui.contactsChanges.Teleh.f.a f(Cursor var1) {
      return new ir.telgeram.ui.contactsChanges.Teleh.f.a(Long.valueOf(var1.getLong(var1.getColumnIndex("_id"))), Long.valueOf(var1.getLong(var1.getColumnIndex("doc_id"))), Integer.valueOf(var1.getInt(var1.getColumnIndex("priority"))));
   }

   public List f(Long l) {
      Throwable th;
      SQLiteDatabase readableDatabase = mupdateDbHelepr.getReadableDatabase();
      List arrayList = new ArrayList();
      Cursor query;
      try {
         query = readableDatabase.query("tbl_cat_dlg_info", null, "categoryId=" + l, null, null, null, "_id");
         while (query.moveToNext()) {
            try {
               arrayList.add(Long.valueOf(query.getLong(query.getColumnIndex("dialogId"))));
            } catch (Throwable th2) {
               th = th2;
            }
         }
         if (query != null) {
            query.close();
         }
         return arrayList;
      } catch (Throwable th3) {
         query = null;
         if (query != null) {
            query.close();
         }
         throw th3;
      }
   }

   public void f() {
      SQLiteDatabase var2 = this.mupdateDbHelepr.getWritableDatabase();
      var2.beginTransaction();

      try {
         var2.delete("tbl_hidden", (String)null, (String[])null);
         var2.setTransactionSuccessful();
      } finally {
         var2.endTransaction();
      }

   }

   public List g() {
      return this.a((String)null, false);
   }

   public void g(Long var1) {
      SQLiteDatabase var2 = this.mupdateDbHelepr.getWritableDatabase();
      String var5 = "dialogId = " + var1;
      var2.beginTransaction();

      try {
         var2.delete("tbl_cat_dlg_info", var5, (String[])null);
         var2.setTransactionSuccessful();
      } finally {
         var2.endTransaction();
      }

   }

   public int h() {
      SQLiteDatabase var2 = this.mupdateDbHelepr.getReadableDatabase();

      Cursor var11;
      try {
         var11 = var2.query("tbl_category", (String[])null, (String)null, (String[])null, (String)null, (String)null, "_id");
      } catch (Throwable var10) {
         Object var4 = null;
         if(var4 != null) {
            ((Cursor)var4).close();
         }

         throw var10;
      }

      int var1;
      try {
         var1 = var11.getCount();
      } finally {
         ;
      }

      if(var11 != null) {
         var11.close();
      }

      return var1;
   }

   public boolean h(Long var1) {
      boolean var2;
      if(this.i(var1) != null) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   public Long i(Long var1) {
      Object var5 = null;
      String var4 = "dialogId = " + var1;
      SQLiteDatabase var14 = this.mupdateDbHelepr.getReadableDatabase();
      boolean var11 = false;

      Cursor var6;
      try {
         var11 = true;
         var6 = var14.query("tbl_cat_dlg_info", (String[])null, var4, (String[])null, (String)null, (String)null, "_id");
         var11 = false;
      } finally {
         if(var11) {
            if(var5 != null) {
               ((Cursor)var5).close();
            }

         }
      }

      label124: {
         long var2;
         try {
            if(!var6.moveToNext()) {
               break label124;
            }

            var2 = var6.getLong(var6.getColumnIndex("categoryId"));
         } finally {
            ;
         }

         Long var15 = Long.valueOf(var2);
         var1 = var15;
         if(var6 != null) {
            var6.close();
            var1 = var15;
         }

         return var1;
      }

      if(var6 != null) {
         var6.close();
      }

      var1 = null;
      return var1;
   }

   public List i() {
      return this.a((String)null, true);
   }

   public List j() {
      Throwable th;
      SQLiteDatabase readableDatabase = mupdateDbHelepr.getReadableDatabase();
      List arrayList = new ArrayList();
      Cursor query;
      try {
         query = readableDatabase.query("tbl_cat_dlg_info", null, null, null, null, null, "_id");
         while (query.moveToNext()) {
            try {
               arrayList.add(Long.valueOf(query.getLong(query.getColumnIndex("dialogId"))));
            } catch (Throwable th2) {
               th = th2;
            }
         }
         if (query != null) {
            query.close();
         }
         return arrayList;
      } catch (Throwable th3) {
         query = null;
         if (query != null) {
            query.close();
         }
         throw th3;
      }
   }

   public void j(Long var1) {
      SQLiteDatabase var2 = this.mupdateDbHelepr.getWritableDatabase();
      String var5 = "doc_id = " + var1;
      var2.beginTransaction();

      try {
         var2.delete("tbl_favorite_stickers", var5, (String[])null);
         var2.setTransactionSuccessful();
      } finally {
         var2.endTransaction();
      }

   }

   public List k() {
      return this.c((String)null);
   }
}
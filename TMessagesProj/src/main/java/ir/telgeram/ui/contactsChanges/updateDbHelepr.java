package ir.telgeram.ui.contactsChanges;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ir.telgeram.messenger.ApplicationLoader;


public class updateDbHelepr extends SQLiteOpenHelper {
   public updateDbHelepr(Context var1) {
      super(var1, "tbl_update" + ".db", null, 1);
   }

   private void a(SQLiteDatabase var1) {
      var1.execSQL("create table tbl_update ( _id integer primary key autoincrement, type integer,old_value text,new_value text,user_id integer,is_new integer,change_date integer default (strftime(\'%s\',\'now\') * 1000))");
   }

   private void b(SQLiteDatabase var1) {
      var1.execSQL("create table tbl_setting ( _id integer primary key autoincrement, key text, value text)");
      var1.execSQL("INSERT INTO tbl_setting VALUES (1,\'notifyChanges\',\'true\')");
      var1.execSQL("INSERT INTO tbl_setting VALUES (2,\'notifyNameChanges\',\'true\')");
      var1.execSQL("INSERT INTO tbl_setting VALUES (3,\'notifyStatusChanges\',\'true\')");
      var1.execSQL("INSERT INTO tbl_setting VALUES (4,\'notifyPhotoChanges\',\'true\')");
      var1.execSQL("INSERT INTO tbl_setting VALUES (5,\'notifyPhoneChanges\',\'true\')");
   }

   private void c(SQLiteDatabase var1) {
      var1.execSQL("create table tbl_alarm ( _id integer primary key autoincrement, title text,message text,imageUrl text,positiveBtnText text,positiveBtnAction text,positiveBtnUrl text,negativeBtnText text,negativeBtnAction text,negativeBtnUrl text,showCount integer,exitOnDismiss integer,targetNetwork integer,displayCount integer,targetVersion integer)");
   }

   private void d(SQLiteDatabase var1) {
      var1.execSQL("create table tbl_favorite ( _id integer primary key autoincrement, chatID integer)");
   }

   private void e(SQLiteDatabase var1) {
      var1.execSQL("create table tbl_hidden ( _id integer primary key autoincrement, dialogID integer)");
   }

   private void f(SQLiteDatabase var1) {
      var1.execSQL("create table tbl_category ( _id integer primary key autoincrement, name text,priority integer)");
   }

   private void g(SQLiteDatabase var1) {
      var1.execSQL("create table tbl_cat_dlg_info ( _id integer primary key autoincrement, dialogId integer,categoryId integer, foreign key( categoryId ) references tbl_category ( _id ) ON DELETE CASCADE )");
   }

   private void h(SQLiteDatabase var1) {
      var1.execSQL("CREATE TRIGGER trg_category_priority_from_id AFTER INSERT ON tbl_category FOR EACH ROW  WHEN NEW.priority IS NULL  BEGIN  UPDATE tbl_category SET priority= NEW._id WHERE rowid = NEW.rowid;END;");
   }

   private void i(SQLiteDatabase var1) {
      var1.execSQL("create table tbl_favorite_stickers ( _id integer primary key autoincrement, doc_id integer,priority integer)");
   }

   private void j(SQLiteDatabase var1) {
      var1.execSQL("CREATE TRIGGER trg_fav_stickers_priority_from_id AFTER INSERT ON tbl_favorite_stickers FOR EACH ROW  WHEN NEW.priority IS NULL  BEGIN  UPDATE tbl_favorite_stickers SET priority= NEW._id WHERE rowid = NEW.rowid;END;");
   }

   boolean a(SQLiteDatabase var1, String var2) {
      boolean var5 = true;
      boolean var6 = false;
      boolean var4 = var6;
      if(var2 != null) {
         var4 = var6;
         if(var1 != null) {
            if(!var1.isOpen()) {
               var4 = var6;
            } else {
               Cursor var7 = var1.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", var2});
               var4 = var6;
               if(var7.moveToFirst()) {
                  int var3 = var7.getInt(0);
                  var7.close();
                  if(var3 > 0) {
                     var4 = var5;
                  } else {
                     var4 = false;
                  }
               }
            }
         }
      }

      return var4;
   }

   public void onCreate(SQLiteDatabase var1) {
      this.a(var1);
      this.b(var1);
      this.c(var1);
      this.d(var1);
      this.e(var1);
      this.f(var1);
      this.g(var1);
      this.h(var1);
      this.i(var1);
      this.j(var1);
   }

   public void onOpen(SQLiteDatabase var1) {
      super.onOpen(var1);
      if(!var1.isReadOnly()) {
         var1.execSQL("PRAGMA foreign_keys=ON;");
      }

   }

   public void onUpgrade(SQLiteDatabase var1, int var2, int var3) {
      var3 = var2 + 1;
      var2 = var3;
      if(var3 == 1) {
         var2 = var3 + 1;
      }

      var3 = var2;
      if(var2 <= 'ﾌ') {
         var3 = 'ﾍ';
      }

      var2 = var3;
      if(var3 == 'ﾍ') {
         var2 = var3 + 1;
         this.c(var1);
      }

      int var4 = var2;
      if(var2 == 'ﾎ') {
         var4 = var2 + 1;
      }

      var3 = var4;
      if(var4 == 'ﾏ') {
         var3 = var4 + 1;
      }

      var2 = var3;
      if(var3 == 'ﾐ') {
         var2 = var3 + 1;
         var1.execSQL("drop table tbl_alarm");
         this.c(var1);
         this.d(var1);
      }

      var3 = var2;
      if(var2 <= 68528) {
         var3 = 68529;
      }

      var2 = var3;
      if(var3 == 68529) {
         ++var3;
         SharedPreferences var5 = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
         var2 = var3;
         if(var5.getInt("default_tab", 0) == 2) {
            var5.edit().putInt("default_tab", 7).commit();
            var2 = var3;
         }
      }

      var3 = var2;
      if(var2 <= 71944) {
         var3 = 71945;
      }

      var4 = var3;
      if(var3 == 71945) {
         var4 = var3 + 1;
         this.e(var1);
      }

      var2 = var4;
      if(var4 <= 71955) {
         var2 = 71956;
      }

      var3 = var2;
      if(var2 == 71956) {
         var3 = var2 + 1;
         this.f(var1);
         this.g(var1);
         this.h(var1);
      }

      var2 = var3;
      if(var3 == 71957) {
         var2 = var3 + 1;
         this.i(var1);
         this.j(var1);
      }

      var3 = var2;
      if(var2 <= 71963) {
         var3 = 71964;
      }

      if(var3 == 71964) {
         if(!this.a(var1, "tbl_category")) {
            this.f(var1);
            this.g(var1);
            this.h(var1);
         }

         if(!this.a(var1, "tbl_favorite_stickers")) {
            this.i(var1);
            this.j(var1);
         }
      }

   }
}

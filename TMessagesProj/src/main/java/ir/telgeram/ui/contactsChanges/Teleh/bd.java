package ir.telgeram.ui.contactsChanges.Teleh;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import ir.telgeram.ui.contactsChanges.SolarCalendar;

public class bd {
   public static int a() {
      return (new Random(System.currentTimeMillis())).nextInt();
   }

   public static int a(Context var0) {
      int var1;
      try {
         var1 = var0.getPackageManager().getPackageInfo(var0.getPackageName(), 0).versionCode;
      } catch (NameNotFoundException var2) {
         var1 = 1;
      }

      return var1;
   }

   public static String a(int var0, int var1) {
      char[] var2 = new char[var1];
      Arrays.fill(var2, '0');
      return (new DecimalFormat(String.valueOf(var2))).format((long) var0);
   }

   public static String a(String str) {
      return str.substring(str.lastIndexOf(".") + 1);
   }

   public static String a(Date date) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      return new SolarCalendar(cal).getShortDesDate();
   }



   public static File c() {
      File externalStorageDirectory;
      synchronized (bd.class) {
         externalStorageDirectory = Environment.getExternalStorageDirectory();
         if (!(externalStorageDirectory == null || externalStorageDirectory.exists() || externalStorageDirectory.mkdirs())) {
            externalStorageDirectory = null;
         }
      }
      return externalStorageDirectory;
   }

   public static String c(Context context) {
      return a(context.getPackageName());
   }


}

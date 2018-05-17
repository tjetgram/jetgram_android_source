package ir.telgeram.ui.contactsChanges;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ContactsController;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.MessagesController;
import ir.telgeram.messenger.R;
import ir.telgeram.tgnet.TLObject;
import ir.telgeram.tgnet.TLRPC.User;
import ir.telgeram.ui.Components.AvatarDrawable;
import ir.telgeram.ui.Components.BackupImageView;
import ir.telgeram.ui.Components.CheckBox;
import ir.telgeram.ui.Components.LayoutHelper;
import ir.telgeram.ui.contactsChanges.SimpleTextView;
import ir.telgeram.ui.contactsChanges.Teleh.bd;

import java.util.Calendar;
import java.util.Date;

public class UpdateCell extends FrameLayout {
   private AvatarDrawable avatarDrawable;
   private BackupImageView avatarImageView;
   private CheckBox checkBox;
   private User currentUser;
   private SimpleTextView dateTextView;
   private ImageView imageView;
   private String lastName;
   private SimpleTextView nameTextView;
   private int newValueColor;
   private SimpleTextView newValueTextView;
   private int oldValueColor;
   private SimpleTextView oldValueTextView;
   private UpdateModel updateModel;

   @SuppressLint({"RtlHardcoded"})
   public UpdateCell(Context context, int i) {
      super(context);
      int i2 = 5;
      this.currentUser = null;
      this.lastName = null;
      this.oldValueColor = -5723992;
      this.newValueColor = -12876608;
      this.avatarDrawable = new AvatarDrawable();
      this.avatarImageView = new BackupImageView(context);
      this.avatarImageView.setRoundRadius(AndroidUtilities.dp(24.0f));
      boolean z = LocaleController.isRTL;
      addView(this.avatarImageView, LayoutHelper.createFrame(48, 48.0f, (z ? 5 : 3) | 48, z ? 0.0f : (float) (i + 7), 8.0f, z ? (float) (i + 7) : 0.0f, 0.0f));
      this.nameTextView = new SimpleTextView(context);
      this.nameTextView.setTextColor(-14606047);
      this.nameTextView.setTextSize(17);
      this.nameTextView.setGravity((z ? 5 : 3) | 48);
      addView(this.nameTextView, LayoutHelper.createFrame(-1, 20.0f, (z ? 5 : 3) | 48, z ? 28.0f : (float) (i + 68), 11.5f, z ? (float) (i + 68) : 28.0f, 0.0f));
      this.oldValueTextView = new SimpleTextView(context);
      this.oldValueTextView.setTextSize(14);
      this.oldValueTextView.setGravity((z ? 5 : 3) | 48);
      addView(this.oldValueTextView, LayoutHelper.createFrame(-1, 20.0f, (z ? 5 : 3) | 48, z ? 28.0f : (float) (i + 68), 34.5f, z ? (float) (i + 68) : 28.0f, 0.0f));
      this.newValueTextView = new SimpleTextView(context);
      this.newValueTextView.setTextSize(14);
      this.newValueTextView.setGravity((z ? 5 : 3) | 48);
      addView(this.newValueTextView, LayoutHelper.createFrame(-1, 20.0f, (z ? 5 : 3) | 48, z ? 28.0f : (float) (i + 68), 57.5f, z ? (float) (i + 68) : 28.0f, 0.0f));
      this.dateTextView = new SimpleTextView(context);
      this.dateTextView.setTextSize(14);
      this.dateTextView.setGravity((z ? 3 : 5) | 48);
      addView(this.dateTextView, LayoutHelper.createFrame(-1, 20.0f, (z ? 3 : 5) | 48, z ? (float) (i + 5) : 28.0f, 80.5f, z ? 28.0f : (float) (i + 10), 0.0f));
      this.imageView = new ImageView(context);
      this.imageView.setScaleType(ScaleType.CENTER);
      this.imageView.setVisibility(GONE);
      addView(this.imageView, LayoutHelper.createFrame(-2, -2.0f, (z ? 5 : 3) | 16, z ? 0.0f : 16.0f, 0.0f, z ? 16.0f : 0.0f, 0.0f));
      this.checkBox = new CheckBox(context, R.drawable.round_check2);
      this.checkBox.setVisibility(INVISIBLE);
      View view = this.checkBox;
      if (!z) {
         i2 = 3;
      }
      addView(view, LayoutHelper.createFrame(22, 22.0f, i2 | 48, z ? 0.0f : (float) (i + 37), 38.0f, z ? (float) (i + 37) : 0.0f, 0.0f));
   }

   public BackupImageView getAvatarImageView() {
      return this.avatarImageView;
   }

   protected void onMeasure(int i, int i2) {
      super.onMeasure(i, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(104.0f), MeasureSpec.EXACTLY));
   }

   public void setChecked(boolean z, boolean z2) {
      if (this.checkBox.getVisibility() != VISIBLE) {
         this.checkBox.setVisibility(VISIBLE);
      }
      this.checkBox.setChecked(z, z2);
   }

   public void setData(ir.telgeram.ui.contactsChanges.UpdateModel updateModel) {
      User user = MessagesController.getInstance().getUser(Integer.valueOf(updateModel.getUserId()));
      if (user == null) {
         this.nameTextView.setText("");
         this.avatarImageView.setImageDrawable(null);
      }
      this.currentUser = user;
      this.updateModel = updateModel;
      update();
   }

   public void update() {
      if (this.currentUser != null) {
         TLObject tLObject = null;
         if (this.currentUser.photo != null) {
            tLObject = this.currentUser.photo.photo_small;
         }
         this.avatarDrawable.setInfo(this.currentUser);
         this.avatarImageView.setImage(tLObject, "50_50", this.avatarDrawable);
         this.lastName = ContactsController.formatName(this.currentUser.first_name, this.currentUser.last_name);
         this.nameTextView.setText(this.lastName);
      }
      this.oldValueTextView.setTextColor(this.oldValueColor);
      this.newValueTextView.setTextColor(this.newValueColor);
      if (this.updateModel.getType() == 1) {
         this.oldValueTextView.setText("");
         if (this.updateModel.getNewValue().equals("1")) {
            this.newValueTextView.setText(getContext().getString(R.string.get_online));
         } else {
            this.newValueTextView.setText(getContext().getString(R.string.get_offline));
         }
      } else if (this.updateModel.getType() == 2) {
         this.oldValueTextView.setText(getContext().getString(R.string.old_name) + " " + this.updateModel.getOldValue().replace(";;;", " - "));
         this.newValueTextView.setText(getContext().getString(R.string.new_name) + " " + this.updateModel.getNewValue().replace(";;;", " - "));
      } else if (this.updateModel.getType() == 3) {
         this.oldValueTextView.setText("");
         this.newValueTextView.setText(getContext().getString(R.string.changed_photo));
      } else if (this.updateModel.getType() == 4) {
         this.oldValueTextView.setText(getContext().getString(R.string.old_phone) + " " + this.updateModel.getOldValue());
         this.newValueTextView.setText(getContext().getString(R.string.new_phone) + " " + this.updateModel.getNewValue());
      }
      Long valueOf = Long.valueOf(Long.parseLong(this.updateModel.getChangeDate()));
      if (valueOf.longValue() != 0) {
         Date date = new Date(valueOf.longValue());
         String a = bd.a(date);
         Calendar instance = Calendar.getInstance();
         instance.setTime(date);
         this.dateTextView.setText(a + " - " + bd.a(instance.get(Calendar.HOUR_OF_DAY), 2) + ":" + bd.a(instance.get(Calendar.MINUTE), 2));
      }
   }
}

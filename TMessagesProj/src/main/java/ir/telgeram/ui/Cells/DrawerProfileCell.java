/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.telgeram.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ir.telgeram.Adel.ThemeChanger;
import ir.telgeram.PhoneFormat.PhoneFormat;
import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.UserObject;
import ir.telgeram.messenger.FileLog;
import ir.telgeram.messenger.R;
import ir.telgeram.tgnet.TLRPC;
import ir.telgeram.ui.Components.AvatarDrawable;
import ir.telgeram.ui.Components.BackupImageView;
import ir.telgeram.ui.Components.LayoutHelper;
import ir.telgeram.ui.ActionBar.Theme;

public class DrawerProfileCell extends FrameLayout {

    private BackupImageView avatarImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private ImageView shadowView;
    private CloudView cloudView;
    private Rect srcRect = new Rect();
    private Rect destRect = new Rect();
    private Paint paint = new Paint();
    private Integer currentColor;
    private Drawable cloudDrawable;
    private int lastCloudColor;

    private class CloudView extends View {

        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public CloudView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (Theme.isCustomTheme() && Theme.getCachedWallpaper() != null) {
                paint.setColor(Theme.getServiceMessageColor());
            } else {
                paint.setColor(Theme.getColor(Theme.key_chats_menuCloudBackgroundCats));
            }
            int newColor = Theme.getColor(Theme.key_chats_menuCloud);
            if (lastCloudColor != newColor) {
                cloudDrawable.setColorFilter(new PorterDuffColorFilter(lastCloudColor = Theme.getColor(Theme.key_chats_menuCloud), PorterDuff.Mode.MULTIPLY));
            }
            canvas.drawCircle(getMeasuredWidth() / 2.0f, getMeasuredHeight() / 2.0f, AndroidUtilities.dp(34) / 2.0f, paint);
            int l = (getMeasuredWidth() - AndroidUtilities.dp(33)) / 2;
            int t = (getMeasuredHeight() - AndroidUtilities.dp(33)) / 2;
            cloudDrawable.setBounds(l, t, l + AndroidUtilities.dp(33), t + AndroidUtilities.dp(33));
            cloudDrawable.draw(canvas);
        }
    }

    public DrawerProfileCell(Context context) {
        super(context);

        setBackgroundColor(ThemeChanger.getcurrent().getActionbarcolor()); // Adel
        ThemeChanger.addView(this); // Adel

        cloudDrawable = context.getResources().getDrawable(R.drawable.cloud);
        cloudDrawable.setColorFilter(new PorterDuffColorFilter(lastCloudColor = Theme.getColor(Theme.key_chats_menuCloud), PorterDuff.Mode.MULTIPLY));

        shadowView = new ImageView(context);
        shadowView.setVisibility(INVISIBLE);
        shadowView.setScaleType(ImageView.ScaleType.FIT_XY);
        shadowView.setImageResource(R.drawable.bottom_shadow);
        addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 70, Gravity.LEFT | Gravity.BOTTOM));

        avatarImageView = new BackupImageView(context);
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(32));
        addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 0, 67));

        nameTextView = new TextView(context);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 76, 28));

        phoneTextView = new TextView(context);
        phoneTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        phoneTextView.setLines(1);
        phoneTextView.setMaxLines(1);
        phoneTextView.setSingleLine(true);
        phoneTextView.setGravity(Gravity.LEFT);
        phoneTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // Adel
        addView(phoneTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 76, 9));

        cloudView = new CloudView(context);
        addView(cloudView, LayoutHelper.createFrame(61, 61, Gravity.RIGHT | Gravity.BOTTOM));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (Build.VERSION.SDK_INT >= 21) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148) + AndroidUtilities.statusBarHeight, MeasureSpec.EXACTLY));
        } else {
            try {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148), MeasureSpec.EXACTLY));
            } catch (Exception e) {
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(148));
                FileLog.e(e);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable backgroundDrawable = Theme.getCachedWallpaper();
        int color;
        if (Theme.hasThemeKey(Theme.key_chats_menuTopShadow)) {
            color = Theme.getColor(Theme.key_chats_menuTopShadow);
        } else {
            color = Theme.getServiceMessageColor() | 0xff000000;
        }
        if (currentColor == null || currentColor != color) {
            currentColor = color;
            shadowView.getDrawable().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        }
        nameTextView.setTextColor(Theme.getColor(Theme.key_chats_menuName));
        if (Theme.isCustomTheme() && backgroundDrawable != null) {
            phoneTextView.setTextColor(Theme.getColor(Theme.key_chats_menuPhone));
            shadowView.setVisibility(VISIBLE);
            if (backgroundDrawable instanceof ColorDrawable) {
                backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                backgroundDrawable.draw(canvas);
            } else if (backgroundDrawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
                float scaleX = (float) getMeasuredWidth() / (float) bitmap.getWidth();
                float scaleY = (float) getMeasuredHeight() / (float) bitmap.getHeight();
                float scale = scaleX < scaleY ? scaleY : scaleX;
                int width = (int) (getMeasuredWidth() / scale);
                int height = (int) (getMeasuredHeight() / scale);
                int x = (bitmap.getWidth() - width) / 2;
                int y = (bitmap.getHeight() - height) / 2;
                srcRect.set(x, y, x + width, y + height);
                destRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                try {
                    canvas.drawBitmap(bitmap, srcRect, destRect, paint);
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
        } else {
            shadowView.setVisibility(INVISIBLE);
            phoneTextView.setTextColor(Theme.getColor(Theme.key_chats_menuPhoneCats));
            super.onDraw(canvas);
        }
    }

    public void setUser(TLRPC.User user) {
        if (user == null) {
            return;
        }
        TLRPC.FileLocation photo = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
        }
        nameTextView.setText(UserObject.getUserName(user));
        phoneTextView.setText(PhoneFormat.getInstance().format("+" + user.phone));
        AvatarDrawable avatarDrawable = new AvatarDrawable(user);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        avatarImageView.setImage(photo, "50_50", avatarDrawable);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        cloudView.invalidate();
    }
}

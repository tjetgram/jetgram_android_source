/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package ir.telgeram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.FileLog;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.MessagesController;
import ir.telgeram.messenger.MessagesStorage;
import ir.telgeram.messenger.NotificationCenter;
import ir.telgeram.messenger.R;
import ir.telgeram.messenger.UserConfig;
import ir.telgeram.tgnet.TLRPC;
import ir.telgeram.ui.ActionBar.ActionBar;
import ir.telgeram.ui.ActionBar.ActionBarMenu;
import ir.telgeram.ui.ActionBar.AlertDialog;
import ir.telgeram.ui.ActionBar.BaseFragment;
import ir.telgeram.ui.ActionBar.Theme;
import ir.telgeram.ui.ActionBar.ThemeDescription;
import ir.telgeram.ui.Cells.ShadowSectionCell;
import ir.telgeram.ui.Cells.TextCheckCell;
import ir.telgeram.ui.Cells.TextInfoPrivacyCell;
import ir.telgeram.ui.Cells.TextSettingsCell;
import ir.telgeram.ui.Components.AvatarDrawable;
import ir.telgeram.ui.Components.AvatarUpdater;
import ir.telgeram.ui.Components.BackupImageView;
import ir.telgeram.ui.Components.LayoutHelper;

import java.util.concurrent.Semaphore;

public class ChannelEditActivity extends BaseFragment implements AvatarUpdater.AvatarUpdaterDelegate, NotificationCenter.NotificationCenterDelegate {

    private View doneButton;
    private EditText nameTextView;
    private EditText descriptionTextView;
    private BackupImageView avatarImage;
    private AvatarDrawable avatarDrawable;
    private AvatarUpdater avatarUpdater;
    private AlertDialog progressDialog;
    private TextSettingsCell typeCell;
    private TextSettingsCell adminCell;
    private LinearLayout linearLayout2;
    private LinearLayout linearLayout3;
    private View lineView;
    private View lineView2;
    private FrameLayout container1;
    private FrameLayout container2;
    private FrameLayout container3;
    private ShadowSectionCell sectionCell;
    private ShadowSectionCell sectionCell2;
    private TextCheckCell textCheckCell;
    private TextInfoPrivacyCell infoCell;
    private TextSettingsCell textCell;
    private TextInfoPrivacyCell infoCell2;

    private TLRPC.FileLocation avatar;
    private TLRPC.Chat         currentChat;
    private TLRPC.ChatFull     info;
    private int                chatId;
    private TLRPC.InputFile    uploadedAvatar;
    private boolean            signMessages;

    private boolean createAfterUpload;
    private boolean donePressed;

    private final static int done_button = 1;

    public ChannelEditActivity(Bundle args) {
        super(args);
        avatarDrawable = new AvatarDrawable();
        avatarUpdater = new AvatarUpdater();
        chatId = args.getInt("chat_id", 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onFragmentCreate() {
        currentChat = MessagesController.getInstance().getChat(chatId);
        if (currentChat == null) {
            final Semaphore semaphore = new Semaphore(0);
            MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                @Override
                public void run() {
                    currentChat = MessagesStorage.getInstance().getChat(chatId);
                    semaphore.release();
                }
            });
            try {
                semaphore.acquire();
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (currentChat != null) {
                MessagesController.getInstance().putChat(currentChat, true);
            } else {
                return false;
            }
            if (info == null) {
                MessagesStorage.getInstance().loadChatInfo(chatId, semaphore, false, false);
                try {
                    semaphore.acquire();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (info == null) {
                    return false;
                }
            }
        }
        avatarUpdater.parentFragment = this;
        avatarUpdater.delegate = this;
        signMessages = currentChat.signatures;
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (avatarUpdater != null) {
            avatarUpdater.clear();
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    if (donePressed) {
                        return;
                    }
                    if (nameTextView.length() == 0) {
                        Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        if (v != null) {
                            v.vibrate(200);
                        }
                        AndroidUtilities.shakeView(nameTextView, 2, 0);
                        return;
                    }
                    donePressed = true;

                    if (avatarUpdater.uploadingAvatar != null) {
                        createAfterUpload = true;
                        progressDialog = new AlertDialog(getParentActivity(), 1);
                        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setCancelable(false);
                        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createAfterUpload = false;
                                progressDialog = null;
                                donePressed = false;
                                try {
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                            }
                        });
                        progressDialog.show();
                        return;
                    }
                    if (!currentChat.title.equals(nameTextView.getText().toString())) {
                        MessagesController.getInstance().changeChatTitle(chatId, nameTextView.getText().toString());
                    }
                    if (info != null && !info.about.equals(descriptionTextView.getText().toString())) {
                        MessagesController.getInstance().updateChannelAbout(chatId, descriptionTextView.getText().toString(), info);
                    }
                    if (signMessages != currentChat.signatures) {
                        currentChat.signatures = true;
                        MessagesController.getInstance().toogleChannelSignatures(chatId, signMessages);
                    }
                    if (uploadedAvatar != null) {
                        MessagesController.getInstance().changeChatAvatar(chatId, uploadedAvatar);
                    } else if (avatar == null && currentChat.photo instanceof TLRPC.TL_chatPhoto) {
                        MessagesController.getInstance().changeChatAvatar(chatId, null);
                    }
                    finishFragment();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

        LinearLayout linearLayout;

        fragmentView = new ScrollView(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        ScrollView scrollView = (ScrollView) fragmentView;
        scrollView.setFillViewport(true);
        linearLayout = new LinearLayout(context);
        scrollView.addView(linearLayout, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        actionBar.setTitle(LocaleController.getString("ChannelEdit", R.string.ChannelEdit));

        linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout.addView(linearLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        FrameLayout frameLayout = new FrameLayout(context);
        linearLayout2.addView(frameLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(32));
        avatarDrawable.setInfo(5, null, null, false);
        avatarDrawable.setDrawPhoto(true);
        frameLayout.addView(avatarImage, LayoutHelper.createFrame(64, 64, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 16, 12, LocaleController.isRTL ? 16 : 0, 12));
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                CharSequence[] items;

                if (avatar != null) {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                } else {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)};
                }

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            avatarUpdater.openCamera();
                        } else if (i == 1) {
                            avatarUpdater.openGallery();
                        } else if (i == 2) {
                            avatar = null;
                            uploadedAvatar = null;
                            avatarImage.setImage(avatar, "50_50", avatarDrawable);
                        }
                    }
                });
                showDialog(builder.create());
            }
        });

        nameTextView = new EditText(context);
        if (currentChat.megagroup) {
            nameTextView.setHint(LocaleController.getString("GroupName", R.string.GroupName));
        } else {
            nameTextView.setHint(LocaleController.getString("EnterChannelName", R.string.EnterChannelName));
        }
        nameTextView.setMaxLines(4);
        nameTextView.setGravity(Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        nameTextView.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        nameTextView.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        nameTextView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        nameTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameTextView.setPadding(0, 0, 0, AndroidUtilities.dp(8));
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(100);
        nameTextView.setFilters(inputFilters);
        AndroidUtilities.clearCursorDrawable(nameTextView);
        nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // Adel
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, LocaleController.isRTL ? 16 : 96, 0, LocaleController.isRTL ? 96 : 16, 0));
        nameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                avatarDrawable.setInfo(5, nameTextView.length() > 0 ? nameTextView.getText().toString() : null, null, false);
                avatarImage.invalidate();
            }
        });

        lineView = new View(context);
        lineView.setBackgroundColor(Theme.getColor(Theme.key_divider));
        linearLayout.addView(lineView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        linearLayout3 = new LinearLayout(context);
        linearLayout3.setOrientation(LinearLayout.VERTICAL);
        linearLayout3.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout.addView(linearLayout3, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        descriptionTextView = new EditText(context);
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        descriptionTextView.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        descriptionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        descriptionTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // Adel
        descriptionTextView.setPadding(0, 0, 0, AndroidUtilities.dp(6));
        descriptionTextView.setBackgroundDrawable(null);
        descriptionTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        descriptionTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        descriptionTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(255);
        descriptionTextView.setFilters(inputFilters);
        descriptionTextView.setHint(LocaleController.getString("DescriptionOptionalPlaceholder", R.string.DescriptionOptionalPlaceholder));
        AndroidUtilities.clearCursorDrawable(descriptionTextView);
        linearLayout3.addView(descriptionTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 17, 12, 17, 6));
        descriptionTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE && doneButton != null) {
                    doneButton.performClick();
                    return true;
                }
                return false;
            }
        });
        descriptionTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sectionCell = new ShadowSectionCell(context);
        sectionCell.setSize(20);
        linearLayout.addView(sectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        container1 = new FrameLayout(context);
        container1.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout.addView(container1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        typeCell = new TextSettingsCell(context);
        updateTypeCell();
        typeCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        container1.addView(typeCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        lineView2 = new View(context);
        lineView2.setBackgroundColor(Theme.getColor(Theme.key_divider));
        linearLayout.addView(lineView2, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        container2 = new FrameLayout(context);
        container2.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout.addView(container2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (!currentChat.megagroup) {
            textCheckCell = new TextCheckCell(context);
            textCheckCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            textCheckCell.setTextAndCheck(LocaleController.getString("ChannelSignMessages", R.string.ChannelSignMessages), signMessages, false);
            container2.addView(textCheckCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCheckCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signMessages = !signMessages;
                    ((TextCheckCell) v).setChecked(signMessages);
                }
            });

            infoCell = new TextInfoPrivacyCell(context);
            infoCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
            infoCell.setText(LocaleController.getString("ChannelSignMessagesInfo", R.string.ChannelSignMessagesInfo));
            linearLayout.addView(infoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        } else {
            adminCell = new TextSettingsCell(context);
            updateAdminCell();
            adminCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            container2.addView(adminCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            adminCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putInt("chat_id", chatId);
                    args.putInt("type", 1);
                    presentFragment(new ChannelUsersActivity(args));
                }
            });

            sectionCell2 = new ShadowSectionCell(context);
            sectionCell2.setSize(20);
            linearLayout.addView(sectionCell2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            if (!currentChat.creator) {
                sectionCell2.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            }
        }

        if (currentChat.creator) {
            container3 = new FrameLayout(context);
            container3.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            linearLayout.addView(container3, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            textCell = new TextSettingsCell(context);
            textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText5));
            textCell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            if (currentChat.megagroup) {
                textCell.setText(LocaleController.getString("DeleteMega", R.string.DeleteMega), false);
            } else {
                textCell.setText(LocaleController.getString("ChannelDelete", R.string.ChannelDelete), false);
            }
            container3.addView(textCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    if (currentChat.megagroup) {
                        builder.setMessage(LocaleController.getString("MegaDeleteAlert", R.string.MegaDeleteAlert));
                    } else {
                        builder.setMessage(LocaleController.getString("ChannelDeleteAlert", R.string.ChannelDeleteAlert));
                    }
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
                            if (AndroidUtilities.isTablet()) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, -(long) chatId);
                            } else {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            }
                            MessagesController.getInstance().deleteUserFromChat(chatId, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), info);
                            finishFragment();
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                }
            });

            infoCell2 = new TextInfoPrivacyCell(context);
            infoCell2.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            if (currentChat.megagroup) {
                infoCell2.setText(LocaleController.getString("MegaDeleteInfo", R.string.MegaDeleteInfo));
            } else {
                infoCell2.setText(LocaleController.getString("ChannelDeleteInfo", R.string.ChannelDeleteInfo));
            }
            linearLayout.addView(infoCell2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        nameTextView.setText(currentChat.title);
        nameTextView.setSelection(nameTextView.length());
        if (info != null) {
            descriptionTextView.setText(info.about);
        }
        if (currentChat.photo != null) {
            avatar = currentChat.photo.photo_small;
            avatarImage.setImage(avatar, "50_50", avatarDrawable);
        } else {
            avatarImage.setImageDrawable(avatarDrawable);
        }

        return fragmentView;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.chatInfoDidLoaded) {
            TLRPC.ChatFull chatFull = (TLRPC.ChatFull) args[0];
            if (chatFull.id == chatId) {
                if (info == null) {
                    descriptionTextView.setText(chatFull.about);
                }
                info = chatFull;
                updateAdminCell();
                updateTypeCell();
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            int updateMask = (Integer) args[0];
            if ((updateMask & MessagesController.UPDATE_MASK_CHANNEL) != 0) {
                updateTypeCell();
            }
        }
    }

    @Override
    public void didUploadedPhoto(final TLRPC.InputFile file, final TLRPC.PhotoSize small, final TLRPC.PhotoSize big) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                uploadedAvatar = file;
                avatar = small.location;
                avatarImage.setImage(avatar, "50_50", avatarDrawable);
                if (createAfterUpload) {
                    try {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    doneButton.performClick();
                }
            }
        });
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (avatarUpdater != null && avatarUpdater.currentPicturePath != null) {
            args.putString("path", avatarUpdater.currentPicturePath);
        }
        if (nameTextView != null) {
            String text = nameTextView.getText().toString();
            if (text != null && text.length() != 0) {
                args.putString("nameTextView", text);
            }
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (avatarUpdater != null) {
            avatarUpdater.currentPicturePath = args.getString("path");
        }
    }

    public void setInfo(TLRPC.ChatFull chatFull) {
        info = chatFull;
    }

    private void updateTypeCell() {
        String type = currentChat.username == null || currentChat.username.length() == 0 ? LocaleController.getString("ChannelTypePrivate", R.string.ChannelTypePrivate) : LocaleController.getString("ChannelTypePublic", R.string.ChannelTypePublic);
        if (currentChat.megagroup) {
            typeCell.setTextAndValue(LocaleController.getString("GroupType", R.string.GroupType), type, false);
        } else {
            typeCell.setTextAndValue(LocaleController.getString("ChannelType", R.string.ChannelType), type, false);
        }

        if (currentChat.creator && (info == null || info.can_set_username)) {
            typeCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putInt("chat_id", chatId);
                    ChannelEditTypeActivity fragment = new ChannelEditTypeActivity(args);
                    fragment.setInfo(info);
                    presentFragment(fragment);
                }
            });
            typeCell.getTextView().setTag(Theme.key_windowBackgroundWhiteBlackText);
            typeCell.getValueTextView().setTag(Theme.key_windowBackgroundWhiteValueText);
            typeCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            typeCell.setTextValueColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
        } else {
            typeCell.setOnClickListener(null);
            typeCell.getTextView().setTag(Theme.key_windowBackgroundWhiteGrayText);
            typeCell.getValueTextView().setTag(Theme.key_windowBackgroundWhiteGrayText);
            typeCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            typeCell.setTextValueColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        }
    }

    private void updateAdminCell() {
        if (adminCell == null) {
            return;
        }
        if (info != null) {
            adminCell.setTextAndValue(LocaleController.getString("ChannelAdministrators", R.string.ChannelAdministrators), String.format("%d", info.admins_count), false);
        } else {
            adminCell.setText(LocaleController.getString("ChannelAdministrators", R.string.ChannelAdministrators), false);
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate сellDelegate = new ThemeDescription.ThemeDescriptionDelegate() {
            @Override
            public void didSetColor(int color) {
                if (avatarImage != null) {
                    avatarDrawable.setInfo(5, nameTextView.length() > 0 ? nameTextView.getText().toString() : null, null, false);
                    avatarImage.invalidate();
                }
            }
        };
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),

                new ThemeDescription(nameTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(nameTextView, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText),
                new ThemeDescription(nameTextView, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField),
                new ThemeDescription(nameTextView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated),
                new ThemeDescription(descriptionTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(descriptionTextView, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText),

                new ThemeDescription(linearLayout2, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(linearLayout3, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(container1, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(container2, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(container3, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(null, 0, null, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable}, сellDelegate, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundBlue),

                new ThemeDescription(lineView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_divider),
                new ThemeDescription(lineView2, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_divider),

                new ThemeDescription(sectionCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(typeCell, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),
                new ThemeDescription(typeCell, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(typeCell, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),
                new ThemeDescription(typeCell, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText),
                new ThemeDescription(typeCell, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText),

                new ThemeDescription(textCheckCell, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),
                new ThemeDescription(textCheckCell, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(textCheckCell, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumb),
                new ThemeDescription(textCheckCell, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(textCheckCell, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumbChecked),
                new ThemeDescription(textCheckCell, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(infoCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(infoCell, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4),

                new ThemeDescription(adminCell, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),
                new ThemeDescription(adminCell, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(adminCell, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),

                new ThemeDescription(sectionCell2, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(textCell, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),
                new ThemeDescription(textCell, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteRedText5),

                new ThemeDescription(infoCell2, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(infoCell2, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4),
        };
    }
}

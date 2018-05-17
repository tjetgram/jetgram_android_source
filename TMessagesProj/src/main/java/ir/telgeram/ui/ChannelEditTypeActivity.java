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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ApplicationLoader;
import ir.telgeram.messenger.FileLog;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.MessagesController;
import ir.telgeram.messenger.MessagesStorage;
import ir.telgeram.messenger.NotificationCenter;
import ir.telgeram.messenger.R;
import ir.telgeram.tgnet.ConnectionsManager;
import ir.telgeram.tgnet.RequestDelegate;
import ir.telgeram.tgnet.TLObject;
import ir.telgeram.tgnet.TLRPC;
import ir.telgeram.ui.ActionBar.ActionBar;
import ir.telgeram.ui.ActionBar.ActionBarMenu;
import ir.telgeram.ui.ActionBar.AlertDialog;
import ir.telgeram.ui.ActionBar.BaseFragment;
import ir.telgeram.ui.ActionBar.Theme;
import ir.telgeram.ui.ActionBar.ThemeDescription;
import ir.telgeram.ui.Cells.AdminedChannelCell;
import ir.telgeram.ui.Cells.HeaderCell;
import ir.telgeram.ui.Cells.LoadingCell;
import ir.telgeram.ui.Cells.RadioButtonCell;
import ir.telgeram.ui.Cells.ShadowSectionCell;
import ir.telgeram.ui.Cells.TextBlockCell;
import ir.telgeram.ui.Cells.TextInfoPrivacyCell;
import ir.telgeram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class ChannelEditTypeActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private LinearLayout             linearLayout;
    private LinearLayout             linearLayout2;
    private LinearLayout             adminnedChannelsLayout;
    private LinearLayout             linkContainer;
    private LinearLayout             publicContainer;
    private TextBlockCell            privateContainer;
    private RadioButtonCell          radioButtonCell1;
    private RadioButtonCell          radioButtonCell2;
    private ShadowSectionCell        sectionCell;
    private TextInfoPrivacyCell      typeInfoCell;
    private EditText                 editText;
    private TextView                 checkTextView;
    private HeaderCell               headerCell;
    private EditText                 nameTextView;
    private boolean                  isPrivate;
    private boolean                  loadingInvite;
    private TLRPC.ExportedChatInvite invite;

    private boolean canCreatePublic = true;
    private boolean loadingAdminedChannels;
    private TextInfoPrivacyCell adminedInfoCell;
    private ArrayList<AdminedChannelCell> adminedChannelCells = new ArrayList<>();
    private LoadingCell loadingAdminedCell;

    private int checkReqId;
    private String lastCheckName;
    private Runnable checkRunnable;
    private boolean lastNameAvailable;
    private TLRPC.Chat currentChat;
    private int chatId;

    private boolean donePressed;

    private final static int done_button = 1;

    public ChannelEditTypeActivity(Bundle args) {
        super(args);
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
        }
        isPrivate = currentChat.username == null || currentChat.username.length() == 0;
        if (isPrivate) {
            TLRPC.TL_channels_checkUsername req = new TLRPC.TL_channels_checkUsername();
            req.username = "1";
            req.channel = new TLRPC.TL_inputChannelEmpty();
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                @Override
                public void run(TLObject response, final TLRPC.TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            canCreatePublic = error == null || !error.text.equals("CHANNELS_ADMIN_PUBLIC_TOO_MUCH");
                            if (!canCreatePublic) {
                                loadAdminedChannels();
                            }
                        }
                    });
                }
            });
        }
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoDidLoaded);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);
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

                    if (!isPrivate && ((currentChat.username == null && nameTextView.length() != 0) || (currentChat.username != null && !currentChat.username.equalsIgnoreCase(nameTextView.getText().toString())))) {
                        if (nameTextView.length() != 0 && !lastNameAvailable) {
                            Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                            if (v != null) {
                                v.vibrate(200);
                            }
                            AndroidUtilities.shakeView(checkTextView, 2, 0);
                            return;
                        }
                    }
                    donePressed = true;

                    String oldUserName = currentChat.username != null ? currentChat.username : "";
                    String newUserName = isPrivate ? "" : nameTextView.getText().toString();
                    if (!oldUserName.equals(newUserName)) {
                        MessagesController.getInstance().updateChannelUserName(chatId, newUserName);
                    }
                    finishFragment();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

        fragmentView = new ScrollView(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        ScrollView scrollView = (ScrollView) fragmentView;
        scrollView.setFillViewport(true);
        linearLayout = new LinearLayout(context);
        scrollView.addView(linearLayout, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        if (currentChat.megagroup) {
            actionBar.setTitle(LocaleController.getString("GroupType", R.string.GroupType));
        } else {
            actionBar.setTitle(LocaleController.getString("ChannelType", R.string.ChannelType));
        }

        linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout.addView(linearLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        radioButtonCell1 = new RadioButtonCell(context);
        radioButtonCell1.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        if (currentChat.megagroup) {
            radioButtonCell1.setTextAndValue(LocaleController.getString("MegaPublic", R.string.MegaPublic), LocaleController.getString("MegaPublicInfo", R.string.MegaPublicInfo), !isPrivate);
        } else {
            radioButtonCell1.setTextAndValue(LocaleController.getString("ChannelPublic", R.string.ChannelPublic), LocaleController.getString("ChannelPublicInfo", R.string.ChannelPublicInfo), !isPrivate);
        }
        linearLayout2.addView(radioButtonCell1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        radioButtonCell1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPrivate) {
                    return;
                }
                isPrivate = false;
                updatePrivatePublic();
            }
        });

        radioButtonCell2 = new RadioButtonCell(context);
        radioButtonCell2.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        if (currentChat.megagroup) {
            radioButtonCell2.setTextAndValue(LocaleController.getString("MegaPrivate", R.string.MegaPrivate), LocaleController.getString("MegaPrivateInfo", R.string.MegaPrivateInfo), isPrivate);
        } else {
            radioButtonCell2.setTextAndValue(LocaleController.getString("ChannelPrivate", R.string.ChannelPrivate), LocaleController.getString("ChannelPrivateInfo", R.string.ChannelPrivateInfo), isPrivate);
        }
        linearLayout2.addView(radioButtonCell2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        radioButtonCell2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPrivate) {
                    return;
                }
                isPrivate = true;
                updatePrivatePublic();
            }
        });

        sectionCell = new ShadowSectionCell(context);
        linearLayout.addView(sectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        linkContainer = new LinearLayout(context);
        linkContainer.setOrientation(LinearLayout.VERTICAL);
        linkContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout.addView(linkContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        headerCell = new HeaderCell(context);
        linkContainer.addView(headerCell);

        publicContainer = new LinearLayout(context);
        publicContainer.setOrientation(LinearLayout.HORIZONTAL);
        linkContainer.addView(publicContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 17, 7, 17, 0));

        editText = new EditText(context);
        editText.setText(MessagesController.getInstance().linkPrefix + "/");
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // Adel
        editText.setMaxLines(1);
        editText.setLines(1);
        editText.setEnabled(false);
        editText.setBackgroundDrawable(null);
        editText.setPadding(0, 0, 0, 0);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        publicContainer.addView(editText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36));

        nameTextView = new EditText(context);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        if (!isPrivate) {
            nameTextView.setText(currentChat.username);
        }
        nameTextView.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // Adel
        nameTextView.setMaxLines(1);
        nameTextView.setLines(1);
        nameTextView.setBackgroundDrawable(null);
        nameTextView.setPadding(0, 0, 0, 0);
        nameTextView.setSingleLine(true);
        nameTextView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        nameTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nameTextView.setHint(LocaleController.getString("ChannelUsernamePlaceholder", R.string.ChannelUsernamePlaceholder));
        AndroidUtilities.clearCursorDrawable(nameTextView);
        publicContainer.addView(nameTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36));
        nameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                checkUserName(nameTextView.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        privateContainer = new TextBlockCell(context);
        privateContainer.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        linkContainer.addView(privateContainer);
        privateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (invite == null) {
                    return;
                }
                try {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("label", invite.link);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getParentActivity(), LocaleController.getString("LinkCopied", R.string.LinkCopied), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        });

        checkTextView = new TextView(context);
        checkTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        checkTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        checkTextView.setVisibility(View.GONE);
        checkTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // Adel
        linkContainer.addView(checkTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 17, 3, 17, 7));

        typeInfoCell = new TextInfoPrivacyCell(context);
        typeInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        linearLayout.addView(typeInfoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        loadingAdminedCell = new LoadingCell(context);
        linearLayout.addView(loadingAdminedCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        adminnedChannelsLayout = new LinearLayout(context);
        adminnedChannelsLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        adminnedChannelsLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(adminnedChannelsLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        adminedInfoCell = new TextInfoPrivacyCell(context);
        adminedInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        linearLayout.addView(adminedInfoCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        updatePrivatePublic();

        return fragmentView;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.chatInfoDidLoaded) {
            TLRPC.ChatFull chatFull = (TLRPC.ChatFull) args[0];
            if (chatFull.id == chatId) {
                invite = chatFull.exported_invite;
                updatePrivatePublic();
            }
        }
    }

    public void setInfo(TLRPC.ChatFull chatFull) {
        if (chatFull != null) {
            if (chatFull.exported_invite instanceof TLRPC.TL_chatInviteExported) {
                invite = chatFull.exported_invite;
            } else {
                generateLink();
            }
        }
    }

    private void loadAdminedChannels() {
        if (loadingAdminedChannels) {
            return;
        }
        loadingAdminedChannels = true;
        updatePrivatePublic();
        TLRPC.TL_channels_getAdminedPublicChannels req = new TLRPC.TL_channels_getAdminedPublicChannels();
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingAdminedChannels = false;
                        if (response != null) {
                            if (getParentActivity() == null) {
                                return;
                            }
                            for (int a = 0; a < adminedChannelCells.size(); a++) {
                                linearLayout.removeView(adminedChannelCells.get(a));
                            }
                            adminedChannelCells.clear();
                            TLRPC.TL_messages_chats res = (TLRPC.TL_messages_chats) response;

                            for (int a = 0; a < res.chats.size(); a++) {
                                AdminedChannelCell adminedChannelCell = new AdminedChannelCell(getParentActivity(), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AdminedChannelCell cell = (AdminedChannelCell) view.getParent();
                                        final TLRPC.Chat channel = cell.getCurrentChannel();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        if (channel.megagroup) {
                                            builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("RevokeLinkAlert", R.string.RevokeLinkAlert, MessagesController.getInstance().linkPrefix + "/" + channel.username, channel.title)));
                                        } else {
                                            builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("RevokeLinkAlertChannel", R.string.RevokeLinkAlertChannel, MessagesController.getInstance().linkPrefix + "/" + channel.username, channel.title)));
                                        }
                                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                        builder.setPositiveButton(LocaleController.getString("RevokeButton", R.string.RevokeButton), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                TLRPC.TL_channels_updateUsername req = new TLRPC.TL_channels_updateUsername();
                                                req.channel = MessagesController.getInputChannel(channel);
                                                req.username = "";
                                                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                                                    @Override
                                                    public void run(TLObject response, TLRPC.TL_error error) {
                                                        if (response instanceof TLRPC.TL_boolTrue) {
                                                            AndroidUtilities.runOnUIThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    canCreatePublic = true;
                                                                    if (nameTextView.length() > 0) {
                                                                        checkUserName(nameTextView.getText().toString());
                                                                    }
                                                                    updatePrivatePublic();
                                                                }
                                                            });
                                                        }
                                                    }
                                                }, ConnectionsManager.RequestFlagInvokeAfter);
                                            }
                                        });
                                        showDialog(builder.create());
                                    }
                                });
                                adminedChannelCell.setChannel(res.chats.get(a), a == res.chats.size() - 1);
                                adminedChannelCells.add(adminedChannelCell);
                                linearLayout.addView(adminedChannelCell, linearLayout.getChildCount() - 1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 72));
                            }
                            updatePrivatePublic();
                        }
                    }
                });
            }
        });
    }

    private void updatePrivatePublic() {
        if (sectionCell == null) {
            return;
        }
        if (!isPrivate && !canCreatePublic) {
            typeInfoCell.setText(LocaleController.getString("ChangePublicLimitReached", R.string.ChangePublicLimitReached));
            typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
            typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
            linkContainer.setVisibility(View.GONE);
            sectionCell.setVisibility(View.GONE);
            if (loadingAdminedChannels) {
                loadingAdminedCell.setVisibility(View.VISIBLE);
                adminnedChannelsLayout.setVisibility(View.GONE);
                typeInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(typeInfoCell.getContext(), R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                adminedInfoCell.setVisibility(View.GONE);
            } else {
                typeInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(typeInfoCell.getContext(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                loadingAdminedCell.setVisibility(View.GONE);
                adminnedChannelsLayout.setVisibility(View.VISIBLE);
                adminedInfoCell.setVisibility(View.VISIBLE);
            }
        } else {
            typeInfoCell.setTag(Theme.key_windowBackgroundWhiteGrayText4);
            typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
            sectionCell.setVisibility(View.VISIBLE);
            adminedInfoCell.setVisibility(View.GONE);
            typeInfoCell.setBackgroundDrawable(Theme.getThemedDrawable(typeInfoCell.getContext(), R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            adminnedChannelsLayout.setVisibility(View.GONE);
            linkContainer.setVisibility(View.VISIBLE);
            loadingAdminedCell.setVisibility(View.GONE);
            if (currentChat.megagroup) {
                typeInfoCell.setText(isPrivate ? LocaleController.getString("MegaPrivateLinkHelp", R.string.MegaPrivateLinkHelp) : LocaleController.getString("MegaUsernameHelp", R.string.MegaUsernameHelp));
                headerCell.setText(isPrivate ? LocaleController.getString("ChannelInviteLinkTitle", R.string.ChannelInviteLinkTitle) : LocaleController.getString("ChannelLinkTitle", R.string.ChannelLinkTitle));
            } else {
                typeInfoCell.setText(isPrivate ? LocaleController.getString("ChannelPrivateLinkHelp", R.string.ChannelPrivateLinkHelp) : LocaleController.getString("ChannelUsernameHelp", R.string.ChannelUsernameHelp));
                headerCell.setText(isPrivate ? LocaleController.getString("ChannelInviteLinkTitle", R.string.ChannelInviteLinkTitle) : LocaleController.getString("ChannelLinkTitle", R.string.ChannelLinkTitle));
            }
            publicContainer.setVisibility(isPrivate ? View.GONE : View.VISIBLE);
            privateContainer.setVisibility(isPrivate ? View.VISIBLE : View.GONE);
            linkContainer.setPadding(0, 0, 0, isPrivate ? 0 : AndroidUtilities.dp(7));
            privateContainer.setText(invite != null ? invite.link : LocaleController.getString("Loading", R.string.Loading), false);
            checkTextView.setVisibility(!isPrivate && checkTextView.length() != 0 ? View.VISIBLE : View.GONE);
        }
        radioButtonCell1.setChecked(!isPrivate, true);
        radioButtonCell2.setChecked(isPrivate, true);
        nameTextView.clearFocus();
        AndroidUtilities.hideKeyboard(nameTextView);
    }

    private boolean checkUserName(final String name) {
        if (name != null && name.length() > 0) {
            checkTextView.setVisibility(View.VISIBLE);
        } else {
            checkTextView.setVisibility(View.GONE);
        }
        if (checkRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(checkRunnable);
            checkRunnable = null;
            lastCheckName = null;
            if (checkReqId != 0) {
                ConnectionsManager.getInstance().cancelRequest(checkReqId, true);
            }
        }
        lastNameAvailable = false;
        if (name != null) {
            if (name.startsWith("_") || name.endsWith("_")) {
                checkTextView.setText(LocaleController.getString("LinkInvalid", R.string.LinkInvalid));
                typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
                typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
                return false;
            }
            for (int a = 0; a < name.length(); a++) {
                char ch = name.charAt(a);
                if (a == 0 && ch >= '0' && ch <= '9') {
                    if (currentChat.megagroup) {
                        checkTextView.setText(LocaleController.getString("LinkInvalidStartNumberMega", R.string.LinkInvalidStartNumberMega));
                        typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
                        typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
                    } else {
                        checkTextView.setText(LocaleController.getString("LinkInvalidStartNumber", R.string.LinkInvalidStartNumber));
                        typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
                        typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
                    }
                    return false;
                }
                if (!(ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_')) {
                    checkTextView.setText(LocaleController.getString("LinkInvalid", R.string.LinkInvalid));
                    typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
                    typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
                    return false;
                }
            }
        }
        if (name == null || name.length() < 5) {
            if (currentChat.megagroup) {
                checkTextView.setText(LocaleController.getString("LinkInvalidShortMega", R.string.LinkInvalidShortMega));
                typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
                typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
            } else {
                checkTextView.setText(LocaleController.getString("LinkInvalidShort", R.string.LinkInvalidShort));
                typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
                typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
            }
            return false;
        }
        if (name.length() > 32) {
            checkTextView.setText(LocaleController.getString("LinkInvalidLong", R.string.LinkInvalidLong));
            typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
            typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
            return false;
        }


        checkTextView.setText(LocaleController.getString("LinkChecking", R.string.LinkChecking));
        checkTextView.setTag(Theme.key_windowBackgroundWhiteGrayText8);
        checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText8));
        lastCheckName = name;
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                TLRPC.TL_channels_checkUsername req = new TLRPC.TL_channels_checkUsername();
                req.username = name;
                req.channel = MessagesController.getInputChannel(chatId);
                checkReqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(final TLObject response, final TLRPC.TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                checkReqId = 0;
                                if (lastCheckName != null && lastCheckName.equals(name)) {
                                    if (error == null && response instanceof TLRPC.TL_boolTrue) {
                                        checkTextView.setText(LocaleController.formatString("LinkAvailable", R.string.LinkAvailable, name));
                                        checkTextView.setTag(Theme.key_windowBackgroundWhiteGreenText);
                                        checkTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGreenText));
                                        lastNameAvailable = true;
                                    } else {
                                        if (error != null && error.text.equals("CHANNELS_ADMIN_PUBLIC_TOO_MUCH")) {
                                            canCreatePublic = false;
                                            loadAdminedChannels();
                                        } else {
                                            checkTextView.setText(LocaleController.getString("LinkInUse", R.string.LinkInUse));
                                        }
                                        typeInfoCell.setTag(Theme.key_windowBackgroundWhiteRedText4);
                                        typeInfoCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
                                        lastNameAvailable = false;
                                    }
                                }
                            }
                        });
                    }
                }, ConnectionsManager.RequestFlagFailOnServerErrors);
            }
        };
        AndroidUtilities.runOnUIThread(checkRunnable, 300);
        return true;
    }

    private void generateLink() {
        if (loadingInvite || invite != null) {
            return;
        }
        loadingInvite = true;
        TLRPC.TL_channels_exportInvite req = new TLRPC.TL_channels_exportInvite();
        req.channel = MessagesController.getInputChannel(chatId);
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            invite = (TLRPC.ExportedChatInvite) response;
                        }
                        loadingInvite = false;
                        privateContainer.setText(invite != null ? invite.link : LocaleController.getString("Loading", R.string.Loading), false);
                    }
                });
            }
        });
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate сellDelegate = new ThemeDescription.ThemeDescriptionDelegate() {
            @Override
            public void didSetColor(int color) {
                if (adminnedChannelsLayout != null) {
                    int count = adminnedChannelsLayout.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = adminnedChannelsLayout.getChildAt(a);
                        if (child instanceof AdminedChannelCell) {
                            ((AdminedChannelCell) child).update();
                        }
                    }
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

                new ThemeDescription(linearLayout2, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(linkContainer, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(sectionCell, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(headerCell, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),
                new ThemeDescription(editText, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(editText, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText),
                new ThemeDescription(checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, Theme.key_windowBackgroundWhiteRedText4),
                new ThemeDescription(checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, Theme.key_windowBackgroundWhiteGrayText8),
                new ThemeDescription(checkTextView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, Theme.key_windowBackgroundWhiteGreenText),

                new ThemeDescription(typeInfoCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(typeInfoCell, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4),
                new ThemeDescription(typeInfoCell, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteRedText4),

                new ThemeDescription(adminedInfoCell, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),
                new ThemeDescription(adminnedChannelsLayout, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(privateContainer, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),
                new ThemeDescription(privateContainer, 0, new Class[]{TextBlockCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(loadingAdminedCell, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle),
                new ThemeDescription(radioButtonCell1, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),
                new ThemeDescription(radioButtonCell1, ThemeDescription.FLAG_CHECKBOX, new Class[]{RadioButtonCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackground),
                new ThemeDescription(radioButtonCell1, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{RadioButtonCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackgroundChecked),
                new ThemeDescription(radioButtonCell1, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{RadioButtonCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(radioButtonCell1, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{RadioButtonCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(radioButtonCell2, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),
                new ThemeDescription(radioButtonCell2, ThemeDescription.FLAG_CHECKBOX, new Class[]{RadioButtonCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackground),
                new ThemeDescription(radioButtonCell2, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{RadioButtonCell.class}, new String[]{"radioButton"}, null, null, null, Theme.key_radioBackgroundChecked),
                new ThemeDescription(radioButtonCell2, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{RadioButtonCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(radioButtonCell2, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{RadioButtonCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),

                new ThemeDescription(adminnedChannelsLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{AdminedChannelCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(adminnedChannelsLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{AdminedChannelCell.class}, new String[]{"statusTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText),
                new ThemeDescription(adminnedChannelsLayout, ThemeDescription.FLAG_LINKCOLOR, new Class[]{AdminedChannelCell.class}, new String[]{"statusTextView"}, null, null, null, Theme.key_windowBackgroundWhiteLinkText),
                new ThemeDescription(adminnedChannelsLayout, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{AdminedChannelCell.class}, new String[]{"deleteButton"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText),
                new ThemeDescription(null, 0, null, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable}, сellDelegate, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundRed),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundOrange),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundViolet),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundGreen),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundCyan),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundBlue),
                new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundPink),
        };
    }
}

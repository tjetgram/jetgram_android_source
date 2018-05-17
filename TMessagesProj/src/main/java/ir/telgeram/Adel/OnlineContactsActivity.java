package ir.telgeram.Adel;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ApplicationLoader;
import ir.telgeram.messenger.ContactsController;
import ir.telgeram.messenger.ContactsController.Contact;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.MessagesController;
import ir.telgeram.messenger.MessagesStorage;
import ir.telgeram.messenger.NotificationCenter;
import ir.telgeram.messenger.NotificationCenter.NotificationCenterDelegate;
import ir.telgeram.messenger.R;
import ir.telgeram.messenger.SecretChatHelper;
import ir.telgeram.messenger.UserConfig;
import ir.telgeram.messenger.UserObject;
import ir.telgeram.messenger.Utilities;
import ir.telgeram.tgnet.TLRPC.EncryptedChat;
import ir.telgeram.tgnet.TLRPC.User;
import ir.telgeram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import ir.telgeram.ui.ActionBar.ActionBarMenu;
import ir.telgeram.ui.ActionBar.ActionBarMenuItem;
import ir.telgeram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import ir.telgeram.ui.ActionBar.BaseFragment;
import ir.telgeram.ui.Adapters.ContactsAdapter;
import ir.telgeram.ui.Adapters.SearchAdapter;
import ir.telgeram.ui.Cells.UserCell;
import ir.telgeram.ui.ChannelCreateActivity;
import ir.telgeram.ui.ChannelIntroActivity;
import ir.telgeram.ui.ChatActivity;
import ir.telgeram.ui.ContactAddActivity;
import ir.telgeram.ui.GroupCreateActivity;
import ir.telgeram.ui.GroupCreateActivity.GroupCreateActivityDelegate;
import ir.telgeram.ui.GroupInviteActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OnlineContactsActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int add_button = 1;
    private static final int delete = 3;
    private static final int done_button = 10;
    private static final int refresh = 2;
    private static final int search_button = 0;
    private static final int select_all_button = 11;
    private boolean allowBots;
    private boolean allowUsernameSearch;
    private int chat_id;
    private boolean createSecretChat;
    private boolean creatingChat;
    private ContactsActivityDelegate delegate;
    private boolean destroyAfterSelect;
    private TextView emptyTextView;
    private HashMap<Integer, User> ignoreUsers;
    private LetterSectionsListView listView;
    private BaseSectionsAdapter listViewAdapter;
    private ContactsActivityMultiSelectDelegate multiSelectDelegate;
    private boolean multiSelectMode;
    private boolean needForwardCount;
    private boolean needPhonebook;
    private boolean onlyOnlines;
    private boolean onlyUsers;
    ProgressDialog progressDialog;
    private boolean returnAsResult;
    private OnlineContactsSearchAdapter searchListViewAdapter;
    private boolean searchWas;
    private boolean searching;
    private String selectAlertString;
    private HashMap<Integer, ChipSpan> selectedContacts;

    public interface ContactsActivityDelegate {
        void didSelectContact(User user, String str);
    }

    public interface ContactsActivityMultiSelectDelegate {
        void didSelectContacts(List<Integer> list, String str);
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.10 */
    class AnonymousClass10 implements OnClickListener {
        final /* synthetic */ ArrayList val$selectedContacts;

        AnonymousClass10(ArrayList arrayList) {
            this.val$selectedContacts = arrayList;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            ArrayList arrayList = new ArrayList();
            Iterator it = this.val$selectedContacts.iterator();
            while (it.hasNext()) {
                arrayList.add(MessagesController.getInstance().getUser((Integer) it.next()));
            }
            ContactsController.getInstance().deleteContact(arrayList);
            if (OnlineContactsActivity.this.listViewAdapter != null) {
                OnlineContactsActivity.this.listViewAdapter.notifyDataSetChanged();
            }
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.12 */
    class AnonymousClass12 implements TextWatcher {
        final /* synthetic */ EditText val$editTextFinal;

        AnonymousClass12(EditText editText) {
            this.val$editTextFinal = editText;
        }

        public void afterTextChanged(Editable editable) {
            try {
                String obj = editable.toString();
                if (obj.length() != 0) {
                    int intValue = Utilities.parseInt(obj).intValue();
                    if (intValue < 0) {
                        this.val$editTextFinal.setText("0");
                        this.val$editTextFinal.setSelection(this.val$editTextFinal.length());
                    } else if (intValue > 300) {
                        this.val$editTextFinal.setText("300");
                        this.val$editTextFinal.setSelection(this.val$editTextFinal.length());
                    } else if (!obj.equals("" + intValue)) {
                        this.val$editTextFinal.setText("" + intValue);
                        this.val$editTextFinal.setSelection(this.val$editTextFinal.length());
                    }
                }
            } catch (Throwable e) {
              //  FileLog.m18e("tmessages", e);
            }
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.13 */
    class AnonymousClass13 implements OnClickListener {
        final /* synthetic */ EditText val$finalEditText;
        final /* synthetic */ List val$userIds;

        AnonymousClass13(List list, EditText editText) {
            this.val$userIds = list;
            this.val$finalEditText = editText;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            OnlineContactsActivity.this.didSelectMultiResult(this.val$userIds, false, this.val$finalEditText != null ? this.val$finalEditText.getText().toString() : "0");
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.1 */
    class C15041 extends ActionBarMenuOnItemClick {
        C15041() {
        }

        public void onItemClick(int i) {
            if (i == -1) {
                OnlineContactsActivity.this.finishFragment();
            } else if (i == OnlineContactsActivity.add_button) {
                //OnlineContactsActivity.this.presentFragment(new NewContactActivity());
            } else if (i == OnlineContactsActivity.refresh) {
                OnlineContactsActivity.this.reloadOnlineList();
            } else if (i == OnlineContactsActivity.delete) {
                OnlineContactsActivity.this.doDeleteMultipleContacts();
            } else if (i == OnlineContactsActivity.done_button) {
                OnlineContactsActivity.this.didSelectMultiResult(new ArrayList(OnlineContactsActivity.this.selectedContacts.keySet()), true, null);
            } else if (i == OnlineContactsActivity.select_all_button) {
                OnlineContactsActivity.this.selectAll();
            }
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.2 */
    class C15052 extends ActionBarMenuItemSearchListener {
        C15052() {
        }

        public void onSearchCollapse() {
            OnlineContactsActivity.this.searchListViewAdapter.searchDialogs(null);
            OnlineContactsActivity.this.searching = false;
            OnlineContactsActivity.this.searchWas = false;
            OnlineContactsActivity.this.listView.setAdapter(OnlineContactsActivity.this.listViewAdapter);
            OnlineContactsActivity.this.listViewAdapter.notifyDataSetChanged();
            OnlineContactsActivity.this.listView.setFastScrollAlwaysVisible(true);
            OnlineContactsActivity.this.listView.setFastScrollEnabled(true);
            OnlineContactsActivity.this.listView.setVerticalScrollBarEnabled(false);
            OnlineContactsActivity.this.emptyTextView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
        }

        public void onSearchExpand() {
            OnlineContactsActivity.this.searching = true;
        }

        public void onTextChanged(EditText editText) {
            if (OnlineContactsActivity.this.searchListViewAdapter != null) {
                String obj = editText.getText().toString();
                if (obj.length() != 0) {
                    OnlineContactsActivity.this.searchWas = true;
                    if (OnlineContactsActivity.this.listView != null) {
                        OnlineContactsActivity.this.listView.setAdapter(OnlineContactsActivity.this.searchListViewAdapter);
                        OnlineContactsActivity.this.searchListViewAdapter.notifyDataSetChanged();
                        OnlineContactsActivity.this.listView.setFastScrollAlwaysVisible(false);
                        OnlineContactsActivity.this.listView.setFastScrollEnabled(false);
                        OnlineContactsActivity.this.listView.setVerticalScrollBarEnabled(true);
                    }
                    if (OnlineContactsActivity.this.emptyTextView != null) {
                        OnlineContactsActivity.this.emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
                    }
                }
                OnlineContactsActivity.this.searchListViewAdapter.searchDialogs(obj);
            }
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.3 */
    class C15063 implements OnTouchListener {
        C15063() {
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.4 */
    class C15084 implements OnItemClickListener {

        /* renamed from: ir.telgeram.ui.OnlineContactsActivity.4.1 */
        class C15071 implements OnClickListener {
            final /* synthetic */ String val$arg1;

            C15071(String str) {
                this.val$arg1 = str;
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.fromParts("sms", this.val$arg1, null));
                    intent.putExtra("sms_body", LocaleController.getString("InviteText", R.string.InviteText));
                    OnlineContactsActivity.this.getParentActivity().startActivityForResult(intent, 500);
                } catch (Throwable e) {
                    //FileLog.m18e("tmessages", e);
                }
            }
        }

        C15084() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            User user;
            Bundle bundle;
            if (OnlineContactsActivity.this.searching && OnlineContactsActivity.this.searchWas) {
                user = (User) OnlineContactsActivity.this.searchListViewAdapter.getItem(i);
                if (user != null) {
                    if (OnlineContactsActivity.this.searchListViewAdapter.isGlobalSearch(i)) {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(user);
                        MessagesController.getInstance().putUsers(arrayList, false);
                        MessagesStorage.getInstance().putUsersAndChats(arrayList, null, false, true);
                    }
                    if (OnlineContactsActivity.this.returnAsResult) {
                        if (OnlineContactsActivity.this.ignoreUsers == null || !OnlineContactsActivity.this.ignoreUsers.containsKey(Integer.valueOf(user.id))) {
                            OnlineContactsActivity.this.didSelectResult(user, true, null);
                            return;
                        }
                        return;
                    } else if (!OnlineContactsActivity.this.createSecretChat) {
                        bundle = new Bundle();
                        bundle.putInt("user_id", user.id);
                        if (MessagesController.checkCanOpenChat(bundle, OnlineContactsActivity.this)) {
                            OnlineContactsActivity.this.presentFragment(new ChatActivity(bundle), false);
                            return;
                        }
                        return;
                    } else if (user.id != UserConfig.getClientUserId()) {
                        OnlineContactsActivity.this.creatingChat = true;
                        SecretChatHelper.getInstance().startSecretChat(OnlineContactsActivity.this.getParentActivity(), user);
                        return;
                    } else {
                        return;
                    }
                }
                return;
            }
            int sectionForPosition = OnlineContactsActivity.this.listViewAdapter.getSectionForPosition(i);
            int positionInSectionForPosition = OnlineContactsActivity.this.listViewAdapter.getPositionInSectionForPosition(i);
            if (positionInSectionForPosition >= 0 && sectionForPosition >= 0) {
                if ((OnlineContactsActivity.this.onlyUsers && OnlineContactsActivity.this.chat_id == 0) || sectionForPosition != 0) {
                    Object item = OnlineContactsActivity.this.listViewAdapter.getItem(sectionForPosition, positionInSectionForPosition);
                    if (item instanceof User) {
                        user = (User) item;
                        if (OnlineContactsActivity.this.returnAsResult) {
                            if (OnlineContactsActivity.this.ignoreUsers == null || !OnlineContactsActivity.this.ignoreUsers.containsKey(Integer.valueOf(user.id))) {
                                OnlineContactsActivity.this.didSelectResult(user, true, null);
                            }
                        } else if (OnlineContactsActivity.this.createSecretChat) {
                            OnlineContactsActivity.this.creatingChat = true;
                            SecretChatHelper.getInstance().startSecretChat(OnlineContactsActivity.this.getParentActivity(), user);
                        } else {
                            bundle = new Bundle();
                            bundle.putInt("user_id", user.id);
                            if (MessagesController.checkCanOpenChat(bundle, OnlineContactsActivity.this)) {
                                OnlineContactsActivity.this.presentFragment(new ChatActivity(bundle), false);
                            }
                        }
                    } else if (item instanceof Contact) {
                        Contact contact = (Contact) item;
                        String str = !contact.phones.isEmpty() ? (String) contact.phones.get(OnlineContactsActivity.search_button) : null;
                        if (str != null && OnlineContactsActivity.this.getParentActivity() != null) {
                            Builder builder = new Builder(OnlineContactsActivity.this.getParentActivity());
                            builder.setMessage(LocaleController.getString("InviteUser", R.string.InviteUser));
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new C15071(str));
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            OnlineContactsActivity.this.showDialog(builder.create());
                        }
                    }
                } else if (OnlineContactsActivity.this.needPhonebook) {
                    if (positionInSectionForPosition == 0) {
                        try {
                            Intent intent = new Intent("android.intent.action.SEND");
                            intent.setType("text/plain");
                            intent.putExtra("android.intent.extra.TEXT", ContactsController.getInstance().getInviteText());
                            OnlineContactsActivity.this.getParentActivity().startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteFriends", R.string.InviteFriends)), 500);
                        } catch (Throwable e) {
                          //  FileLog.m18e("tmessages", e);
                        }
                    } else if (positionInSectionForPosition == OnlineContactsActivity.add_button) {
                        Bundle r0 = new Bundle();
                        r0.putBoolean("addContact", true);
                        r0.putBoolean("isNew", true);
                        OnlineContactsActivity.this.presentFragment(new ContactAddActivity(r0));
                    }
                } else if (OnlineContactsActivity.this.chat_id != 0) {
                    if (positionInSectionForPosition == 0) {
                        OnlineContactsActivity.this.presentFragment(new GroupInviteActivity(OnlineContactsActivity.this.chat_id));
                    } else if (positionInSectionForPosition == OnlineContactsActivity.add_button) {
                        Bundle r0 = new Bundle();
                        r0.putBoolean("addContact", true);
                        r0.putBoolean("isNew", true);
                        OnlineContactsActivity.this.presentFragment(new ContactAddActivity(r0));
                    }
                } else if (positionInSectionForPosition == 0) {
                    if (MessagesController.isFeatureEnabled("chat_create", OnlineContactsActivity.this)) {
                        OnlineContactsActivity.this.presentFragment(new GroupCreateActivity(), false);
                    }
                } else if (positionInSectionForPosition == OnlineContactsActivity.add_button) {
                    Bundle r0 = new Bundle();
                    r0.putBoolean("onlyUsers", true);
                    r0.putBoolean("destroyAfterSelect", true);
                    r0.putBoolean("createSecretChat", true);
                    r0.putBoolean("allowBots", false);
                    OnlineContactsActivity.this.presentFragment(new OnlineContactsActivity(r0), false);
                } else if (positionInSectionForPosition == OnlineContactsActivity.refresh) {
                    if (MessagesController.isFeatureEnabled("broadcast_create", OnlineContactsActivity.this)) {
                        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", OnlineContactsActivity.search_button);
                        if (sharedPreferences.getBoolean("channel_intro", false)) {
                            Bundle r0 = new Bundle();
                            r0.putInt("step", OnlineContactsActivity.search_button);
                            OnlineContactsActivity.this.presentFragment(new ChannelCreateActivity(r0));
                            return;
                        }
                        OnlineContactsActivity.this.presentFragment(new ChannelIntroActivity());
                        sharedPreferences.edit().putBoolean("channel_intro", true).commit();
                    }
                } else if (positionInSectionForPosition == OnlineContactsActivity.delete) {
                    Bundle r0 = new Bundle();
                    r0.putBoolean("addContact", true);
                    r0.putBoolean("isNew", true);
                    OnlineContactsActivity.this.presentFragment(new ContactAddActivity(r0));
                }
            }
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.5 */
    class C15095 implements OnScrollListener {
        C15095() {
        }

        public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            if (absListView.isFastScrollEnabled()) {
                AndroidUtilities.clearDrawableAnimation(absListView);
            }
        }

        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (i == OnlineContactsActivity.add_button && OnlineContactsActivity.this.searching && OnlineContactsActivity.this.searchWas) {
                AndroidUtilities.hideKeyboard(OnlineContactsActivity.this.getParentActivity().getCurrentFocus());
            }
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.6 */
    class C15106 implements TextWatcher {
        final /* synthetic */ EditText val$editTextFinal;

        C15106(EditText editText) {
            this.val$editTextFinal = editText;
        }

        public void afterTextChanged(Editable editable) {
            try {
                String obj = editable.toString();
                if (obj.length() != 0) {
                    int intValue = Utilities.parseInt(obj).intValue();
                    if (intValue < 0) {
                        this.val$editTextFinal.setText("0");
                        this.val$editTextFinal.setSelection(this.val$editTextFinal.length());
                    } else if (intValue > 300) {
                        this.val$editTextFinal.setText("300");
                        this.val$editTextFinal.setSelection(this.val$editTextFinal.length());
                    } else if (!obj.equals("" + intValue)) {
                        this.val$editTextFinal.setText("" + intValue);
                        this.val$editTextFinal.setSelection(this.val$editTextFinal.length());
                    }
                }
            } catch (Throwable e) {
                //FileLog.m18e("tmessages", e);
            }
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.7 */
    class C15117 implements OnClickListener {
        final /* synthetic */ EditText val$finalEditText;
        final /* synthetic */ User val$user;

        C15117(User user, EditText editText) {
            this.val$user = user;
            this.val$finalEditText = editText;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            OnlineContactsActivity.this.didSelectResult(this.val$user, false, this.val$finalEditText != null ? this.val$finalEditText.getText().toString() : "0");
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.8 */
    class C15128 implements OnClickListener {
        C15128() {
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
        }
    }

    /* renamed from: ir.telgeram.ui.OnlineContactsActivity.9 */
    class C15139 implements GroupCreateActivityDelegate {
        C15139() {
        }

        public void didSelectUsers(ArrayList<Integer> arrayList) {
            OnlineContactsActivity.this.showDeleteContactsConfirmation(arrayList);
        }
    }

    public OnlineContactsActivity(Bundle bundle) {
        super(bundle);
        this.creatingChat = false;
        this.allowBots = true;
        this.needForwardCount = true;
        this.selectAlertString = null;
        this.allowUsernameSearch = true;
        this.selectedContacts = new HashMap();
    }

    private void didSelectMultiResult(List<Integer> list, boolean z, String str) {
        if (!z || this.selectAlertString == null) {
            if (this.multiSelectDelegate != null) {
                this.multiSelectDelegate.didSelectContacts(list, str);
                this.multiSelectDelegate = null;
            }
            finishFragment();
        } else if (getParentActivity() != null) {
            User user;
            String str2;
            Object[] objArr;
            CharSequence formatStringSimple;
            EditText editText;
            for (Integer user2 : list) {
                user = MessagesController.getInstance().getUser(user2);
                if (user.bot && user.bot_nochats) {
                    try {
                        Toast.makeText(getParentActivity(), LocaleController.getString("BotCantJoinGroups", R.string.BotCantJoinGroups), Toast.LENGTH_SHORT).show();
                        return;
                    } catch (Throwable e) {
                       // FileLog.m18e("tmessages", e);
                        return;
                    }
                }
            }
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            if (list.size() == add_button) {
                user = MessagesController.getInstance().getUser((Integer) list.get(search_button));
                str2 = this.selectAlertString;
                objArr = new Object[add_button];
                objArr[search_button] = UserObject.getUserName(user);
                formatStringSimple = LocaleController.formatStringSimple(str2, objArr);
            } else {
                String str3 = this.selectAlertString;
                Object[] objArr2 = new Object[add_button];
                objArr2[search_button] = list.size() + " " + LocaleController.getString("User", R.string.User);
                formatStringSimple = LocaleController.formatStringSimple(str3, objArr2);
            }
            if (this.needForwardCount) {
                objArr = new Object[refresh];
                objArr[search_button] = formatStringSimple;
                objArr[add_button] = LocaleController.getString("AddToTheGroupForwardCount", R.string.AddToTheGroupForwardCount);
                str2 = String.format("%s\n\n%s", objArr);
                EditText editText2 = new EditText(getParentActivity());
                editText2.setTextSize(18.0f);
                editText2.setText("50");
                editText2.setGravity(17);
                editText2.setInputType(refresh);
                editText2.setImeOptions(6);
                editText2.addTextChangedListener(new AnonymousClass12(editText2));
                builder.setView(editText2);
                EditText view = editText2;
                formatStringSimple = str2;
                editText = view;
            } else {
                editText = null;
            }
            builder.setMessage(formatStringSimple);
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new AnonymousClass13(list, editText));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
            if (editText != null) {
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) editText.getLayoutParams();
                if (marginLayoutParams != null) {
                    if (marginLayoutParams instanceof LayoutParams) {
                        ((LayoutParams) marginLayoutParams).gravity = add_button;
                    }
                    int dp = AndroidUtilities.dp(10.0f);
                    marginLayoutParams.leftMargin = dp;
                    marginLayoutParams.rightMargin = dp;
                    editText.setLayoutParams(marginLayoutParams);
                }
                editText.setSelection(editText.getText().length());
            }
        }
    }

    private void didSelectResult(User user, boolean z, String str) {
        if (this.multiSelectMode) {
            selectUser(user);
        } else if (!z || this.selectAlertString == null) {
            if (this.delegate != null) {
                this.delegate.didSelectContact(user, str);
                this.delegate = null;
            }
            finishFragment();
        } else if (getParentActivity() == null) {
        } else {
            if (user.bot && user.bot_nochats) {
                try {
                    Toast.makeText(getParentActivity(), LocaleController.getString("BotCantJoinGroups", R.string.BotCantJoinGroups), Toast.LENGTH_SHORT).show();
                    return;
                } catch (Throwable e) {
                 //   FileLog.m18e("tmessages", e);
                    return;
                }
            }
            EditText editText;
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            String str2 = this.selectAlertString;
            Object[] objArr = new Object[add_button];
            objArr[search_button] = UserObject.getUserName(user);
            CharSequence formatStringSimple = LocaleController.formatStringSimple(str2, objArr);
            if (user.bot || !this.needForwardCount) {
                editText = null;
            } else {
                Object[] objArr2 = new Object[refresh];
                objArr2[search_button] = formatStringSimple;
                objArr2[add_button] = LocaleController.getString("AddToTheGroupForwardCount", R.string.AddToTheGroupForwardCount);
                String format = String.format("%s\n\n%s", objArr2);
                EditText editText2 = new EditText(getParentActivity());
                editText2.setTextSize(18.0f);
                editText2.setText("50");
                editText2.setGravity(17);
                editText2.setInputType(refresh);
                editText2.setImeOptions(6);
                editText2.addTextChangedListener(new C15106(editText2));
                builder.setView(editText2);
                EditText view = editText2;
                formatStringSimple = format;
                editText = view;
            }
            builder.setMessage(formatStringSimple);
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new C15117(user, editText));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
            if (editText != null) {
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) editText.getLayoutParams();
                if (marginLayoutParams != null) {
                    if (marginLayoutParams instanceof LayoutParams) {
                        ((LayoutParams) marginLayoutParams).gravity = add_button;
                    }
                    int dp = AndroidUtilities.dp(10.0f);
                    marginLayoutParams.leftMargin = dp;
                    marginLayoutParams.rightMargin = dp;
                    editText.setLayoutParams(marginLayoutParams);
                }
                editText.setSelection(editText.getText().length());
            }
        }
    }

    private void doDeleteMultipleContacts() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isDelete", true);
        GroupCreateActivity groupCreateActivity = new GroupCreateActivity(bundle);
        groupCreateActivity.setDelegate(new C15139());
        presentFragment(groupCreateActivity);
    }

    private void initTheme() {
//        if (ThemeUtil.m2005b()) {
//            int i = AdvanceTheme.bc;
//            this.actionBar.setBackgroundColor(i);
//            int i2 = AdvanceTheme.bd;
//            if (i2 > 0) {
//                Orientation orientation;
//                switch (i2) {
//                    case refresh /*2*/:
//                        orientation = Orientation.LEFT_RIGHT;
//                        break;
//                    case delete /*3*/:
//                        orientation = Orientation.TL_BR;
//                        break;
//                    case VideoPlayer.STATE_READY /*4*/:
//                        orientation = Orientation.BL_TR;
//                        break;
//                    default:
//                        orientation = Orientation.TOP_BOTTOM;
//                        break;
//                }
//                int i3 = AdvanceTheme.be;
//                int[] iArr = new int[refresh];
//                iArr[search_button] = i;
//                iArr[add_button] = i3;
//                this.actionBar.setBackgroundDrawable(new GradientDrawable(orientation, iArr));
//            }
//
//        }
    }

    private void initThemeListView() {

    }

    private void initThemeSearchItem(ActionBarMenuItem actionBarMenuItem) {
//        if (ThemeUtil.m2005b()) {
//            actionBarMenuItem.getSearchField().setTextColor(AdvanceTheme.bb);
//            Drawable drawable = getParentActivity().getResources().getDrawable(R.drawable.ic_close_white);
//            drawable.setColorFilter(AdvanceTheme.ba, Mode.MULTIPLY);
//            actionBarMenuItem.getClearButton().setImageDrawable(drawable);
//        }
    }

    private void reloadOnlineList() {
        if (this.onlyOnlines) {
            ContactsController.getInstance().initOnlineUsersSectionsDict();
            this.listViewAdapter = new OnlineContactsAdapter(ApplicationLoader.applicationContext, this.onlyUsers, false, this.ignoreUsers, this.chat_id != 0);
            this.listView.setAdapter(this.listViewAdapter);
        }
    }

    private void selectAll() {
        this.progressDialog = new ProgressDialog(getParentActivity());
        this.progressDialog.setMessage(LocaleController.getString("PleaseWait", R.string.PleaseWait));
        this.progressDialog.setCanceledOnTouchOutside(false);
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                OnlineContactsActivity.this.selectedContacts.clear();
                int i;
                User user;
                if (OnlineContactsActivity.this.searching && OnlineContactsActivity.this.searchWas) {
                    for (i = OnlineContactsActivity.search_button; i < OnlineContactsActivity.this.searchListViewAdapter.getCount(); i += OnlineContactsActivity.add_button) {
                        user = (User) OnlineContactsActivity.this.searchListViewAdapter.getItem(i);
                        if (user != null) {
                            OnlineContactsActivity.this.selectedContacts.put(Integer.valueOf(user.id), null);
                        }
                    }
                } else {
                    for (i = OnlineContactsActivity.search_button; i < OnlineContactsActivity.this.listViewAdapter.getSectionCount(); i += OnlineContactsActivity.add_button) {
                        for (int i2 = OnlineContactsActivity.search_button; i2 < OnlineContactsActivity.this.listViewAdapter.getCountForSection(i); i2 += OnlineContactsActivity.add_button) {
                            user = (User) OnlineContactsActivity.this.listViewAdapter.getItem(i, i2);
                            if (user != null) {
                                OnlineContactsActivity.this.selectedContacts.put(Integer.valueOf(user.id), null);
                            }
                        }
                    }
                }
                if (OnlineContactsActivity.this.searching || OnlineContactsActivity.this.searchWas) {
                    OnlineContactsActivity.this.searchListViewAdapter.searchDialogs(null);
                    OnlineContactsActivity.this.searching = false;
                    OnlineContactsActivity.this.searchWas = false;
                    OnlineContactsActivity.this.listView.setAdapter(OnlineContactsActivity.this.listViewAdapter);
                    OnlineContactsActivity.this.listViewAdapter.notifyDataSetChanged();
                    if (VERSION.SDK_INT >= OnlineContactsActivity.select_all_button) {
                        OnlineContactsActivity.this.listView.setFastScrollAlwaysVisible(true);
                    }
                    OnlineContactsActivity.this.listView.setFastScrollEnabled(true);
                    OnlineContactsActivity.this.listView.setVerticalScrollBarEnabled(false);
                    OnlineContactsActivity.this.emptyTextView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
                }
                if (OnlineContactsActivity.this.listViewAdapter != null) {
                    OnlineContactsActivity.this.listViewAdapter.notifyDataSetChanged();
                }
                if (OnlineContactsActivity.this.searchListViewAdapter != null) {
                    OnlineContactsActivity.this.searchListViewAdapter.notifyDataSetChanged();
                }
                OnlineContactsActivity.this.progressDialog.dismiss();
            }
        }, 500);
    }

    private void selectUser(User user) {
        if (this.selectedContacts.containsKey(Integer.valueOf(user.id))) {
            this.selectedContacts.remove(Integer.valueOf(user.id));
        } else {
            this.selectedContacts.put(Integer.valueOf(user.id), null);
        }
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
        if (this.searchListViewAdapter != null) {
            this.searchListViewAdapter.notifyDataSetChanged();
        }
    }

    private void showDeleteContactsConfirmation(ArrayList<Integer> arrayList) {
        Builder builder = new Builder(getParentActivity());
        builder.setMessage(LocaleController.getString("AreYouSureDeleteContacts", R.string.AreYouSureDeleteContacts));
        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new AnonymousClass10(arrayList));
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void showHelpDialog() {
//        if (UserConfig.isClientActivated()) {
//            SettingManager settingManager = new SettingManager();
//            if (!settingManager.m786b("mutualContactHelpDisplayed") && MoboConstants.f1168c) {
//                settingManager.m785a("mutualContactHelpDisplayed", true);
//                Builder builder = new Builder(getParentActivity());
//                builder.setTitle(LocaleController.getString("SeparateMutualContacts", R.string.SeparateMutualContacts)).setMessage(LocaleController.getString("SeparateMutualContactsHelp", R.string.SeparateMutualContactsHelp));
//                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new C15128());
//                builder.create().show();
//            }
//        }
    }

    private void updateVisibleRows(int i) {
        if (this.listView != null) {
            int childCount = this.listView.getChildCount();
            for (int i2 = search_button; i2 < childCount; i2 += add_button) {
                View childAt = this.listView.getChildAt(i2);
                if (childAt instanceof UserCell) {
                    ((UserCell) childAt).update(i);
                }
            }
        }
    }

    public View createView(Context context) {
        int i = add_button;
        this.searching = false;
        this.searchWas = false;
//        if (ThemeUtil.m2005b()) {
//            Drawable drawable = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_back);
//            drawable.setColorFilter(AdvanceTheme.ba, Mode.MULTIPLY);
//            this.actionBar.setBackButtonDrawable(drawable);
//        } else {
            this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        }
        this.actionBar.setAllowOverlayTitle(true);
        if (this.destroyAfterSelect) {
            if (this.returnAsResult) {
                this.actionBar.setTitle(LocaleController.getString("SelectContact", R.string.SelectContact));
            } else if (this.createSecretChat) {
                this.actionBar.setTitle(LocaleController.getString("NewSecretChat", R.string.NewSecretChat));
            } else {
                this.actionBar.setTitle(LocaleController.getString("NewMessageTitle", R.string.NewMessageTitle));
            }
        } else if (this.onlyOnlines) {
            this.actionBar.setTitle(LocaleController.getString("OnlineContacts", R.string.OnlineContacts));
        } else {
            this.actionBar.setTitle(LocaleController.getString("Contacts", R.string.Contacts));
        }
        this.actionBar.setActionBarMenuOnItemClick(new C15041());
        ActionBarMenu createMenu = this.actionBar.createMenu();
        ActionBarMenuItem actionBarMenuItemSearchListener = createMenu.addItem((int) search_button, (int) R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new C15052());
        actionBarMenuItemSearchListener.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
//        createMenu.addItem((int) add_button, (int) R.drawable.addmember);
        initThemeSearchItem(actionBarMenuItemSearchListener);
//        if (this.onlyOnlines) {
//            createMenu.addItem((int) refresh, (int) R.drawable.quantum_ic_refresh_white_24);
//        } else if (!this.multiSelectMode) {
//            createMenu.addItem((int) delete, (int) R.drawable.ic_ab_fwd_delete);
//        }
//        if (this.multiSelectMode) {
//            createMenu.addItemWithWidth((int) select_all_button, (int) R.drawable.ic_all_messages_bluew, AndroidUtilities.dp(56.0f));
//            createMenu.addItemWithWidth((int) done_button, (int) R.drawable.ic_done, AndroidUtilities.dp(56.0f));
//        }
        this.searchListViewAdapter = new OnlineContactsSearchAdapter(context, this.ignoreUsers, this.allowUsernameSearch, false, false, this.allowBots);
        if (this.multiSelectMode) {
            this.searchListViewAdapter.setUseUserCell(true);
            this.searchListViewAdapter.setCheckedMap(this.selectedContacts);
        }
        if (this.onlyOnlines) {
            ContactsController.getInstance().initOnlineUsersSectionsDict();
            this.listViewAdapter = new OnlineContactsAdapter(context, this.onlyUsers, false, this.ignoreUsers, this.chat_id != 0);
        }
        this.fragmentView = new FrameLayout(context);
        LinearLayout linearLayout = new LinearLayout(context);

        linearLayout.setVisibility(LinearLayout.INVISIBLE);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ((FrameLayout) this.fragmentView).addView(linearLayout);
        LayoutParams layoutParams = (LayoutParams) linearLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 48;
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOnTouchListener(new C15063());
        this.emptyTextView = new TextView(context);
        this.emptyTextView.setTextColor(-8355712);
        
        this.emptyTextView.setTextSize(add_button, 20.0f);
        this.emptyTextView.setGravity(17);
        this.emptyTextView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
        linearLayout.addView(this.emptyTextView);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.emptyTextView.getLayoutParams();
        layoutParams2.width = -1;
        layoutParams2.height = -1;
        layoutParams2.weight = 0.5f;
        this.emptyTextView.setLayoutParams(layoutParams2);
        View frameLayout = new FrameLayout(context);
        linearLayout.addView(frameLayout);
        layoutParams2 = (LinearLayout.LayoutParams) frameLayout.getLayoutParams();
        layoutParams2.width = -1;
        layoutParams2.height = -1;
        layoutParams2.weight = 0.5f;
        frameLayout.setLayoutParams(layoutParams2);
        this.listView = new LetterSectionsListView(context);
        initThemeListView();
        this.listView.setEmptyView(linearLayout);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setDivider(null);
        this.listView.setDividerHeight(search_button);
        this.listView.setFastScrollEnabled(true);
        this.listView.setScrollBarStyle(33554432);
        this.listView.setAdapter(this.listViewAdapter);
        this.listView.setFastScrollAlwaysVisible(true);
        LetterSectionsListView letterSectionsListView = this.listView;
        if (!LocaleController.isRTL) {
            i = refresh;
        }
        letterSectionsListView.setVerticalScrollbarPosition(i);
        ((FrameLayout) this.fragmentView).addView(this.listView);
        layoutParams = (LayoutParams) this.listView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        this.listView.setLayoutParams(layoutParams);
        this.listView.setOnItemClickListener(new C15084());
        this.listView.setOnScrollListener(new C15095());
        showHelpDialog();
        return this.fragmentView;
    }

    public void didReceivedNotification(int i, Object... objArr) {
        if (i == NotificationCenter.contactsDidLoaded) {
            if (this.listViewAdapter != null) {
                this.listViewAdapter.notifyDataSetChanged();
                reloadOnlineList();
            }
        } else if (i == NotificationCenter.updateInterfaces) {
            int intValue = ((Integer) objArr[search_button]).intValue();
            if ((intValue & 4) == 0 || this.onlyOnlines) {
            }
            if ((intValue & refresh) != 0 || (intValue & add_button) != 0 || (intValue & 4) != 0) {
                updateVisibleRows(intValue);
            }
        } else if (i == NotificationCenter.encryptedChatCreated) {
            if (this.createSecretChat && this.creatingChat) {
                EncryptedChat encryptedChat = (EncryptedChat) objArr[search_button];
                Bundle bundle = new Bundle();
                bundle.putInt("enc_id", encryptedChat.id);
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, new Object[search_button]);
                presentFragment(new ChatActivity(bundle), true);
            }
        } else if (i == NotificationCenter.closeChats && !this.creatingChat) {
            removeSelfFromStack();
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        this.onlyOnlines = true;
        if (this.arguments != null) {
            this.onlyUsers = getArguments().getBoolean("onlyUsers", false);
            this.onlyOnlines = true;//getArguments().getBoolean("onlyOnlines", false);
            this.destroyAfterSelect = this.arguments.getBoolean("destroyAfterSelect", false);
            this.returnAsResult = this.arguments.getBoolean("returnAsResult", false);
            this.createSecretChat = this.arguments.getBoolean("createSecretChat", false);
            this.selectAlertString = this.arguments.getString("selectAlertString");
            this.allowUsernameSearch = this.arguments.getBoolean("allowUsernameSearch", true);
            this.needForwardCount = this.arguments.getBoolean("needForwardCount", true);
            this.allowBots = this.arguments.getBoolean("allowBots", true);
            this.chat_id = this.arguments.getInt("chat_id", search_button);
            this.multiSelectMode = this.arguments.getBoolean("multiSelectMode", false);
        } else {
            this.needPhonebook = true;
        }
        ContactsController.getInstance().checkInviteText();
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        this.delegate = null;
    }

    public void onPause() {
        super.onPause();
        if (this.actionBar != null) {
            this.actionBar.closeSearchField();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
        initTheme();
    }

    public void setDelegate(ContactsActivityDelegate contactsActivityDelegate) {
        this.delegate = contactsActivityDelegate;
    }

    public void setIgnoreUsers(HashMap<Integer, User> hashMap) {
        this.ignoreUsers = hashMap;
    }

    public void setMultiSelectDelegate(ContactsActivityMultiSelectDelegate contactsActivityMultiSelectDelegate) {
        this.multiSelectDelegate = contactsActivityMultiSelectDelegate;
    }
}

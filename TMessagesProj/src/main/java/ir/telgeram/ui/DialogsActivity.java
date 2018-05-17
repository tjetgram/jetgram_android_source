package ir.telgeram.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

import java.util.ArrayList;

import ir.telgeram.Adel.BaseApplication;
import ir.telgeram.Adel.FavoriteController;
import ir.telgeram.Adel.HiddenController;
import ir.telgeram.Adel.IGetLastZangoolehId;
import ir.telgeram.Adel.OnSwipeTouchListener;
import ir.telgeram.Adel.Setting2;
import ir.telgeram.Adel.TabSetting;
import ir.telgeram.Adel.ThemeChanger;
import ir.telgeram.Adel.WebService;
import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ApplicationLoader;
import ir.telgeram.messenger.BuildVars;
import ir.telgeram.messenger.ChatObject;
import ir.telgeram.messenger.ContactsController;
import ir.telgeram.messenger.DialogObject;
import ir.telgeram.messenger.FileLog;
import ir.telgeram.messenger.ImageLoader;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.MessageObject;
import ir.telgeram.messenger.MessagesController;
import ir.telgeram.messenger.MessagesStorage;
import ir.telgeram.messenger.NotificationCenter;
import ir.telgeram.messenger.NotificationsController;
import ir.telgeram.messenger.R;
import ir.telgeram.messenger.UserConfig;
import ir.telgeram.messenger.UserObject;
import ir.telgeram.messenger.query.SearchQuery;
import ir.telgeram.messenger.query.StickersQuery;
import ir.telgeram.messenger.support.widget.LinearLayoutManager;
import ir.telgeram.messenger.support.widget.RecyclerView;
import ir.telgeram.tgnet.TLRPC;
import ir.telgeram.ui.ActionBar.ActionBar;
import ir.telgeram.ui.ActionBar.ActionBarMenu;
import ir.telgeram.ui.ActionBar.ActionBarMenuItem;
import ir.telgeram.ui.ActionBar.AlertDialog;
import ir.telgeram.ui.ActionBar.BaseFragment;
import ir.telgeram.ui.ActionBar.BottomSheet;
import ir.telgeram.ui.ActionBar.MenuDrawable;
import ir.telgeram.ui.ActionBar.Theme;
import ir.telgeram.ui.ActionBar.ThemeDescription;
import ir.telgeram.ui.Adapters.DialogsAdapter;
import ir.telgeram.ui.Adapters.DialogsSearchAdapter;
import ir.telgeram.ui.Cells.DialogCell;
import ir.telgeram.ui.Cells.DividerCell;
import ir.telgeram.ui.Cells.DrawerActionCell;
import ir.telgeram.ui.Cells.DrawerProfileCell;
import ir.telgeram.ui.Cells.GraySectionCell;
import ir.telgeram.ui.Cells.HashtagSearchCell;
import ir.telgeram.ui.Cells.HintDialogCell;
import ir.telgeram.ui.Cells.LoadingCell;
import ir.telgeram.ui.Cells.ProfileSearchCell;
import ir.telgeram.ui.Cells.UserCell;
import ir.telgeram.ui.Components.AlertsCreator;
import ir.telgeram.ui.Components.CombinedDrawable;
import ir.telgeram.ui.Components.EmptyTextProgressView;
import ir.telgeram.ui.Components.FragmentContextView;
import ir.telgeram.ui.Components.LayoutHelper;
import ir.telgeram.ui.Components.RadialProgressView;
import ir.telgeram.ui.Components.RecyclerListView;

public class DialogsActivity extends BaseFragment
		implements NotificationCenter.NotificationCenterDelegate, IGetLastZangoolehId
{
	public static Context thiscontext;
	public static Object thiscontextbase;
	public static int currenttab = 0;
	public static boolean dialogsLoaded;
	private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();
	public Context context;

	private boolean hiddenMode;
	private boolean enterHiddenPassMode = false;
	private ImageView         floatingButtonLock;
	private ActionBarMenuItem searchFiledItem;
	private ActionBarMenuItem zangoolehMenu; // Adel
	private BadgeView         zangoolehBadge; // Adel
	private Handler           zangoolehHandler; // Adel
	private Runnable          zangoolehRunnable; // Adel

	private TabLayout                                tabHost;
	private RecyclerListView.OnItemClickListener     onItemListenerForDialogs;
	private RecyclerListView.OnItemLongClickListener onItemListenerLongForDialogs;
	private OnSwipeTouchListener                     onSwipeTouchListener;
	private RecyclerListView                         listView;
	private LinearLayoutManager                      layoutManager;
	private DialogsAdapter                           dialogsAdapter;
	private DialogsSearchAdapter                     dialogsSearchAdapter;
	private EmptyTextProgressView                    searchEmptyView;
	private RadialProgressView                       progressView;
	private LinearLayout                             emptyView;
	private ActionBarMenuItem                        passcodeItem;
	private ImageView                                floatingButton;
	private RecyclerView                             sideMenu;
	private FragmentContextView                      fragmentContextView;
	private TextView                                 emptyTextView1;
	private TextView                                 emptyTextView2;
	private AlertDialog                              permissionDialog;
	private int                                      prevPosition;
	private int                                      prevTop;
	private boolean                                  scrollUpdated;
	private boolean                                  floatingHidden;
	private boolean checkPermission = true;
	private String                  selectAlertString;
	private String                  selectAlertStringGroup;
	private String                  addToGroupAlertString;
	private int                     dialogsType;
	private boolean                 searching;
	private boolean                 searchWas;
	private boolean                 onlySelect;
	private long                    selectedDialog;
	private String                  searchString;
	private long                    openedDialogId;
	private boolean                 cantSendToChannels;
	private DialogsActivityDelegate delegate;

	public DialogsActivity(Bundle args)
	{
		super(args);
	}

	// Adel
	public static void RebuildTabs()
	{
		currenttab = 0;
		if (thiscontextbase != null)
		{
			currenttab = 0;
			((DialogsActivity) thiscontextbase).buildTAbs();
		}
	}

	// Adel
	private void buildTAbs()
	{
		currenttab = 0;
		TabSetting.GetTabs(tabHost, context);
	}

	// Adel
	public void RebuildAll()
	{
		try
		{
			parentLayout.rebuildAllFragmentViews(false);
		} catch (Exception e)
		{

		}
	}

	// Adel
	private void ToggleHidden(long selectedDialog)
	{
		if (!HiddenController.IsHidden(selectedDialog))
		{
			HiddenController.addToHidden(selectedDialog);
		}
		else
		{
			HiddenController.RemoveFromHidden(selectedDialog);
		}
		dialogsAdapter.Hiddenmode = hiddenMode;
		dialogsAdapter.notifyDataSetChanged();
		//listView.getAdapter().notifyDataSetChanged();
		if (!Setting2.HiddenMsgDisplayed())
		{
			Setting2.HiddenMsgDisplayedYes();
			final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
			builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
			builder.setMessage(LocaleController.getString("HiddenMsg1", R.string.HiddenMsg1));
			builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialogInterface, int i)
				{
					dialogInterface.dismiss();
				}
			});
			builder.create().show();
		}
	}

	// Adel
	private void gotoHiddenMode()
	{
		if (Setting2.HideHavePass())
		{
			searchFiledItem.openSearch(true);
			searchFiledItem.getSearchField().setHint(LocaleController.getString("PasscodePassword", R.string.PasscodePassword));
			if (Setting2.getHidePasswordType() == 0)
			{
				searchFiledItem.getSearchField().setInputType(3);
			}
			else
			{
				searchFiledItem.getSearchField().setInputType(129);
			}
			searchFiledItem.getSearchField().setTransformationMethod(PasswordTransformationMethod.getInstance());
			enterHiddenPassMode = true;
		}
		else
		{
			android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
			builder.setMessage(LocaleController.getString("EnterPasswordForHideChats", R.string.EnterPasswordForHideChats));
			builder.setTitle(LocaleController.getString("CreatePassword", R.string.CreatePassword));

			// Set up the input
			final EditText input = new EditText(context);
			// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
			input.setInputType(3);
			builder.setView(input);

			// Set up the buttons
			builder.setPositiveButton(LocaleController.getString("SavePassword", R.string.SavePassword), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					String m_Text = input.getText().toString();
					if (Setting2.setHidePassword(m_Text))
					{
						Snackbar.make(listView, LocaleController.getString("PasswordSaved", R.string.PasswordSaved), Snackbar.LENGTH_SHORT).show();

						dialog.cancel();
						final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getParentActivity());
						builder.setTitle(LocaleController.getString("ChangePassword", R.string.ChangePassword));
						builder.setMessage(LocaleController.getString("ChangePasswordMsg", R.string.ChangePasswordMsg));
						builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialogInterface, int i)
							{
								dialogInterface.dismiss();
							}
						});
						builder.create().show();
						hiddenMode = true;
						floatingButtonLock.setVisibility(View.VISIBLE);
						dialogsAdapter.Hiddenmode = true;
						dialogsAdapter.notifyDataSetChanged();
					}
					else
					{
						input.setText(null);
						Snackbar.make(listView, LocaleController.getString("PasswordError", R.string.PasswordError), Snackbar.LENGTH_SHORT).show();
						gotoHiddenMode();
					}
				}
			});
			builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.cancel();
				}
			});

			android.app.AlertDialog Dialogx = builder.create();
			Dialogx.show();
			Dialogx.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			Dialogx.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		}
	}

	// Adel
	private void ChangePassword()
	{
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
		builder.setMessage(LocaleController.getString("EnterNewPassword", R.string.EnterNewPassword));
		builder.setTitle(LocaleController.getString("ChangePassword", R.string.ChangePassword));

		// Set up the input
		final EditText input = new EditText(context);
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(3);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton(LocaleController.getString("SavePassword", R.string.SavePassword), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String m_Text = input.getText().toString();
				if (Setting2.setHidePassword(m_Text))
				{
					Snackbar.make(listView, LocaleController.getString("PasswordSaved", R.string.PasswordSaved), Snackbar.LENGTH_SHORT).show();
					dialog.cancel();
				}
				else
				{
					input.setText(null);
					Snackbar.make(listView, LocaleController.getString("PasswordError", R.string.PasswordError), Snackbar.LENGTH_SHORT).show();
					ChangePassword();
				}

			}
		});
		builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.cancel();
			}
		});
		android.app.AlertDialog Dialogx = builder.create();
		Dialogx.show();
		Dialogx.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		Dialogx.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

	}

	// Adel
	private void onSwipeRight()
	{
		int current = tabHost.getSelectedTabPosition();
		if (current == 0)
		{
			current = tabHost.getTabCount() - 1;
		}
		else
		{
			current--;
		}
		tabHost.getTabAt(current).select();
	}

	// Adel
	private void onSwipeLeft()
	{
		int current = tabHost.getSelectedTabPosition();
		if (current == tabHost.getTabCount() - 1)
		{
			current = 0;
		}
		else
		{
			current++;
		}
		tabHost.getTabAt(current).select();
	}

	@Override
	public boolean onFragmentCreate()
	{
		super.onFragmentCreate();

		if (getArguments() != null)
		{
			onlySelect = arguments.getBoolean("onlySelect", false);
			cantSendToChannels = arguments.getBoolean("cantSendToChannels", false);
			dialogsType = arguments.getInt("dialogsType", 0);
			selectAlertString = arguments.getString("selectAlertString");
			selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
			addToGroupAlertString = arguments.getString("addToGroupAlertString");
			hiddenMode = arguments.getBoolean("hiddenMode", false);
		}

		if (searchString == null)
		{
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
			NotificationCenter.getInstance().addObserver(this, NotificationCenter.reloadHints);
		}


		if (!dialogsLoaded)
		{
			MessagesController.getInstance().loadDialogs(0, 100, true);
			ContactsController.getInstance().checkInviteText();
			MessagesController.getInstance().loadPinnedDialogs(0, null);
			StickersQuery.checkFeaturedStickers();
			dialogsLoaded = true;
		}
		return true;
	}

	@Override
	public void onFragmentDestroy()
	{
		// Adel
		try
		{
			zangoolehHandler.removeCallbacks(zangoolehRunnable);
		}
		catch (Exception ignored)
		{

		}

		super.onFragmentDestroy();
		if (searchString == null)
		{
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
			NotificationCenter.getInstance().removeObserver(this, NotificationCenter.reloadHints);
		}
		delegate = null;
	}

	@Override
	public View createView(final Context context)
	{
		// Adel
		thiscontext=context;
		thiscontextbase = this;
		this.context = context;

		searching = false;
		searchWas = false;

		AndroidUtilities.runOnUIThread(new Runnable()
		{
			@Override
			public void run()
			{
				Theme.createChatResources(context, false);
			}
		});

		ActionBarMenu menu = actionBar.createMenu();
		if (!onlySelect && searchString == null)
		{
			passcodeItem = menu.addItem(1, R.drawable.lock_close);
			updatePasscodeButton();
		}

		// Adel ------------- Search Start ----------------
		searchFiledItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() // Adel
		{
			@Override
			public void onSearchExpand()
			{
				searching = true;
				if (listView != null)
				{
					if (searchString != null)
					{
						listView.setEmptyView(searchEmptyView);
						progressView.setVisibility(View.GONE);
						emptyView.setVisibility(View.GONE);
					}
					if (!onlySelect)
					{
						floatingButton.setVisibility(View.GONE);
					}
				}
				updatePasscodeButton();
			}

			@Override
			public boolean canCollapseSearch()
			{
				if (searchString != null)
				{
					finishFragment();
					return false;
				}
				return true;
			}

			@Override
			public void onSearchCollapse()
			{
				// Adel Start
				if (Setting2.HideHavePass() && searchFiledItem != null && enterHiddenPassMode)
				{
					searchFiledItem.getSearchField().setInputType(524289);
					searchFiledItem.getSearchField().setTransformationMethod(null);
					searchFiledItem.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
				}
				enterHiddenPassMode = false;
				// Adel End

				searching = false;
				searchWas = false;
				if (listView != null)
				{
					searchEmptyView.setVisibility(View.GONE);
					if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) // Adel
					{
						emptyView.setVisibility(View.GONE);
						listView.setEmptyView(progressView);
					}
					else
					{
						progressView.setVisibility(View.GONE);
						listView.setEmptyView(emptyView);
					}
					if (!onlySelect)
					{
						floatingButton.setVisibility(View.VISIBLE);
						floatingHidden = true;
						floatingButton.setTranslationY(AndroidUtilities.dp(100));
						hideFloatingButton(false);
					}
					if (listView.getAdapter() != dialogsAdapter)
					{
						listView.setAdapter(dialogsAdapter);
						dialogsAdapter.notifyDataSetChanged();
					}
				}
				if (dialogsSearchAdapter != null)
				{
					dialogsSearchAdapter.searchDialogs(null);
				}
				updatePasscodeButton();
			}

			@Override
			public void onTextChanged(EditText editText)
			{
				String text = editText.getText().toString();
				if (!enterHiddenPassMode) // Adel
				{
					if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch())
					{
						searchWas = true;
						if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter)
						{
							listView.setAdapter(dialogsSearchAdapter);
							dialogsSearchAdapter.notifyDataSetChanged();
						}
						if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView)
						{
							emptyView.setVisibility(View.GONE);
							progressView.setVisibility(View.GONE);
							searchEmptyView.showTextView();
							listView.setEmptyView(searchEmptyView);
						}
					}
					if (dialogsSearchAdapter != null)
					{
						dialogsSearchAdapter.searchDialogs(text);
					}
				}
				else if (Setting2.CheckHidePassword(text)) // Adel
				{
					editText.setText(null);
					if (actionBar != null && actionBar.isSearchFieldVisible())
					{
						actionBar.closeSearchField();
					}

					Snackbar.make(listView, LocaleController.getString("HiddenChats", R.string.HiddenChats), Snackbar.LENGTH_SHORT).show();

					floatingButtonLock.setVisibility(View.VISIBLE);
					hiddenMode = true;
					dialogsAdapter.Hiddenmode = true;
					dialogsAdapter.notifyDataSetChanged();
				}
			}
		});
		searchFiledItem.getSearchField().setHint(LocaleController.getString("Search", R.string.Search)); // Adel
		// Adel ------------- Search End ----------------
		// Adel ------------- Zangooleh Start ----------------
		/*zangoolehMenu = menu.addItem(5, R.drawable.ic_notifications_active_white_24dp);
		zangoolehMenu.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				zangoolehBadge.hide();
				onGetLastZangoolehIdCompleted(BaseApplication.EXCEPTION);

				Intent intent = new Intent(context, ZangoolehActivity.class);
				context.startActivity(intent);
			}
		});*/
		//sina user info
		final ActionBarMenuItem myacount = menu.addItem(4, R.drawable.ic_remove_red_eye_black_24dp);
		myacount.setOnClickListener(new View.OnClickListener() {
																	@Override
																	public void onClick(View v) {
																		Bundle args = new Bundle();
																		args.putInt("user_id", UserConfig.getClientUserId());
																		presentFragment(new ChatActivity(args));
																	}
																});

			// Adel ------------- Zangooleh End ----------------
	/*	// Adel ------------- Ghost Start ----------------
		GhostPorotocol.update();
		final ActionBarMenuItem ghostmenu = menu.addItem(4, (Setting2.getGhostMode() ? R.drawable.ic_ghost_selected : R.drawable.ic_ghost));
		ghostmenu.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (Setting2.GetGhostFirstTime())
				{
					new AlertDialog.Builder(context)
							.setTitle(LocaleController.getString("GhostFirstTimeTitle", R.string.GhostFirstTimeTitle))
							.setMessage(LocaleController.getString("GhostFirstTimeMessage", R.string.GhostFirstTimeMessage))
							.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialogInterface, int i)
								{
									dialogInterface.dismiss();
								}
							})
							.show();

					Setting2.SetGhostFirstTime();
				}

				if (Setting2.getGhostMode())
				{
					ghostmenu.setIcon(R.drawable.ic_ghost);
					Snackbar snack = Snackbar.make(listView, LocaleController.getString("GhostModeIsNotActive", R.string.GhostModeIsNotActive), Snackbar.LENGTH_SHORT);
					View     viewz = snack.getView();
					TextView tv    = (TextView) viewz.findViewById(android.support.design.R.id.snackbar_text);
					tv.setTextColor(Color.WHITE);
					snack.show();
					GhostPorotocol.trun(false);
					actionBar.changeGhostModeVisibility();
				}
				else
				{
					ghostmenu.setIcon(R.drawable.ic_ghost_selected);
					Snackbar snack = Snackbar.make(listView, LocaleController.getString("GhostModeIsActive", R.string.GhostModeIsActive), Snackbar.LENGTH_SHORT);
					View     viewz = snack.getView();
					TextView tv    = (TextView) viewz.findViewById(android.support.design.R.id.snackbar_text);
					tv.setTextColor(Color.WHITE);
					snack.show();
					GhostPorotocol.trun(true);
					actionBar.changeGhostModeVisibility();
				}
			}
		});
		// Adel ------------- Ghost End ---------------- */


			hiddenMode = ApplicationLoader.GetHiddenMode(context); // Adel

		if (onlySelect)
		{
			actionBar.setBackButtonImage(R.drawable.ic_ab_back);
			actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
		}
		else
		{
			if (searchString != null)
			{
				actionBar.setBackButtonImage(R.drawable.ic_ab_back);
			}
			else
			{
				actionBar.setBackButtonDrawable(new MenuDrawable());
			}
			if (BuildVars.DEBUG_VERSION)
			{
				actionBar.setTitle(LocaleController.getString("AppNameBeta", R.string.AppNameBeta));
			}
			else
			{
				actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
			}
		}
		actionBar.setAllowOverlayTitle(true);

		actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
		{
			@Override
			public void onItemClick(int id)
			{
				if (id == -1)
				{
					if (onlySelect)
					{
						finishFragment();
					}
					else if (parentLayout != null)
					{
						parentLayout.getDrawerLayoutContainer().openDrawer(false);
					}
				}
				else if (id == 1)
				{
					UserConfig.appLocked = !UserConfig.appLocked;
					UserConfig.saveConfig(false);
					updatePasscodeButton();
				}
			}
		});

		if (sideMenu != null)
		{
			sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
			sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
			sideMenu.getAdapter().notifyDataSetChanged();
		}

		FrameLayout frameLayout = new FrameLayout(context);
		fragmentView = frameLayout;

		listView = new RecyclerListView(context);
		listView.setVerticalScrollBarEnabled(true);
		listView.setItemAnimator(null);
		listView.setInstantClick(true);
		listView.setLayoutAnimation(null);
		listView.setTag(4);
		listView.setHasFixedSize(true); // Adel
		listView.setBackgroundColor(0xffffffff); // Adel
		layoutManager = new LinearLayoutManager(context)
		{
			@Override
			public boolean supportsPredictiveItemAnimations()
			{
				return false;
			}
		};
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		listView.setLayoutManager(layoutManager);
		listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);

		// Adel ------------------ Tabs Start ---------------------
		Boolean isTabsUpside = Setting2.getTabIsUp();
		if (isTabsUpside)
		{
			frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 0, 34, 0, 0)); // Adel changed 48 to 34
		}
		else
		{
			frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 0, 0, 0, 48));
		}

		// Tabs
		tabHost = new TabLayout(frameLayout.getContext());
		tabHost.setBackgroundColor(Setting2.getTabcolor());
		ThemeChanger.settabhost(tabHost);
		tabHost.setSelectedTabIndicatorColor(0xffffffff);
		tabHost.setTabMode(TabLayout.MODE_FIXED);
		tabHost.setTabGravity(TabLayout.GRAVITY_FILL);
		tabHost.setVisibility(Setting2.getProTelegram() ? View.VISIBLE : View.GONE);
		tabHost.setSelectedTabIndicatorHeight(AndroidUtilities.dp(3));
		tabHost.setTabTextColors(Color.argb(100, 255, 255, 255), Color.WHITE);
		tabHost.setClipToPadding(true);
		tabHost.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
		{
			@Override
			public void onTabSelected(TabLayout.Tab tab)
			{
//				tabHost.setSelectedTabIndicatorHeight(0);
//				AndroidUtilities.runOnUIThread(new Runnable()
//				{
//					@Override
//					public void run()
//					{
//						tabHost.setSelectedTabIndicatorHeight(AndroidUtilities.dp(3));
//					}
//				}, 100);

				final int tabid = tab.getPosition();
//				TabSetting.SetTabIcon(tab, TabSetting.getSelectedICon(tabid));
				currenttab = tabid;
//				Drawable d = new ColorDrawable(0xffffffff);
				floatingButton.setVisibility(View.VISIBLE);
//				listView.setBackgroundDrawable(d);
//				listView.setBackgroundColor(0xffffffff);
				listView.setOnItemLongClickListener(onItemListenerLongForDialogs);
				listView.setOnItemClickListener(onItemListenerForDialogs);
				dialogsAdapter.categoryId = TabSetting.getTabModels().get(tabid).getId();
				dialogsAdapter.Hiddenmode = hiddenMode;
				dialogsAdapter.notifyDataSetChanged();
				actionBar.setTitle(LocaleController.getString("TabTitle", TabSetting.getTabModels().get(tabid).getTitle()));

				AndroidUtilities.runOnUIThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (!MessagesController.getInstance().loadingDialogs && dialogsAdapter.getDialogsArray().isEmpty())
						{
							emptyView.setVisibility(View.VISIBLE);
						}
						else
						{
							emptyView.setVisibility(View.GONE);
						}
					}
				}, 1000);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab)
			{
//				TabSetting.SetTabIcon(tab, TabSetting.getNormalIcon(tab.getPosition()));
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab)
			{

			}
		});

		// ---
		if (isTabsUpside)
		{
			frameLayout.addView(tabHost, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 34, Gravity.TOP)); // Adel Changed Wrap_Content to 34
		}
		else
		{
			frameLayout.addView(tabHost, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));
		}

		// ---
		//		AndroidUtilities.runOnUIThread(new Runnable()
		//		{
		//			@Override
		//			public void run()
		//			{
		//				try
		//				{
		//					dialogsAdapter.notifyDataSetChanged();
		//				} catch (Exception e)
		//				{
		//					e.printStackTrace();
		//				}
		//			}
		//		}, 10);
		// Adel ------------------ Tabs End -----------------------

		onItemListenerForDialogs = new RecyclerListView.OnItemClickListener()
		{
			@Override
			public void onItemClick(View view, int position)
			{
				if (listView == null || listView.getAdapter() == null)
				{
					return;
				}
				long                 dialog_id  = 0;
				int                  message_id = 0;
				RecyclerView.Adapter adapter    = listView.getAdapter();
				if (adapter == dialogsAdapter)
				{
					TLRPC.TL_dialog dialog = dialogsAdapter.getItem(position);
					if (dialog == null)
					{
						return;
					}
					dialog_id = dialog.id;
					MessagesController.getInstance().dialogsUnreadOnly.remove(dialog); // Adel
				}
				else if (adapter == dialogsSearchAdapter)
				{
					Object obj = dialogsSearchAdapter.getItem(position);
					if (obj instanceof TLRPC.User)
					{
						dialog_id = ((TLRPC.User) obj).id;
						if (dialogsSearchAdapter.isGlobalSearch(position))
						{
							ArrayList<TLRPC.User> users = new ArrayList<>();
							users.add((TLRPC.User) obj);
							MessagesController.getInstance().putUsers(users, false);
							MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
						}
						if (!onlySelect)
						{
							dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.User) obj);
						}
					}
					else if (obj instanceof TLRPC.Chat)
					{
						if (dialogsSearchAdapter.isGlobalSearch(position))
						{
							ArrayList<TLRPC.Chat> chats = new ArrayList<>();
							chats.add((TLRPC.Chat) obj);
							MessagesController.getInstance().putChats(chats, false);
							MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
						}
						if (((TLRPC.Chat) obj).id > 0)
						{
							dialog_id = -((TLRPC.Chat) obj).id;
						}
						else
						{
							dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
						}
						if (!onlySelect)
						{
							dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.Chat) obj);
						}
					}
					else if (obj instanceof TLRPC.EncryptedChat)
					{
						dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
						if (!onlySelect)
						{
							dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.EncryptedChat) obj);
						}
					}
					else if (obj instanceof MessageObject)
					{
						MessageObject messageObject = (MessageObject) obj;
						dialog_id = messageObject.getDialogId();
						message_id = messageObject.getId();
						dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
					}
					else if (obj instanceof String)
					{
						actionBar.openSearchField((String) obj);
					}
				}

				if (dialog_id == 0)
				{
					return;
				}

				if (onlySelect)
				{
					didSelectResult(dialog_id, true, false);
				}
				else
				{
					Bundle args       = new Bundle();
					int    lower_part = (int) dialog_id;
					int    high_id    = (int) (dialog_id >> 32);
					if (lower_part != 0)
					{
						if (high_id == 1)
						{
							args.putInt("chat_id", lower_part);
						}
						else
						{
							if (lower_part > 0)
							{
								args.putInt("user_id", lower_part);
							}
							else if (lower_part < 0)
							{
								if (message_id != 0)
								{
									TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
									if (chat != null && chat.migrated_to != null)
									{
										args.putInt("migrated_to", lower_part);
										lower_part = -chat.migrated_to.channel_id;
									}
								}
								args.putInt("chat_id", -lower_part);
							}
						}
					}
					else
					{
						args.putInt("enc_id", high_id);
					}
					if (message_id != 0)
					{
						args.putInt("message_id", message_id);
					}
					else
					{
						if (actionBar != null)
						{
							actionBar.closeSearchField();
						}
					}
					if (AndroidUtilities.isTablet())
					{
						if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter)
						{
							return;
						}
						if (dialogsAdapter != null)
						{
							dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
							updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
						}
					}
					if (searchString != null)
					{
						if (MessagesController.checkCanOpenChat(args, DialogsActivity.this))
						{
							NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
							presentFragment(new ChatActivity(args));
						}
					}
					else
					{
						if (MessagesController.checkCanOpenChat(args, DialogsActivity.this))
						{
							presentFragment(new ChatActivity(args));
						}
					}
				}
			}
		};

		onItemListenerLongForDialogs = new RecyclerListView.OnItemLongClickListener()
		{
			@Override
			public boolean onItemClick(View view, int position)
			{
				if (onlySelect || searching && searchWas || getParentActivity() == null)
				{
					if (searchWas && searching || dialogsSearchAdapter.isRecentSearchDisplayed())
					{
						RecyclerView.Adapter adapter = listView.getAdapter();
						if (adapter == dialogsSearchAdapter)
						{
							Object item = dialogsSearchAdapter.getItem(position);
							if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed())
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
								builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
								builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
								builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										if (dialogsSearchAdapter.isRecentSearchDisplayed())
										{
											dialogsSearchAdapter.clearRecentSearch();
										}
										else
										{
											dialogsSearchAdapter.clearRecentHashtags();
										}
									}
								});
								builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
								showDialog(builder.create());
								return true;
							}
						}
					}
					return false;
				}
				TLRPC.TL_dialog            dialog;
				ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
				if (position < 0 || position >= dialogs.size())
				{
					return false;
				}
				dialog = dialogs.get(position);
				selectedDialog = dialog.id;
				final boolean pinned = dialog.pinned;

				BottomSheet.Builder builder  = new BottomSheet.Builder(getParentActivity());
				int                 lower_id = (int) selectedDialog;
				int                 high_id  = (int) (selectedDialog >> 32);

				if (DialogObject.isChannel(dialog))
				{
					final TLRPC.Chat chat     = MessagesController.getInstance().getChat(-lower_id);
					CharSequence     items[];
					final boolean    isFavor  = FavoriteController.isFavor(selectedDialog); // Adel
					final boolean    isHidden = HiddenController.IsHidden(selectedDialog);  // Adel
					boolean ismuted = MessagesController.getInstance().isDialogMuted(selectedDialog); //sina
					int icons[] = new int[]{
							R.drawable.ic_star_gray_24dp, // Adel
							R.drawable.ic_settings_white_24dp, // Adel
							dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
							R.drawable.bot_music,
							R.drawable.chats_clear,
							R.drawable.chats_leave
					};
					if (chat != null && chat.megagroup)
					{
						items = new CharSequence[]{
								isFavor ? LocaleController.getString("RemoveFromFavorites", R.string.RemoveFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites), // Adel
								isHidden ? LocaleController.getString("RemoveFromHiddens", R.string.RemoveFromHiddens) : LocaleController.getString("AddToHiddens", R.string.AddToHiddens), // Adel
								dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
								ismuted ? LocaleController.getString("MuteToggle", R.string.MuteNotifications) : LocaleController.getString("AddToFavorites", R.string.MuteNotifications), // sina
								LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
								chat == null || !chat.creator ? LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu) : LocaleController.getString("DeleteMegaMenu", R.string.DeleteMegaMenu)
						};
					}
					else
					{
						items = new CharSequence[]{
								isFavor ? LocaleController.getString("RemoveFromFavorites", R.string.RemoveFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites), // Adel
								isHidden ? LocaleController.getString("RemoveFromHiddens", R.string.RemoveFromHiddens) : LocaleController.getString("AddToHiddens", R.string.AddToHiddens), // Adel
								dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
							ismuted ? LocaleController.getString("MuteToggle", R.string.MuteNotifications) : LocaleController.getString("AddToFavorites", R.string.MuteNotifications), // sina
							LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
								chat == null || !chat.creator ? LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu) : LocaleController.getString("ChannelDeleteMenu", R.string.ChannelDeleteMenu)};
					}
					builder.setItems(items, icons, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, final int which)
						{
							if (which == 0)
							{
								if (!FavoriteController.isFavor(selectedDialog))
								{
									FavoriteController.addToFavor(selectedDialog);
								}
								else
								{
									FavoriteController.RemoveFromFavor(selectedDialog);
								}
								MessagesController.getInstance().sortDialogs(null);
							}
							else if (which == 1)
							{
								ToggleHidden(selectedDialog);
							}
							else if (which == 2)
							{
								if (MessagesController.getInstance().pinDialog(selectedDialog, !pinned, null, 0) && !pinned)
								{
									listView.smoothScrollToPosition(0);
								}
							}
							else if(which == 3)
							{
								toggleMute(false,selectedDialog);
							}
							else
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
								builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
								if (which == 4)
								{
									if (chat != null && chat.megagroup)
									{
										builder.setMessage(LocaleController.getString("AreYouSureClearHistorySuper", R.string.AreYouSureClearHistorySuper));
									}
									else
									{
										builder.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", R.string.AreYouSureClearHistoryChannel));
									}
									builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialogInterface, int i)
										{
											MessagesController.getInstance().deleteDialog(selectedDialog, 2);
										}
									});
								}
								else
								{
									if (chat != null && chat.megagroup)
									{
										if (!chat.creator)
										{
											builder.setMessage(LocaleController.getString("MegaLeaveAlert", R.string.MegaLeaveAlert));
										}
										else
										{
											builder.setMessage(LocaleController.getString("MegaDeleteAlert", R.string.MegaDeleteAlert));
										}
									}
									else
									{
										if (chat == null || !chat.creator)
										{

											builder.setMessage(LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert));
										}
										else
										{
											builder.setMessage(LocaleController.getString("ChannelDeleteAlert", R.string.ChannelDeleteAlert));
										}
									}
									builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialogInterface, int i)
										{
											MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, UserConfig.getCurrentUser(), null);
											if (AndroidUtilities.isTablet())
											{
												NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
											}
										}
									});
								}
								builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
								showDialog(builder.create());
							}
							listView.getAdapter().notifyDataSetChanged(); // Adel
						}
					});
					showDialog(builder.create());
				}
				else
				{
					final boolean isChat = lower_id < 0 && high_id != 1;
					TLRPC.User    user   = null;
					if (!isChat && lower_id > 0 && high_id != 1)
					{
						user = MessagesController.getInstance().getUser(lower_id);
					}
					final boolean isBot    = user != null && user.bot;
					final boolean isFavor  = FavoriteController.isFavor(selectedDialog); // Adel
					final boolean isHidden = HiddenController.IsHidden(selectedDialog);  // Adel
					boolean ismuted = MessagesController.getInstance().isDialogMuted(selectedDialog); //sina

					builder.setItems(new CharSequence[]{
							isFavor ? LocaleController.getString("RemoveFromFavorites", R.string.RemoveFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites), // Adel
							isHidden ? LocaleController.getString("RemoveFromHiddens", R.string.RemoveFromHiddens) : LocaleController.getString("AddToHiddens", R.string.AddToHiddens), // Adel
							dialog.pinned || MessagesController.getInstance().canPinDialog(lower_id == 0) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
							ismuted ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications), // sina
							LocaleController.getString("ClearHistory", R.string.ClearHistory),
						isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) : isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete)

					}, new int[]{
							R.drawable.ic_star_gray_24dp, // Adel
							R.drawable.ic_settings_white_24dp, // Adel
							dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
							R.drawable.bot_music,
							R.drawable.chats_clear,
							isChat ? R.drawable.chats_leave : R.drawable.chats_delete
					}, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, final int which)
						{
							if (which == 0)
							{
								if (!FavoriteController.isFavor(selectedDialog))
								{
									FavoriteController.addToFavor(selectedDialog);
								}
								else
								{
									FavoriteController.RemoveFromFavor(selectedDialog);
								}
								MessagesController.getInstance().sortDialogs(null);
							}
							else if (which == 1)
							{
								ToggleHidden(selectedDialog);
								//toggleMute(false,selectedDialog);
							}
							else if (which == 2)
							{
								if (MessagesController.getInstance().pinDialog(selectedDialog, !pinned, null, 0) && !pinned)
								{
									listView.smoothScrollToPosition(0);
								}
							}
							else if(which == 3)
							{
								toggleMute(false,selectedDialog);
							}
							else
							{
								AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
								builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
								if (which == 4)
								{
									builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
								}
								else
								{
									if (isChat)
									{
										builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
									}
									else
									{
										builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
									}
								}
								builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										if (which != 4)
										{
											if (isChat)
											{
												TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
												if (currentChat != null && ChatObject.isNotInChat(currentChat))
												{
													MessagesController.getInstance().deleteDialog(selectedDialog, 0);
												}
												else
												{
													MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), null);
												}
											}
											else
											{
												MessagesController.getInstance().deleteDialog(selectedDialog, 0);
											}
											if (isBot)
											{
												MessagesController.getInstance().blockUser((int) selectedDialog);
											}
											if (AndroidUtilities.isTablet())
											{
												NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
											}
										}
										else
										{
											MessagesController.getInstance().deleteDialog(selectedDialog, 1);
										}
									}
								});
								builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
								showDialog(builder.create());
							}
							listView.getAdapter().notifyDataSetChanged();
						}
					});
					showDialog(builder.create());
				}
				return true;
			}
		};

		onSwipeTouchListener = new OnSwipeTouchListener(context)
		{
			public void onSwipeRight()
			{
				DialogsActivity.this.onSwipeRight();
			}

			public void onSwipeLeft()
			{
				DialogsActivity.this.onSwipeLeft();
			}
		};

		listView.setOnItemClickListener(onItemListenerForDialogs);
		listView.setOnItemLongClickListener(onItemListenerLongForDialogs);
		listView.setOnTouchListener(onSwipeTouchListener);

		searchEmptyView = new EmptyTextProgressView(context);
		searchEmptyView.setVisibility(View.GONE);
		searchEmptyView.setShowAtCenter(true);
		searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
		frameLayout.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

		emptyView = new LinearLayout(context);
		emptyView.setOrientation(LinearLayout.VERTICAL);
		emptyView.setVisibility(View.GONE);
		emptyView.setGravity(Gravity.CENTER);
		emptyView.setOnTouchListener(onSwipeTouchListener); // Adel
		frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.NO_GRAVITY, 0, 34, 0, 0));

		emptyTextView1 = new TextView(context);
		emptyTextView1.setText(LocaleController.getString("NoChats", R.string.NoChats));
		emptyTextView1.setTextColor(Theme.getColor(Theme.key_emptyListPlaceholder));
		emptyTextView1.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // Adel
		emptyTextView1.setGravity(Gravity.CENTER);
		emptyTextView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		emptyView.addView(emptyTextView1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

		emptyTextView2 = new TextView(context);
		String help = LocaleController.getString("NoChatsHelp", R.string.NoChatsHelp);
		if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet())
		{
			help = help.replace('\n', ' ');
		}
		emptyTextView2.setText(help);
		emptyTextView2.setTextColor(Theme.getColor(Theme.key_emptyListPlaceholder));
		emptyTextView2.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // Adel
		emptyTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		emptyTextView2.setGravity(Gravity.CENTER);
		emptyTextView2.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(6), AndroidUtilities.dp(8), 0);
		emptyTextView2.setLineSpacing(AndroidUtilities.dp(2), 1);
		emptyView.addView(emptyTextView2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

		progressView = new RadialProgressView(context);
		progressView.setVisibility(View.GONE);
		frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

		floatingButton = new ImageView(context);
		ThemeChanger.setFloatingbutton(floatingButton); // Adel
		floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
		floatingButton.setScaleType(ImageView.ScaleType.CENTER);
		floatingButton.setBackgroundDrawable(ThemeChanger.getFloating(Setting2.getActionbarcolor())); // Adel
		floatingButton.setImageResource(R.drawable.floating_pencil); // Adel

		// Adel
		floatingButtonLock = new ImageView(context);
		floatingButtonLock.setVisibility(!hiddenMode ? View.GONE : View.VISIBLE);
		floatingButtonLock.setScaleType(ImageView.ScaleType.CENTER);
		floatingButtonLock.setBackgroundResource(R.drawable.floating_pink);
		floatingButtonLock.setImageResource(R.drawable.lock_close);

		Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
		if (Build.VERSION.SDK_INT < 21)
		{
			Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
			shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
			CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
			combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
			drawable = combinedDrawable;
		}
		//		floatingButton.setBackgroundDrawable(drawable); // Adel Commented
		//		floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY)); // Adel Commented
		//		floatingButton.setImageResource(R.drawable.floating_pencil); // Adel Commented
		if (Build.VERSION.SDK_INT >= 21)
		{
			StateListAnimator animator = new StateListAnimator();
			animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
			animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
			floatingButton.setStateListAnimator(animator);
			floatingButton.setOutlineProvider(new ViewOutlineProvider()
			{
				@SuppressLint("NewApi")
				@Override
				public void getOutline(View view, Outline outline)
				{
					outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
				}
			});
		}
		frameLayout.addView(floatingButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
		frameLayout.addView(floatingButtonLock, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, (isTabsUpside ? 74 : 110))); // Adel

		floatingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Bundle args = new Bundle();
				args.putBoolean("destroyAfterSelect", true);
				presentFragment(new ContactsActivity(args));
			}
		});
		// Adel
		floatingButtonLock.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				hiddenMode = false;
				dialogsAdapter.Hiddenmode = hiddenMode;
				dialogsAdapter.notifyDataSetChanged();
				floatingButtonLock.setVisibility(View.GONE);
				Snackbar.make(listView, LocaleController.getString("NormalChats", R.string.NormalChats), Snackbar.LENGTH_LONG).show();
			}
		});

		listView.setOnScrollListener(new RecyclerView.OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState)
			{
				if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas)
				{
					AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy)
			{
				int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
				int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
				int totalItemCount   = recyclerView.getAdapter().getItemCount();

				if (searching && searchWas)
				{
					if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached())
					{
						dialogsSearchAdapter.loadMoreSearchMessages();
					}
					return;
				}
				if (visibleItemCount > 0)
				{
					if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray().size() - 10)
					{
						boolean fromCache = !MessagesController.getInstance().dialogsEndReached;
						if (fromCache || !MessagesController.getInstance().serverDialogsEndReached)
						{
							MessagesController.getInstance().loadDialogs(-1, 100, fromCache);
						}
					}
				}

				if (floatingButton.getVisibility() != View.GONE)
				{
					final View topChild     = recyclerView.getChildAt(0);
					int        firstViewTop = 0;
					if (topChild != null)
					{
						firstViewTop = topChild.getTop();
					}
					boolean goingDown;
					boolean changed = true;
					if (prevPosition == firstVisibleItem)
					{
						final int topDelta = prevTop - firstViewTop;
						goingDown = firstViewTop < prevTop;
						changed = Math.abs(topDelta) > 1;
					}
					else
					{
						goingDown = firstVisibleItem > prevPosition;
					}
					if (changed && scrollUpdated)
					{
						hideFloatingButton(goingDown);
					}
					prevPosition = firstVisibleItem;
					prevTop = firstViewTop;
					scrollUpdated = true;
				}
			}
		});

		if (searchString == null)
		{
			dialogsAdapter = new DialogsAdapter(context, dialogsType);
			if (AndroidUtilities.isTablet() && openedDialogId != 0)
			{
				dialogsAdapter.setOpenedDialogId(openedDialogId);
			}
			dialogsAdapter.setDialogsType(0);
			dialogsAdapter.categoryId = TabSetting.getTabModels().get(0).getId(); // Adel
			dialogsAdapter.Hiddenmode = hiddenMode; // Adel
			listView.setAdapter(dialogsAdapter);
		}
		int type = 0;
		if (searchString != null)
		{
			type = 2;
		}
		else if (!onlySelect)
		{
			type = 1;
		}
		dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
		dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate()
		{
			@Override
			public void searchStateChanged(boolean search)
			{
				if (searching && searchWas && searchEmptyView != null)
				{
					if (search)
					{
						searchEmptyView.showProgress();
					}
					else
					{
						searchEmptyView.showTextView();
					}
				}
			}

			@Override
			public void didPressedOnSubDialog(int did)
			{
				if (onlySelect)
				{
					didSelectResult(did, true, false);
				}
				else
				{
					Bundle args = new Bundle();
					if (did > 0)
					{
						args.putInt("user_id", did);
					}
					else
					{
						args.putInt("chat_id", -did);
					}
					if (actionBar != null)
					{
						actionBar.closeSearchField();
					}
					if (AndroidUtilities.isTablet())
					{
						if (dialogsAdapter != null)
						{
							dialogsAdapter.setOpenedDialogId(openedDialogId = did);
							updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
						}
					}
					if (searchString != null)
					{
						if (MessagesController.checkCanOpenChat(args, DialogsActivity.this))
						{
							NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
							presentFragment(new ChatActivity(args));
						}
					}
					else
					{
						if (MessagesController.checkCanOpenChat(args, DialogsActivity.this))
						{
							presentFragment(new ChatActivity(args));
						}
					}
				}
			}

			@Override
			public void needRemoveHint(final int did)
			{
				if (getParentActivity() == null)
				{
					return;
				}
				TLRPC.User user = MessagesController.getInstance().getUser(did);
				if (user == null)
				{
					return;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
				builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
				builder.setMessage(LocaleController.formatString("ChatHintsDelete", R.string.ChatHintsDelete, ContactsController.formatName(user.first_name, user.last_name)));
				builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						SearchQuery.removePeer(did);
					}
				});
				builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
				showDialog(builder.create());
			}
		});

		if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) // Adel
		{
			searchEmptyView.setVisibility(View.GONE);
			emptyView.setVisibility(View.GONE);
			listView.setEmptyView(progressView);
		}
		else
		{
			searchEmptyView.setVisibility(View.GONE);
			progressView.setVisibility(View.GONE);
			listView.setEmptyView(emptyView);
		}
		if (searchString != null)
		{
			actionBar.openSearchField(searchString);
		}

		if (!onlySelect && dialogsType == 0)
		{
			frameLayout.addView(fragmentContextView = new FragmentContextView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
		}




//		new PmSettingPacket().Send();

		// Adel
		floatingButton.setOnLongClickListener(new View.OnLongClickListener()
		{
			public boolean onLongClick(View view)
			{
				DialogsActivity.this.gotoHiddenMode();
				return true;
			}
		});

		// Adel
		floatingButtonLock.setOnLongClickListener(new View.OnLongClickListener()
		{
			public boolean onLongClick(View view)
			{
				ChangePassword();
				return true;
			}
		});

		// Adel
		TabInit();
		ZangoolehInit();


		//sina




			Setting2.DisplayedWellComeMessage();
		/*	//Setting.setCurrentJoiningChannel("channeluser");
			String text=LocaleController.getString("WellComeInfo",R.string.WellComeInfo);
			String title=LocaleController.getString("WellCome",R.string.WellCome);
			android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
			builder.setTitle(title);
			builder.setMessage(text);
			builder.setPositiveButton(LocaleController.getString("SendComment", R.string.SendComment).toUpperCase(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					LaunchActivity.VoteOnApp();
					dialogInterface.cancel();
				}
			});
			builder.setNegativeButton(LocaleController.getString("OK", R.string.OK).toUpperCase(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.cancel();
				}
			});
			builder.show();
			//show();*/


			return fragmentView;
	}


	private void TabInit()
	{
		TabSetting.init();
		TabSetting.GetTabs(tabHost, context);

		final ViewGroup test   = (ViewGroup) (tabHost.getChildAt(0)); // tabs is your Tablayout
		int             tabLen = test.getChildCount();
		for (int i = 0; i < tabLen; i++)
		{
			View v = test.getChildAt(i);
			v.setPadding(0, 0, 0, 0);

			final Integer tabId = i;
			// Adel
			v.setOnLongClickListener(new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View view)
				{
					// Read All Tab Messages
					tabHost.getTabAt(tabId).select();

					ArrayList<TLRPC.TL_dialog> list = dialogsAdapter.getDialogsArray();
					for (int j = 0; j < list.size(); j++)
					{
						long dialog_id = list.get(j).id;
						try
						{
							if (list.get(j).unread_count > 0)
							{
								int t = MessagesController.getInstance().dialogMessage.get(dialog_id).getId();
								MessagesController.getInstance().markDialogAsRead(dialog_id, t, t, 0, true, false);
							}
						} catch (Exception ignored)
						{

						}
					}

					return false;
				}
			});
		}

		if (LocaleController.getInstance().getCurrentLanguageName().equals("فارسی"))
		{
			tabHost.getTabAt(TabSetting.getTabModels().size() - 1).select();
		}

//		TabSetting.startThread(); // Adel
	}

	private void ZangoolehInit()
	{
		zangoolehBadge = new BadgeView(context, zangoolehMenu);
		zangoolehBadge.setText(LocaleController.getString("ZangoolehBadge", R.string.ZangoolehBadge));
		zangoolehBadge.setPadding(5, 0, 5, 0);
		zangoolehBadge.setTextSize(11);
		zangoolehBadge.setTextColor(Color.WHITE);
		zangoolehBadge.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
		zangoolehBadge.setBadgeBackgroundColor(Color.RED);
		zangoolehBadge.setBadgePosition(BadgeView.POSITION_BOTTOM_RIGHT);
		zangoolehBadge.hide();

		zangoolehHandler = new Handler();
		zangoolehRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				new WebService(DialogsActivity.this).execute("GetLastZangoolehId");
			}
		};

		zangoolehHandler.postDelayed(zangoolehRunnable, 10);
	}

	@Override
	public void onGetLastZangoolehIdCompleted(String response)
	{
		if (response.equals(BaseApplication.NOT_EXIST) || response.equals(BaseApplication.EXCEPTION))
		{
			zangoolehBadge.hide();
			zangoolehHandler.postDelayed(zangoolehRunnable, 60000);
		}
		else if (response.equals(BaseApplication.ANDROID_EXCEPTION))
		{
			zangoolehBadge.hide();
			zangoolehHandler.postDelayed(zangoolehRunnable, 20000);
		}
		else
		{
			int lastId = Integer.parseInt(response);

			if (ApplicationLoader.GetLastZangoolehId(context) < lastId)
			{
				zangoolehBadge.show();
			}
			else
			{
				zangoolehBadge.hide();
				onGetLastZangoolehIdCompleted(BaseApplication.EXCEPTION);
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (dialogsAdapter != null)
		{
			dialogsAdapter.Hiddenmode = hiddenMode; // Adel
			dialogsAdapter.notifyDataSetChanged();
		}
		if (dialogsSearchAdapter != null)
		{
			dialogsSearchAdapter.notifyDataSetChanged();
		}
		if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23)
		{
			Activity activity = getParentActivity();
			if (activity != null)
			{
				checkPermission = false;
				if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
				{
					if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS))
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
						builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
						builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
						showDialog(permissionDialog = builder.create());
					}
					else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
						builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
						builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
						showDialog(permissionDialog = builder.create());
					}
					else
					{
						askForPermissons();
					}
				}
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void askForPermissons()
	{
		Activity activity = getParentActivity();
		if (activity == null)
		{
			return;
		}
		ArrayList<String> permissons = new ArrayList<>();
		if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
		{
			permissons.add(Manifest.permission.READ_CONTACTS);
			permissons.add(Manifest.permission.WRITE_CONTACTS);
			permissons.add(Manifest.permission.GET_ACCOUNTS);
		}
		if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		String[] items = permissons.toArray(new String[permissons.size()]);
		activity.requestPermissions(items, 1);
	}

	@Override
	protected void onDialogDismiss(Dialog dialog)
	{
		super.onDialogDismiss(dialog);
		if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null)
		{
			askForPermissons();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		if (!onlySelect && floatingButton != null)
		{
			floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
			{
				@Override
				public void onGlobalLayout()
				{
					floatingButton.setTranslationY(floatingHidden ? AndroidUtilities.dp(100) : 0);
					floatingButton.setClickable(!floatingHidden);
					if (floatingButton != null)
					{
						if (Build.VERSION.SDK_INT < 16)
						{
							floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
						else
						{
							floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						}
					}
				}
			});
		}
	}

	@Override
	public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults)
	{
		if (requestCode == 1)
		{
			for (int a = 0; a < permissions.length; a++)
			{
				if (grantResults.length <= a || grantResults[a] != PackageManager.PERMISSION_GRANTED)
				{
					continue;
				}
				switch (permissions[a])
				{
					case Manifest.permission.READ_CONTACTS:
						ContactsController.getInstance().readContacts();
						break;
					case Manifest.permission.WRITE_EXTERNAL_STORAGE:
						ImageLoader.getInstance().checkMediaPaths();
						break;
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void didReceivedNotification(int id, Object... args)
	{
		TabSetting.setBadges(); // Adel

		if (id == NotificationCenter.dialogsNeedReload)
		{
			if (dialogsAdapter != null)
			{
				if (dialogsAdapter.isDataSetChanged())
				{
					dialogsAdapter.Hiddenmode = hiddenMode; // Adel
					dialogsAdapter.notifyDataSetChanged();
				}
				else
				{
					updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
				}
			}
			if (dialogsSearchAdapter != null)
			{
				dialogsSearchAdapter.notifyDataSetChanged();
			}
			if (listView != null)
			{
				try
				{
					if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) // Adel
					{
						searchEmptyView.setVisibility(View.GONE);
						emptyView.setVisibility(View.GONE);
						listView.setEmptyView(progressView);
					}
					else
					{
						progressView.setVisibility(View.GONE);
						if (searching && searchWas)
						{
							emptyView.setVisibility(View.GONE);
							listView.setEmptyView(searchEmptyView);
						}
						else
						{
							searchEmptyView.setVisibility(View.GONE);
							listView.setEmptyView(emptyView);
						}
					}
				} catch (Exception e)
				{
					FileLog.e(e); //TODO fix it in other way?
				}
			}
		}
		else if (id == NotificationCenter.emojiDidLoaded)
		{
			updateVisibleRows(0);
		}
		else if (id == NotificationCenter.updateInterfaces)
		{
			updateVisibleRows((Integer) args[0]);
		}
		else if (id == NotificationCenter.appDidLogout)
		{
			dialogsLoaded = false;
		}
		else if (id == NotificationCenter.encryptedChatUpdated)
		{
			updateVisibleRows(0);
		}
		else if (id == NotificationCenter.contactsDidLoaded)
		{
			updateVisibleRows(0);
		}
		else if (id == NotificationCenter.openedChatChanged)
		{
			if (dialogsType == 0 && AndroidUtilities.isTablet())
			{
				boolean close     = (Boolean) args[1];
				long    dialog_id = (Long) args[0];
				if (close)
				{
					if (dialog_id == openedDialogId)
					{
						openedDialogId = 0;
					}
				}
				else
				{
					openedDialogId = dialog_id;
				}
				if (dialogsAdapter != null)
				{
					dialogsAdapter.setOpenedDialogId(openedDialogId);
				}
				updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
			}
		}
		else if (id == NotificationCenter.notificationsSettingsUpdated)
		{
			updateVisibleRows(0);
		}
		else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError)
		{
			updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
		}
		else if (id == NotificationCenter.didSetPasscode)
		{
			updatePasscodeButton();
		}
		else if (id == NotificationCenter.needReloadRecentDialogsSearch)
		{
			if (dialogsSearchAdapter != null)
			{
				dialogsSearchAdapter.loadRecentSearch();
			}
		}
		else if (id == NotificationCenter.didLoadedReplyMessages)
		{
			updateVisibleRows(0);
		}
		else if (id == NotificationCenter.reloadHints)
		{
			if (dialogsSearchAdapter != null)
			{
				dialogsSearchAdapter.notifyDataSetChanged();
			}
		}
	}

	private ArrayList<TLRPC.TL_dialog> getDialogsArray()
	{
		if (dialogsType == 0)
		{
			dialogsAdapter.Hiddenmode = hiddenMode; // Adel
			return dialogsAdapter.getDialogsArray(); // Adel
		}
		else if (dialogsType == 1)
		{
			return MessagesController.getInstance().dialogsServerOnly;
		}
		else if (dialogsType == 2)
		{
			return MessagesController.getInstance().dialogsGroupsOnly;
		}
		return null;
	}

	public void setSideMenu(RecyclerView recyclerView)
	{
		sideMenu = recyclerView;
		sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
		sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
	}

	private void updatePasscodeButton()
	{
		if (passcodeItem == null)
		{
			return;
		}
		if (UserConfig.passcodeHash.length() != 0 && !searching)
		{
			passcodeItem.setVisibility(View.VISIBLE);
			if (UserConfig.appLocked)
			{
				passcodeItem.setIcon(R.drawable.lock_close);
			}
			else
			{
				passcodeItem.setIcon(R.drawable.lock_open);
			}
		}
		else
		{
			passcodeItem.setVisibility(View.GONE);
		}
	}

	private void hideFloatingButton(boolean hide)
	{
		// Adel
		if (hiddenMode)
		{
			return;
		}

		if (floatingHidden == hide)
		{
			return;
		}
		floatingHidden = hide;
		ObjectAnimator animator = ObjectAnimator.ofFloat(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(100) : 0).setDuration(300);
		animator.setInterpolator(floatingInterpolator);
		floatingButton.setClickable(!hide);
		animator.start();
	}

	private void updateVisibleRows(int mask)
	{
		if (listView == null)
		{
			return;
		}
		int count = listView.getChildCount();
		for (int a = 0; a < count; a++)
		{
			View child = listView.getChildAt(a);
			if (child instanceof DialogCell)
			{
				if (listView.getAdapter() != dialogsSearchAdapter)
				{
					DialogCell cell = (DialogCell) child;
					if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0)
					{
						cell.checkCurrentDialogIndex();
						if (dialogsType == 0 && AndroidUtilities.isTablet())
						{
							cell.setDialogSelected(cell.getDialogId() == openedDialogId);
						}
					}
					else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0)
					{
						if (dialogsType == 0 && AndroidUtilities.isTablet())
						{
							cell.setDialogSelected(cell.getDialogId() == openedDialogId);
						}
					}
					else
					{
						cell.update(mask);
					}
				}
			}
			else if (child instanceof UserCell)
			{
				((UserCell) child).update(mask);
			}
			else if (child instanceof ProfileSearchCell)
			{
				((ProfileSearchCell) child).update(mask);
			}
			else if (child instanceof RecyclerListView)
			{
				RecyclerListView innerListView = (RecyclerListView) child;
				int              count2        = innerListView.getChildCount();
				for (int b = 0; b < count2; b++)
				{
					View child2 = innerListView.getChildAt(b);
					if (child2 instanceof HintDialogCell)
					{
						((HintDialogCell) child2).checkUnreadCounter(mask);
					}
				}
			}
		}
	}

	public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate)
	{
		delegate = dialogsActivityDelegate;
	}

	public void setSearchString(String string)
	{
		searchString = string;
	}

	public boolean isMainDialogList()
	{
		return delegate == null && searchString == null;
	}

	private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param)
	{
		if (addToGroupAlertString == null)
		{
			if ((int) dialog_id < 0)
			{
				TLRPC.Chat chat = MessagesController.getInstance().getChat(-(int) dialog_id);
				if (ChatObject.isChannel(chat) && !chat.megagroup && (cantSendToChannels || !ChatObject.isCanWriteToChannel(-(int) dialog_id)))
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
					builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
					builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
					builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
					showDialog(builder.create());
					return;
				}
			}
		}
		if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null))
		{
			if (getParentActivity() == null)
			{
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
			builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
			int lower_part = (int) dialog_id;
			int high_id    = (int) (dialog_id >> 32);
			if (lower_part != 0)
			{
				if (high_id == 1)
				{
					TLRPC.Chat chat = MessagesController.getInstance().getChat(lower_part);
					if (chat == null)
					{
						return;
					}
					builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
				}
				else
				{
					if (lower_part > 0)
					{
						TLRPC.User user = MessagesController.getInstance().getUser(lower_part);
						if (user == null)
						{
							return;
						}
						builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
					}
					else if (lower_part < 0)
					{
						TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
						if (chat == null)
						{
							return;
						}
						if (addToGroupAlertString != null)
						{
							builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
						}
						else
						{
							builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
						}
					}
				}
			}
			else
			{
				TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
				TLRPC.User          user = MessagesController.getInstance().getUser(chat.user_id);
				if (user == null)
				{
					return;
				}
				builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
			}

			builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialogInterface, int i)
				{
					didSelectResult(dialog_id, false, false);
				}
			});
			builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
			showDialog(builder.create());
		}
		else
		{
			if (delegate != null)
			{
				delegate.didSelectDialog(DialogsActivity.this, dialog_id, param);
				delegate = null;
			}
			else
			{
				finishFragment();
			}
		}
	}

	@Override
	public ThemeDescription[] getThemeDescriptions()
	{
		ThemeDescription.ThemeDescriptionDelegate сellDelegate = new ThemeDescription.ThemeDescriptionDelegate()
		{
			@Override
			public void didSetColor(int color)
			{
				int count = listView.getChildCount();
				for (int a = 0; a < count; a++)
				{
					View child = listView.getChildAt(a);
					if (child instanceof ProfileSearchCell)
					{
						((ProfileSearchCell) child).update(0);
					}
					else if (child instanceof DialogCell)
					{
						((DialogCell) child).update(0);
					}
				}
				RecyclerListView recyclerListView = dialogsSearchAdapter.getInnerListView();
				if (recyclerListView != null)
				{
					count = recyclerListView.getChildCount();
					for (int a = 0; a < count; a++)
					{
						View child = recyclerListView.getChildAt(a);
						if (child instanceof HintDialogCell)
						{
							((HintDialogCell) child).update();
						}
					}
				}
			}
		};
		return new ThemeDescription[]{
				new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

				new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
				new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
				new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
				new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
				new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),
				new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch),
				new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder),

				new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

				new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

				new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
				new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

				new ThemeDescription(emptyTextView1, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
				new ThemeDescription(emptyTextView2, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),

				new ThemeDescription(floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionIcon),
				new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionBackground),
				new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionPressedBackground),

				new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable}, null, Theme.key_avatar_text),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundRed),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundOrange),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundViolet),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundGreen),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundCyan),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundBlue),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_avatar_backgroundPink),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_namePaint, null, null, Theme.key_chats_name),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_nameEncryptedPaint, null, null, Theme.key_chats_secretName),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_lockDrawable}, null, Theme.key_chats_secretIcon),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, null, Theme.key_chats_nameIcon),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_pinnedDrawable}, null, Theme.key_chats_pinnedIcon),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint, null, null, Theme.key_chats_message),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_nameMessage),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_draft),
				new ThemeDescription(null, 0, null, null, null, сellDelegate, Theme.key_chats_attachMessage),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePrintingPaint, null, null, Theme.key_chats_actionMessage),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, null, null, Theme.key_chats_date),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, null, null, Theme.key_chats_pinnedOverlay),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, null, null, Theme.key_chats_tabletSelectedOverlay),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkDrawable, Theme.dialogs_halfCheckDrawable}, null, Theme.key_chats_sentCheck),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_clockDrawable}, null, Theme.key_chats_sentClock),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, null, null, Theme.key_chats_sentError),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_errorDrawable}, null, Theme.key_chats_sentErrorIcon),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground),
				new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_muteDrawable}, null, Theme.key_chats_muteIcon),

				new ThemeDescription(sideMenu, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_chats_menuBackground),
				new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuName),
				new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhone),
				new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhoneCats),
				new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuCloudBackgroundCats),
				new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, new String[]{"cloudDrawable"}, null, null, null, Theme.key_chats_menuCloud),
				new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chat_serviceBackground),
				new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuTopShadow),

				new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon),
				new ThemeDescription(sideMenu, 0, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),

				new ThemeDescription(sideMenu, 0, new Class[]{DividerCell.class}, Theme.dividerPaint, null, null, Theme.key_divider),

				new ThemeDescription(listView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle),

				new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3),
				new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3),

				new ThemeDescription(listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
				new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection),

				new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{HashtagSearchCell.class}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

				new ThemeDescription(progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

				new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
				new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
				new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
				new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

				new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerBackground),
				new ThemeDescription(fragmentContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, null, null, null, Theme.key_inappPlayerPlayPause),
				new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerTitle),
				new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerPerformer),
				new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, null, null, null, Theme.key_inappPlayerClose),

				new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_returnToCallBackground),
				new ThemeDescription(fragmentContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_returnToCallText),

				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackground),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlack),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextLink),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLinkSelection),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue2),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue3),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue4),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray2),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray3),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray4),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogIcon),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextHint),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputField),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputFieldActivated),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareBackground),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareCheck),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareUnchecked),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareDisabled),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackground),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackgroundChecked),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogProgressCircle),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButton),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButtonSelector),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogScrollGlow),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBox),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBoxCheck),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeBackground),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeText),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgress),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgressBackground),
				new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogGrayLine),
				};
	}

	public interface DialogsActivityDelegate
	{
		void didSelectDialog(DialogsActivity fragment, long dialog_id, boolean param);
	}
	private void toggleMute(boolean instant,long dialog_id) {
		boolean muted = MessagesController.getInstance().isDialogMuted(dialog_id);
		if (!muted) {
			if (instant) {
				long flags;
				SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putInt("notify2_" + dialog_id, 2);
				flags = 1;
				MessagesStorage.getInstance().setDialogFlags(dialog_id, flags);
				editor.commit();
				TLRPC.TL_dialog dialog = MessagesController.getInstance().dialogs_dict.get(dialog_id);
				if (dialog != null) {
					dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
					dialog.notify_settings.mute_until = Integer.MAX_VALUE;
				}
				NotificationsController.updateServerNotificationsSettings(dialog_id);
				NotificationsController.getInstance().removeNotificationsForDialog(dialog_id);
			} else {
				showDialog(AlertsCreator.createMuteAlert(getParentActivity(), dialog_id));
			}
		} else {
			SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("notify2_" + dialog_id, 0);
			MessagesStorage.getInstance().setDialogFlags(dialog_id, 0);
			editor.commit();
			TLRPC.TL_dialog dialog = MessagesController.getInstance().dialogs_dict.get(dialog_id);
			if (dialog != null) {
				dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
			}
			NotificationsController.updateServerNotificationsSettings(dialog_id);
		}
	}

}

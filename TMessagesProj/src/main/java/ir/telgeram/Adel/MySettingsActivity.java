package ir.telgeram.Adel;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ApplicationLoader;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.R;
import ir.telgeram.ui.ActionBar.ActionBar;
import ir.telgeram.ui.ActionBar.BaseFragment;
import ir.telgeram.ui.Cells.EmptyCell;
import ir.telgeram.ui.Cells.HeaderCell;
import ir.telgeram.ui.Cells.ShadowSectionCell;
import ir.telgeram.ui.Cells.TextCheckCell;
import ir.telgeram.ui.Cells.TextSettingsCell;
import ir.telgeram.ui.Components.LayoutHelper;
import ir.telgeram.ui.DialogsActivity;

public class MySettingsActivity extends BaseFragment {

    private ListView listView;
    private ListAdapter listAdapter;
    private int sticker_req;

    private int voice_req;
    private int ProTelegram;
    private int rowCount;
    private int newgeramsectionrow;
    private int newgeramsectionrow2;
    private int Graphicsectionrow;
    private int Graphicsectionrow2;
    private int TabisUpside;
    private int DefaultTab;
    private int DefaultFont;

    private int tabsectionrow;
    private int tabsectionrow2;
    private int favor;
    private int bot;
    private int unread;
    private int channel;
    private int sgroup;
    private int ngroup;
    private int contact;
    private int all;
    private boolean needRestart;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;
        newgeramsectionrow=rowCount++;
        newgeramsectionrow2=rowCount++;

        sticker_req =rowCount++;
        voice_req =rowCount++;
        Graphicsectionrow=rowCount++;
        Graphicsectionrow2=rowCount++;
//        ProTelegram=rowCount++;
       TabisUpside=rowCount++;
      //  DefaultTab=rowCount++;
        DefaultFont=rowCount++;


//        tabsectionrow=rowCount++;
//        tabsectionrow2=rowCount++;
//        favor=rowCount++;
//        bot=rowCount++;
//        unread=rowCount++;
//        channel=rowCount++;
//        sgroup=rowCount++;
//        ngroup=rowCount++;
//        contact=rowCount++;
//        all=rowCount++;
        return true;
    }

    @Override
    public View createView(final Context context) {

        actionBar.setBackgroundColor(ThemeChanger.getcurrent().getActionbarcolor());
        actionBar.setItemsBackgroundColor(ThemeChanger.getcurrent().getActionbarcolor(), false);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                                                  @Override
                                                  public void onItemClick(int id) {
                                                      if (id == -1) {
                                                          finishFragment();
                                                      }
                                                  }
                                              });
actionBar.setTitle(LocaleController.getString("MySettings",R.string.MySettings));
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context) ;
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if  (i == sticker_req) {
                    boolean send = Setting2.getStickerReq();
                    Setting2.setStickerReq(!send);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == DefaultTab) {
                    presentFragment(new DefaultTabSelectActivity());
                }else if (i == DefaultFont) {
                    presentFragment(new SelectFontActivity());
                }else if (i == voice_req) {
                    boolean send = Setting2.getVoiceReq();
                    Setting2.setVoiceReq(!send);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == TabisUpside) {
                    boolean send = Setting2.getTabIsUp();
                    Setting2.setTabIsUp(!send);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                    parentLayout.rebuildAllFragmentViews(false);
                }else if(i==favor||i==unread||i==channel||i==sgroup||i==ngroup||i==contact||i==all||i==bot){
                    String tabname="favor";
                    if(i==unread)tabname="unread";
                    if(i==channel)tabname="channel";
                    if(i==sgroup)tabname="sgroup";
                    if(i==ngroup)tabname="ngroup";
                    if(i==bot)tabname="bot";
                    if(i==contact)tabname="contact";
                    if(i==all)tabname="all";
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Setting2.ToggleTab(tabname));
                    }
                    parentLayout.rebuildAllFragmentViews(false);
                    DialogsActivity.RebuildTabs();
                    needRestart=true;
                }
                }
        });
        frameLayout.addView(actionBar);
        needLayout();
        return fragmentView;
    }



    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != 0) {
                layoutParams.topMargin = 0;
                listView.setLayoutParams(layoutParams);
             //   extraHeightView.setTranslationY(newTop);
            }
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }




    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return i==DefaultFont||i==DefaultTab ||i==TabisUpside||i== sticker_req ||i==ProTelegram||i== voice_req ||i==favor||i==bot||i==all||i==channel||i==contact||i==ngroup||i==sgroup||i==unread ;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new EmptyCell(mContext);
                }

            } else if (type == 1) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == DefaultTab) {
                    textCell.setTextAndValue(LocaleController.getString("DefaultTab", R.string.DefaultTab), LocaleController.getString("TabTitle", TabSetting.getTabModels().get(Setting2.GetCurrentTab()).getTitle()), true);

                }else if (i == DefaultFont) {
                    textCell.setTextAndValue(LocaleController.getString("SelectFont", R.string.SelectFont), FontHelper.getFontTitle(Setting2.getCurrentFont()), true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if  (i == sticker_req) {
                    textCell.setTextAndCheck(LocaleController.getString("stickerReq", R.string.stickerReq), Setting2.getStickerReq(),true);
                } else if (i == TabisUpside) {
                    textCell.setTextAndCheck(LocaleController.getString("TabIsUp", R.string.TabIsUp), Setting2.getTabIsUp(),true);
                } else if (i == voice_req) {
                    textCell.setTextAndCheck(LocaleController.getString("voiceReq", R.string.voiceReq), Setting2.getVoiceReq(),true);
                }else if (i == favor){textCell.setTextAndCheck(LocaleController.getString("Favorites", R.string.Favorites), Setting2.TabisShowed("favor"),true);
                }else if (i == all){textCell.setTextAndCheck(LocaleController.getString("AllChats", R.string.AllChats), Setting2.TabisShowed("all"),true);
                }else if (i == channel){textCell.setTextAndCheck(LocaleController.getString("Channels", R.string.Channels), Setting2.TabisShowed("channel"),true);
                }else if (i == sgroup){textCell.setTextAndCheck(LocaleController.getString("SuperGroups", R.string.SuperGroups),  Setting2.TabisShowed("sgroup"),true);
                }else if (i == ngroup){textCell.setTextAndCheck(LocaleController.getString("Groups", R.string.Groups), Setting2.TabisShowed("ngroup"),true);
                }else if (i == bot){textCell.setTextAndCheck(LocaleController.getString("Bot", R.string.Bot),  Setting2.TabisShowed("bot"),true);
                }else if (i == contact){textCell.setTextAndCheck(LocaleController.getString("Contacts", R.string.Contacts), Setting2.TabisShowed("contact"),true);
                }else if (i == unread){textCell.setTextAndCheck(LocaleController.getString("Unread", R.string.Unread), Setting2.TabisShowed("unread"),true);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
               if(i==newgeramsectionrow2){
                    ((HeaderCell) view).setText(LocaleController.getString("GhostProtocolSetting", R.string.ChatSetting));
                }else if(i==Graphicsectionrow2) {
                   ((HeaderCell) view).setText(LocaleController.getString("GraphicSetting", R.string.GraphicSetting));
               }else if(i==tabsectionrow2){
                   ((HeaderCell) view).setText(LocaleController.getString("TabSetting", R.string.TabSetting));
               }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i==newgeramsectionrow||Graphicsectionrow==i||tabsectionrow==i ) {
                return 1;
            } else if (TabisUpside==i || sticker_req ==i||i== voice_req ||favor==i||bot==i||all==i||contact==i||sgroup==i||ngroup==i||unread==i||channel==i) {
                return 3;
            } else if (i==DefaultFont||i==ProTelegram|| i==DefaultTab) {
                return 2;
            } else if (newgeramsectionrow2==i||i==Graphicsectionrow2||tabsectionrow2==i ) {
                return 4;
            } else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 7;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}

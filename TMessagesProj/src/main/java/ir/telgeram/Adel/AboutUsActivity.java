package ir.telgeram.Adel;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ApplicationLoader;
import ir.telgeram.messenger.BuildConfig;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.R;
import ir.telgeram.ui.ActionBar.ActionBar;
import ir.telgeram.ui.ActionBar.BaseFragment;
import ir.telgeram.ui.Components.LayoutHelper;

public class AboutUsActivity extends BaseFragment
{
	private ListView listView;
	private Context  thiscontext;

	@Override
	public View createView(Context context)
	{
		thiscontext = context;

		actionBar.setBackButtonImage(R.drawable.ic_ab_back);
		actionBar.setTitle(LocaleController.getString("AboutUs", R.string.AboutUs));
		actionBar.setAddToContainer(false);
		actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
		{
			@Override
			public void onItemClick(int id)
			{
				if (id == -1)
				{
					finishFragment();
				}
			}
		});
		if (AndroidUtilities.isTablet())
		{
			actionBar.setOccupyStatusBar(false);
		}

		fragmentView = new FrameLayout(context);
		FrameLayout frameLayout = (FrameLayout) fragmentView;

		listView = new ListView(context);
		listView.setDivider(null);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			//			listView.setBackground(context.getResources().getDrawable(R.drawable.about_list_background));
		}
		listView.setDividerHeight(AndroidUtilities.dp(5));
		listView.setVerticalScrollBarEnabled(false);
		listView.setAdapter(new AboutListAdapter(thiscontext));


		TextView txtappname = new TextView(context);
		txtappname.setText(R.string.AppName);
		TextView txtappversion = new TextView(context);
		String   versionName   = "0";
		try
		{
			versionName = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e)
		{
			e.printStackTrace();
		}
		txtappversion.setText(LocaleController.getString("Version", R.string.Version) + ": " + BuildConfig.VERSION_NAME);
		txtappname.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
		txtappversion.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
		txtappname.setTextSize(15);
		txtappversion.setTextSize(15);
		txtappname.setTypeface(null, Typeface.BOLD);
		ImageView img = new ImageView(context);
		img.setImageDrawable(context.getResources().getDrawable(R.drawable.logo));
		frameLayout.addView(img, LayoutHelper.createFrame(120, 120, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 90, 0, 5));
		frameLayout.addView(txtappname, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 220, 0, 5));
		frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 300, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 40, 250, 40, 0));
		frameLayout.addView(txtappversion, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 10, 0, 10));

		frameLayout.addView(actionBar);

		return fragmentView;
	}

	private class AboutListAdapter extends BaseAdapter
	{
		private Context context;

		public AboutListAdapter(Context context)
		{
			this.context = context;
		}

		@Override
		public int getCount()
		{
			return 3;
		}

		@Override
		public Object getItem(int i)
		{
			return null;
		}

		@Override
		public long getItemId(int i)
		{
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup)
		{
			TextView                 txt          = new TextView(context);
			FrameLayout.LayoutParams layoutParams = LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 5, 0, 5);
			//			txt.setLayoutParams(layoutParams);
			txt.setPadding(50, 30, 50, 30);
			txt.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			{
			//	txt.setBackground(context.getResources().getDrawable(R.drawable.rect_rounded_green));
			//		txt.setTextColor(Color.parseColor("#ffffff"));
			}

			txt.setGravity(Gravity.CENTER);
			if (i == 0)
			{
				txt.setText("صاحب امتیاز پروژه: مهدی موسوی");
			}
			else if (i == 1)
			{
				txt.setText(LocaleController.getString("ContactUs", R.string.ContactUs) + ": " + "info@jetgram.me");
			}
			else if (i == 2)
			{
				txt.setText(LocaleController.getString("ProgrammedBy", R.string.ProgrammedBy) + " " + "");
			}

			return txt;
		}
	}
}

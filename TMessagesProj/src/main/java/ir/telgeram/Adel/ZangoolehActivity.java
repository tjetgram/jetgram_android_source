package ir.telgeram.Adel;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ApplicationLoader;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.R;

public class ZangoolehActivity extends BaseActivity
		implements IZangoolehList
{
	LinearLayout actionBar;
	ImageView    imgBack;
	TextView     txtTitle;

	ListView             listView;
	ZangoolehListAdapter adapter;

	// ------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zangooleh_list);

		// Find Views
		FindViews();

		// Set Fonts
		SetFonts(AndroidUtilities.getTypeface(""));

		// Set Theme and title
		actionBar.setBackgroundColor(ThemeChanger.getcurrent().getActionbarcolor());
		txtTitle.setText(LocaleController.getString("AppName", R.string.AppName));
		txtTitle.setTypeface(AndroidUtilities.getTypeface(""));

		// Get Zangooleh List From Server
		ZangoolehList();
	}

	@Override
	public void FindViews()
	{
		super.FindViews();

		actionBar = (LinearLayout) findViewById(R.id.actionBar);
		imgBack = (ImageView) findViewById(R.id.imgBack);
		txtTitle = (TextView) findViewById(R.id.txtTitle);

		listView = (ListView) findViewById(R.id.listView);

		// Set imgBack onClickListener
		imgBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				finish();
			}
		});
	}

	// ------------------------------------------------------------
	public void ZangoolehList()
	{
		ShowLoadLayout();

		new WebService(this).execute("ZangoolehList");
	}

	@Override
	public void onZangoolehListCompleted(final String response)
	{
		if (response.equals(BaseApplication.NOT_EXIST))
		{
			progressBar.setVisibility(View.GONE);
			txtLoad.setText("هیچ اطلاعیه ای موجود نیست!");
			btnTryAgain.setText("بازگشت");
			btnTryAgain.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					finish();
				}
			});
			btnTryAgain.setVisibility(View.VISIBLE);
		}
		else if (response.equals(BaseApplication.EXCEPTION))
		{
			progressBar.setVisibility(View.GONE);
			txtLoad.setText("مشکلی پیش اومد، لطفا مجددا تلاش کنید");
			btnTryAgain.setText("تلاش مجدد");
			btnTryAgain.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					ZangoolehList();
				}
			});
			btnTryAgain.setVisibility(View.VISIBLE);
		}
		else if (response.equals(BaseApplication.ANDROID_EXCEPTION))
		{
			progressBar.setVisibility(View.GONE);
			txtLoad.setText("ارتباط با سرور برقرار نشد، تنظیمات اینترنت را بررسی کنید");
			btnTryAgain.setText("تلاش مجدد");
			btnTryAgain.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					ZangoolehList();
				}
			});
			btnTryAgain.setVisibility(View.VISIBLE);
		}
		else
		{
			new AsyncTask<Void, Void, ArrayList<Zangooleh>>()
			{
				@Override
				protected ArrayList<Zangooleh> doInBackground(Void... params)
				{
					ArrayList<Zangooleh> zangoolehList = new ArrayList<>();
					int                  maxId         = 0;

					try
					{
						JSONArray jsonArray = new JSONArray(response);
						for (int i = 0; i < jsonArray.length(); i++)
						{
							JSONObject jsonObject = jsonArray.getJSONObject(i);

							Zangooleh zangooleh = new Zangooleh();
							zangooleh.Id = jsonObject.getInt("Id");
							zangooleh.Title = jsonObject.getString("Title");
							zangooleh.Content = jsonObject.getString("Content");
							zangooleh.Link = jsonObject.getString("Link");
							zangooleh.ImageUrl = jsonObject.getString("ImageUrl");
							zangooleh.ButtonText = jsonObject.getString("ButtonText");
							zangooleh.Time = jsonObject.getString("Time");

							if (maxId < zangooleh.Id)
							{
								maxId = zangooleh.Id;
							}

							zangoolehList.add(zangooleh);
						}

						ApplicationLoader.SetLastZangoolehId(ZangoolehActivity.this, maxId);
					} catch (JSONException e)
					{
						return null;
					}

					return zangoolehList;
				}

				@Override
				protected void onPostExecute(ArrayList<Zangooleh> zangoolehList)
				{
					if (zangoolehList == null)
					{
						onZangoolehListCompleted(BaseApplication.EXCEPTION);
						return;
					}

					// First of all we set adapter to listView
					adapter = new ZangoolehListAdapter(ZangoolehActivity.this, AndroidUtilities.getTypeface(""), zangoolehList);
					listView.setAdapter(adapter);

					// Then Show Main Layout
					ShowMainLayout();
				}
			}.execute();
		}
	}
}

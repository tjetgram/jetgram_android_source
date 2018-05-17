package ir.telgeram.Adel;

/**
 * Created by Pouya on 8/2/2016.
 */

import android.content.Context;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils.TruncateAt;
import android.text.style.CharacterStyle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.mp4parser.authoring.tracks.h265.NalUnitTypes;

import java.util.ArrayList;

import ir.telgeram.messenger.AndroidUtilities;
import ir.telgeram.messenger.ApplicationLoader;
import ir.telgeram.messenger.LocaleController;
import ir.telgeram.messenger.MessageObject;
import ir.telgeram.messenger.R;
import ir.telgeram.messenger.support.widget.helper.ItemTouchHelper;
import ir.telgeram.tgnet.TLRPC;
import ir.telgeram.tgnet.TLRPC.Message;
import ir.telgeram.tgnet.TLRPC.MessageMedia;
import ir.telgeram.tgnet.TLRPC.TL_message;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaAudio_layer45;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaContact;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaDocument;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaDocument_old;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaEmpty;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaGeo;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaPhoto;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaPhoto_old;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaUnsupported;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaUnsupported_old;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaVenue;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaVideo_layer45;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaVideo_old;
import ir.telgeram.tgnet.TLRPC.TL_messageMediaWebPage;
import ir.telgeram.tgnet.TLRPC.TL_message_secret;
import ir.telgeram.ui.ActionBar.ActionBar;
import ir.telgeram.ui.ActionBar.BaseFragment;
import ir.telgeram.ui.Cells.ChatMessageCell;
import ir.telgeram.ui.Components.ChatActivityEnterView;
import ir.telgeram.ui.Components.LayoutHelper;
import ir.telgeram.ui.Components.PhotoFilterView;
import ir.telgeram.ui.Components.SizeNotifierFrameLayout;

public class EditAndForward extends BaseFragment
{
	public    FrameLayout           frameLayout;
	protected ChatActivityEnterView enterView;
	private   MessageObject         messageObject;

	public EditAndForward(MessageObject messageObject)
	{
		this.messageObject = new MessageObject(m389a(messageObject.messageOwner, messageObject), null, true);
		this.messageObject.photoThumbs = messageObject.photoThumbs;
	}

	private Message m389a(Message message, MessageObject messageObject)
	{
		if (message == null)
		{
			return null;
		}
		Message message2 = new Message();
		if (message instanceof TL_message)
		{
			message2 = new TL_message();
		}
		else if (message instanceof TL_message_secret)
		{
			message2 = new TL_message_secret();
		}
		message2.id = message.id;
		message2.from_id = message.from_id;
		message2.to_id = message.to_id;
		message2.date = message.date;
		message2.action = message.action;
		message2.reply_to_msg_id = message.reply_to_msg_id;
		message2.fwd_from = message.fwd_from;
		message2.reply_to_random_id = message.reply_to_random_id;
		message2.via_bot_name = message.via_bot_name;
		message2.edit_date = message.edit_date;
		message2.silent = message.silent;
		if (message2.message != null)
		{
			message2.message = message.message;
		}
		else if (messageObject.messageText != null)
		{
			message2.message = messageObject.messageText.toString();
		}
		if (message.media != null)
		{
			message2.media = m390a(message.media);
		}
		message2.flags = message.flags;
		message2.mentioned = message.mentioned;
		message2.media_unread = message.media_unread;
		message2.out = message.out;
		message2.unread = message.unread;
		message2.entities = message.entities;
		message2.reply_markup = message.reply_markup;
		message2.views = message.views;
		message2.via_bot_id = message.via_bot_id;
		message2.send_state = message.send_state;
		message2.fwd_msg_id = message.fwd_msg_id;
		message2.attachPath = message.attachPath;
		message2.params = message.params;
		message2.random_id = message.random_id;
		message2.local_id = message.local_id;
		message2.dialog_id = message.dialog_id;
		message2.ttl = message.ttl;
		message2.destroyTime = message.destroyTime;
		message2.layer = message.layer;
		message2.seq_in = message.seq_in;
		message2.seq_out = message.seq_out;
		message2.replyMessage = message.replyMessage;
		return message2;
	}

	private MessageMedia m390a(MessageMedia messageMedia)
	{
		MessageMedia tL_messageMediaUnsupported_old = messageMedia instanceof TL_messageMediaUnsupported_old ? new TL_messageMediaUnsupported_old() : messageMedia instanceof TL_messageMediaAudio_layer45 ? new TL_messageMediaAudio_layer45() : messageMedia instanceof TL_messageMediaPhoto_old ? new TL_messageMediaPhoto_old() : messageMedia instanceof TL_messageMediaUnsupported ? new TL_messageMediaUnsupported() : messageMedia instanceof TL_messageMediaEmpty ? new TL_messageMediaEmpty() : messageMedia instanceof TL_messageMediaVenue ? new TL_messageMediaVenue() : messageMedia instanceof TL_messageMediaVideo_old ? new TL_messageMediaVideo_old() : messageMedia instanceof TL_messageMediaDocument_old ? new TL_messageMediaDocument_old() : messageMedia instanceof TL_messageMediaDocument ? new TL_messageMediaDocument() : messageMedia instanceof TL_messageMediaContact ? new TL_messageMediaContact() : messageMedia instanceof TL_messageMediaPhoto ? new TL_messageMediaPhoto() : messageMedia instanceof TL_messageMediaVideo_layer45 ? new TL_messageMediaVideo_layer45() : messageMedia instanceof TL_messageMediaWebPage ? new TL_messageMediaWebPage() : messageMedia instanceof TL_messageMediaGeo ? new TL_messageMediaGeo() : new MessageMedia();
		tL_messageMediaUnsupported_old.bytes = messageMedia.bytes;
		tL_messageMediaUnsupported_old.caption = messageMedia.caption;
		tL_messageMediaUnsupported_old.photo = messageMedia.photo;
		tL_messageMediaUnsupported_old.audio_unused = messageMedia.audio_unused;
		tL_messageMediaUnsupported_old.geo = messageMedia.geo;
		tL_messageMediaUnsupported_old.title = messageMedia.title;
		tL_messageMediaUnsupported_old.address = messageMedia.address;
		tL_messageMediaUnsupported_old.provider = messageMedia.provider;
		tL_messageMediaUnsupported_old.venue_id = messageMedia.venue_id;
		tL_messageMediaUnsupported_old.document = messageMedia.document;
		tL_messageMediaUnsupported_old.video_unused = messageMedia.video_unused;
		tL_messageMediaUnsupported_old.phone_number = messageMedia.phone_number;
		tL_messageMediaUnsupported_old.first_name = messageMedia.first_name;
		tL_messageMediaUnsupported_old.last_name = messageMedia.last_name;
		tL_messageMediaUnsupported_old.user_id = messageMedia.user_id;
		tL_messageMediaUnsupported_old.webpage = messageMedia.webpage;
		return tL_messageMediaUnsupported_old;
	}

	private void m391a()
	{
		ArrayList arrayList = new ArrayList();
		arrayList.add(this.messageObject);
		showDialog(new PouyaShare(getParentActivity(), arrayList, false, true, false, new PouyaShare.OnDoneListener()
		{
			public void onDone()
			{
				Toast.makeText(getParentActivity(), LocaleController.getString("Sent", R.string.Sent), Toast.LENGTH_SHORT).show();
				finishFragment();
			}
		}));
	}

	private boolean m395c()
	{
		return (this.messageObject.messageOwner == null || this.messageObject.messageOwner.media == null || (this.messageObject.messageOwner.media instanceof TL_messageMediaWebPage) || (this.messageObject.messageOwner.media instanceof TL_messageMediaEmpty)) ? false : true;
	}

	public View createView(Context context)
	{
		View   view;
		String obj = "";
		this.actionBar.setBackButtonImage(R.drawable.ic_ab_back);
		this.actionBar.setAllowOverlayTitle(true);
		this.actionBar.setTitle(LocaleController.getString("ProForward", R.string.ProForward));
		this.actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick()
		{
			public void onItemClick(int i)
			{
				if (i == -1)
				{
					EditAndForward.this.finishFragment();
				}
			}
		});
		this.fragmentView = new ae(this, context);
		SizeNotifierFrameLayout sizeNotifierFrameLayout = (SizeNotifierFrameLayout) this.fragmentView;
		sizeNotifierFrameLayout.setBackgroundImage(ApplicationLoader.getCachedWallpaper());
		this.frameLayout = new FrameLayout(context);
		sizeNotifierFrameLayout.addView(this.frameLayout, LayoutHelper.createFrame(-1, -2, 17));
		this.frameLayout.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent)
			{
				return true;
			}
		});
		FrameLayout frameLayout = new FrameLayout(context);
		frameLayout.setBackgroundColor(-1);
		if (m395c())
		{
			this.frameLayout.addView(frameLayout, LayoutHelper.createFrame(-1, 48, 48));
		}
		View view2 = new View(context);
		view2.setBackgroundColor(-1513240);
		frameLayout.addView(view2, LayoutHelper.createFrame(-1, 1, 83));
		TextView textView = new TextView(context);
		textView.setTextSize(1, 14.0f);
		textView.setTextColor(-13141330);
		//textView.setTypeface(C0925e.m452a().m441c());
		textView.setSingleLine(true);
		textView.setEllipsize(TruncateAt.END);
		textView.setMaxLines(1);
		textView.setText(LocaleController.getString("Media", R.string.Media) + " : ");
		frameLayout.addView(textView, LayoutHelper.createFrame(-2, -2.0f, (LocaleController.isRTL ? 5 : 3) | 16, 10.0f, 4.0f, 10.0f, 0.0f));
		if (this.enterView != null)
		{
			this.enterView.onDestroy();
		}
		this.enterView = new ChatActivityEnterView(getParentActivity(), sizeNotifierFrameLayout, null, false);
		this.enterView.setDialogId(this.messageObject.getDialogId());
		this.enterView.getMessageEditText().setMaxLines(m395c() ? 10 : 15);

		sizeNotifierFrameLayout.addView(this.enterView, sizeNotifierFrameLayout.getChildCount() - 1, LayoutHelper.createFrame(-1, -2, 83));
		//this.enterView.setDelegate(new ag(this));
		String          str = null;
		ChatMessageCell messgcell;
		if (m395c())
		{
			messgcell = new ChatMessageCell(getParentActivity());
			String str2 = this.messageObject.messageOwner.media.caption;
			this.messageObject.messageOwner.media.caption = null;
			this.messageObject.caption = null;
			str = str2;
		}
		else
		{
			textView.setVisibility(View.GONE);
			CharSequence charSequence = this.messageObject.messageText;
			this.messageObject.messageText = null;
			if (this.messageObject.messageOwner != null)
			{
				this.messageObject.messageOwner.message = null;
			}
			messgcell = new ChatMessageCell(getParentActivity());
			str = charSequence.toString();
		}
		messgcell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate()
		{
			@Override
			public void didPressedUserAvatar(ChatMessageCell cell, TLRPC.User user)
			{

			}

			@Override
			public void didPressedViaBot(ChatMessageCell cell, String username)
			{

			}

			@Override
			public void didPressedChannelAvatar(ChatMessageCell cell, TLRPC.Chat chat, int postId)
			{

			}

			@Override
			public void didPressedCancelSendButton(ChatMessageCell cell)
			{

			}

			@Override
			public void didLongPressed(ChatMessageCell cell)
			{

			}

			@Override
			public void didPressedReplyMessage(ChatMessageCell cell, int id)
			{

			}

			@Override
			public void didPressedUrl(MessageObject messageObject, CharacterStyle url, boolean longPress)
			{

			}

			@Override
			public void needOpenWebView(String url, String title, String description, String originalUrl, int w, int h)
			{

			}

			@Override
			public void didPressedImage(ChatMessageCell cell)
			{

			}

			@Override
			public void didPressedShare(ChatMessageCell cell)
			{

			}

			@Override
			public void didPressedOther(ChatMessageCell cell)
			{

			}

			@Override
			public void didPressedBotButton(ChatMessageCell cell, TLRPC.KeyboardButton button)
			{

			}

			@Override
			public void didPressedInstantButton(ChatMessageCell cell)
			{

			}

			@Override
			public boolean needPlayAudio(MessageObject messageObject)
			{
				return false;
			}

			@Override
			public boolean canPerformActions()
			{
				return false;
			}
		});
		messgcell.setMessageObject(this.messageObject, false, false); // Adel add false, false to params
		if (m395c())
		{
			this.frameLayout.addView(messgcell, LayoutHelper.createFrame(-1, -2.0f, 48, 0.0f, m395c() ? 48.0f : 0.0f, 0.0f, 0.0f));
		}
		this.enterView.setFieldText(str);
		this.enterView.getSendButton().setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String fieldText = EditAndForward.this.enterView.getFieldText().toString();
				if (fieldText != null && fieldText.length() == 0)
				{
					fieldText = null;
				}
				if (EditAndForward.this.m395c())
				{
					EditAndForward.this.messageObject.messageOwner.media.caption = fieldText;
				}
				else
				{
					EditAndForward.this.messageObject.messageText = fieldText;
					if (EditAndForward.this.messageObject.messageOwner != null)
					{
						EditAndForward.this.messageObject.messageOwner.message = fieldText;
					}
				}
				EditAndForward.this.enterView.closeKeyboard();
				EditAndForward.this.m391a();
			}
		});
		FrameLayout FrameLayout2 = new FrameLayout(context);
		FrameLayout2.setClickable(true);
		this.enterView.addTopView(FrameLayout2, 48);
		view2 = new View(context);
		view2.setBackgroundColor(-1513240);
		FrameLayout2.addView(view2, LayoutHelper.createFrame(-1, 1, 83));
		TextView txt = new TextView(context);
		txt.setTextSize(1, 14.0f);
		txt.setTextColor(-13141330);
		// txt.setTypeface(C0925e.m452a().m441c());
		txt.setSingleLine(true);
		txt.setEllipsize(TruncateAt.END);
		txt.setMaxLines(1);
		if (m395c())
		{
			txt.setText(LocaleController.getString("MediaCaption", R.string.MediaCaption) + " : ");
			this.enterView.getMessageEditText().setFilters(new InputFilter[]{new LengthFilter(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION)});
			this.enterView.getMessageEditText().setHint(LocaleController.getString("MediaCaption", R.string.MediaCaption));
		}
		else
		{
			txt.setText(LocaleController.getString("EditText", R.string.EditText) + " : ");
			this.enterView.getMessageEditText().setHint(LocaleController.getString("EditText", R.string.EditText));
		}
		FrameLayout2.addView(txt, LayoutHelper.createFrame(-2, -2.0f, (LocaleController.isRTL ? 5 : 3) | 16, 10.0f, 4.0f, 10.0f, 0.0f));
		this.enterView.showTopView(true, false);
		return this.fragmentView;
	}
}

class ae extends SizeNotifierFrameLayout
{
	final /* synthetic */ EditAndForward f433b;
	int f432a;

	public ae(EditAndForward acVar, Context context)
	{
		super(context);
		this.f433b = acVar;

		this.f432a = 0;
	}

	protected void onLayout(boolean z, int i, int i2, int i3, int i4)
	{
		int childCount   = getChildCount();
		int emojiPadding = getKeyboardHeight() <= AndroidUtilities.dp(20.0f) ? this.f433b.enterView.getEmojiPadding() : 0;
		setBottomClip(emojiPadding);
		for (int i5 = 0; i5 < childCount; i5++)
		{
			View childAt = getChildAt(i5);
			if (childAt.getVisibility() != View.GONE)
			{
				int                      i6;
				FrameLayout.LayoutParams layoutParams   = (FrameLayout.LayoutParams) childAt.getLayoutParams();
				int                      measuredWidth  = childAt.getMeasuredWidth();
				int                      measuredHeight = childAt.getMeasuredHeight();
				int                      i7             = layoutParams.gravity;
				if (i7 == -1)
				{
					i7 = 51;
				}
				int i8 = i7 & 112;
				switch ((i7 & 7) & 7)
				{
					case PhotoFilterView.CurvesToolValue.CurvesTypeRed /*1*/:
						i7 = ((((i3 - i) - measuredWidth) / 2) + layoutParams.leftMargin) - layoutParams.rightMargin;
						break;
					case 5: // Adel changed Request.Method.OPTIONS to 5
						i7 = (i3 - measuredWidth) - layoutParams.rightMargin;
						break;
					default:
						i7 = layoutParams.leftMargin;
						break;
				}
				switch (i8)
				{
					case TLRPC.USER_FLAG_PHONE /*16*/:
						i6 = (((((i4 - emojiPadding) - i2) - measuredHeight) / 2) + layoutParams.topMargin) - layoutParams.bottomMargin;
						break;
					case NalUnitTypes.NAL_TYPE_UNSPEC48 /*48*/:
						i6 = layoutParams.topMargin + getPaddingTop();
						break;
					case 80:
						i6 = (((i4 - emojiPadding) - i2) - measuredHeight) - layoutParams.bottomMargin;
						break;
					default:
						i6 = layoutParams.topMargin;
						break;
				}
				if (this.f433b.enterView.isPopupView(childAt))
				{
					i6 = this.f433b.enterView.getBottom();
				}
				childAt.layout(i7, i6, measuredWidth + i7, measuredHeight + i6);
			}
		}
		notifyHeightChanged();
	}

	protected void onMeasure(int i, int i2)
	{
		int size  = View.MeasureSpec.getSize(i);
		int size2 = View.MeasureSpec.getSize(i2);
		setMeasuredDimension(size, size2);
		size2 -= getPaddingTop();
		int emojiPadding = getKeyboardHeight() <= AndroidUtilities.dp(20.0f) ? size2 - this.f433b.enterView.getEmojiPadding() : size2;
		int childCount   = getChildCount();
		measureChildWithMargins(this.f433b.enterView, i, 0, i2, 0);
		this.f432a = this.f433b.enterView.getMeasuredHeight();
		for (int i3 = 0; i3 < childCount; i3++)
		{
			View childAt = getChildAt(i3);
			if (!(childAt == null || childAt.getVisibility() == View.GONE || childAt == this.f433b.enterView))
			{
				try
				{
					if (childAt == this.f433b.frameLayout)
					{
						childAt.measure(View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(emojiPadding, 1073741824));
					}
					else if (this.f433b.enterView.isPopupView(childAt))
					{
						childAt.measure(View.MeasureSpec.makeMeasureSpec(size, 1073741824), View.MeasureSpec.makeMeasureSpec(childAt.getLayoutParams().height, 1073741824));
					}
					else
					{
						measureChildWithMargins(childAt, i, 0, i2, 0);
					}
				} catch (Throwable e)
				{

				}
			}
		}
	}
}

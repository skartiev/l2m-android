package com.zuzex.look2meet.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.DialogObject;
import com.zuzex.look2meet.DataModel.IUpdateStatus;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.Look2meet;
import com.zuzex.look2meet.ProfileActivity;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.media.CaptureMedia;
import com.zuzex.look2meet.socket.SocketThreadCallbackAdapter;
import com.zuzex.look2meet.socket.SocketWorker;
import com.zuzex.look2meet.utils.AnimationPreloader;
import com.zuzex.look2meet.utils.BitmapResizer;
import com.zuzex.look2meet.utils.SoftKeyboardHandledLinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.IOAcknowledge;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

public class ChatActivity extends CaptureMedia implements EmoticonsGridAdapter.KeyClickListener, PopupMenu.OnMenuItemClickListener {
    private EditText messageEditText = null;
    private ChatAdapter adapter;
    private ListView messagesContainer;
	private ImageView imageAvatar;
	private TextView textViewTop;
	private LinearLayout attachesLayout;
    private AnimationPreloader preloader;
	private ArrayList<ChatAttachView> attachesArray = new ArrayList<ChatAttachView>();
	public DialogObject currDialogObject;
	public int currUserId = -1;
    private static final int NO_OF_EMOTICONS = 54;
    private LinearLayout emoticonsCover;
    private PopupWindow popupWindow;
    private View popUpView;
    private int keyboardHeight;
    private boolean isKeyBoardVisible;
    private SoftKeyboardHandledLinearLayout rootLayout;
    private int maxSymbolsInMessage;

    private boolean isHistoryUpdated = false;

    private ArrayList<ChatMessage> chatMessages;

    private ArrayList<ChatMessage> chatHistroy;

    private boolean isNeedLoad = true;

    private static final String TAG = "ChatActivity";
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isHistoryUpdated = false;
        Emoticon.setup(this);
        this.context = this;
        preloader = new AnimationPreloader(this);
	    setContentView(R.layout.activity_chat);
        rootLayout = (SoftKeyboardHandledLinearLayout) findViewById(R.id.root_layout);
        rootLayout.setOnSoftKeyboardVisibilityChangeListener(
                new SoftKeyboardHandledLinearLayout.SoftKeyboardVisibilityChangeListener() {
                    @Override
                    public void onSoftKeyboardShow() {
                        Log.w(TAG, "onKeyboardShow");
                        popupWindow.dismiss();
                    }
                    @Override
                    public void onSoftKeyboardHide() {
                        Log.w(TAG, "onKeyboardHide");
                    }
                });
	    currDialogObject = (DialogObject) getIntent().getSerializableExtra("currDialogObject");

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(messageReceiver, new IntentFilter("reciveChatMessage"));

        Intent intent = new Intent("reciveChatMessage");
        intent.putExtra("unread_messages_count", UserProfile.getInstance().unreadMessages-currDialogObject.unreadMessages);
        LocalBroadcastManager.getInstance(Look2meet.getContext()).sendBroadcast(intent);
        maxSymbolsInMessage = UserProfile.getInstance().maxSymbolsInMessage;
        if(UserProfile.getInstance().id == -1) {
            UserProfile.getInstance().reload(new IUpdateStatus() {
                @Override
                public void onUpdateSuccess(JSONObject response) {
                    initViews();
                    emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);
                    popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);
                    // Defining default height of keyboard which is equal to 230 dip
                    final float popUpheight = 330;
                    changeKeyboardHeight((int) popUpheight);
                    enablePopUpView();
                    checkKeyboardHeight(rootLayout);
                    enableFooterView();
                    checkConnection();
                }
                @Override
                public void onUpdateError() {

                }
            });
        } else {
            currUserId = UserProfile.getInstance().id;
            initViews();
            emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);
            popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);
            // Defining default height of keyboard which is equal to 230 dip
            final float popUpheight = 330;
            changeKeyboardHeight((int) popUpheight);
            enablePopUpView();
            checkKeyboardHeight(rootLayout);
            enableFooterView();
            checkConnection();
        }
    }

    @Override
	protected void onDestroy() {
        super.onDestroy();
        SocketWorker.getInstance().exitDialog(currDialogObject.dialogId);
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(messageReceiver);
	}

	public void BackClicked(View v) {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        finish();
	}

    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        messageEditText.setFilters(new InputFilter[] { messageLengthFilter });
	    imageAvatar = (ImageView) findViewById(R.id.imageAvatar);
	    textViewTop = (TextView) findViewById(R.id.textViewTop);
	    attachesLayout = (LinearLayout) findViewById(R.id.attachesLayout);
	    Button sendButton = (Button) findViewById(R.id.chatSendButton);
        chatMessages = new ArrayList<ChatMessage>();
        chatHistroy = new ArrayList<ChatMessage>();
        adapter = new ChatAdapter(this, chatMessages, Emoticon.getEmoticons());
        messagesContainer.setAdapter(adapter);
        messagesContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                popupWindow.dismiss();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastMsg = getChatMessage(messageEditText);
                JSONArray array = null;
                if (attachesArray.size() > 0) {
                    array = new JSONArray();
                    for (int i = 0; i < attachesArray.size(); i++) {
                        ChatAttachView attach = attachesArray.get(i);
                        if (attach.isUploaded == false) {
                            preloader.cancel(getString(R.string.please_wait), getString(R.string.wait_for_load));
                            return;
                        }
                        array.put(attach.mediaUploadResponse.uploadInfo);
                    }
                }
                if (lastMsg.equals("") && array == null) {
                    return;
                }
                if (getChatMessage(messageEditText).length() <= maxSymbolsInMessage) {
                    messageEditText.setText("");
                    if (attachesLayout.getChildCount() > 0) {
                        attachesLayout.removeAllViews();
                    }
//                    Log.w(TAG, "userId is "+currUserId);
                    SocketWorker.getInstance().sendMessageToChat(currDialogObject.dialogId, lastMsg, array, null, new IOAcknowledge() {
                        @Override
                        public void ack(Object... args) {
                            JSONObject ackJson = (JSONObject) args[0];
                            if (GlobalHelper.successStatusFromJson(ackJson, true, null)) {
                            }
                        }
                    });
                    attachesArray.clear();
                } else {
                    preloader.cancel(getString(R.string.error), getString(R.string.msg_size_max_reached));
                }
            }
        });

        messagesContainer.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem == 0) {
                    if(loadHistory()) {
                        messagesContainer.setSelection(10);
                    }
                }
            }
        });
    }

    private void checkConnection() {
        if(SocketWorker.getInstance().isConnected) {
            updateHistory();
        } else {
            preloader.launch();
            final Scheduler.Worker worker;
            worker = AndroidSchedulers.mainThread().createWorker();
            worker.schedule(new Action0() {
                @Override
                public void call() {
                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            worker.unsubscribe();
                            ChatActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SocketWorker.getInstance().setSocketCallBackAdapter(null);
                                    preloader.done();
                                    showNetWorkAlertDialog();
                                    Log.w(TAG, "STOP PRELOADER");
                                }
                            });
                        }
                    }, 10000);
                }
            });
            SocketWorker.getInstance().setSocketCallBackAdapter(new SocketThreadCallbackAdapter() {
                @Override
                public void callback(JSONArray data) throws JSONException {

                }

                @Override
                public void on(String event, JSONObject data) {

                }

                @Override
                public void onMessage(String message) {

                }

                @Override
                public void onMessage(JSONObject json) {

                }

                @Override
                public void onConnect() {
                    updateHistory();
                }

                @Override
                public void onDisconnect() {
                    checkConnection();
                }

                @Override
                public void onConnectFailure() {
                    showNetWorkAlertDialog();
                }
            });
        }
    }

    private void showNetWorkAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.message_no_connect_to_server);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

	//========== ========== ========== ========== ========== ========== ========== ========== ==========

	private void updateHistory() {
        if(isHistoryUpdated == false) {
            preloader.launch();
            final Timer networkTimeoutTimer = new Timer();
            networkTimeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            preloader.done();
                            showNetWorkAlertDialog();
                        }
                    });
                }
            }, 15000);
            textViewTop.setText(currDialogObject.name);
            ImageLoader.getInstance().displayImage(currDialogObject.avatarUrl, imageAvatar);
            int dialogId = currDialogObject.dialogId;
            SocketWorker.getInstance().getDialogHistory(dialogId, new IOAcknowledge() {
                @Override
                public void ack(Object... args) {
                    networkTimeoutTimer.cancel();
                    final JSONObject ackJson = (JSONObject) args[0];
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (GlobalHelper.successStatusFromJson(ackJson, true, null)) {
                                parseHistory(ackJson);

                            } else {
                                networkTimeoutTimer.cancel();
                                preloader.cancel(getString(R.string.error), getString(R.string.message_network_error));
                            }
                        }
                    });
                }
            });
        }
	}

	private void parseHistory(JSONObject json) {
        if(isHistoryUpdated == false) {
            chatHistroy.clear();
            chatMessages.clear();
            JSONArray messages = json.optJSONArray("data");
            for (int i = 0; i < messages.length(); i++) {
                JSONObject mes = messages.optJSONObject(i);
                ChatMessage messageObject = new ChatMessage(mes);
                messageObject.updateIsIncomingBySelfId(currUserId);
                chatHistroy.add(messageObject);
            }
            if(chatHistroy.size() != 0) {
                loadHistory();
                setMessages();
            }
            isHistoryUpdated = true;
        }
        preloader.done();
	}

	//========== ========== ========== ========== ========== ========== ========== ========== ==========

	private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("message")) {
                ChatMessage message = (ChatMessage) intent.getSerializableExtra("message");
                if(message != null) {
                    message.updateIsIncomingBySelfId(currUserId);
                    addMessage(message);
                }
            }
		}
	};

	//========== ========== ========== ========== ========== ========== ========== ========== ==========

    public void addMessage(ChatMessage message) {
        Log.w(TAG, "addMessage to chat");
        chatMessages.add(message);
        adapter.notifyDataSetChanged();
    }

    public void setMessages() {
        adapter.notifyDataSetChanged();
        preloader.done();
    }
    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    public void SmileButtonClick(View v) {
		    PopupMenu popup = new PopupMenu(this, v);
		    MenuInflater inflater = popup.getMenuInflater();
		    inflater.inflate(R.menu.chat_attach_mode, popup.getMenu());
		    popup.setOnMenuItemClickListener(this);
		    popup.show();
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.chat_attach_smile: {
				if (!popupWindow.isShowing()) {
					popupWindow.setHeight((int) (keyboardHeight));
					if (isKeyBoardVisible) {
						emoticonsCover.setVisibility(LinearLayout.GONE);
					} else {
						emoticonsCover.setVisibility(LinearLayout.VISIBLE);
					}
					popupWindow.showAtLocation(rootLayout, Gravity.BOTTOM, 0, 0);
				} else {
					popupWindow.dismiss();
				}
			}   break;
			case R.id.chat_attach_image_from_camera:
                capturePhotoFromCamera();
                break;
			case R.id.chat_attach_image_from_gallery:
                capturePhotoFromGallery();
				break;
            case R.id.chat_attach_video_from_camera:
                captureVideoFromCamera();
                break;
            case R.id.chat_attach_video_from_gallery:
                captureVideoFromGallery();
                break;
			default:
				return false;
		}
        return true;
	}

    /**
     * Defining all components of emoticons keyboard
     */
    private void enablePopUpView() {
        ViewPager pager = (ViewPager) popUpView.findViewById(R.id.emoticons_pager);
        pager.setOffscreenPageLimit(3);
        ArrayList<String> paths = new ArrayList<String>();
        for (short i = 1; i <= NO_OF_EMOTICONS; i++) {
            paths.add(i + ".png");
        }
        EmoticonsPagerAdapter adapter = new EmoticonsPagerAdapter(ChatActivity.this, Emoticon.getEmoticons(), this);
        pager.setAdapter(adapter);
        // Creating a pop window for emoticons keyboard
        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                (int) keyboardHeight, false);
        ImageButton backSpace = (ImageButton) popUpView.findViewById(R.id.back);
        backSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageEditText.getText().length()>0) {
                    KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    messageEditText.dispatchKeyEvent(event);
                } else {
                    popupWindow.dismiss();
                }
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                emoticonsCover.setVisibility(LinearLayout.GONE);
            }
        });
    }

    @Override
    public void keyClickedIndex(final Emoticon emoticon) {
        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            public Drawable getDrawable(String source) {
                return emoticon.drawable;
            }
        };
        Spanned cs = Html.fromHtml("<img src ='"+ emoticon.url +"'/>", imageGetter, null);
        int cursorPosition = messageEditText.getSelectionStart();
        messageEditText.getText().insert(cursorPosition, cs);
    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height
     *            minimum height by which we can make sure actual keyboard is
     *            open or not
     */
    private void changeKeyboardHeight(int height) {
        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            emoticonsCover.setLayoutParams(params);
        }
    }

    /**
     * Checking keyboard height and keyboard visibility
     */
    int previousHeightDiffrence = 0;
    private void checkKeyboardHeight(final View parentLayout) {
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);
                        int screenHeight = parentLayout.getRootView()
                                .getHeight();
                        int heightDifference = screenHeight - (r.bottom);
                        if (previousHeightDiffrence - heightDifference > 50) {
                            popupWindow.dismiss();
                        }
                        previousHeightDiffrence = heightDifference;
                        if (heightDifference > 100) {
                            isKeyBoardVisible = true;
                            changeKeyboardHeight(heightDifference);
                        } else {
                            isKeyBoardVisible = false;
                        }
                    }
                });
    }

    /**
     * Enabling all content in footer i.e. post window
     */
    private void enableFooterView() {
        messageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
    }

    private String getChatMessage(EditText et) {
        if(et.getText().toString().equals(" "))
            return " ";
        return getChatMessage(Html.toHtml(et.getText()));
    }

    private String getChatMessage(String htmlText) {
        if(htmlText.equals(" "))
            return " ";
        htmlText = htmlText.replace("\n", "");
        if (htmlText.length() < 1)
            return "";
        Pattern p = Pattern.compile("<img src=\"(.*?)\">");
        Matcher m = p.matcher(htmlText);
        StringBuffer sb = new StringBuffer(htmlText.length());
        while(m.find()) {
            String url = m.group(1);
            Emoticon emoticon = Emoticon.findEmoticonWithUrl(url);
            if(emoticon != null) {
                m.appendReplacement(sb, Matcher.quoteReplacement(emoticon.text));
            }
        }
        m.appendTail(sb);
        String text = sb.toString();
        text = Html.fromHtml(text).toString();
        if(text.length() > 1) {
            text = text.substring(0, text.length() - 2).replace("\n", "<br>");
        }
        return text;
    }

    public void openProfile(View view) {
        Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
        intent.putExtra("id", currDialogObject.id);
        startActivity(intent);
    }

	//========== ========== ========== ========== ========== ========== ========== ========== ==========

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_IMAGE_FROM_CAMERA):
                    addAttach(mFile, ChatMessageAttachment.ChatMessageAttachmentType_image);
                    break;
                case (REQUEST_IMAGE_FROM_GALLERY):
                    addAttach(new File(getRealPathFromURI(data.getData())), ChatMessageAttachment.ChatMessageAttachmentType_image);
                    break;
                case (REQUEST_VIDEO_FROM_CAMERA):
                   addAttach(mFile, ChatMessageAttachment.ChatMessageAttachmentType_video);
                    break;
                case (REQUEST_VIDEO_FROM_GALLERY):
                    addAttach(new File(getRealPathFromURI(data.getData())), ChatMessageAttachment.ChatMessageAttachmentType_video);
                    break;
                default:
                    return;
            }
        }
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }
	private void addAttach(File file, int type) {
		Uri iconUri = null;
		if (type == ChatMessageAttachment.ChatMessageAttachmentType_image) {
			iconUri = Uri.fromFile(file);
        }
	    if (attachesArray.size() < 3) {
		    final ChatAttachView attach = ChatAttachView.createChatAttachView(this);
            attach.currentFile = file;
            attachesLayout.addView(attach, attach.relParam);
            attachesArray.add(attach);
            attach.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Look2meetApi.cancel(context);
                    attachesLayout.removeView(attach);
                    attachesArray.remove(attach);
                }
            });
            if (iconUri != null) {
                attach.imageView.setImageBitmap(BitmapResizer.scale(this, attach.currentFile));
            }
            attach.startUpload(file);
	    }
	}

    private InputFilter messageLengthFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            int msgSize = getChatMessage(messageEditText).length();
            msgSize += ((end - start) - (dend - dstart));
            if( msgSize > maxSymbolsInMessage - 1) {
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_PROP_BEEP);
            }
            return null;
        }
    };

    boolean loadHistory() {
        int chatHistorySize = chatHistroy.size();
        if(chatHistorySize == 0) {
            return false;
        }
        if(chatHistorySize > 10) {
            chatHistorySize = 10;
        }
        for(int i = 0; i < chatHistorySize; i++) {
            int lastMessageIndex = chatHistroy.size() - 1;
            chatMessages.add(0, chatHistroy.get(lastMessageIndex));
            chatHistroy.remove(lastMessageIndex);
        }
        return true;
    };

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        YandexMetrica.onResumeActivity(this);
        checkConnection();
    }

    @Override
    public void onPause() {
        super.onPause();
        YandexMetrica.onPauseActivity(this);
    }
}

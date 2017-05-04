package com.zuzex.look2meet.api;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;

import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.Log.SystemInfo;
import com.zuzex.look2meet.Look2meet;
import com.zuzex.look2meet.R;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by romanabashin on 14.07.14.
 */
public class GlobalHelper {

	static public boolean successStatusFromJson(JSONObject jsonObject, boolean isShowAlert, Context atContext) {
		if (jsonObject == null) {
			GlobalHelper.showAlert("Json Error", atContext);
			return false;
		}
		if (jsonObject.optBoolean("success") == true)
			return true;
		if (jsonObject.optInt("success") != 0)
			return true;
		if (jsonObject.optString("status").equals("success"))
			return true;

		if (isShowAlert) {
			String message = jsonObject.optString("message");
//			Log.wtf("GlobalHelper", "successStatusFromJson "+message);

			GlobalHelper.showAlert(message, atContext);
		}
		return false;
	}

    static public boolean successStatusFromJson(JSONObject jsonObject) {
        return  successStatusFromJson(jsonObject, false, Look2meet.getContext());
    }

	public static void showAlert(String message, Context atContext) {
		if (atContext != null) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(atContext);
			builder.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
							dialog.dismiss();
						}
					});
			final AlertDialog alert = builder.create();
			alert.show();
		} else {
			Context sharedContext = Look2meet.getContext();
			Intent popupIntent = new Intent(sharedContext, PopupActivity.class);
			if (message.length() > 0)
				popupIntent.putExtra("text", message);
			popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			sharedContext.startActivity(popupIntent);
		}
	}

//	public static void showActionSheet(String message, ArrayList<String> buttons, Context atContext) {
//
//		final AlertDialog.Builder builder = new AlertDialog.Builder(atContext);
//		builder.setMessage(message);
//		builder.setCancelable(false);
////		ListAdapter listAdapter = new
//		builder.s()
//		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//			public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//				dialog.dismiss();
//			}
//		});
//		final AlertDialog alert = builder.create();
//		alert.show();
//	}


    public static String GetCheckinType(String localizedName) {
        Context ctx = Look2meet.getContext();
        if(localizedName.equals(ctx.getString(R.string.checkin_type_now))) {
            return "now";
        } else if(localizedName.equals(ctx.getString(R.string.checkin_type_plan))) {
            return "plan";
        } else if(localizedName.equals(ctx.getString(R.string.checkin_type_soon))) {
            return "soon";
        } else if(localizedName.equals(ctx.getString(R.string.checkin_type_all))) {
            return "all";
        }

        return "";
    }

    public static String GetLocalizedCheckinType(String ct) {
        Context ctx = Look2meet.getContext();
        if(ct.equals("now")) {
            return ctx.getString(R.string.checkin_type_now);
        } else if(ct.equals("plan")) {
            return ctx.getString(R.string.checkin_type_plan);
        } else if(ct.equals("soon")) {
            return ctx.getString(R.string.checkin_type_soon);
        } else if(ct.equals("all")) {
            return ctx.getString(R.string.checkin_type_all);
        }

        return "";
    }
    public static void loggSend(String message) {
        SystemInfo info = new SystemInfo(UserProfile.getInstance().id,message);
//        Logging logging = new Logging("look2meet-Android-JSON_EXCEPTION", info.getString());
//        logging.SendMessage();
    }
	public static String urlForImageByGuid(String guid, String ext) {
		if (guid == null || guid.length() < 4)
			return "";
		// TODO: replace with shared host
		String DefApiHost = "http://look2meet.com/";

		String result = DefApiHost+"uploads/img/";
		result = result + guid.substring(0,2) +"/"+ guid.substring(2,4) +"/"+ guid +"/"+ guid +"."+ ext;
		return result;
	}

	public static String urlForVideoByGuid(String guid, String ext) {
		if (guid == null || guid.length() < 4)
			return "";
		// TODO: replace with shared host
		String DefApiHost = "http://look2meet.com/";

		String result = DefApiHost+"uploads/video/";
		result = result + guid.substring(0,2) +"/"+ guid.substring(2,4) +"/"+ guid +"/"+ guid +"."+ ext;
		return result;
	}

	public static String urlForVideoThumbnailByGuid(String guid) {
		if (guid == null || guid.length() < 4)
			return "";
		// TODO: replace with shared host
		String DefApiHost = "http://look2meet.com/";

		String result = DefApiHost+"uploads/video/";
		result = result + guid.substring(0,2) +"/"+ guid.substring(2,4) +"/"+ guid + "/thumbnail.png";
		return result;
	}

	public static String urlForGiftByPath(String path) {
		String DefApiHost = "http://look2meet.com/";
		return DefApiHost + path;
	}

//	private static String

	public static File createTempFile(String fileName, String fileExt) {
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = null;
		try {
			image = File.createTempFile(fileName, fileExt, storageDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

	public static String getPathFromFile(File file) {
		return "file:" + file.getAbsolutePath();
	}

    public static String getGender(String localizedGender) {
        Context ctx = Look2meet.getContext();
        if(localizedGender.equals(ctx.getString(R.string.gender_type_male))) {
            return "male";
        } else if(localizedGender.equals(ctx.getString(R.string.gender_type_female))) {
            return "female";
        } else {
            return "";
        }
    }

    public static  String getLocalizedGender(String gender) {
        Context ctx = Look2meet.getContext();
        if(gender.equals("male")) {
            return ctx.getString(R.string.gender_type_male);
        } else if(gender.equals("female")) {
            return ctx.getString(R.string.gender_type_female);
        } else if(gender.equals("all")) {
            return ctx.getString(R.string.gender_type_all);
        } else {
            return ctx.getString(R.string.gender_type_all);
        }
    }

    public static int getGenderIndex(String gender) {
        if(gender.equals("male")) {
            return 1;
        } else if(gender.equals("female")) {
            return 2;
        }
        return 0;
    }
}

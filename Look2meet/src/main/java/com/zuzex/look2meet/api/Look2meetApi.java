package com.zuzex.look2meet.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.zuzex.look2meet.DataModel.IUpdateStatus;
import com.zuzex.look2meet.DataModel.MediaUploadResponse;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.Look2meet;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.apiData.LoginRequest;
import com.zuzex.look2meet.utils.AnimationPreloader;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

public class Look2meetApi {
    private static final String TAG = "Look2meetApi";
    private volatile static Look2meetApi instance;

	//   private final static String HOST = "http://look2meet.zuzex.org/";
    //   private final static String HOST = "http://edanko.l2m/";
	public final static String HOST = "http://look2meet.com/";
    private static AsyncHttpClient client = new AsyncHttpClient();

	private static String apiToken = "wrong_apiToken";
    public String getApiToken() {
        return apiToken;
    }
	public void setApiToken(String token) {
		this.apiToken = token;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Look2meet.getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("apiTokenBackup", token);
        editor.commit();
	}

	//========== ========== ========== ========== ========== ========== ========== ========== ========== ==========

	public static Look2meetApi getInstance() {
		if (instance == null) {
			synchronized (Look2meetApi.class) {
				if (instance == null) {
                    client = createAsyncClient();
                    instance = new Look2meetApi();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Look2meet.getContext());
                    apiToken = preferences.getString("apiTokenBackup", "");
                }
			}
		}
		return instance;
	}

    private static AsyncHttpClient createAsyncClient() {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setUserAgent("look2meet/Android");
        asyncHttpClient.setMaxRetriesAndTimeout(10, 2000);
        asyncHttpClient.setConnectTimeout(20000);
        asyncHttpClient.setResponseTimeout(20000);
        asyncHttpClient.setTimeout(20000);
        return asyncHttpClient;
    }

    public static void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("apiToken", Look2meetApi.getInstance().getApiToken());
        client.post(context, HOST+url, params, responseHandler);
    }

    public static void post(String url, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("apiToken", apiToken);
        client.post(null, HOST+url, null, responseHandler);
    }

    public static void cancel(Context context) {
        client.cancelRequests(context, true);
    }

	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if(netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		else {
			Context sharedContext = Look2meet.getContext();
			Intent popupIntent = new Intent(sharedContext, PopupActivity.class);
			String message = sharedContext.getString(R.string.api_network_disabled);
			popupIntent.putExtra("text",message);
			popupIntent.putExtra("isOkMode",false);
			popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			sharedContext.startActivity(popupIntent);
		}
		return false;
	}

	//========== ========== ========== ========== ========== ========== ========== ========== ========== ==========

	public void login(LoginRequest request, AsyncHttpResponseHandler responseHandler) {
		final String apiPath = "api/login";
        RequestParams params = new RequestParams();
        params.add("email", request.getEmail());
        params.add("password", request.getPassword());
        params.add("phone", request.getPhone());
        params.add("remember", request.getRemember());
		post(null, apiPath, params, responseHandler);
	}

    public void registration(RequestParams params, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/user/add";
        post(null, apiPath, null, jsonHttpResponseHandler);
    }

	//========== ========== ========== ========== ========== ========== ========== ========== ========== ==========
    public void getGiftList(Integer page, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/gifts/list";
        RequestParams params = new RequestParams();
        params.add("page",page.toString());
        params.add("items_per_page","100");
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getBlackList(Integer page, Integer itemsPerPage, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/blackLists/list";
        RequestParams params = new RequestParams();
        params.add("page", page.toString());
        params.add("items_per_page", itemsPerPage.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getWhiteList(Integer page, Integer itemsPerPage, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/whiteLists/list";
        RequestParams params = new RequestParams();
        params.add("page", page.toString());
        params.add("items_per_page", itemsPerPage.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getMaskList(Integer page, Integer itemsPerPage, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/avatarWhiteLists/list";
        RequestParams params = new RequestParams();
        params.add("page", page.toString());
        params.add("items_per_page", itemsPerPage.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getTempObjects(JsonHttpResponseHandler responseHandler) {
        final String apiPath = "api/objects/getMyObjects";
        post(null, apiPath, null, responseHandler);
    }
    public void getDialogs(int page, int itemsPerPage, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        RequestParams params = new RequestParams();
        final String apiPath = "api/dialogs/list";
        params.add("page", String.valueOf(page));
        params.add("items_per_page", String.valueOf(itemsPerPage));
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
	public void addDialogsWithUser(Integer userId, JsonHttpResponseHandler jsonHttpResponseHandler)
	{
		final String apiPath = "api/dialog/add";
        RequestParams params = new RequestParams();
		params.add("id", userId.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
	}
    public void deleteFromBlackList(Integer id, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/blackList/delete";
        RequestParams params = new RequestParams();
        params.add("id",id.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void addToBlackList(Integer id, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/blackList/delete";
        RequestParams params = new RequestParams();
        params.add("id",id.toString());
        post(null, apiPath, params,jsonHttpResponseHandler);
    }
    public void deleteFromWhiteList(Integer id, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/whiteList/delete";
        RequestParams params = new RequestParams();
        params.add("id",id.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void deleteFromMaskList(Integer id, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/avatarWhiteList/delete";
        RequestParams params = new RequestParams();
        params.add("id",id.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getObjectView(int id, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/object/view";
        RequestParams params = new RequestParams();
        params.add("id", String.valueOf(id));
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getFavoritesObject(Integer page, Integer itemsPerPage, String sortedBy, String order, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/Objects/getFavoriteObjects";
        RequestParams params = new RequestParams();
        params.add("page", page.toString());
        params.add("items_per_page",itemsPerPage.toString());
        params.add("sort", sortedBy);
        params.add("order", order);
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getUserProfile(JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/userProfile/get";
//        r.checkLoginExpired = true;
        post(apiPath, jsonHttpResponseHandler);
    }
    public void getUsersProfileByID(Integer id, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/user/get";
        RequestParams params = new RequestParams();
        params.add("id",id.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getCurrentCountPeoples(JsonHttpResponseHandler jsonResponseHandler) {
        final String apiPath = "api/users/current";
        post(null, apiPath, null, jsonResponseHandler);
    }

    public void deleteFriend(Integer id,JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/friend/delete";
        RequestParams params = new RequestParams();
        params.add("id",id.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void addFriend(Integer id,JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/friend/add";
        RequestParams params = new RequestParams();
        params.add("id",id.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void getObjects(RequestParams params, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/objects/search";
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void getProfiles(JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/userProfile/list";
        RequestParams params = new RequestParams();
        params.add("apiToken", apiToken);
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void setActiveProfile(Integer id, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/userProfile/set";
        RequestParams params = new RequestParams();
        params.add("id", String.valueOf(id));
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void setLike(String guid, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/like/set";
        RequestParams params = new RequestParams();
        params.add("guid", guid);
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void setFavorites(Integer id, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/user/favorite";
        RequestParams params = new RequestParams();
        params.add("id", String.valueOf(id));
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getUsersList(Integer page, Integer itemsPerPage, Integer ageFrom, Integer ageTo, String sex, String status, Integer id, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/object/searchUsers";
        RequestParams params = new RequestParams();
        params.add("object_id",id.toString());
        params.add("page", page.toString());
        params.add("items_per_page", itemsPerPage.toString());

        if(!sex.isEmpty())
            params.add("filter[sex]", sex);
        if(!status.isEmpty())
            params.add("filter[status]",status);
        if(ageFrom != 0)
            params.add("filter[age_from]",ageFrom.toString());
        if(ageTo != 0)
            params.add("filter[age_to]",ageTo.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
        
    }
    public void getFriendsList(Integer page, Integer itemsPerPage, String name, JsonHttpResponseHandler jsonHttpResponseHandler)
    {
        final String apiPath = "api/friends/list";
        RequestParams params = new RequestParams();
        params.add("page", page.toString());
        params.add("items_per_page", itemsPerPage.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }
    public void getCheckinsList(Integer page, Integer itemPerPage, Boolean showTotalFlag, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/checkins/list";
        RequestParams params = new RequestParams();
        params.add("page", String.valueOf(page));
        params.add("items_per_page", String.valueOf(itemPerPage));
        params.add("get_total", Boolean.toString(showTotalFlag));
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void searchCountries(String filter, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/countries/list";
        RequestParams params = new RequestParams();
        params.add("filter", filter);
        post(null, apiPath, params, jsonHttpResponseHandler);
        
    }

    public void searchCities(String countryId, String filter, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/cities/list";
        RequestParams params = new RequestParams();
        params.add("country_id", countryId);
        params.add("filter", filter);
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void getCategoriesList(JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/categories/list";
        RequestParams params = new RequestParams();
        params.add("", "");
        post(null, apiPath, params, jsonHttpResponseHandler);
        
    }

    public void searchObjects(Integer page, Integer itemsPerPage, RequestParams params, String sort, String order, boolean isFilterSet, double lat, double lon, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/objects/search";
        params.add("page", page.toString());
        params.add("items_per_page", itemsPerPage.toString());
        params.add("sort", sort);
        params.add("order", order);
        params.add("userCoords[lat]", Double.toString(lat));
        params.add("userCoords[lon]", Double.toString(lon));
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void getAnnounces(RequestParams params, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/objects/anons";
        //todo fix filter
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void getTop(double lat, double lon, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/objects/top";
        if(lat == 0 || lon == 0) {
            post(null, apiPath, null, jsonHttpResponseHandler);
        } else {
            RequestParams params = new RequestParams();
            params.add("latitude", Double.toString(lat));
            params.add("longitude", Double.toString(lon));
            post(null, apiPath, params, jsonHttpResponseHandler);
        }
    }

    public void updateProfile(UserProfile profile, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/userProfile/update";
        post(null, apiPath, profile.get(), jsonHttpResponseHandler);
    }

	//========== ========== ========== ========== ========== ========== ========== ========== ========== ==========

    public void addVideoToGallery(final Context context, File file, final AnimationPreloader preloader, final  IUpdateStatus updateStatus) {
        uploadMedia(context, file, preloader, updateStatus);
    }

    public void addPhotoToGallery(final Context context, File file, final AnimationPreloader preloader, final  IUpdateStatus updateStatus) {
        uploadMedia(context, file, preloader, updateStatus);
    }

    private void uploadMedia(final Context context, File file,final AnimationPreloader preloader, final IUpdateStatus updateStatus) {
        preloader.launch();
        uploadFile(context, file, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                MediaUploadResponse r = null;
                r = new MediaUploadResponse(response);
                UserProfile.getInstance().addFile(r.id);
                updateProfile(UserProfile.getInstance(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        preloader.done();
                        UserProfile.getInstance().reload(updateStatus);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        preloader.cancel(context.getString(R.string.error), context.getString(R.string.message_network_error));
                        updateStatus.onUpdateError();
                    }

                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        preloader.cancel(context.getString(R.string.error), context.getString(R.string.message_network_error));
                        updateStatus.onUpdateError();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                preloader.cancel(context.getString(R.string.error), context.getString(R.string.message_network_error));
                updateStatus.onUpdateError();
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(context.getString(R.string.error), context.getString(R.string.message_network_error));
                updateStatus.onUpdateError();
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                int progress = (int)((bytesWritten * 1.0 / totalSize) * 100);
                preloader.setProgress(progress);
            }
        });
    }

    public void uploadFile(Context context, File file, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/file/upload";
        RequestParams params = new RequestParams();
        params.add("source", "user");
        try {
            params.put("file", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        post(null, apiPath, params, jsonHttpResponseHandler);
    }


    public void upload2(Context context, File file, JsonHttpResponseHandler jsonResponseHandler) {
        RequestParams params = new RequestParams();
        try {
            params.put("file", file);
            params.put("apiToken", getApiToken());
            params.put("source", file.getName());
        } catch(FileNotFoundException e) {}
        post(context, "api/file/upload", params, jsonResponseHandler);
    }

	//========== ========== ========== ========== ========== ========== ========== ========== ========== ==========

    public void setCheckin(String checkinType, Integer objectId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/checkin/add";
        RequestParams params = new RequestParams();
        params.add("type", checkinType);
        params.add("object_id", objectId.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void deleteCheckin(Integer objectId, JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/checkin/delete";
        RequestParams params = new RequestParams();
        params.add("object_id", objectId.toString());
        post(null, apiPath, params, jsonHttpResponseHandler);
    }

    public void setRegistrationId(final Context context, String id, final JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/android/setdeviceid";
        manageRegistration(context, apiPath, id, jsonHttpResponseHandler);
    }
    public void deleteRegistrationId(final Context context, final String id, final JsonHttpResponseHandler jsonHttpResponseHandler) {
        final String apiPath = "api/android/deletedeviceid";
        manageRegistration(context, apiPath, id, jsonHttpResponseHandler);
    }

    public void manageRegistration(final Context context, final String apiPath, String id, final JsonHttpResponseHandler jsonHttpResponseHandler) {
        final RequestParams params = new RequestParams();
        params.add("token", id);
        final SyncHttpClient syncClient = new SyncHttpClient();
        syncClient.setUserAgent("look2meet/Android");
        syncClient.addHeader("apiToken", Look2meetApi.getInstance().getApiToken());
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncClient.post(context, HOST + apiPath, params, jsonHttpResponseHandler);
            }
        }).start();
    }

}


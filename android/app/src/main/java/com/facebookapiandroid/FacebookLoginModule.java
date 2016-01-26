package com.facebookapiandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scgough on 22/01/2016.
 */
public class FacebookLoginModule extends ReactContextBaseJavaModule {

    private final String REACT_CLASS = "FacebookLoginModule";

    private final String CALLBACK_TYPE_SUCCESS = "success";
    private final String CALLBACK_TYPE_ERROR = "error";
    private final String CALLBACK_TYPE_CANCEL = "cancel";

    private Context mActivityContext;
    private CallbackManager mCallbackManager;
    private Callback mTokenCallback;
    private Callback mLogoutCallback;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public FacebookLoginModule(ReactApplicationContext reactContext, Context activityContext) {
        super(reactContext);

        mActivityContext = activityContext;

        FacebookSdk.sdkInitialize(mActivityContext.getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        //--------------------------------------------------------------------------------
        //register the manager for the login
        //--------------------------------------------------------------------------------
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        if (loginResult.getRecentlyGrantedPermissions().contains("email")) {

                            Log.d(REACT_CLASS,"Callback - Success");

                            GraphRequest request = GraphRequest.newMeRequest(
                                    loginResult.getAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject me, GraphResponse response) {
                                            if (mTokenCallback != null) {
                                                FacebookRequestError error = response.getError();

                                                if (error != null) {
                                                    WritableMap map = Arguments.createMap();

                                                    map.putString("errorType", error.getErrorType());
                                                    map.putString("message", error.getErrorMessage());
                                                    map.putString("recoveryMessage", error.getErrorRecoveryMessage());
                                                    map.putString("userMessage", error.getErrorUserMessage());
                                                    map.putString("userTitle", error.getErrorUserTitle());
                                                    map.putInt("code", error.getErrorCode());
                                                    map.putString("eventName", "onError");

                                                    consumeCallback(CALLBACK_TYPE_ERROR, map);
                                                } else {
                                                    WritableMap map = Arguments.createMap();

                                                    map.putString("token", loginResult.getAccessToken().getToken());
                                                    map.putString("expiration", String.valueOf(loginResult.getAccessToken().getExpires()));

                                                    //TODO: figure out a way to return profile as WriteableMap
                                                    //    OR: expose method to get current profile
                                                    map.putString("profile", me.toString());
                                                    map.putString("eventName", "onLogin");

                                                    consumeCallback(CALLBACK_TYPE_SUCCESS, map);
                                                }
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            String fields = "id,name,email,first_name,last_name," +
                                    "age_range,link,picture,gender,locale,timezone," +
                                    "updated_time,verified";
                            parameters.putString("fields", fields);
                            request.setParameters(parameters);
                            request.executeAsync();
                        } else {
                            handleInsufficientPermissions("Insufficient permissions", "onPermissionsMissing", CALLBACK_TYPE_ERROR);
                        }
                    }

                    @Override
                    public void onCancel() {

                        Log.d(REACT_CLASS,"Callback - Cancel");

                        if (mTokenCallback != null) {
                            WritableMap map = Arguments.createMap();
                            map.putString("message", "FacebookCallback onCancel event triggered");
                            map.putString("eventName", "onCancel");
                            consumeCallback(CALLBACK_TYPE_CANCEL, map);
                        }
                    }

                    @Override
                    public void onError(FacebookException exception) {

                        Log.d(REACT_CLASS,"Callback - Error");

                        if (mTokenCallback != null) {
                            WritableMap map = Arguments.createMap();

                            map.putString("message", exception.getMessage());
                            map.putString("eventName", "onError");

                            consumeCallback(CALLBACK_TYPE_ERROR, map);
                        }
                    }
                });
        //--------------------------------------------------------------------------------
    }

    private void handleInsufficientPermissions(String value, String onPermissionsMissing, String callbackType) {
        WritableMap map = Arguments.createMap();

        map.putString("message", value);
        map.putString("eventName", onPermissionsMissing);

        consumeCallback(callbackType, map);
    }

    private void consumeCallback(String type, WritableMap map) {
        if (mTokenCallback != null) {
            map.putString("type", type);
            map.putString("provider", "facebook");

            if(type == CALLBACK_TYPE_SUCCESS){
                mTokenCallback.invoke(null, map);
            }else{
                mTokenCallback.invoke(map, null);
            }

            mTokenCallback = null;
        }
    }

    @ReactMethod
    public void loginWithReadPermissions(ReadableArray permissions, final Callback callback) {
        if (mTokenCallback != null) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();

            WritableMap map = Arguments.createMap();

            if (accessToken != null) {
                map.putString("token", AccessToken.getCurrentAccessToken().getToken());
                map.putString("eventName", "onLoginFound");
                map.putBoolean("cache", true);

                consumeCallback(CALLBACK_TYPE_SUCCESS, map);
            } else {
                map.putString("message", "Cannot register multiple callbacks");
                map.putString("eventName", "onCancel");
                consumeCallback(CALLBACK_TYPE_CANCEL, map);
            }
        }

        mTokenCallback = callback;

        List<String> _permissions = getPermissions(permissions);
        if(_permissions != null && _permissions.size() > 0 && _permissions.contains("email")){
            Log.i(REACT_CLASS, "Using: " + _permissions.toString());

            LoginManager.getInstance().logInWithReadPermissions(
                    (Activity) mActivityContext, _permissions);
        } else {
            handleInsufficientPermissions("Insufficient permissions", "onPermissionsMissing", CALLBACK_TYPE_ERROR);
        }

    }

    @ReactMethod
    public void loginWithPublishPermissions(ReadableArray permissions, final Callback callback) {
        if (mTokenCallback != null) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();

            WritableMap map = Arguments.createMap();

            if (accessToken != null) {
                map.putString("token", AccessToken.getCurrentAccessToken().getToken());
                map.putString("eventName", "onLoginFound");
                map.putBoolean("cache", true);

                consumeCallback(CALLBACK_TYPE_SUCCESS, map);
            } else {
                map.putString("message", "Cannot register multiple callbacks");
                map.putString("eventName", "onCancel");
                consumeCallback(CALLBACK_TYPE_CANCEL, map);
            }
        }

        mTokenCallback = callback;

        List<String> _permissions = getPermissions(permissions);
        if(_permissions != null && _permissions.size() > 0 && _permissions.contains("publish_actions")){
            Log.i("FBLoginPermissions", "Using: " + _permissions.toString());

            //publish_actions
            LoginManager.getInstance().logInWithPublishPermissions(
                    (Activity) mActivityContext, _permissions);
        }else{
            handleInsufficientPermissions("Insufficient permissions", "onPermissionsMissing", CALLBACK_TYPE_ERROR);
        }
    }

    @ReactMethod
    public void logout(final Callback callback) {
        WritableMap map = Arguments.createMap();

        mTokenCallback = callback;
        LoginManager.getInstance().logOut();

        map.putString("message", "Facebook Logout executed");
        map.putString("eventName", "onLogout");
        consumeCallback(CALLBACK_TYPE_SUCCESS, map);
    }

    @ReactMethod
    public void checkPermissions(String userID, final Callback callback) {

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if(accessToken != null){
            new GraphRequest(
                    accessToken,
                    "/" + userID + "/permissions",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            callback.invoke(response.getJSONObject().toString());
                        }
                    }
            ).executeAsync();
        }else{
            callback.invoke("");
        }
    }

    @ReactMethod
    public void getCurrentToken(final Callback callback) {
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        if(currentAccessToken != null){
            callback.invoke(currentAccessToken.getToken());
        }else{
            callback.invoke("");
        }
    }

    private List<String> getPermissions(ReadableArray permissions) {
        List<String> _permissions = new ArrayList<String>();
        if(permissions != null && permissions.size() > 0){
            for(int i = 0; i < permissions.size(); i++){
                if(permissions.getType(i).name() == "String"){
                    String permission = permissions.getString(i);
                    Log.i(REACT_CLASS, "adding permission: " + permission);
                    _permissions.add(permission);
                }
            }
        }

        return _permissions;
    }

    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        return mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
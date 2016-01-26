package com.facebookapiandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

/**
 * Created by scgough on 22/01/2016.
 */
public class FacebookGraphModule extends ReactContextBaseJavaModule {

    private final String REACT_CLASS = "FacebookGraphModule";

    private final String CALLBACK_SUCCESS = "success";
    private final String CALLBACK_ERROR = "error";

    private Context mActivityContext;
    private CallbackManager mCallbackManager;
    private Callback mCallback;
    private WritableMap mResults;

    public FacebookGraphModule(ReactApplicationContext reactContext, Context activityContext) {
        super(reactContext);

        mActivityContext = activityContext;

        FacebookSdk.sdkInitialize(mActivityContext.getApplicationContext());
    }

    private void consumeCallback(String type, WritableMap obj) {
        if(mCallback!=null) {
            obj.putString("type", type);

            mCallback.invoke(obj);
            mCallback = null;
        }
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }


    @ReactMethod
    public void search(String type, String url, String keyword, double lat, double lng, int distance, Callback callback) {

        if (mCallback == null) {
            mResults = Arguments.createMap();
        }

        mCallback = callback;

        Bundle _params = new Bundle();
        _params.putString("q",keyword);
        _params.putString("type",type);

        //hard-coded params for place lookup - need to alter this
        if(lat>=-90 && lat<=90 && lng>=-180 && lng<=180) {
            _params.putString("center", String.valueOf(lat) + "," + String.valueOf(lng));
        }
        if(distance>0) {
            _params.putInt("distance",distance);
        }

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null){
            new GraphRequest(
                    accessToken,
                    url,
                    _params,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {

                            JSONObject resp = response.getJSONObject();
                            if(resp.has("error")) {
                                try {
                                    mResults.putString("results", resp.toString());
                                }
                                catch(Exception e){}

                                consumeCallback(CALLBACK_ERROR,mResults);
                            }
                            else {
                                mResults.putString("results", response.getJSONObject().toString());

                                consumeCallback(CALLBACK_SUCCESS, mResults);
                            }
                        }
                    }
            ).executeAsync();
        } else {
            mResults.putString("message", "No Access");

            consumeCallback(CALLBACK_ERROR, mResults);
        }
    }

    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        return mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

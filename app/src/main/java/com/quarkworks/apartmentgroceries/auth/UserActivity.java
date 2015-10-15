package com.quarkworks.apartmentgroceries.auth;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quarkworks.apartmentgroceries.R;
import com.quarkworks.apartmentgroceries.main.UserCell;
import com.quarkworks.apartmentgroceries.service.SyncUser;
import com.quarkworks.apartmentgroceries.service.models.RUser;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = UserActivity.class.getSimpleName();

    private RealmResults<RUser> users;

    private String sessionToken;
    private TextView usernameTextView;
    private TextView phoneTextView;
    private EditText phoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);

        usernameTextView = (TextView) findViewById(R.id.user_activity_username_id);
        phoneTextView = (TextView) findViewById(R.id.user_activity_phone_id);
        phoneEditText = (EditText) findViewById(R.id.user_activity_edit_text_phone_id);

        SharedPreferences sharedPreferences = getSharedPreferences("login", 0);
        sessionToken = sharedPreferences.getString("sessionToken", null);
        Toast.makeText(this, "sessionToken:" + sessionToken, Toast.LENGTH_LONG).show();

        String username = sharedPreferences.getString("username", null);
        String phone = sharedPreferences.getString("phone", null);

        usernameTextView.setText(username);
        phoneTextView.setText(phone);


        SyncUser.getAllUser();

        Realm realm = Realm.getInstance(this);

        RealmQuery<RUser> query = realm.where(RUser.class);
        users = query.findAll();

        Log.d(TAG, "users0:" + users.get(0).getName());

        RealmBaseAdapter<RUser> realmBaseAdapter = new RealmBaseAdapter<RUser>(this, users, true) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                UserCell userCell = convertView != null ?
                        (UserCell) convertView : new UserCell(parent.getContext());
                userCell.setViewData(getItem(position));

                return userCell;
            }
        };

        ListView listView = (ListView) findViewById(R.id.user_list_view_id);
        listView.setAdapter(realmBaseAdapter);
    }

    /**
     * update user button onclick
     * @param view
     */
    public void updateUserButtonOnClick(View view) {
        String newPhone = phoneEditText.getText().toString();

        if(newPhone != null && !newPhone.isEmpty()) {
            if (sessionToken != null) {
                UpdateUserAsyncTask updateUserAsyncTask = new UpdateUserAsyncTask();
                updateUserAsyncTask.execute(sessionToken, newPhone);
            }
        } else {
            Toast.makeText(this, "please input a new phone number", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * update user AsyncTask
     * - only user owner can update their own profile
     * - this action need sessionToken which can be obtained when user login
     */
    private class UpdateUserAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String sessionToken = params[0];
            String phone = params[1];

            if (sessionToken == null) return null;

            // build request body, update phone number
            //String json = "{\"phone\":\"111-333-4444\"}";
            String json = "{\"phone\":\"" + phone + "\"}";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, json);

            OkHttpClient client;
            client = new OkHttpClient();

            String url = "https://api.parse.com/1/users/3gmi9l5mjn";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-Parse-Application-Id", "GlvuJjSGKTkc3DxedowpvgCMNOZeGQjxvRApSqGD")
                    .addHeader("X-Parse-REST-API-Key", "0b3HDSEgt3EgXxyDHLOV0M7yQZwsexVG8ryTqzKI")
                    .addHeader("X-Parse-Session-Token", sessionToken)
                    .addHeader("X-Parse-Revocable-Session", "1")
                    .method("PUT", requestBody)
                    .build();

            Response response = null;

            try {
                response = client.newCall(request).execute();
                Log.d(TAG, response.body().string());
                Toast.makeText(getApplicationContext(), "update success!", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.d(TAG, "Error: client.newCall failed");
            }

            return null;
        }

        protected void onPostExecute(String result) {
            // do something or not
        }
    }
}

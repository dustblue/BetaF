package com.example.rakesh.betaf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends Activity {
    String username;
    String password;
    EditText usernameText, passwordText;
    Button signIn;
    DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = new DBHandler(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login_screen);
        handleButtonClick();
    }

    private void handleButtonClick() {
        usernameText = (EditText) findViewById(R.id.rollNumber);
        passwordText = (EditText) findViewById(R.id.password);
        signIn = (Button) findViewById(R.id.signInButton);

        signIn.setOnClickListener(v -> {
                username = usernameText.getText().toString();
                if (username.length() != 9)
                    usernameText.setError("Invalid roll number");
                else {
                    password = passwordText.getText().toString();
                    //Passing username and password to the server
                    new authTask().execute();
                    signIn.setClickable(false);
                }
        });

        CheckBox checkBox = (CheckBox) findViewById(R.id.showPasswordCheckBox);
        checkBox.setOnCheckedChangeListener((buttonView,isChecked) -> {
                if (isChecked) {
                    passwordText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                if (!isChecked) {
                    passwordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
        });
    }

    private LoginInterface getInterfaceService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(/*BASE_URL*/)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(LoginInterface.class);
    }

    private void login(final String username, String password){
        LoginInterface mLoginService = this.getInterfaceService();
        Call<Login> mService = mLoginService.doAuth(username, password);
        mService.enqueue(new Callback<Login>() {
            @Override
            public void onResponse(Call<Login> call, Response<Login> response) {
                Login mLoginObject = response.body();
                String returnedResponse = mLoginObject.isLogin;
                //Toast.makeText(LoginActivity.this, "Returned " + returnedResponse, Toast.LENGTH_LONG).show();
                if(returnedResponse.trim().equals("1")){
                    // Go to Main Activity page
                    Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);

                    SharedPreferences.Editor editor = Utilities.prefs.edit();
                    editor.putInt("status", Utilities.status);
                    editor.putString("user_name", username);
                    Utilities.username = username;
                    editor.putString("user_pass", password);
                    Utilities.password = password;
                    editor.apply();
                    signIn.setClickable(false);

                    startActivity(loginIntent);
                }
                if(returnedResponse.trim().equals("0")){
                    Toast.makeText(LoginActivity.this, "Please check your Username and Password", Toast.LENGTH_LONG).show();
                    passwordText.requestFocus();
                }
            }
            @Override
            public void onFailure(Call<Login> call, Throwable t) {
                call.cancel();
                Toast.makeText(LoginActivity.this, "Please check your network connection", Toast.LENGTH_LONG).show();
                usernameText.setText("");
                passwordText.setText("");
                signIn.setClickable(true);
            }
        });
    }

    class authTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog = null;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            login(username, password);
            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            super.onPostExecute(error);
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}

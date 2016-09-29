package com.medavox.repeats.ui;

import java.util.regex.*;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.medavox.repeats.R;
import com.medavox.repeats.application.Application;
import com.medavox.repeats.datamodels.Authentication;
import com.medavox.repeats.network.NetworkController;

/**
 * A login screen that offers login via email/password.
 */
public class TrialRunnerLoginActivity extends AppCompatActivity {

    private static String TAG = "TrialRunnerLogin";

    /**
     * A dummy authentication store containing known user names and passwords.
     */
    private static final String DEMO_CREDENTIALS = "Adam.Howard@elucid-mHealth.com:lancaster.89";


    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText trialIDView;
    private EditText userIDView;
    private EditText[] editTexts;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial_runner_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        trialIDView = (EditText) findViewById(R.id.trial_id);
        userIDView = (EditText) findViewById(R.id.user_id);
        editTexts  = new EditText[]{mEmailView, mPasswordView, trialIDView, userIDView};

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        userIDView.setError(null);
        trialIDView.setError(null);

        String email;
        String password;
        String userID;
        int trialID;
        boolean isValid;

        //if we're in development, ignore form values and use stored credentials for login
        if(Application.getBuildMode() == Application.BuildMode.DEV
                || Application.getBuildMode() == Application.BuildMode.DEMO) {
            //use stored credentials for faster testing during dev
            Log.i(TAG, "DEVMODE login");
            showProgress(true);
            String[] credentials = DEMO_CREDENTIALS.split(":");

            email = credentials[0];
            password = credentials[1];
            userID = "TIHM-P3";
            trialID = 101;
            isValid = true;

        }
        else {

            // Store values at the time of the login attempt.
            email = mEmailView.getText().toString();
            password = mPasswordView.getText().toString();
            userID = userIDView.getText().toString();

            isValid = validateInput(email, password, userID);

            try {//ensure trialID parses to a valid number
                trialID = Integer.parseInt(trialIDView.getText().toString());
            }
            catch(NumberFormatException nfe) {
                trialID = -1;
                isValid = false;
            }

        }

        if (isValid) {
            //-----------input validation passed-----------
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            //request JWT from platform over network
            NetworkController.getInstance().authenticateUser(new Authentication(email, password));

            //write gotten trialID and userID to SharedPreferences
            SharedPreferences sp = getSharedPreferences(getString(R.string.shared_prefs_tag), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(getString(R.string.trial_id), trialID);
            editor.putString(getString(R.string.user_id), userID);
            Log.i(TAG, "storing user and trial ID in SP...");
            editor.apply();
        }
    }

    private boolean validateInput(String email, String password, String userID) {
        boolean isValid = true;
        View focusView = null;

        //-----------input validation--------------

        //ensure that all fields are non-empty
        for(EditText et : editTexts) {
            String s = et.getText().toString();
            if (TextUtils.isEmpty(s)) {
                et.setError(getString(R.string.error_field_required));
                focusView = et;
                isValid = false;
                break;
            }
        }

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            isValid = false;
        }
        // Check for a valid email address.
        if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            isValid = false;
        }
        if(focusView != null) {
            focusView.requestFocus();
        }
        return isValid;
    }

    private boolean isEmailValid(String email) {
        Pattern pat = Pattern.compile("[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+){1,4}");
        return pat.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

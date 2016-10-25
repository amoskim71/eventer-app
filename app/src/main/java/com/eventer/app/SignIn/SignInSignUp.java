package com.eventer.app.SignIn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eventer.app.JsonApi.ServerCallback;
import com.eventer.app.JsonApi.ValidateReg;
import com.eventer.app.MainActivity;
import com.eventer.app.R;
import com.eventer.app.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pixelcan.inkpageindicator.InkPageIndicator;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Gulzar on 24-10-2016.
 */
public class SignInSignUp extends AppCompatActivity {
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.indicator) InkPageIndicator mindicator;
    @BindView(R.id.button_sign_in) Button mbutton_sign_in;
    @BindView(R.id.button_sign_up) Button mbutton_sign_up;
    @BindView(R.id.field_email) EditText mRegField;
    @BindView(R.id.field_password) EditText mPasswordField;
    @BindView(R.id.email_password_fields) View memail_password_fields;
    @BindView(R.id.email_password_buttons) View memail_password_buttons;
    @BindView(R.id.progressbar) ProgressBar mProgressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "SignInSignUp";
    public String regNo, pswd ,email;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_activity);
        ButterKnife.bind(this);
        // initialize firebase database ref
        mDatabase= FirebaseDatabase.getInstance().getReference();
        /*
            firebase auth start
         */
        // [initialize firebase instance]
        mAuth = FirebaseAuth.getInstance();
        // [initalize firebase listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    startActivity(new Intent(SignInSignUp.this,MainActivity.class));
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        /*
            end firebase auth
         */

        //Setting Up page adapter
        MyPagerAdapter adapter = new MyPagerAdapter();
        mViewPager.setAdapter(adapter);
        mindicator.setViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //Page Adapter
    private class MyPagerAdapter extends PagerAdapter {

        public int getCount() {
            return 3;
        }
        public Object instantiateItem(View collection, int position) {

            LayoutInflater inflater = (LayoutInflater) collection.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.layout.eventer_logo;
                    break;
                case 1:
                    resId = R.layout.eventer_logo;
                    break;
                case 2:
                    resId = R.layout.eventer_logo;
                    break;
            }

            View view = inflater.inflate(resId, null);

            ((ViewPager) collection).addView(view, 0);

            return view;
        }
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }
        @Override
        public Parcelable saveState() {
            return null;
        }
        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);

        }
    }
    //Validate TextFields
    boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mRegField.getText().toString())) {
            mRegField.setError("Required");
            result = false;
        } else {
            mRegField.setError(null);
        }

        if (mRegField.length()!=7) {
            mRegField.setError("Enter Valid Reg.");
            result = false;
        } else {
            mRegField.setError(null);
        }
        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);}
        return result;
    }


    public void toggleProgressVisibility() {
        if(mProgressBar.getVisibility()==View.INVISIBLE)
        {
            mProgressBar.setVisibility(View.VISIBLE);
            memail_password_buttons.setVisibility(View.INVISIBLE);
            memail_password_fields.setVisibility(View.INVISIBLE);
        }
        else
        {
            mProgressBar.setVisibility(View.INVISIBLE);
            memail_password_buttons.setVisibility(View.VISIBLE);
            memail_password_fields.setVisibility(View.VISIBLE);
        }

    }

    /*
        user authentication system
    */
    void initVar()
    {
        regNo=mRegField.getText().toString();
        pswd=mPasswordField.getText().toString();
        email=regNo+"@app.in";
    }

    // [create user with email and password ]
    private void setUserName(String f)
    {
        String name="";
        for(int i=0;i<f.length();i++)
        {
            if(Character.isDigit(f.charAt(i)))
            {
                name=f.substring(0,i-1);
                break;
            }
        }
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final User userDetails = new User(regNo, name);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            mDatabase.child("user").child(user.getUid()).setValue(userDetails);

                        }
                    }
                });
    }
    private void createUser(String email,String password,final String f)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInSignUp.this, "Wrong Credentials",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            setUserName(f);

                        }
                    }
                });
    }

    private void signUp()
    {
        toggleProgressVisibility();
        Log.d(TAG, regNo+pswd);
        ValidateReg vr=new ValidateReg();
        //Controller
        vr.validateRegJSON(regNo,pswd, new ServerCallback() {
            @Override
            public void onSuccessResult(JSONObject result) {
                try {
                    String r = result.getJSONArray("items").getString(0);
                    JSONObject n = new JSONObject(r);
                    String f = n.getString("result");
                    if (f.equalsIgnoreCase("login failed"))
                        Toast.makeText(SignInSignUp.this, "INVALID CREDENTIALS!!!", Toast.LENGTH_SHORT).show();
                    else {createUser(email,pswd,f);
                            }
                } catch (Exception e) {}
                toggleProgressVisibility();
            }
        });}
    // [end create user with email and password ]

    //[sign in user with email and password]
    private void signIn(String email, String password)
    {
        toggleProgressVisibility();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(SignInSignUp.this, "Wrong Credentials", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            // open new activity
                            startActivity(new Intent(SignInSignUp.this, MainActivity.class));
                            finish();
                        }
                        toggleProgressVisibility();


                        // ...
                    }
                });
    }
    //[end sign in user with email and password]
    /*
        end user autentication system
     */


    @OnClick(R.id.button_sign_up)void signupButton()
    {
        if(!validateForm())
            return;
        hideSoftKeyboard(this);
        //initialize variables
        initVar();
        signUp();

    }
    @OnClick(R.id.button_sign_in)void signinButton()
    {
        if(!validateForm())
            return;
        hideSoftKeyboard(this);
        //initialize variables
        initVar();
        signIn(email,pswd);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }




}
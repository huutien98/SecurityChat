package com.example.securitychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.securitychat.Model.Users;
import com.example.securitychat.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigInteger;


public class SignInActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    ActivitySignInBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;
    GoogleApiClient googleApiClient;
    String SiteKey = "6LcPN9MdAAAAAJ18jBzHjtglILys-Gv5E65Du6_o";
    int clickcount=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Đăng nhập");
        progressDialog.setMessage("Chờ Tý");

//        Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("282723123870-smq8bsppbd9kms7bdfkht6137gj5e7c4.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(this)
                .build();
        googleApiClient.connect();



        binding.btnSignin.setOnClickListener(v -> {
            clickcount=clickcount+1;
            if (clickcount > 3){
                SafetyNet.SafetyNetApi.verifyWithRecaptcha(googleApiClient,SiteKey)
                        .setResultCallback(recaptchaTokenResult -> {
                            Status status = recaptchaTokenResult.getStatus();
                            if (status.isSuccess()){
                                Toast.makeText(getApplicationContext(),"Xác thực capcha thành công",
                                        Toast.LENGTH_LONG).show();
                                checkInfomation();
                            }else {
                                Toast.makeText(getApplicationContext(),"capcha không chính xác",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                Toast.makeText(getApplicationContext(),"xác nhận tôi không phải là robot!",Toast.LENGTH_LONG).show();
            }else {
                checkInfomation();
            }
        });




        binding.chuacotaikhoan.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
        binding.btnPhone.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, PhoneLoginActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnGoogle.setOnClickListener(v -> {
            progressDialog.show();
            signIn();
        });
    }

    int RC_SIGN_IN = 65;

    private void checkInfomation(){
        if (binding.eEmail.getText().toString().isEmpty()) {
            binding.eEmail.setError("Email không được để trống");
            return;
        }
        if (binding.ePassWord.getText().toString().isEmpty()) {
            binding.ePassWord.setError("Mật Khẩu không được để trống");
            return;
        }
        progressDialog.show();
        byte[] md5input = binding.ePassWord.getText().toString().getBytes();
        BigInteger md5Data = null;

        try {
            md5Data = new BigInteger(1, md5.encryptMD5(md5input));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String md5Str = md5Data.toString(16);
        if (md5Str.length() < 32) {
            md5Str = 0 + md5Str;
        }
        auth.signInWithEmailAndPassword(
                binding.eEmail.getText().toString(), md5Str)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        binding.eEmail.getText().clear();
                        binding.ePassWord.getText().clear();
                        Toast.makeText(SignInActivity.this, "email hoặc mật khẩu không đúng", Toast.LENGTH_LONG).show();

                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Users users = new Users();
                            users.setUserId(user.getUid());
                            users.setUserName(user.getDisplayName());
                            users.setProfilepic(user.getPhotoUrl().toString());
                            database.getReference().child("Users").child(user.getUid()).setValue(users);
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(SignInActivity.this, "Đăng nhập với Google thành công", Toast.LENGTH_LONG).show();
                        } else {
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
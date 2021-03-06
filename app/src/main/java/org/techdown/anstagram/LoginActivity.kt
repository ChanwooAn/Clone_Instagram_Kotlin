package org.techdown.anstagram

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.security.auth.callback.Callback
import kotlinx.android.synthetic.main.activity_login.*


const val GOOGLE_LOGIN_CODE=9001

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth?=null
    var googleSignInClient: GoogleSignInClient?=null
    var callbackManager:CallbackManager?=null

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth=FirebaseAuth.getInstance()


        sign_email.setOnClickListener{
            signinAndSignup()
        }
        login_google.setOnClickListener{
            googleLogin()
        }
        login_facebook.setOnClickListener{
            facebookLogin()
        }


        // google login
        var gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)// default sign in??? ?????????id??? ???????????? ????????? ????????? ??????
            .requestIdToken(BuildConfig.googlekey)
            .requestEmail()
            .build()

        googleSignInClient= GoogleSignIn.getClient(this,gso)
        // googleSignIn ????????? ??????.





        //fb ?????????
        callbackManager= CallbackManager.Factory.create()






    }


    fun googleLogin(){
        var signInIntent=googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }
    fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))

        LoginManager.getInstance()
            .registerCallback(callbackManager,object : FacebookCallback<LoginResult>{
                override fun onCancel() {
                }

                override fun onError(error: FacebookException) {
                }

                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result?.accessToken)
                }

            })
        //fb???????????? ?????? callback????????? ????????????.
    }

    //facebook ???????????? ???????????? ??????callback??? ?????? token??? firebase????????? ????????????.
    fun handleFacebookAccessToken(token:AccessToken?){
        var credential=FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task->
                if(task.isSuccessful)
                {
                    moveMainPage(task.result?.user)
                }
                else{
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("LoginAc","result")

        callbackManager?.onActivityResult(requestCode,resultCode,data)

        if(requestCode== GOOGLE_LOGIN_CODE){
            var test=try{
                GoogleSignIn.getSignedInAccountFromIntent(data)
            }
            catch(e :Exception)
            {
                Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show()
                null

            }
            Log.d("LoginAc",test?.getResult()?.displayName.toString())


            var result= Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if(result!!.isSuccess){
                var account=result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }
    fun firebaseAuthWithGoogle(account:GoogleSignInAccount?){
        var credential= GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task->
                if(task.isSuccessful)
                {
                    moveMainPage(task.result?.user)
                }
                else{
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }




    fun signinAndSignup(){
        if(id_edit.text.toString().isEmpty()){
            Toast.makeText(this,"null",Toast.LENGTH_SHORT).show()
            Log.d("Login","sssssssssssssssssss")
            return
        }else{
            auth?.createUserWithEmailAndPassword(id_edit.text.toString(),pw_edit.text.toString())
                ?.addOnCompleteListener {
                        task ->
                    if(task.isSuccessful)
                    {
                        //creating a user account
                        moveMainPage(task.result.user)
                        Log.d("Login","createuser_success")
                    }
                    else if(task?.exception?.message.isNullOrEmpty()){
                        //show error message
                        Log.d("Login","error create")
                        Toast.makeText(this,task.exception?.message+"          create",Toast.LENGTH_LONG).show()


                    }else{
                        //login if you  already have account
                        singinEmail()
                    }

                }
        }

    }
    fun singinEmail(){
        auth?.signInWithEmailAndPassword(id_edit.text.toString(),pw_edit.text.toString())
            ?.addOnCompleteListener {
                    task->
                if(task.isSuccessful){
                    //login
                    moveMainPage(task.result.user)
                }
                else{
                    //show error msg
                    Toast.makeText(this,task.exception?.message+"what thefurck??",Toast.LENGTH_SHORT).show()
                }
            }
    }
    fun moveMainPage(user: FirebaseUser?)
    {
        if(user!=null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }














    // hash?????? fb?????? ?????????????????? fb???????????? ???????????????.
    // ?????? keytool??? ???????????? package??? ?????? keyhash??? ?????? ?????? ????????? ????????? ???????????? ????????? ??????????????????.
    fun printHashKey() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
            //package Info??? ?????????
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i("Login", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("Login", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("Login", "printHashKey()", e)
        }
    }
}
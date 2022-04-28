package org.techdown.anstagram

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import org.techdown.anstagram.navigation.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_home->{
                var detailViewFragment=DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true;
            }
            R.id.action_search->{
                var gridViewFragment=GridViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridViewFragment).commit()
                return true;
            }
            R.id.action_photo->{
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)==
                        PackageManager.PERMISSION_GRANTED){
                    startActivity(Intent(this,AddPhotoActivity::class.java))
                }
                return true;
            }
            R.id.action_favorite_alarm->{
                var alarmViewFragment=AlarmViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmViewFragment).commit()
                return true;
            }
            R.id.action_account->{
                var userViewFragment= UserViewFragment()
                var bundle=Bundle()
                var uid=FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid",uid)
                userViewFragment.arguments=bundle

                supportFragmentManager.beginTransaction().replace(R.id.main_content,userViewFragment).commit()
                return true;
            }
        }
        return false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        bottom_navigation.setOnNavigationItemSelectedListener(this)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        //외부 저장소 접근 권한 허용


        bottom_navigation.selectedItemId=R.id.action_home

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==UserViewFragment.PICK_PROFILE_FROM_ALBUM && resultCode== Activity.RESULT_OK){
            var imageUri=data?.data
            var uid=FirebaseAuth.getInstance().currentUser?.uid
            var storageRef=FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask{task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri->
                var map=HashMap<String,Any>()
                map["image"]=uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }


}
package org.techdown.anstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import org.techdown.anstagram.R
import org.techdown.anstagram.navigation.model.ContentDTO
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM=0
    var storage:FirebaseStorage?=null
    var photoUri:Uri?=null
    var auth: FirebaseAuth?=null
    var firestore:FirebaseFirestore?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        storage= FirebaseStorage.getInstance()
        auth= FirebaseAuth.getInstance()
        firestore= FirebaseFirestore.getInstance()


        //album app열어서 사진 선택
        var photoPickerIntent=Intent(Intent.ACTION_PICK)
        photoPickerIntent.type="image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        addphoto_btn.setOnClickListener{
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_FROM_ALBUM){
            if(resultCode== Activity.RESULT_OK){
                photoUri=data?.data
                addphoto_image.setImageURI(photoUri)
            }else{
                finish()
            }
        }

    }
    fun contentUpload(){
        var timestamp= SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName="Image_"+timestamp+"_.png"

        var storageRef=storage?.reference?.child("images")?.child(imageFileName)

        //promise방식
        storageRef?.putFile(photoUri!!)?.continueWithTask{
            task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef?.downloadUrl
        }?.addOnSuccessListener { uri->
            var contentDTO= ContentDTO()

            contentDTO.imageUrl=uri.toString()
            contentDTO.uid=auth?.currentUser?.uid
            contentDTO.userId=auth?.currentUser?.email
            contentDTO.explain=addphoto_edit_explain.text.toString()
            contentDTO.timeStamp=System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)


            setResult(Activity.RESULT_OK)

            finish()
        }

        /*
        callback방식으로 진행

        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri->
                var contentDTO= ContentDTO()

                contentDTO.imageUrl=uri.toString()
                contentDTO.uid=auth?.currentUser?.uid
                contentDTO.userId=auth?.currentUser?.email
                contentDTO.explain=addphoto_edit_explain.text.toString()
                contentDTO.timeStamp=System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)


                setResult(Activity.RESULT_OK)

                finish()

            }
        }*/
    }
}
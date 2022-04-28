package org.techdown.anstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import org.techdown.anstagram.R
import org.techdown.anstagram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_user.view.*
import org.techdown.anstagram.LoginActivity
import org.techdown.anstagram.MainActivity
import org.techdown.anstagram.navigation.model.FollowDTO
private const val TAG="UserViewFrag"
class UserViewFragment : Fragment() {
    var fragmentView:View?=null
    var firestore:FirebaseFirestore?=null
    var uid:String?=null
    var auth: FirebaseAuth?=null
    var currentUserId:String?=null
    companion object{
        var PICK_PROFILE_FROM_ALBUM=10

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"onCreateView")
        fragmentView=LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid=arguments?.getString("destinationUid")
        firestore= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()
        currentUserId=auth?.currentUser?.uid



        if(uid==currentUserId){
            //현재 내 page를 보고있을 때
            fragmentView?.account_btn_follow_signout?.text=getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener{
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }
        }else{
            //다른 user의 page를 보고 있을 때
            fragmentView?.account_btn_follow_signout?.text=getString(R.string.follow)
            var mainActivity=(activity as MainActivity)
            mainActivity.toolbar_btn_back?.setOnClickListener{
                mainActivity.bottom_navigation.selectedItemId=R.id.action_home
            }

        }

        fragmentView?.account_iv_profile?.setOnClickListener{
            var photoPickerIntent=Intent(Intent.ACTION_PICK)
            photoPickerIntent.type="image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        fragmentView?.account_btn_follow_signout?.setOnClickListener{
            requestFollow()
        }





        getProfileImage()
        getFollowAndFollowing()
        fragmentView?.account_recyclerView?.adapter=UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerView?.layoutManager=GridLayoutManager(activity,3)
        return fragmentView
    }

    fun getFollowAndFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener{
            documentSnapshot,firebaseFirestoreException->
            if(documentSnapshot==null){
                return@addSnapshotListener
            }
            var followDTO=documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.follwingCount!=null){
                fragmentView?.account_tv_following_count?.text= followDTO.follwingCount?.toString()
            }
            if(followDTO?.followerCount!=null){
                fragmentView?.account_tv_follower_count?.text=followDTO.follwingCount?.toString()
                if(followDTO?.follwers?.containsKey(currentUserId!!)){
                    fragmentView?.account_btn_follow_signout?.text=activity?.getString(R.string.follow_cancel)
                }
                else{
                    fragmentView?.account_btn_follow_signout?.text=activity?.getString(R.string.follow)

                }
            }


        }
    }

    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener{
            documentSnapshot,firebaseFirestoreException ->
            if(documentSnapshot==null)return@addSnapshotListener

            if(documentSnapshot.data !=null){
                var url=documentSnapshot?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()//image를 원형으로 받기
                ).into(fragmentView?.account_iv_profile!!)
            }
        }
    }
    fun requestFollow(){
        var tsDocFollowing=firestore?.collection("users")?.document(currentUserId!!)
        firestore?.runTransaction{transaction->
            var followDTO=transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO==null){
                followDTO=FollowDTO()
                followDTO!!.followerCount=1
                followDTO!!.follwers[uid!!]=true

                transaction.set(tsDocFollowing,followDTO)
            }
            if(followDTO.followings.containsKey(uid)){
                followDTO.follwingCount=followDTO?.follwingCount-1
                followDTO.follwers?.remove(uid)
                //unfollow
            }else{
                followDTO.follwingCount=followDTO?.follwingCount+1
                followDTO.follwers[uid!!]=true
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        var tsDocFollower=firestore?.collection("users")?.document(uid!!) //현재 user의 uid값으로 users에서 정보추출
        firestore?.runTransaction{
            transaction->
            var followDTO=transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)

            if(followDTO==null){
                followDTO= FollowDTO()
                followDTO!!.followerCount=1
                followDTO!!.follwers[currentUserId!!]=true

                transaction.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }
            if(followDTO!!.follwers.containsKey(currentUserId)){
                followDTO!!.followerCount=followDTO!!.followerCount-1
                followDTO!!.follwers.remove(currentUserId)
            }else{
                followDTO!!.followerCount=followDTO!!.followerCount+1
                followDTO!!.follwers[currentUserId!!]=true
            }
            transaction.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }

    inner class UserFragmentRecyclerViewAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs:ArrayList<ContentDTO> =arrayListOf()

        init{
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener{
                querySnapshot,firebaseFirestore->
                if(querySnapshot==null)return@addSnapshotListener

                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)//contentdto배열에 삽입
                }
                fragmentView?.account_tv_post_count?.text=contentDTOs.size.toString()
                notifyDataSetChanged()
            }// uid 검색 query 를 firebase에 날리기
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width=resources.displayMetrics.widthPixels/3//폭의 1/3

            var imageView= ImageView(parent.context)
            imageView.layoutParams= LinearLayoutCompat.LayoutParams(width,width)

            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {


        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.imageView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop() //이미지를 중앙으로
            ).into(imageView)


        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }
    
}
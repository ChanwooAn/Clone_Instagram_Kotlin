package org.techdown.anstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*
import org.techdown.anstagram.R
import org.techdown.anstagram.navigation.model.ContentDTO

class DetailViewFragment : Fragment() {
    var firestore:FirebaseFirestore?=null
    var uid :String?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_detail,container,false)
        uid=FirebaseAuth.getInstance().currentUser?.uid
        Log.d("DetailView","oncreview")

        firestore= FirebaseFirestore.getInstance()



        view.detail_view_fragment_recyclerview.adapter=DetailViewRecyclerViewAdapter()
        view.detail_view_fragment_recyclerview.layoutManager=LinearLayoutManager(context)



        return view
    }


    inner class DetailViewRecyclerViewAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        inner class ImageViewHolder(view:View):RecyclerView.ViewHolder(view)


        var contentDTOs:ArrayList<ContentDTO> = arrayListOf()
        var contentUidList:ArrayList<String> = arrayListOf()
        init{
           Log.d("Detail",firestore?.collection("images")?.document()?.get().toString())

            firestore?.collection("images")?.orderBy("timeStamp")?.addSnapshotListener{
                querySnapshot,firebaseFirestoreException->
                Log.d("Detail",querySnapshot?.documents.toString())

                contentDTOs.clear()
                contentUidList.clear()

                if(querySnapshot==null)return@addSnapshotListener

                    for(snapshot in querySnapshot!!.documents){
                        var item=snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                        Log.d("Detail",item.toString())
                    }
                notifyDataSetChanged()
            }

        }

        override fun getItemCount(): Int {
            Log.d("Detail",contentDTOs.size.toString())
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder=holder.itemView

            viewHolder.detail_view_profile_text_view.text=contentDTOs!![position].userId
            Glide.with(viewHolder.context).load(contentDTOs!![position].imageUrl).into(viewHolder.detail_view_content_image)
            viewHolder.detail_view_item_explain_text.text=contentDTOs[position].explain
            viewHolder.detail_view_item_favoritecount_text.text="Likes"+contentDTOs[position].favoriteCount
            Glide.with(viewHolder.context).load(contentDTOs!![position].imageUrl).into(viewHolder.detail_view_profile_image)

            viewHolder.detail_view_favorite_image_view.setOnClickListener{
                favoriteEvent(position)
            }
            viewHolder.detail_view_profile_image.setOnClickListener{
                var fragment=UserViewFragment()
                var bundle=Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("userId",contentDTOs[position].userId)
                fragment.arguments=bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
            viewHolder.detail_view_comment_image_view.setOnClickListener{
                v->
                var intent= Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[position])
                startActivity(intent)
            }





            if(contentDTOs!![position].favorites.containsKey(uid)){
                viewHolder.detail_view_favorite_image_view.setImageResource(R.drawable.ic_favorite)
            }else{
                viewHolder.detail_view_favorite_image_view.setImageResource(R.drawable.ic_favorite_border)

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view=layoutInflater.inflate(R.layout.item_detail,parent,false)
            Log.d("DetailView","oncreateviewhodler")

            return ImageViewHolder(view)
        }
        fun favoriteEvent(position: Int){
            var tsDoc=firestore?.collection("images")?.document(contentUidList[position]) //firestore images가서
            firestore?.runTransaction{transaction->
                var uid= FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO=transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    //when the btn was alreay clicked
                    contentDTO?.favoriteCount--
                    contentDTO?.favorites.remove(uid)
                }else{
                    contentDTO.favoriteCount++
                    contentDTO.favorites[uid!!]=true
                }
                transaction.set(tsDoc,contentDTO)
            }
        }



    }

}
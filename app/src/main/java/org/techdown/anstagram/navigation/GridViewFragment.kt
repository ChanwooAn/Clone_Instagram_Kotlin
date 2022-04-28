package org.techdown.anstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_grid.view.*
import org.techdown.anstagram.R
import org.techdown.anstagram.navigation.model.ContentDTO

class GridViewFragment : Fragment() {

    var firestore:FirebaseFirestore?=null
    var fragmentView:View?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView=inflater.inflate(R.layout.fragment_grid,container,false)
        firestore= FirebaseFirestore.getInstance()
        fragmentView?.grid_recycler_view?.adapter=UserFragmentRecyclerViewAdapter()
        fragmentView?.grid_recycler_view?.layoutManager=GridLayoutManager(requireActivity(),3)
        return fragmentView
    }
    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs:ArrayList<ContentDTO> =arrayListOf()

        init{
            firestore?.collection("images")?.addSnapshotListener{
                    querySnapshot,firebaseFirestore->
                if(querySnapshot==null)return@addSnapshotListener

                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)//contentdto배열에 삽입
                }
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
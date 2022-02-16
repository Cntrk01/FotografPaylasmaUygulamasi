package com.example.fotografpaylasmauygulamasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycler_row.view.*

class HarberRecyclerAdapter(val postList:ArrayList<ModelClass>) : RecyclerView.Adapter<HarberRecyclerAdapter.PostHolder>() {
    //ArrayList içindeki Class isimi videoda Post olarak geçiyor bende ModelClass!!!
    class PostHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val inflater=LayoutInflater.from(parent.context)
        val view=inflater.inflate(R.layout.recycler_row,parent,false)
        return PostHolder(view)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
       holder.itemView.recycler_row_kullaniciemail.text=postList[position].kisiIsimi //7.. kisi isimini cektik
       holder.itemView.recycler_row_kullaniciyorumu.text=postList[position].yorum //7.. kisi yorumunu cektik
        //resim url oldugu için dış kütüphane kullanacagız göstermek için
        Picasso.get().load(postList[position].gorselUrl).into(holder.itemView.recycler_row_resim) //loada pozisyonu yüklüyoruz.into içerisine de gidecek hedef yeri yazıyoruz


    }

    override fun getItemCount(): Int {
       return postList.size
    }
}
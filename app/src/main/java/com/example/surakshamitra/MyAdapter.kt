package com.example.surakshamitra

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class MyDataModel(val imageRes: Int, val text: String)

class MyAdapter(private val context: Context, private var dataList: List<MyDataModel>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.agencyrecyclerview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        // Set image and text for each item
        holder.imageView.setImageResource(data.imageRes)
        holder.textView.text = data.text
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setDataList(newDataList: List<MyDataModel>) {
        dataList = newDataList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}

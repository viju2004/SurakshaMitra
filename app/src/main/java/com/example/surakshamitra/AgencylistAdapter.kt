package com.example.surakshamitra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AgencyListDataModel(
    val imageResource1: Int,
    val textBehindImage: String,
    val imageResource2: Int,
    val imageResource3: Int
)
class AgencylistAdapter(private var dataList: List<AgencyListDataModel>) :
    RecyclerView.Adapter<AgencylistAdapter.YourViewHolder>() {

    class YourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView1: ImageView = itemView.findViewById(R.id.agimageView1)
        val textViewBehindImage: TextView = itemView.findViewById(R.id.textViewBehindImage)
        val imageView2: ImageView = itemView.findViewById(R.id.agimageView2)
        val imageView3: ImageView = itemView.findViewById(R.id.agimageView3)
    }

    fun setDataList(newDataList: List<AgencyListDataModel>) {
        dataList = newDataList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.agencylistrecycler, parent, false)
        return YourViewHolder(view)
    }

    override fun onBindViewHolder(holder: YourViewHolder, position: Int) {
        // Bind your data to the views in each ViewHolder
        val currentItem = dataList[position]

        // Example: Load images into ImageView
        holder.imageView1.setImageResource(currentItem.imageResource1)
        holder.textViewBehindImage.text = currentItem.textBehindImage
        holder.imageView2.setImageResource(currentItem.imageResource2)
        holder.imageView3.setImageResource(currentItem.imageResource3)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}

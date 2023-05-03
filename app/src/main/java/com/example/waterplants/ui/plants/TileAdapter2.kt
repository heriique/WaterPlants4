package com.example.waterplants.ui.plants

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.example.waterplants.R
import com.example.waterplants.model.Plant

class TileAdapter2(var context: Context, private var plants: LiveData<List<Plant>>,
                   private var selectedPlant: LiveData<Int>
) : BaseAdapter() {
    override fun getCount(): Int {
        return plants.value!!.size + 1 // add new item
    }

    override fun getItem(position: Int): Any {
        if (position == plants.value!!.size)
            return Plant(null, null, null, null, null, "New Plant...", null)
        return (plants.value!![position])
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var myView = convertView
        val holder: ViewHolder
        if (myView == null) {
            val inflater = (context as Activity).layoutInflater
            myView = inflater.inflate(R.layout.grid_item2, parent, false)
            holder = ViewHolder()
            holder.imageView = myView!!.findViewById(R.id.image_grid2) as ImageView
            holder.textView = myView.findViewById(R.id.text_grid2) as TextView
            myView.tag = holder
        }
        else {
            holder = myView.tag as ViewHolder
        }
        holder.imageView!!.setImageResource(R.drawable.baseline_image_not_supported_24)
        if (position < plants.value!!.size) {
            val uri = plants.value?.get(position)?.imageUri
            if (uri != null)
                holder.imageView!!.setImageURI(uri)
        }
        if (selectedPlant.value == position)
            myView.setBackgroundResource(R.drawable.border)
        else
            myView.setBackgroundResource(0)
        if (position < plants.value!!.size)
        holder.textView!!.text = plants.value?.get(position)?.name ?: "No name"
        else
            holder.textView!!.text = "New Plant"
        return myView
    }
    class ViewHolder {
        var imageView: ImageView? = null
        var textView: TextView? = null
    }

}
package com.example.waterplants.ui.schedule

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.waterplants.R
import com.example.waterplants.model.Plant

/**
 * Adapter class to show a grid of tiles with a plant image and text below it
 * Reference:
 * https://medium.com/@hasperong/custom-gridview-image-and-text-using-kotlin-e7feb0b0189c
 */
class TileAdapter(context: Context, plants: LiveData<List<Plant>>, chosenPlants: LiveData<List<Plant>>, selectedHose: LiveData<Int>) : BaseAdapter() {
    var context = context
    var plants = plants
    var chosenPlants = chosenPlants
    var selectedHose = selectedHose
    override fun getCount(): Int {
        return plants.value!!.size
    }

    override fun getItem(position: Int): Any {
        return (plants.value!![position])
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var myView = convertView
        var holder: ViewHolder
        if (myView == null) {
            val inflater = (context as Activity).layoutInflater
            myView = inflater.inflate(R.layout.grid_item, parent, false)
            holder = ViewHolder()
            holder.imageView = myView!!.findViewById(R.id.image_grid) as ImageView
            holder.textView = myView.findViewById(R.id.text_grid) as TextView
            myView.tag = holder
        }
        else {
            holder = myView.tag as ViewHolder
        }
        holder.imageView!!.setImageResource(R.drawable.baseline_image_not_supported_24)
        val uri = plants.value?.get(position)?.imageUri
        if (uri != null)
            holder.imageView!!.setImageURI(uri)
        if (chosenPlants.value!![selectedHose.value!!] == plants.value?.get(position))
            myView.setBackgroundResource(R.drawable.border)
        else
            myView.setBackgroundResource(0)
        holder.textView!!.text = plants.value?.get(position)?.name ?: "No name"
        return myView
    }
    class ViewHolder {
        var imageView: ImageView? = null
        var textView: TextView? = null
    }

}


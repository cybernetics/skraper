package ru.sokomishalov.skraper.example.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ru.sokomishalov.skraper.example.R
import ru.sokomishalov.skraper.model.Post

class ListViewModelAdapter(
    private val context: Context,
    private val data: MutableList<Post>
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View?
        val vh: ViewHolder

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(context)
            view = layoutInflater.inflate(R.layout.list_view_item, parent, false)
            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        vh.tvTitle.text = data[position].id
        vh.tvContent.text = data[position].text
        return view
    }

    override fun getItem(position: Int): Any = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = data.size

    operator fun plus(posts: List<Post>) {
        data += posts
        notifyDataSetChanged()
    }
}

private class ViewHolder(view: View?) {
    val tvTitle: TextView = view?.findViewById(R.id.tvTitle) as TextView
    val tvContent: TextView = view?.findViewById(R.id.tvContent) as TextView
}
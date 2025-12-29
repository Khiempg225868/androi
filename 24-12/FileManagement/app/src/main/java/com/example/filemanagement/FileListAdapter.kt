package com.example.filemanagement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class FileListAdapter(
    context: Context,
    private val items: List<FileListItem>
) : ArrayAdapter<FileListItem>(context, R.layout.item_file, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_file, parent, false)

        val icon = view.findViewById<ImageView>(R.id.imageIcon)
        val title = view.findViewById<TextView>(R.id.textTitle)
        val subtitle = view.findViewById<TextView>(R.id.textSubtitle)

        val item = items[position]

        title.text = item.title

        when (item.kind) {
            FileListItem.Kind.UP -> {
                icon.setImageResource(R.drawable.ic_arrow_up_24)
                subtitle.text = "thu muc cha"
            }
            FileListItem.Kind.DIRECTORY -> {
                icon.setImageResource(R.drawable.ic_folder_24)
                subtitle.text = "thu muc"
            }
            FileListItem.Kind.FILE -> {
                icon.setImageResource(R.drawable.ic_file_24)
                subtitle.text = "tap tin"
            }
        }

        return view
    }
}

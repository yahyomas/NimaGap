package com.yahyomas.nimagap

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class AwesomeMessageAdapter(
    private val activity: Activity, resource: Int,
    private val messages: List<AwesomeMessage>
) : ArrayAdapter<AwesomeMessage?>(activity, resource, messages) {
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        val layoutInflater = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater
        val awesomeMessage = getItem(position)
        var layoutResource = 0
        val viewType = getItemViewType(position)
        layoutResource = if (viewType == 0) {
            R.layout.your_message_item
        } else {
            R.layout.my_message_item
        }
        if (convertView != null) {
            viewHolder = convertView.tag as ViewHolder
        } else {
            convertView = layoutInflater.inflate(
                layoutResource, parent, false
            )
            viewHolder = ViewHolder(convertView)
            convertView.tag = viewHolder
        }
        val isText = awesomeMessage!!.imageUrl == null
        if (isText) {
            viewHolder.messageTextView.visibility = View.VISIBLE
            viewHolder.photoImageView.visibility = View.GONE
            viewHolder.messageTextView.setText(awesomeMessage!!.text)
            viewHolder.nameTextView.setText(awesomeMessage!!.name)
        } else {
            viewHolder.nameTextView.setText(awesomeMessage!!.name)
            viewHolder.messageTextView.visibility = View.GONE
            viewHolder.photoImageView.visibility = View.VISIBLE
            Glide.with(viewHolder.photoImageView.context).load(awesomeMessage!!.imageUrl)
                .into(viewHolder.photoImageView)
        }
        return convertView!!
    }

    override fun getItemViewType(position: Int): Int {
        val flag: Int
        val awesomeMessage = messages[position]
        flag = if (awesomeMessage.isMine) {
            0
        } else {
            1
        }
        return flag
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    private inner class ViewHolder(view: View?) {
        val photoImageView: ImageView
        val messageTextView: TextView
        val nameTextView: TextView

        init {
            photoImageView = view!!.findViewById(R.id.photoImageView)
            messageTextView = view.findViewById(R.id.messageTextView)
            nameTextView = view.findViewById(R.id.nameTextView)
        }
    }

}

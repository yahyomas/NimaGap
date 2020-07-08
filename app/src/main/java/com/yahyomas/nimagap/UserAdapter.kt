package com.yahyomas.nimagap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class UserAdapter(private val users: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder?>() {
    private var listener: OnUserClickListener? = null

    interface OnUserClickListener {
        fun onUserClick(position: Int)
    }

    fun setOnUserClickListener(listener: OnUserClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): UserViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.user_item, viewGroup, false)
        return UserViewHolder(view, listener)
    }

    override fun onBindViewHolder(@NonNull userViewHolder: UserViewHolder, i: Int) {
        val currentUser = users[i]
        userViewHolder.avatarImageView.setImageResource(currentUser.avatarMockUpResource)
        userViewHolder.userNameTextView.text=currentUser.name
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class UserViewHolder(
        @NonNull itemView: View,
        listener: OnUserClickListener?
    ) :
        RecyclerView.ViewHolder(itemView) {
        var avatarImageView: ImageView
        var userNameTextView: TextView

        init {
            avatarImageView = itemView.findViewById(R.id.avatarImageView)
            userNameTextView = itemView.findViewById(R.id.userNameTextView)
            itemView.setOnClickListener {
                if (listener != null) {
                    val position: Int = getAdapterPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onUserClick(position)
                    }
                }
            }
        }
    }

}

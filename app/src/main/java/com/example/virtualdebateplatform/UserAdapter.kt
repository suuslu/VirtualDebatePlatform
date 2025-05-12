package com.example.virtualdebateplatform

import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualdebateplatform.model.User
import android.view.LayoutInflater
import android.view.ViewGroup

class UserAdapter(
    private val users: List<User>,
    private val selectedUsers: MutableList<String>
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val checkbox: CheckBox = view.findViewById(R.id.user_checkbox)

        fun bind(user: User) {
            checkbox.text = user.username
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = selectedUsers.contains(user.username)

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!selectedUsers.contains(user.username)) {
                        selectedUsers.add(user.username)
                    }
                } else {
                    selectedUsers.remove(user.username)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}

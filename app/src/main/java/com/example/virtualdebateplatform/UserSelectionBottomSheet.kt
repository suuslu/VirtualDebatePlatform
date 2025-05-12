package com.example.virtualdebateplatform

import android.content.Context

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.virtualdebateplatform.model.User

class UserSelectionBottomSheet(
    private val users: List<User>,
    private val onUserSelected: (User) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_user_selection, container, false)
        val recycler = view.findViewById<RecyclerView>(R.id.user_recycler)
        recycler.layoutManager = LinearLayoutManager(context)
        val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val currentUsername = prefs.getString("username", null)
        val filteredUsers = users.filter { it.username != currentUsername }
        val selectedUsers = mutableListOf<String>()
        recycler.adapter = UserAdapter(filteredUsers, selectedUsers)
        return view
    }
}
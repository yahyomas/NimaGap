package com.yahyomas.nimagap

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.collections.ArrayList

class UserListActivity : AppCompatActivity() {
    private var userName: String? = null
    private var auth: FirebaseAuth? = null
    private var usersDatabaseReference: DatabaseReference? = null
    private var usersChildEventListener: ChildEventListener? = null
    private var userArrayList: ArrayList<User> =ArrayList<User>()
    private var userRecyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var userLayoutManager: RecyclerView.LayoutManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        if (auth!!.getCurrentUser() == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            onDestroy()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        val intent = intent
        if (intent != null) {
            userName = intent.getStringExtra(userName)
        }

        userArrayList = ArrayList()
        attachUserDatabaseReferenceListener()
        buildRecyclerView()
    }

    private fun attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        if (usersChildEventListener == null) {
            usersChildEventListener = object : ChildEventListener {
                override fun onChildAdded(
                    @NonNull dataSnapshot: DataSnapshot,
                    @Nullable s: String?
                ) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (!user!!.id.equals(auth!!.currentUser!!.uid)) {
                        user!!.avatarMockUpResource=R.drawable.ic_person_black_24dp
                        userArrayList!!.add(user)
                        userAdapter!!.notifyDataSetChanged()
                    }
                }

                override fun onChildChanged(
                    @NonNull dataSnapshot: DataSnapshot,
                    @Nullable s: String?
                ) {
                }

                override fun onChildRemoved(@NonNull dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(
                    @NonNull dataSnapshot: DataSnapshot,
                    @Nullable s: String?
                ) {
                }

                override fun onCancelled(@NonNull databaseError: DatabaseError) {}
            }
            usersDatabaseReference!!.addChildEventListener(usersChildEventListener as ChildEventListener)
        }
    }

    private fun buildRecyclerView() {
        userRecyclerView = findViewById(R.id.userListRecyclerView)
        userRecyclerView!!.setHasFixedSize(true)
        userRecyclerView!!.addItemDecoration(
            DividerItemDecoration(
                userRecyclerView!!.getContext(), DividerItemDecoration.VERTICAL
            )
        )
        userLayoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(userArrayList)
        userRecyclerView!!.setLayoutManager(userLayoutManager)
        userRecyclerView!!.setAdapter(userAdapter)
        userAdapter!!.setOnUserClickListener(object : UserAdapter.OnUserClickListener {
            override fun onUserClick(position: Int) {
                goToChat(position)
            }
        })
    }

    private fun goToChat(position: Int) {
        val intent = Intent(
            this@UserListActivity,
            ChatActivity::class.java
        )
        intent.putExtra(
            "recipientUserId",
            userArrayList!![position]!!.id
        )
        intent.putExtra(
            "recipientUserName",
            userArrayList!![position]!!.name
        )
        intent.putExtra("userName", userName)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@UserListActivity, SignInActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

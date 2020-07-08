package com.yahyomas.nimagap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var messageListView: ListView? = null
    private var adapter: AwesomeMessageAdapter? = null
    private var progressBar: ProgressBar? = null
    private var sendImageButton: ImageButton? = null
    private var sendMessageButton: Button? = null
    private var messageEditText: EditText? = null
    private var  userName: String? = null
    private var recipientUserId: String? = null
    private var recipientUserName: String? = null
    private var auth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var messagesDatabaseReference: DatabaseReference? = null
    private var messagesChildEventListener: ChildEventListener? = null
    private var usersDatabaseReference: DatabaseReference? = null
    private var usersChildEventListener: ChildEventListener? = null
    private var storage: FirebaseStorage? = null
    private var chatImagesStorageReference: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        auth = FirebaseAuth.getInstance()
        val intent = intent
        if (intent != null) {
            userName = intent.getStringExtra("userName")
            recipientUserId = intent.getStringExtra("recipientUserId")
            recipientUserName = intent.getStringExtra("recipientUserName")
        }
        title = "Chat with $recipientUserName"
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        messagesDatabaseReference = database!!.reference.child("messages")
        usersDatabaseReference = database!!.reference.child("users")
        chatImagesStorageReference = storage!!.getReference().child("chat_images")
        progressBar = findViewById(R.id.progressBar)
        sendImageButton = findViewById(R.id.sendPhotoButton)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        messageEditText = findViewById(R.id.messageEditText)
        messageListView = findViewById(R.id.messageListView)
        val awesomeMessages: List<AwesomeMessage> =
            ArrayList()
        adapter = AwesomeMessageAdapter(
            this, R.layout.message_item,
            awesomeMessages
        )
        messageListView!!.setAdapter(adapter)
        progressBar!!.setVisibility(ProgressBar.INVISIBLE)
        messageEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.toString().trim { it <= ' ' }.length > 0) {
                    sendMessageButton!!.setEnabled(true)
                } else {
                    sendMessageButton!!.setEnabled(false)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        messageEditText!!.setFilters(arrayOf<InputFilter>(LengthFilter(500)))
        sendMessageButton!!.setOnClickListener(View.OnClickListener {
            val message = AwesomeMessage()
            message.text = messageEditText!!.getText().toString()
            message.name = userName
            message.sender = auth!!.getCurrentUser()!!.uid
            message.recipient = recipientUserId
            message.imageUrl = null
            messagesDatabaseReference!!.push().setValue(message)
            messageEditText!!.setText("")
        })
        sendImageButton!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(
                Intent.createChooser(intent, "Choose an image"),
                RC_IMAGE_PICKER
            )
        })
        usersChildEventListener = object : ChildEventListener {
            override fun onChildAdded(
                @NonNull dataSnapshot: DataSnapshot,
                @Nullable s: String?
            ) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user!!.id.equals(FirebaseAuth.getInstance().currentUser!!.uid)) {
                    userName = user.name
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
        messagesChildEventListener = object : ChildEventListener {
            override fun onChildAdded(
                @NonNull dataSnapshot: DataSnapshot,
                @Nullable s: String?
            ) {
                val message = dataSnapshot.getValue(
                    AwesomeMessage::class.java
                )
                if (message!!.sender.equals(auth!!.getCurrentUser()!!.uid)
                    && message.recipient.equals(recipientUserId)
                ) {
                    message.isMine = true
                    message.name = userName
                    adapter!!.add(message)

                } else if (message.recipient.equals(auth!!.getCurrentUser()!!.uid)
                    && message.sender.equals(recipientUserId)
                ) {
                    message.isMine = false
                    adapter!!.add(message)
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
        messagesDatabaseReference!!.addChildEventListener(messagesChildEventListener as ChildEventListener)
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
                startActivity(Intent(this@ChatActivity, SignInActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK) {
            val selectedImageUri = data!!.data
            val imageReference: StorageReference = chatImagesStorageReference!!
                .child(selectedImageUri!!.lastPathSegment!!)
            var uploadTask: UploadTask = imageReference.putFile(selectedImageUri)
            uploadTask = imageReference.putFile(selectedImageUri)
            val urlTask: Task<Uri> =
                uploadTask.continueWithTask(object :
                    Continuation<UploadTask.TaskSnapshot?, Task<Uri>> {
                    @Throws(Exception::class)
                    override fun then(@NonNull task: Task<UploadTask.TaskSnapshot?>): Task<Uri> {
                        if (!task.isSuccessful()) {
                            throw task.getException()!!
                        }

                        // Continue with the task to get the download URL
                        return imageReference.getDownloadUrl()
                    }
                }).addOnCompleteListener(OnCompleteListener<Uri?> { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val message = AwesomeMessage()
                        message.imageUrl = downloadUri.toString()
                        message.name = userName
                        message.sender = auth!!.currentUser!!.uid
                        message.recipient = recipientUserId
                        messagesDatabaseReference!!.push().setValue(message)
                    } else {
                        // Handle failures
                        // ...
                    }
                })
        }

    }


    companion object {
        private const val RC_IMAGE_PICKER = 123
    }
}

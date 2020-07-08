package com.yahyomas.nimagap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var repeatPasswordEditText: EditText? = null
    private var nameEditText: EditText? = null
    private var toggleLoginSignUpTextView: TextView? = null
    private var loginSignUpButton: Button? = null
    private var loginModeActive = false
    private var database: FirebaseDatabase? = null
    private var usersDatabaseReference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersDatabaseReference = database!!.reference.child("users")
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        toggleLoginSignUpTextView = findViewById(R.id.toggleLoginSignUpTextView)
        loginSignUpButton = findViewById(R.id.loginSignUpButton)
        loginSignUpButton!!.setOnClickListener(View.OnClickListener {
            BittaGap.visibility=View.VISIBLE
            loginSignUpUser(emailEditText!!.getText().toString().trim { it <= ' ' },
                passwordEditText!!.getText().toString().trim { it <= ' ' }

            )
        })

    }

    private fun loginSignUpUser(email: String, password: String) {
        if (loginModeActive) {
            if (passwordEditText!!.text.toString().trim { it <= ' ' }.length < 7) {
                Toast.makeText(
                    this, "Password must be at least 7 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (emailEditText!!.text.toString().trim { it <= ' ' } == "") {
                Toast.makeText(
                    this, "Please input your email",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                        this
                    ) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(
                                TAG,
                                "signInWithEmail:success"
                            )
                            val user = auth!!.currentUser
                            val intent = Intent(
                                this@SignInActivity,
                                UserListActivity::class.java
                            )
                            intent.putExtra(
                                "userName",
                                nameEditText!!.text.toString().trim { it <= ' ' }
                            )
                            startActivity(intent)
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(
                                TAG,
                                "signInWithEmail:failure",
                                task.exception
                            )
                            Toast.makeText(
                                this@SignInActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            //updateUI(null);
                        }

                        // ...
                    }
            }
        } else {
            if (passwordEditText!!.text.toString()
                    .trim { it <= ' ' } != repeatPasswordEditText!!.text.toString()
                    .trim { it <= ' ' }
            ) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            } else if (passwordEditText!!.text.toString().trim { it <= ' ' }.length < 7) {
                Toast.makeText(
                    this, "Password must be at least 7 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (emailEditText!!.text.toString().trim { it <= ' ' } == "") {
                Toast.makeText(
                    this, "Please input your email",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                        this
                    ) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(
                                TAG,
                                "createUserWithEmail:success"
                            )
                            val user = auth!!.currentUser
                            createUser(user)
                            //updateUI(user);
                            val intent = Intent(
                                this@SignInActivity,
                                UserListActivity::class.java
                            )
                            intent.putExtra(
                                "userName",
                                nameEditText!!.text.toString().trim { it <= ' ' }
                            )
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(
                                TAG,
                                "createUserWithEmail:failure",
                                task.exception
                            )
                            Toast.makeText(
                                this@SignInActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            //updateUI(null);
                        }

                        // ...
                    }
            }
        }
    }

    private fun createUser(firebaseUser: FirebaseUser?) {
        val user = User()
        user.id=firebaseUser!!.uid
        user.email=firebaseUser!!.email
        user.name=nameEditText!!.text.toString().trim { it <= ' ' }
        usersDatabaseReference!!.push().setValue(user)
    }

    fun toggleLoginMode(view: View?) {
        if (loginModeActive) {
            loginModeActive = false
            loginSignUpButton!!.text = "Sign Up"
            toggleLoginSignUpTextView!!.text = "Or, log in"
            repeatPasswordEditText!!.visibility = View.VISIBLE
            nameEditText!!.visibility=View.VISIBLE
        } else {
            loginModeActive = true
            loginSignUpButton!!.text = "Log In"
            toggleLoginSignUpTextView!!.text = "Or, sign up"
            repeatPasswordEditText!!.visibility = View.GONE
            nameEditText!!.visibility=View.GONE
        }
    }

    companion object {
        private const val TAG = "SignInActivity"
    }
}

package com.example.workouttracker

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentRegisterBinding
import com.example.workouttracker.utils.User
import com.example.workouttracker.utils.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var inputUsername: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputConfirmPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var gotoLogin: TextView
    private var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    private lateinit var progressDialog: ProgressDialog

    // firestore database
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        inputUsername = binding.usernameInput
        inputEmail = binding.emailInput
        inputPassword = binding.passwordInput
        inputConfirmPassword = binding.confirmPasswordInput
        registerButton = binding.registerButton
        gotoLogin = binding.gotoLogin

        progressDialog = ProgressDialog(requireContext())

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get().addOnSuccessListener { user ->
                Log.i("current user", "${user.data}")
            }.addOnFailureListener {
                Log.i("current user", "/")
            }
        }

        // register new user account
        registerButton.setOnClickListener {
            authenticate()
        }

        gotoLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        return binding.root
    }

    private fun authenticate() {
        val username = inputUsername.text.toString()
        val email = inputEmail.text.toString()
        val password = inputPassword.text.toString()
        val confirmPassword = inputConfirmPassword.text.toString()

        if (username.isEmpty()) {
            inputUsername.error = "Enter a username"
        }
        else if (!email.matches(emailPattern.toRegex())) {
            inputEmail.error = "Enter correct E-mail"
        }
        else if (password.isEmpty() || password.length < 6) {
            inputPassword.error = "Your password must be at least 6 characters long"
        }
        else if (password != confirmPassword) {
            inputConfirmPassword.error = "Passwords don't match"
        }
        else {
            progressDialog.setMessage("Please wait while registration takes place...")
            progressDialog.setTitle("Registration")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    val userId = user?.uid
                    val workouts = mutableListOf<Workout>()
                    val user = User(username, email, workouts)

                    Log.i("userID", userId.toString())

                    if (userId != null) {
                        firestore.collection("users").document(userId).set(user).addOnSuccessListener {
                            Log.i("user", "user successfully added")
                        }.addOnFailureListener {
                            Log.e("user", "user failed to be added")
                        }
                    }
                    else {
                        Log.i("user", "nobody is currently logged in")
                    }
                    progressDialog.dismiss()
                    Toast.makeText(context, "Your account was successfully created!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.loginFragment)
                }
                else {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Something went wrong when creating your account!", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
}
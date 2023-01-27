package com.example.workouttracker

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var createNewAccount: TextView
    private var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    private lateinit var progressDialog: ProgressDialog

    // firebase database
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        inputEmail = binding.emailInput
        inputPassword = binding.passwordInput
        loginButton = binding.loginButton
        createNewAccount = binding.createAccount

        progressDialog = ProgressDialog(requireContext())

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            login()
        }

        createNewAccount.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        return binding.root
    }

    private fun login() {
        val email = inputEmail.text.toString()
        val password = inputPassword.text.toString()

        if (!email.matches(emailPattern.toRegex())) {
            inputEmail.error = "Enter correct E-mail"
        }
        else if (password.isEmpty() || password.length < 6) {
            inputPassword.error = "Enter correct password"
        }
        else {
            progressDialog.setMessage("Please wait while login takes place...")
            progressDialog.setTitle("Login")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.homeFragment)
                }
                else {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Something went wrong during login!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
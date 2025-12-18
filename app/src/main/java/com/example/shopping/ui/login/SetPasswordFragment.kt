package com.example.shopping.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.shopping.R
import com.example.shopping.databinding.FragmentSetPasswordBinding
import com.example.shopping.ui.main.MainActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class SetPasswordFragment : Fragment() {

    private lateinit var binding: FragmentSetPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val hasPassword = user.providerData.any {
            it.providerId == EmailAuthProvider.PROVIDER_ID
        }
        if (hasPassword) {
            findNavController().navigate(R.id.navigation_home)
            return
        }
        binding.txtEmail.text = user.email

        binding.btnConfirm.setOnClickListener {
            val pwd = binding.edtPassword.text.toString()
            val confirm = binding.edtConfirmPassword.text.toString()

            if (pwd.length < 6) {
                toast("密碼至少 6 碼")
                return@setOnClickListener
            }

            if (pwd != confirm) {
                toast("密碼不一致")
                return@setOnClickListener
            }

            linkPassword(user.email!!, pwd)
        }
    }

    private fun linkPassword(email: String, password: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val credential = EmailAuthProvider.getCredential(email, password)

        user.linkWithCredential(credential)
            .addOnSuccessListener {
                toast("密碼設定完成")
                findNavController().navigate(R.id.navigation_home)
            }
            .addOnFailureListener { e ->
                toast(e.message ?: "設定失敗")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
    override fun onResume() {
        super.onResume()
        val main = requireActivity() as MainActivity
        main.showAuthToolbar("設定登入密碼")
    }
    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).binding.navView.visibility = View.VISIBLE
    }
}

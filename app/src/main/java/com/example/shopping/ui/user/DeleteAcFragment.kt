package com.example.shopping.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.R
import com.example.shopping.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeleteAcFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_delete_ac, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.apply {
            title = "刪除帳戶"
            setDisplayHomeAsUpEnabled(true)
        }

        val check1 = view.findViewById<CheckBox>(R.id.check1)
        val check2 = view.findViewById<CheckBox>(R.id.check2)
        val check3 = view.findViewById<CheckBox>(R.id.check3)
        val checkConfirm = view.findViewById<CheckBox>(R.id.checkConfirm)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteAccount)

        val listener = View.OnClickListener {
            btnDelete.isEnabled =
                check1.isChecked && check2.isChecked && check3.isChecked && checkConfirm.isChecked
        }

        check1.setOnClickListener(listener)
        check2.setOnClickListener(listener)
        check3.setOnClickListener(listener)
        checkConfirm.setOnClickListener(listener)

        btnDelete.setOnClickListener {
            deleteAccount()
        }
    }
    private fun deleteAccount() {
        val uid = UserSession.documentId
        if (uid.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "登入資訊失效，請重新登入", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(uid)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "帳戶已成功刪除", Toast.LENGTH_SHORT).show()

                UserSession.isLogin = false
                UserSession.email = ""
                UserSession.documentId = ""

                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "刪除帳戶失敗：${it.message}", Toast.LENGTH_SHORT).show()
            }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val main = requireActivity() as MainActivity
        main.showProductToolbar("刪除會員資格")
        main.hideProductActionBar()
        main.setFavoriteMenuVisible(false)
    }


    override fun onStop() {
        super.onStop()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}

package com.example.shopping.ui.user

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.R
import com.example.shopping.utils.UserSession
import com.example.shopping.databinding.FragmentUserBinding
import com.example.shopping.ui.user.adapter.CustomAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

data class UserItem(val usertv: String, val usertv2: String)

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    lateinit var logout: Button
    private lateinit var r1: RecyclerView


    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.userAvatar.setImageURI(uri)
            uploadAvatarToFirebase(uri)
        }
    }

    private val userItems = listOf(
        UserItem("帳戶設定", ">"),
        UserItem("購買紀錄", ">"),
        UserItem("我的最愛", ">"),
        UserItem("聯絡我們", ">"),
        UserItem("刪除會員資格", ">")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        val root = binding.root

        setupRecyclerView()
        loadUserBasicInfo()
        loadUserAvatar()


        binding.userAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        logout = root.findViewById(R.id.logout)
        logout.setOnClickListener {
            UserSession.isLogin = false
            UserSession.email = ""
            UserSession.documentId = ""
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return root
    }


    private fun setupRecyclerView() {
        r1 = binding.r1
        r1.adapter = CustomAdapter(userItems) { position ->
            when (position) {
                0 -> findNavController().navigate(R.id.action_userFragment_to_account)
                1 -> findNavController().navigate(R.id.purchaseHistoryFragment)
                2 -> findNavController().navigate(R.id.favoriteFragment)
                3 -> findNavController().navigate(R.id.contactUsFragment)
                4 -> findNavController().navigate(R.id.deleteAccountFragment)
            }
        }
        r1.layoutManager = LinearLayoutManager(activity)
    }


    private fun loadUserBasicInfo() {
        val docId = UserSession.documentId

        if (docId.isEmpty()) return

        firestore.collection("users")
            .document(docId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                binding.username.text = doc.getString("name") ?: "未設定名稱"
                binding.useremail.text = doc.getString("email") ?: ""
            }
    }


    private fun loadUserAvatar() {
        val docId = UserSession.documentId
        if (docId.isEmpty()) return

        val userRef = firestore.collection("users").document(docId)

        userRef.get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) return@addOnSuccessListener

                val avatarUrl = doc.getString("avatarUrl")
                if (avatarUrl.isNullOrEmpty()) {

                    userRef.update("avatarUrl", "")
                    binding.userAvatar.setImageResource(R.drawable.file)

                } else {

                    Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.file)
                        .into(binding.userAvatar)
                }
            }
    }



    private fun uploadAvatarToFirebase(uri: Uri) {
        val uid = UserSession.documentId
        if (uid.isEmpty()) return

        val ref = storage.reference.child("avatars/$uid.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveAvatarUrl(downloadUrl.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "上傳失敗", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveAvatarUrl(url: String) {
        val uid = UserSession.documentId
        if (uid.isEmpty()) return

        firestore.collection("users")
            .document(uid)
            .update("avatarUrl", url)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "頭像更新成功！", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "無法更新 Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        val main = requireActivity() as MainActivity
        main.showSimpleTitle("會員中心")
        main.hideProductActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.shopping.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shopping.R
import com.example.shopping.ui.main.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ContactFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val serviceEmail = "service@shopping.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.apply {
            title = "聯絡我們"
            setDisplayHomeAsUpEnabled(true)
        }

        val tvPhone = view.findViewById<TextView>(R.id.tvPhone)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val etFeedback = view.findViewById<EditText>(R.id.etFeedback)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)


        val sp = requireActivity().getSharedPreferences("userData", AppCompatActivity.MODE_PRIVATE)
        val uid = sp.getString("uid", null)

        if (uid == null) {
            Toast.makeText(requireContext(), "未登入或登入已過期，請重新登入", Toast.LENGTH_SHORT).show()
            return
        }


        tvPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0912345678"))
            startActivity(intent)
        }


        tvEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$serviceEmail"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "客服詢問")
            startActivity(intent)
        }


        btnSubmit.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()

            if (feedback.isEmpty()) {
                Toast.makeText(requireContext(), "請輸入回饋內容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val data = mapOf(
                "feedback" to feedback,
                "timestamp" to Timestamp.now(),
            )

            db.collection("users")
                .document(uid)
                .collection("feedback")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "回饋已提交！", Toast.LENGTH_SHORT).show()
                    etFeedback.setText("")
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "提交失敗，請稍後再試", Toast.LENGTH_SHORT).show()
                }
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
        main.showProductToolbar("聯絡我們")
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

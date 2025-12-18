package com.example.shopping.ui.user


import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.shopping.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.fragment.findNavController



class account : Fragment() {
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var editBirthday: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var editCity: EditText
    private lateinit var btnSave: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        (activity as AppCompatActivity).supportActionBar?.apply {
            title = "帳戶設定"
            setDisplayHomeAsUpEnabled(true)
        }

        editName = view.findViewById(R.id.editName)
        editEmail = view.findViewById(R.id.editEmail)
        editPhone = view.findViewById(R.id.editPhone)
        editBirthday = view.findViewById(R.id.editBirthday)
        spinnerGender = view.findViewById(R.id.spinnerGender)
        editCity = view.findViewById(R.id.editCity)
        btnSave = view.findViewById(R.id.btnSave)

        setupGenderSpinner()
        setupBirthdayPicker()

        loadUserData()
        btnSave.setOnClickListener {
            saveUserData()

        }
    }
    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                editName.setText(doc.getString("name") ?: "")
                editEmail.setText(doc.getString("email") ?: "")
                editPhone.setText(doc.getString("phone") ?: "")
                editBirthday.setText(doc.getString("birthday") ?: "")
                editCity.setText(doc.getString("address") ?: "")

                val gender = doc.getString("gender")
                val genderList = listOf("請選擇性別", "男", "女", "其他")
                val index = genderList.indexOf(gender)
                if (index >= 0) spinnerGender.setSelection(index)
            }
    }
    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return

        val data = mapOf(
            "name" to editName.text.toString(),
            "email" to editEmail.text.toString(),
            "phone" to editPhone.text.toString(),
            "birthday" to editBirthday.text.toString(),
            "gender" to spinnerGender.selectedItem.toString(),
            "address" to editCity.text.toString()
        )

        db.collection("users").document(uid)
            .update(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "資料已更新", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "更新失敗：${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun setupGenderSpinner() {
        val genderList = listOf("請選擇性別", "男", "女", "其他")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            genderList
        )
        spinnerGender.adapter = adapter
    }
    private fun setupBirthdayPicker() {
        editBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dateText = "${selectedYear}/${selectedMonth + 1}/${selectedDay}"
                    editBirthday.setText(dateText)
                },
                year, month, day
            ).show()
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
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.GONE
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
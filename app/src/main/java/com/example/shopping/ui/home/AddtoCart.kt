package com.example.shopping.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.bumptech.glide.Glide
import com.example.shopping.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.shopping.databinding.BottomsheetAddToCartBinding


class AddToCartBottomSheet(
    private val product: Product,
    private val onConfirm: (String, Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetAddToCartBinding? = null
    private val binding get() = _binding!!

    private var qty = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = BottomsheetAddToCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.previewImage.setImageResource(product.imageResId[0])
        binding.previewPrice.text = "NT$${product.price}"


        val sizes = listOf("US 7", "US 8", "US 9", "US 10")


        sizes.forEach { s ->
            binding.sizeGroup.addView(makeRadioButton(s))
        }


        binding.btnMinus.setOnClickListener {
            if (qty > 1) qty--
            binding.txtQty.text = qty.toString()
        }
        binding.btnPlus.setOnClickListener {
            qty++
            binding.txtQty.text = qty.toString()
        }


        binding.addToCartBtn.setOnClickListener {
            val size = getSelected(binding.sizeGroup)
            onConfirm(size, qty)
            dismiss()
        }

    }
    override fun onStart() {
        super.onStart()

        val sheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        sheet?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.setBackgroundResource(android.R.color.transparent)
        }
    }

    private fun getSelected(group: RadioGroup): String =
        group.findViewById<RadioButton>(group.checkedRadioButtonId)?.text?.toString() ?: ""

    private fun makeRadioButton(text: String): RadioButton {
        val btn = RadioButton(requireContext())
        btn.text = text
        btn.textSize = 16f
        return btn
    }
}

package com.example.finalproject

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import com.example.finalproject.databinding.DialogPostBinding

class CreatePostDialog(
    context: Context,
    private val onPostCreated: (String) -> Unit
) {
    private val dialog: AlertDialog
    private val binding: DialogPostBinding =
        DialogPostBinding.inflate(LayoutInflater.from(context))

    init {
        dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        binding.sendPostButton.setOnClickListener {
            val content = binding.editTextPost.text.toString().trim()
            if (content.isNotEmpty()) {
                onPostCreated(content)
                dialog.dismiss()
            } else {
                binding.editTextPost.error = "內容不能為空"
            }
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun show() {
        dialog.show()
    }
}

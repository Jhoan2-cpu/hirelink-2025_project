package com.example.hirelink_2025.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.hirelink_2025.R

class JobLocationDetailDialog : DialogFragment() {

    companion object {
        private const val ARG_PLACE_NAME = "place_name"
        private const val ARG_ADDRESS = "address"

        fun newInstance(placeName: String, address: String): JobLocationDetailDialog {
            val fragment = JobLocationDetailDialog()
            val args = Bundle()
            args.putString(ARG_PLACE_NAME, placeName)
            args.putString(ARG_ADDRESS, address)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_job_location_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val placeName = arguments?.getString(ARG_PLACE_NAME) ?: "Sin nombre"
        val address = arguments?.getString(ARG_ADDRESS) ?: "Sin dirección"

        val textPlaceName = view.findViewById<TextView>(R.id.textPlaceName)
        val textAddress = view.findViewById<TextView>(R.id.textAddress)
        val buttonClose = view.findViewById<Button>(R.id.buttonClose)

        textPlaceName.text = placeName
        textAddress.text = address

        buttonClose.setOnClickListener {
            dismiss()
        }
    }
}

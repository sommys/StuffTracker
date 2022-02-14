package hu.bme.aut.stufftracker.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import hu.bme.aut.stufftracker.activity.MainActivity
import hu.bme.aut.stufftracker.activity.takePictureWithPermissionCheck
import hu.bme.aut.stufftracker.adapter.StuffListAdapter
import hu.bme.aut.stufftracker.databinding.DialogUpdatePictureBinding
import hu.bme.aut.stufftracker.domain.Stuff


class UpdatePictureDialog(val mainActivity: MainActivity, val holder: StuffListAdapter.StuffViewHolder, val s: Stuff): DialogFragment() {
    companion object{
        const val REQUEST_CAMERA_IMAGE = 101
        const val REQUEST_PICK_IMAGE = 102
    }
    private lateinit var binding: DialogUpdatePictureBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogUpdatePictureBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnCamera.setOnClickListener {
            mainActivity.takePictureWithPermissionCheck(holder, s)
            dismiss()
        }
        binding.btnGallery.setOnClickListener {
            mainActivity.choosePicture(holder ,s)
            dismiss()
        }
    }
}
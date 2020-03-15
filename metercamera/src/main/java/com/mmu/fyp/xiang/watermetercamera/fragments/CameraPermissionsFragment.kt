package com.mmu.fyp.xiang.watermetercamera.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mmu.fyp.xiang.watermetercamera.CameraActivity


/**
 * The sole purpose of this fragment is to request permissions and, once granted, display the
 * camera to the user.
 */
class CameraPermissionsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPermissions(requireContext())) {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        } else {
            // If permissions have already been granted, go back activity open camera
            (requireActivity() as CameraActivity).startCameraFromPermissionsFragment(this)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {

            if (hasAllPermissionsGranted(grantResults)) {
                // Take the user to the success fragment when permission is granted
                Toast.makeText(requireContext().applicationContext, "Permission request granted", Toast.LENGTH_LONG).show()
                // If permissions have already been granted, open camera
                (requireActivity() as CameraActivity).startCameraFromPermissionsFragment(this)
            } else {
                Toast.makeText(requireContext().applicationContext, "Permission request denied", Toast.LENGTH_LONG).show()
                // If permissions denied, go back previous activity
                requireActivity().onBackPressed()
            }
        }
    }

    private fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    companion object {
        private val TAG = CameraPermissionsFragment::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 10
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        /** Convenience method used to check if all permCameraXActivityissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

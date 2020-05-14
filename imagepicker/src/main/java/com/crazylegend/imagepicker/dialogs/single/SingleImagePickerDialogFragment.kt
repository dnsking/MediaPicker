package com.crazylegend.imagepicker.dialogs.single

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.invoke
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.crazylegend.core.R
import com.crazylegend.core.abstracts.AbstractDialogFragment
import com.crazylegend.core.adapters.single.SingleAdapter
import com.crazylegend.core.databinding.FragmentImagesGalleryLayoutBinding
import com.crazylegend.core.modifiers.single.SinglePickerModifier
import com.crazylegend.extensions.viewBinding
import com.crazylegend.imagepicker.contracts.SinglePickerContracts
import com.crazylegend.imagepicker.images.ImageModel
import com.crazylegend.imagepicker.images.ImagesVM
import com.crazylegend.imagepicker.listeners.onImagePicked


/**
 * Created by crazy on 5/8/20 to long live and prosper !
 */
internal class SingleImagePickerDialogFragment : AbstractDialogFragment(R.layout.fragment_images_gallery_layout), SinglePickerContracts {

    override var onImagePicked: onImagePicked? = null
    override val binding by viewBinding(FragmentImagesGalleryLayoutBinding::bind)
    override val imagesVM by viewModels<ImagesVM>()
    override val modifier: SinglePickerModifier?
        get() = arguments?.getParcelable(modifierTag)

    override val singleAdapter by lazy {
        SingleAdapter {
            onImagePicked?.forImage(it as ImageModel)
            dismissAllowingStateLoss()
        }
    }

    override val askForStoragePermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    imagesVM.loadImages()
                } else {
                    Log.e(errorTag, "PERMISSION DENIED")
                    dismissAllowingStateLoss()
                }
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        askForStoragePermission(Manifest.permission.READ_EXTERNAL_STORAGE)

        setupUIForSinglePicker(binding.topIndicator, binding.gallery, singleAdapter, binding.title, binding.close,
        ::applyTitleModifications){
            modifier?.closeButtonModifier?.applyImageParamsRelativeLayout(it)
        }

        imagesVM.images.observe(viewLifecycleOwner) {
            singleAdapter.submitList(it)
        }
        handleUIIndicator(imagesVM.loadingIndicator, binding.loadingIndicator)

    }

    override fun applyTitleModifications(appCompatTextView: AppCompatTextView) {
        modifier?.titleTextModifier?.applyTextParams(appCompatTextView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onImagePicked = null
    }

    override fun addModifier(modifier: SinglePickerModifier) {
        arguments = bundleOf(modifierTag to modifier)
    }

}
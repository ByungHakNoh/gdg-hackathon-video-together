package org.personal.videotogether.view.fragments.home.nestonhomedetail

import android.Manifest
import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile_mine.*
import kotlinx.android.synthetic.main.fragment_profile_mine.nameTextCountTV
import kotlinx.android.synthetic.main.fragment_profile_mine.profileIV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.personal.videotogether.R
import org.personal.videotogether.util.DataState
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.util.view.ImageHandler
import org.personal.videotogether.util.view.ViewHandler
import org.personal.videotogether.view.dialog.ChoiceDialog
import org.personal.videotogether.viewmodel.UserStateEvent
import org.personal.videotogether.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileMineFragment
constructor(
    private val dataStateHandler: DataStateHandler,
    private val imageHandler: ImageHandler,
    private val viewHandler: ViewHandler
) : Fragment(R.layout.fragment_profile_mine), View.OnClickListener, ChoiceDialog.DialogListener, TextWatcher {

    private val TAG = javaClass.name

    private lateinit var homeDetailNavController: NavController

    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    // 프로필 이미지 편집관련 변수
    private lateinit var cameraImage: Uri
    private var profileImageBitmap: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeDetailNavController = Navigation.findNavController(view)
        subscribeObservers()
        setInitView()
        setListener()
    }

    private fun subscribeObservers() {
        // 사용자 정보를 room 으로부터 가져옴
        userViewModel.updateUserDataState.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Loading -> {
                    dataStateHandler.displayLoadingDialog(true, childFragmentManager)
                }
                is DataState.Success -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    requireActivity().onBackPressed()
                }
                is DataState.Error -> {
                    dataStateHandler.displayLoadingDialog(false, childFragmentManager)
                    dataStateHandler.displayError(dataState.exception.toString())
                }
            }
        })
    }

    private fun setInitView() {
        val userData = userViewModel.userData.value!!
        Glide.with(requireContext()).load(userData.profileImageUrl).into(profileIV)
        nameTV.text = userData.name
    }

    private fun setListener() {
        closeBtn.setOnClickListener(this)
        editProfileBtn.setOnClickListener(this)
        // ------- 프로필 수정 시 나타나는 뷰 -------
        editProfileBackBtn.setOnClickListener(this)
        confirmEditProfileBtn.setOnClickListener(this)
        editNameIB.setOnClickListener(this)
        // ------- 이름 수정 시 나타나는 뷰 -------
        closeEditNameBtn.setOnClickListener(this)
        confirmEditNameBtn.setOnClickListener(this)
        editProfileImageIB.setOnClickListener(this)
        clearEditNameIB.setOnClickListener(this)

        editNameET.addTextChangedListener(this)
    }

    // ------------------ 클릭 이벤트 리스너 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.closeBtn -> requireActivity().onBackPressed()
            R.id.editProfileBtn -> handleViewsByEditable(View.GONE, View.VISIBLE, View.VISIBLE)
            // ------- 프로필 수정 시 나타나는 뷰 -------
            R.id.editProfileBackBtn -> {
                handleViewsByEditable(View.VISIBLE, View.GONE, View.GONE)
                setInitView()
            }
            R.id.editNameIB -> {
                handleViewsByEditable(View.GONE, View.GONE)
                editNameCL.visibility = View.VISIBLE
                editNameET.setText(nameTV.text.toString())
            }
            // ------- 이름 수정 시 나타나는 뷰 -------
            R.id.closeEditNameBtn -> {
                handleViewsByEditable(null, View.VISIBLE)
                editNameCL.visibility = View.GONE
            }
            R.id.confirmEditNameBtn -> {
                handleViewsByEditable(null, View.VISIBLE)
                nameTV.text = editNameET.text.toString()
                editNameCL.visibility = View.GONE
            }
            R.id.editProfileImageIB -> {
                val bundle = Bundle().apply { putInt("arrayResource", R.array.cameraOrGallery) }
                val choiceDialog = ChoiceDialog().apply { arguments = bundle }
                choiceDialog.show(childFragmentManager, "Camera Or Gallery Dialog")
            }
            R.id.clearEditNameIB -> editNameET.setText("")

            // ------- 프로필 수정 완료 -------
            R.id.confirmEditProfileBtn -> {
                val userData = userViewModel.userData.value!!
                if (profileImageBitmap == null) {
                    if (nameTV.text.toString() == userData.name) {
                        Toast.makeText(requireContext(), "프로필이 변경 전과 동일합니다", Toast.LENGTH_SHORT).show()
                        // 이름만 변경했을 때
                    } else {
                        userViewModel.setStateEvent(UserStateEvent.UpdateProfile(userData.id, null, nameTV.text.toString()))
                    }
                    // 프로필 사진이 변경 되었을 때
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        imageHandler.bitmapToString(profileImageBitmap).onEach { dataState ->
                            when (dataState) {
                                is DataState.Success<String> -> {
                                    userViewModel.setStateEvent(UserStateEvent.UpdateProfile(userData.id, dataState.data, nameTV.text.toString()))
                                }
                                is DataState.Error -> {
                                    Log.i(TAG, "uploadUserProfile: ${dataState.exception}")
                                }
                            }
                        }.launchIn(CoroutineScope(Dispatchers.Main))
                    }
                }
            }
        }
    }

    // 프로필 수정 여부에 따라 visibility 를 관리하는 메소드
    private fun handleViewsByEditable(closeBtnVisibility: Int?, editNameHeaderVisibility: Int, editableViewsVisibility: Int? = null) {
        if (closeBtnVisibility != null) closeBtn.visibility = closeBtnVisibility // 프로필 프래그먼트 종료 버튼
        // 프로필 편집 시 보여지는 헤더
        editProfileBackBtn.visibility = editNameHeaderVisibility
        editProfileHeaderTV.visibility = editNameHeaderVisibility
        confirmEditProfileBtn.visibility = editNameHeaderVisibility
        // 이름 수정 버튼
        if (editableViewsVisibility != null) {
            editNameIB.visibility = editableViewsVisibility
            editProfileImageIB.visibility = editableViewsVisibility
        }
    }

    // ------------------ Text Change 리스너 ------------------
    override fun afterTextChanged(text: Editable?) {
        viewHandler.handleClearTextBtn(text!!.count(), clearEditNameIB)
        nameTextCountTV.text = String.format("%s/20", text.count())
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    // ------------------ 카메라 Or 갤러리 다이얼로그 리스너 ------------------
    override fun onChoice(whichDialog: Int, choice: String, itemPosition: Int?, id: Int?) {
        when (choice) {
            "갤러리" -> requestGalleryPermission.launch()
            "카메라" -> requestCameraPermission.launch()
        }
    }

    // ------------------ 퍼미션 + ActivityForResult ------------------
    // 갤러리 권한 다이얼로그
    private val requestGalleryPermission by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) { isGranted ->
            if (isGranted) {
                getGalleryImage.launch("image/*")
            } else {
                Toast.makeText(context, "갤러리 접근 권한을 취소하셨습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 외부 저장소에 쓰기 권한이 수락되면 카메라 켜기
    private val requestCameraPermission by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(), arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA)
        ) { isGranted ->
            if (isGranted[WRITE_EXTERNAL_STORAGE]!!) {
                if (isGranted[CAMERA]!!) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.TITLE, "pillPicture")
                        put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                    }
                    cameraImage = context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, cameraImage)
                    }
                    getCameraImage.launch(cameraIntent)
                } else {
                    Toast.makeText(requireContext(), "카메라 권한을 취소하셨습니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "저장소 쓰기 권한을 취소하셨습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val getGalleryImage by lazy {
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            if (imageUri != null) convertUriToBitmap(imageUri)
        }
    }

    private val getCameraImage by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            convertUriToBitmap(cameraImage)
        }
    }

    // 이미지 uri 를 비트맵 객체로 변환하여 전역 변수(나중에 서버에 업로드 시 사용)와 이미지 뷰에 사용
    private fun convertUriToBitmap(imageUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            imageHandler.imageUriToBitmap(requireContext(), imageUri).onEach { dataState ->
                when (dataState) {
                    is DataState.Success<Bitmap?> -> {
                        profileIV.setImageBitmap(dataState.data)
                        profileImageBitmap = dataState.data!!
                    }
                    is DataState.Error -> {
                        Log.i(TAG, "test : Error")
                    }
                }
            }.launchIn(CoroutineScope(Dispatchers.Main))
        }
    }
}
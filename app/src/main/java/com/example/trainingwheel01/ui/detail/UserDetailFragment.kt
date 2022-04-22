package com.example.trainingwheel01.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.trainingwheel01.R
import com.example.trainingwheel01.data.Result
import com.example.trainingwheel01.data.succeeded
import com.example.trainingwheel01.databinding.FragmentUserDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private val userId: String by lazy {
        if (arguments?.getString("user_id") == null) error("No user_id")
        arguments?.getString("user_id")!!
    }

    @Inject lateinit var assistedFactory: UserDetailViewModel.Factory
    private val viewModel: UserDetailViewModel by viewModels {
        UserDetailViewModel.provideFactory(assistedFactory, userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentUserDetailBinding.bind(view)
        binding.bindState(
            uiState = viewModel.state
        )
    }

    private fun FragmentUserDetailBinding.bindState(
        uiState: StateFlow<UiState>
    ) {
        lifecycleScope.launchWhenCreated {
            uiState.collectLatest {
                Timber.d("User Detail: ${it.userData}")
                progressBar.isVisible = it.userData is Result.Loading
                if (it.userData.succeeded) {
                    val userData = (it.userData as Result.Success).data
                    Glide.with(profileImage)
                        .load(userData.photoLarge)
                        .into(profileImage)
                    name.text = userData.name
                }
            }
        }
    }
}
package com.example.trainingwheel01.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.example.trainingwheel01.R
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.databinding.FragmentUserListBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class UserListFragment : Fragment() {

    private val viewModel: UserListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentUserListBinding.bind(view)

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingUserDataFlow,
            uiActions = viewModel.accept
        )
    }

    private fun FragmentUserListBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<UserData>>,
        uiActions: (UiAction) -> Unit
    ) {
        val usersAdapter = UsersAdapter()
        val header = UserLoadStateAdapter { usersAdapter.retry() }
        list.adapter = usersAdapter.withLoadStateHeaderAndFooter(
            header = header,
            footer = UserLoadStateAdapter { usersAdapter.retry() }
        )
        val flexboxLayoutManager = FlexboxLayoutManager(list.context, FlexDirection.ROW, FlexWrap.WRAP)
        flexboxLayoutManager.justifyContent = JustifyContent.SPACE_EVENLY
        list.layoutManager = flexboxLayoutManager

        bindList(
            header = header,
            usersAdapter = usersAdapter,
            uiState = uiState,
            pagingData = pagingData
        )
    }

    private fun FragmentUserListBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onQueryChanged: (UiAction.Search) -> Unit
    ) {
        // TODO: bind search
    }

    private fun FragmentUserListBinding.bindList(
        header: UserLoadStateAdapter,
        usersAdapter: UsersAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<UserData>>
    ) {
        retryButton.setOnClickListener { usersAdapter.retry() }

        lifecycleScope.launchWhenCreated {
            pagingData.collectLatest {
                Timber.d("Data fetched: $it")
                usersAdapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            usersAdapter.loadStateFlow.collect { loadState ->
                Timber.d("Load State: $loadState")

                val isListEmpty = loadState.refresh is LoadState.NotLoading && usersAdapter.itemCount == 0
                // show empty list
                emptyList.isVisible = isListEmpty
                // Only show the list if refresh succeeds, either from the the local db or the remote.
                list.isVisible =  loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading
//                list.isVisible = true
                // Show loading spinner during initial load or refresh.
                progressBar.isVisible = loadState.source?.refresh is LoadState.Loading
                // Show the retry state if initial load or refresh fails.
                retryButton.isVisible = loadState.source?.refresh is LoadState.Error && usersAdapter.itemCount == 0

                val errorState = loadState.mediator?.append as? LoadState.Error
                    ?: loadState.mediator?.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                errorState?.let {
                    Toast.makeText(
                        context,
                        "Error: ${it.error}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
package com.example.trainingwheel01.ui.users

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.trainingwheel01.R
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.databinding.FragmentUserListBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
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
        val usersAdapter = UsersAdapter { position, userData ->
            // TODO: goto detail
            val directions = UserListFragmentDirections.actionUserListFragmentToUserDetailFragment(userId = userData.uuid)
            findNavController().navigate(directions)
        }
        val header = UserLoadStateAdapter { usersAdapter.retry() }
        list.adapter = usersAdapter.withLoadStateHeaderAndFooter(
            header = header,
            footer = UserLoadStateAdapter { usersAdapter.retry() }
        )
        /*val flexboxLayoutManager = FlexboxLayoutManager(list.context, FlexDirection.ROW, FlexWrap.WRAP)
        flexboxLayoutManager.justifyContent = JustifyContent.SPACE_EVENLY
        list.layoutManager = flexboxLayoutManager*/
        val spanCount: Int
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 2
        } else {
            spanCount = 4
        }
        val gridLayoutManager = GridLayoutManager(list.context, spanCount)
        list.layoutManager = gridLayoutManager
        /*val spanCount: Int
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 2
        } else {
            spanCount = 4
        }
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        list.layoutManager = staggeredGridLayoutManager*/

        swipeRefreshLayout.setOnRefreshListener { usersAdapter.refresh() }

        bindSearch(
            uiState = uiState,
            onQueryChanged = uiActions
        )
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
        searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                filterUsersListFromInput(onQueryChanged)
                true
            } else {
                false
            }
        }

        searchBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                filterUsersListFromInput(onQueryChanged)
                true
            } else {
                false
            }
        }

        searchBox.addTextChangedListener {
            val typedText = it.toString()
            if (typedText.isEmpty()) {
                filterUsersListFromInput(onQueryChanged)
            }
        }

        lifecycleScope.launchWhenCreated {
            uiState
                .map { it.query }
                .distinctUntilChanged()
                .collect(searchBox::setText)
        }
    }

    private fun FragmentUserListBinding.filterUsersListFromInput(onQueryChanged: (UiAction.Search) -> Unit) {
        searchBox.text.trim().let {
            if (it.isNotEmpty()) {
                list.scrollToPosition(0)
                onQueryChanged(UiAction.Search(query = it.toString()))
            } else {
                onQueryChanged(UiAction.Search(query = ""))
            }
        }
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

                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = loadState.mediator?.refresh is LoadState.Loading
                }

                val isListEmpty = loadState.refresh is LoadState.NotLoading && usersAdapter.itemCount == 0
                // show empty list
                emptyList.isVisible = isListEmpty
                // Only show the list if refresh succeeds, either from the the local db or the remote.
                list.isVisible =  loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading
//                list.isVisible = true
                // Show loading spinner during initial load or refresh.
                progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
                // Show the retry state if initial load or refresh fails.
                retryButton.isVisible = loadState.mediator?.refresh is LoadState.Error && usersAdapter.itemCount == 0

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
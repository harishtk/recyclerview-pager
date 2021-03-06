package com.example.trainingwheel01.ui.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.repository.DefaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: DefaultRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val state: StateFlow<UiState>

    val pagingUserDataFlow: Flow<PagingData<UserData>>
    val pagingUserSearchResults: Flow<PagingData<UserData>>

    val accept: (UiAction) -> Unit

    init {
        val lastQuery: String = savedStateHandle.get(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        val actionStateFlow = MutableSharedFlow<UiAction>()

        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Search(query = lastQuery)) }

        // TODO: filter results for query
        pagingUserDataFlow = repository.getUsers()
            .cachedIn(viewModelScope)
        pagingUserSearchResults = searches
            .flatMapLatest { repository.getUsers(filter = it.query) }
        /*.combine(searches) { pagingData, searchAction ->
            pagingData.filter { it.name.startsWith(searchAction.query, ignoreCase = true) }
        }
        .cachedIn(viewModelScope)*/

        state = searches
            .map { search ->
                UiState(query = search.query, searching = search.query.isNotEmpty())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState(loading = true)
            )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

}

sealed class UiAction {
    data class Search(val query: String) : UiAction()
}

data class UiState(
    val query: String = DEFAULT_QUERY,
    val loading: Boolean = false,
    val searching: Boolean = false
)

private const val LAST_SEARCH_QUERY: String = "last_search_query"
private const val DEFAULT_QUERY: String = ""
package com.example.trainingwheel01

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.trainingwheel01.data.repository.DefaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DefaultRepository
) : ViewModel() {

    var pagingUserDataFlow = repository.getUsers()
        .cachedIn(viewModelScope)
}
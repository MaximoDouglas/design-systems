package br.com.argmax.githubconsumer.ui.modules.gitrepositories

import androidx.lifecycle.*
import br.com.argmax.githubconsumer.domain.entities.repository.GitRepositoryDto
import br.com.argmax.githubconsumer.service.gitrepository.GitRepositoryRemoteDataSource
import br.com.argmax.githubconsumer.utils.CoroutineContextProvider
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class SelectGitRepositoryViewModel(
    private val gitRepositoryRemoteDataSource: GitRepositoryRemoteDataSource,
    private val contextProvider: CoroutineContextProvider
) : ViewModel() {

    private val stateLiveData = MutableLiveData<SelectGitRepositoryViewModelState>()

    fun getStateLiveData(): LiveData<SelectGitRepositoryViewModelState> = stateLiveData

    private val handler = CoroutineExceptionHandler { _, exception ->
        stateLiveData.value = SelectGitRepositoryViewModelState.Error(exception)
    }

    fun getGitRepositoryApiResponse(page: Int) {
        stateLiveData.value = SelectGitRepositoryViewModelState.Loading
        viewModelScope.launch(handler) {
            val data = withContext(contextProvider.IO) {
                gitRepositoryRemoteDataSource.getGitRepositoryDtoList(page)
            }

            stateLiveData.value = SelectGitRepositoryViewModelState.Success(data)
        }
    }

    sealed class SelectGitRepositoryViewModelState {

        object Loading : SelectGitRepositoryViewModelState()
        data class Error(val throwable: Throwable) : SelectGitRepositoryViewModelState()
        data class Success(val data: List<GitRepositoryDto>) : SelectGitRepositoryViewModelState()

    }

    class SelectGitRepositoryViewModelFactory(
        private val gitRepositoryRemoteDataSource: GitRepositoryRemoteDataSource,
        private val contextProvider: CoroutineContextProvider
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectGitRepositoryViewModel(gitRepositoryRemoteDataSource, contextProvider) as T
        }

    }

}
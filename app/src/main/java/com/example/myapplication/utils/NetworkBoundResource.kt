package com.example.myapplication.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

// Generic class for network-bound resources
// Based on Google's architecture components guide
abstract class NetworkBoundResource<ResultType, RequestType>(
    private val scope: CoroutineScope,
    private val onFetchFailed: (Exception) -> Unit = {}
) {

    val result: MediatorLiveData<Resource<ResultType>> = MediatorLiveData().apply {
        value = Resource.Loading(null)
    }

    init {
        scope.launch {
            val dbSource = loadFromDb()
            val mediator = MediatorLiveData<ResultType>()
            mediator.addSource(dbSource) { data ->
                mediator.removeSource(dbSource)
                if (shouldFetch(data)) {
                    fetchFromNetwork(data)
                } else {
                    result.postValue(Resource.Success(data))
                }
            }
        }
    }

    private fun fetchFromNetwork(dbData: ResultType?) {
        result.postValue(Resource.Loading(dbData))
        
        scope.launch {
            try {
                val apiResponse = createCall()
                apiResponse.collect { response ->
                    if (response.isSuccessful) {
                        saveCallResult(response)
                        val freshData = loadFromDb().value
                        result.postValue(Resource.Success(freshData!!))
                    } else {
                        onFetchFailed(Exception("API error: ${response.code()}"))
                        result.postValue(Resource.Error("API error: ${response.code()}", dbData))
                    }
                }
            } catch (e: Exception) {
                onFetchFailed(e)
                result.postValue(Resource.Error(e.message ?: "Unknown error", dbData))
            }
        }
    }

    // To be implemented by subclasses
    protected abstract fun loadFromDb(): Flow<ResultType>
    protected abstract fun shouldFetch(data: ResultType?): Boolean
    protected abstract fun createCall(): Flow<RequestType>
    protected abstract fun saveCallResult(item: RequestType)
}

sealed class Resource<out T>(val data: T?, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
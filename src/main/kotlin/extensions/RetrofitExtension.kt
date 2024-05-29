package extensions


import kotlinx.coroutines.*
import okio.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> Call<T>.enqueue(callback: CallBackKt<T>.() -> Unit)
{
    val callBackKt = CallBackKt<T>()
    callback.invoke(callBackKt)
    this.enqueue(callBackKt)
}

class CallBackKt<T>() : Callback<T>
{

    private var onResponse: ((Response<T>) -> Unit)? = null
    private var onFailure: ((t: Throwable?) -> Unit)? = null

    override fun onFailure(call: Call<T>, t: Throwable)
    {
        if (t is IOException)
        {
            println("Bad Network : $t")
        }

        t.printStackTrace()
        onFailure?.invoke(t)
    }

    override fun onResponse(call: Call<T>, response: Response<T>)
    {
        onResponse?.invoke(response)
    }

}

@OptIn(DelicateCoroutinesApi::class)
fun <T> Call<T>.executeSync(): T?
{

    try
    {
        val response = execute()

        if (response.body() == null)
            throw RuntimeException("Body Null")

        return response.body()
    }
    catch (e: Exception)
    {
        GlobalScope.launch {
            if (e is IOException)
            {
                println("Bad Network : $e")
            }
            else if (e is RuntimeException)
            {
                println("Server Error : $e")
            }
            else
            {
                println("Unknown Error : $e")
            }
            e.printStackTrace()
        }

    }
    return null
}


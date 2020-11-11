package ch.treasurekeep.service.interactivebrokers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.*
import java.io.IOException
import ch.treasurekeep.service.interactivebrokers.CurrencyConversionCallback as CurrencyConversionCallback1

interface CurrencyConversionCallback{
    fun calculated(value:Double)
    fun error()
}

fun convertCurrency(baseCurrency:String, destinationCurrency: String, amount:Double, apikey:String, callback: CurrencyConversionCallback1) {
    val url = "https://v3.exchangerate-api.com/bulk/" + apikey + "/" + baseCurrency;
    val httpclient = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()
    httpclient.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback.error();
        }
        override fun onResponse(call: Call, response: Response) {
            var exrates = response.body()?.string();
            var element  =  Json.parseToJsonElement(exrates.toString())
            if (element != null) {
                val rate = element.jsonObject.get("rates")?.jsonObject?.get(destinationCurrency).toString().toDouble()
                callback.calculated(rate.times(amount));
            }
            else  { callback.error() }
        }
    })
}

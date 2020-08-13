package org.personal.videotogether.model.server

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestData (
    @SerializedName("request")
    @Expose
    val request: String,

    @SerializedName("data")
    @Expose
    val data: HashMap<String, *>
)
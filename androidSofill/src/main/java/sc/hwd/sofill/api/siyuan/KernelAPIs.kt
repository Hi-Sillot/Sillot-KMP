package sc.hwd.sofill.api.siyuan

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import sc.hwd.sofill.interfaces.gibbet.INbList
import sc.hwd.sofill.interfaces.gibbet.IPayload
import sc.hwd.sofill.interfaces.gibbet.IResponse

interface SiyuanNoteAPI {
    @Headers("Content-Type: application/json")
    @POST("api/notebook/lsNotebooks")
    fun getNotebooks(@Header("Authorization") Authorization: String?, @Body body: Map<String, Boolean>): Call<IResponse<INbList>>

    @Headers("Content-Type: application/json")
    @POST("api/filetree/createDocWithMd")
    fun createNote(
        @Body payload: IPayload,
        @Header("Authorization") Authorization: String?
    ): Call<IResponse<String>>
}

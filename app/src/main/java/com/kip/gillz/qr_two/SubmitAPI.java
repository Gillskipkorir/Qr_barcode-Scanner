package com.kip.gillz.qr_two;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;


public interface SubmitAPI
{

    @FormUrlEncoded
    @POST("/Barcode/barcode.php")
    public void insertUser(
            @Field("Cod") String Cod,

            Callback<Response> callback);
}

package com.fusion.fusionchat.Services;

import com.fusion.fusionchat.Models.MsgModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RetrofitAPI {
    @GET
    Call<MsgModel> getMessage(@Url String url);
}

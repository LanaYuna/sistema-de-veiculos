package com.example.atividade_2.api;

import com.example.atividade_2.model.Modelo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ModeloAPIService {

    @GET("modelos")
    Call<List<Modelo>> listar(@Query("marca") Long idMarca);
    @POST("modelos")
    Call<Modelo> inserir(@Body Modelo m);
    @PUT("modelos/{id}")
    Call<Void> alterar(@Path("id") long id, @Body Modelo m);

    @DELETE("modelos/{id}")
    Call<Void> remover(@Path("id") long id);



}

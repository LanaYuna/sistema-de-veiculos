package com.example.atividade_2.api;

import com.example.atividade_2.model.Marca;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MarcaAPIService {

    @POST("marcas")
    Call<Marca> inserir(@Body Marca m);

    @GET("marcas")
    Call<List<Marca>> listar();

    @GET("marcas/{id}")
    Call<Marca> buscar(@Path("id") long idMarca);

    @PUT("marcas/{id}")
    Call<Marca> alterar(@Path("id") long id, @Body Marca m);

    @DELETE("marcas/{id}")
    Call<Void> remover(@Path("id") long id);

}

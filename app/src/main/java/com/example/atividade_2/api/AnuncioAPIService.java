package com.example.atividade_2.api;


import com.example.atividade_2.model.Anuncio;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AnuncioAPIService {
    @POST("cidades")
    Call<Anuncio> inserir(@Body Anuncio a);

    @GET("cidades")
    Call<List<Anuncio>> listar(@Query("modelo") Long idModelo,
                               @Query("tipo") String tipo,
                               @Query("ano_inicial") Integer anoInicial,
                               @Query("ano_final") Integer anoFinal,
                               @Query("min") Double min,
                               @Query("max") Double max);
    @PUT("cidades/{id}")
    Call<Void> alterar(@Path("id") long id, @Body Anuncio a);

    @DELETE("cidades/{id}")
    Call<Void> remover(@Path("id") long id);
}

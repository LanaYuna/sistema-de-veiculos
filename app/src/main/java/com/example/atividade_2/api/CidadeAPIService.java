package com.example.atividade_2.api;

import com.example.atividade_2.model.Cidade;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CidadeAPIService {

    @POST("cidades")
    Call<Cidade> inserir(@Body Cidade c);

    @GET("cidades")
    Call<List<Cidade>> listar();

    @GET("cidades/{id}")
    Call<Cidade> buscar(@Path("nome") String nome);

    @PUT("cidades/{id}")
    Call<Cidade> alterar(@Path("id") long id, @Body Cidade c);

    @DELETE("cidades/{id}")
    Call<Void> remover(@Path("id") long id);
}

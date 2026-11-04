package com.example.atividade_2.model;

public class Anuncio {
    private Long id;
    private Modelo modelo;
    private Cidade cidade;
    private String descricao;
    private String cor;
    private Double valor;
    private Integer ano;
    private Integer km;
    private Long idCidade;
    private Long idModelo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Modelo getModelo() {
        return modelo;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getKm() { return km; }

    public void setKm(Integer km) {
        this.km = km;
    }

    public Long getIdCidade() {
        return idCidade;
    }

    public void setIdCidade(Long idCidade) {
        this.idCidade = idCidade;
    }

    public Long getIdModelo() { return idModelo; }

    public void setIdModelo(Long idModel) {
        this.idModelo = idModel;
    }

    @Override
    public String toString(){
        return this.descricao;
    }

}

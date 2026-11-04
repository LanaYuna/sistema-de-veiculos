package com.example.atividade_2;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.atividade_2.api.AnuncioAPIService;
import com.example.atividade_2.api.CidadeAPIService;
import com.example.atividade_2.api.ModeloAPIService;
import com.example.atividade_2.model.Anuncio;
import com.example.atividade_2.model.Cidade;
import com.example.atividade_2.model.Modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnunciosActivity extends AppCompatActivity {

    static final int ANO_MIN = 2000;
    static final int PASSO_VALOR = 10000;
    static final int QTD_VALORES = 20;

    AnuncioAPIService serviceAnuncio;
    ModeloAPIService serviceModelo;
    CidadeAPIService serviceCidade;

    Spinner spinnerModelo, spinnerCidade, spinnerFiltroTipo;
    EditText edDescricao, edCor, edValor, edAno, edKm;

    Spinner spinnerFiltroModelo, spinnerFiltroAnoInicial, spinnerFiltroAnoFinal,
            spinnerFiltroMin, spinnerFiltroMax;
    ArrayList<Object> itensFiltroModelo;
    ArrayAdapter<Object> adapterFiltroModelo;

    ListView lista;
    ArrayList<Anuncio> anuncios;
    ArrayAdapter<Anuncio> adapterAnuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anuncios);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerModelo = findViewById(R.id.spinnerModelo);
        spinnerCidade = findViewById(R.id.spinnerCidade);
        edDescricao = findViewById(R.id.edDescricao);
        edCor = findViewById(R.id.edCor);
        edValor = findViewById(R.id.edValor);
        edAno = findViewById(R.id.edAno);
        edKm = findViewById(R.id.edKm);

        spinnerFiltroModelo = findViewById(R.id.spinnerFiltroModelo);
        spinnerFiltroTipo = findViewById(R.id.spinnerFiltroTipo);
        spinnerFiltroAnoInicial = findViewById(R.id.spinnerFiltroAnoInicial);
        spinnerFiltroAnoFinal = findViewById(R.id.spinnerFiltroAnoFinal);
        spinnerFiltroMin = findViewById(R.id.spinnerFiltroMin);
        spinnerFiltroMax = findViewById(R.id.spinnerFiltroMax);
        lista = findViewById(R.id.lista_anuncios);

        anuncios = new ArrayList<>();
        adapterAnuncio = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, anuncios);
        lista.setAdapter(adapterAnuncio);
        registerForContextMenu(lista);

        serviceAnuncio = RetrofitClient.getClient().create(AnuncioAPIService.class);
        serviceModelo = RetrofitClient.getClient().create(ModeloAPIService.class);
        serviceCidade = RetrofitClient.getClient().create(CidadeAPIService.class);

        configurarFiltros();
        carregarModelosParaSpinner();
        carregarCidadesParaSpinner();

        findViewById(R.id.btSalvarAnuncio).setOnClickListener(v -> confirmar());
    }

    void configurarFiltros() {
        itensFiltroModelo = new ArrayList<>();
        itensFiltroModelo.add(getString(R.string.qualquer));
        adapterFiltroModelo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, itensFiltroModelo);
        adapterFiltroModelo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroModelo.setAdapter(adapterFiltroModelo);

        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        List<String> anos = new ArrayList<>();
        anos.add(getString(R.string.qualquer));
        for (int a = ANO_MIN; a <= anoAtual; a++) anos.add(String.valueOf(a));
        spinnerFiltroAnoInicial.setAdapter(criarAdapter(anos));
        spinnerFiltroAnoFinal.setAdapter(criarAdapter(anos));

        List<String> valores = new ArrayList<>();
        valores.add(getString(R.string.qualquer));
        for (int i = 1; i <= QTD_VALORES; i++) {
            valores.add("R$ " + String.format("%,d", i * PASSO_VALOR));
        }
        spinnerFiltroMin.setAdapter(criarAdapter(valores));
        spinnerFiltroMax.setAdapter(criarAdapter(valores));

        List<String> tipos = new ArrayList<>();
        tipos.add(getString(R.string.qualquer));
        tipos.addAll(Arrays.asList("SEDAN", "HATCH", "SUV", "CAMIONETE"));
        spinnerFiltroTipo.setAdapter(criarAdapter(tipos));

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                buscarAnuncios();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                buscarAnuncios();
            }
        };
        spinnerFiltroModelo.setOnItemSelectedListener(listener);
        spinnerFiltroAnoInicial.setOnItemSelectedListener(listener);
        spinnerFiltroAnoFinal.setOnItemSelectedListener(listener);
        spinnerFiltroMin.setOnItemSelectedListener(listener);
        spinnerFiltroMax.setOnItemSelectedListener(listener);
        spinnerFiltroTipo.setOnItemSelectedListener(listener);
    }

    ArrayAdapter<String> criarAdapter(List<String> itens) {
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, itens);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return ad;
    }

    void buscarAnuncios() {
        Long idModelo = null;
        Object itemModelo = spinnerFiltroModelo.getSelectedItem();
        if (itemModelo instanceof Modelo) {
            idModelo = ((Modelo) itemModelo).getId();
        }

        String tipo = null;
        if (spinnerFiltroTipo.getSelectedItemPosition() > 0) {
            tipo = (String) spinnerFiltroTipo.getSelectedItem();
        }

        int posAnoIni = spinnerFiltroAnoInicial.getSelectedItemPosition();
        int posAnoFim = spinnerFiltroAnoFinal.getSelectedItemPosition();
        int posMin = spinnerFiltroMin.getSelectedItemPosition();
        int posMax = spinnerFiltroMax.getSelectedItemPosition();

        Integer anoInicial = posAnoIni > 0 ? ANO_MIN + posAnoIni - 1 : null;
        Integer anoFinal = posAnoFim > 0 ? ANO_MIN + posAnoFim - 1 : null;
        Double min = posMin > 0 ? (double) posMin * PASSO_VALOR : null;
        Double max = posMax > 0 ? (double) posMax * PASSO_VALOR : null;

        if (min != null && max != null && min > max) {
            Toast.makeText(this, getString(R.string.erroValorMinMaiorMax), Toast.LENGTH_SHORT).show();
            return;
        }
        if (anoInicial != null && anoFinal != null && anoInicial > anoFinal) {
            Toast.makeText(this, getString(R.string.erroAnoIniMaiorFim), Toast.LENGTH_SHORT).show();
            return;
        }

        serviceAnuncio.listar(idModelo, tipo, anoInicial, anoFinal, min, max)
                .enqueue(new Callback<List<Anuncio>>() {
                    @Override
                    public void onResponse(Call<List<Anuncio>> call, Response<List<Anuncio>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapterAnuncio.clear();
                            adapterAnuncio.addAll(response.body());
                            adapterAnuncio.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Anuncio>> call, Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(AnunciosActivity.this,
                                getString(R.string.erro) + " " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void carregarModelosParaSpinner() {
        serviceModelo.listar(null).enqueue(new Callback<List<Modelo>>() {
            @Override
            public void onResponse(Call<List<Modelo>> call, Response<List<Modelo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Modelo> listaModelos = response.body();

                    ArrayAdapter<Modelo> adapterSpinnerModelo = new ArrayAdapter<>(
                            AnunciosActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            listaModelos);
                    spinnerModelo.setAdapter(adapterSpinnerModelo);

                    itensFiltroModelo.clear();
                    itensFiltroModelo.add(getString(R.string.qualquer));
                    itensFiltroModelo.addAll(listaModelos);
                    adapterFiltroModelo.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Modelo>> call, Throwable throwable) {
                Toast.makeText(AnunciosActivity.this, getString(R.string.erroCarregarModelos), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void carregarCidadesParaSpinner() {
        serviceCidade.listar(null).enqueue(new Callback<List<Cidade>>() {
            @Override
            public void onResponse(Call<List<Cidade>> call, Response<List<Cidade>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayAdapter<Cidade> adapterSpinnerCidade = new ArrayAdapter<>(
                            AnunciosActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            response.body());
                    spinnerCidade.setAdapter(adapterSpinnerCidade);
                }
            }

            @Override
            public void onFailure(Call<List<Cidade>> call, Throwable throwable) {
                Toast.makeText(AnunciosActivity.this, getString(R.string.erroCarregarCidades), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void confirmar() {
        String descricao = edDescricao.getText().toString().trim();
        String cor = edCor.getText().toString().trim();
        String valorTxt = edValor.getText().toString().trim();
        String anoTxt = edAno.getText().toString().trim();
        String kmTxt = edKm.getText().toString().trim();

        Modelo modeloSelecionado = (Modelo) spinnerModelo.getSelectedItem();
        Cidade cidadeSelecionada = (Cidade) spinnerCidade.getSelectedItem();

        if (modeloSelecionado != null && cidadeSelecionada != null && !descricao.isBlank() &&
                !cor.isBlank() && !valorTxt.isBlank() && !anoTxt.isBlank() && !kmTxt.isBlank()) {

            Anuncio a = new Anuncio();
            a.setValor(Double.parseDouble(valorTxt));
            a.setAno(Integer.parseInt(anoTxt));
            a.setKm(Integer.parseInt(kmTxt));
            a.setDescricao(descricao);
            a.setCor(cor);
            a.setIdModelo(modeloSelecionado.getId());
            a.setIdCidade(cidadeSelecionada.getId());
            edDescricao.setText("");
            edCor.setText("");
            edValor.setText("");
            edAno.setText("");
            edKm.setText("");

            serviceAnuncio.inserir(a).enqueue(new Callback<Anuncio>() {
                @Override
                public void onResponse(Call<Anuncio> call, Response<Anuncio> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Anuncio anuncioCriadoNoServidor = response.body();
                        adapterAnuncio.add(anuncioCriadoNoServidor);
                        adapterAnuncio.notifyDataSetChanged();
                        buscarAnuncios();
                        Toast.makeText(AnunciosActivity.this, getString(R.string.anuncioSalvo), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AnunciosActivity.this,
                                getString(R.string.erroServidor), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Anuncio> call, Throwable throwable) {
                    throwable.printStackTrace();
                    Toast.makeText(AnunciosActivity.this,
                            getString(R.string.erroRede) + ": " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.valoresInvalidos), Toast.LENGTH_SHORT).show();
        }
    }

    void alterar(Anuncio anuncio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.editarAnuncio);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);

        TextView lblModelo = new TextView(this);
        lblModelo.setText(R.string.modelo);
        layout.addView(lblModelo);

        final Spinner spinnerEditModelo = new Spinner(this);
        spinnerEditModelo.setAdapter(spinnerModelo.getAdapter());
        if (anuncio.getModelo() != null) {
            for (int i = 0; i < spinnerEditModelo.getCount(); i++) {
                Modelo m = (Modelo) spinnerEditModelo.getItemAtPosition(i);
                if (Objects.equals(m.getId(), anuncio.getModelo().getId())) {
                    spinnerEditModelo.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spinnerEditModelo);

        TextView lblCidade = new TextView(this);
        lblCidade.setText(R.string.cidade);
        layout.addView(lblCidade);

        final Spinner spinnerEditCidade = new Spinner(this);
        spinnerEditCidade.setAdapter(spinnerCidade.getAdapter());
        if (anuncio.getCidade() != null) {
            for (int i = 0; i < spinnerEditCidade.getCount(); i++) {
                Cidade c = (Cidade) spinnerEditCidade.getItemAtPosition(i);
                if (Objects.equals(c.getId(), anuncio.getCidade().getId())) {
                    spinnerEditCidade.setSelection(i);
                    break;
                }
            }
        }
        layout.addView(spinnerEditCidade);

        final EditText inputDescricao = new EditText(this);
        inputDescricao.setHint(R.string.descricao);
        inputDescricao.setText(anuncio.getDescricao());
        layout.addView(inputDescricao);

        final EditText inputCor = new EditText(this);
        inputCor.setHint(R.string.cor);
        inputCor.setText(anuncio.getCor());
        layout.addView(inputCor);

        final EditText inputValor = new EditText(this);
        inputValor.setHint(R.string.valor);
        inputValor.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputValor.setText(String.valueOf(anuncio.getValor()));
        layout.addView(inputValor);

        final EditText inputAno = new EditText(this);
        inputAno.setHint(R.string.ano);
        inputAno.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputAno.setText(String.valueOf(anuncio.getAno()));
        layout.addView(inputAno);

        final EditText inputKm = new EditText(this);
        inputKm.setHint(R.string.km);
        inputKm.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputKm.setText(String.valueOf(anuncio.getKm()));

        layout.addView(inputKm);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        builder.setView(scroll);

        builder.setPositiveButton(R.string.salvar, (dialog, which) -> {
            String novaDescricao = inputDescricao.getText().toString().trim();
            String novaCor = inputCor.getText().toString().trim();
            String novoValorTxt = inputValor.getText().toString().trim();
            String novoAnoTxt = inputAno.getText().toString().trim();
            String novoKmTxt = inputKm.getText().toString().trim();

            Modelo novoModelo = (Modelo) spinnerEditModelo.getSelectedItem();
            if (novoModelo == null) novoModelo = anuncio.getModelo();
            Cidade novaCidade = (Cidade) spinnerEditCidade.getSelectedItem();
            if (novaCidade == null) novaCidade = anuncio.getCidade();

            if (novaDescricao.isEmpty() || novaCor.isEmpty()) {
                Toast.makeText(this, getString(R.string.preenchaDescricaoCor), Toast.LENGTH_SHORT).show();
                return;
            }
            if (novoValorTxt.isEmpty() || novoAnoTxt.isEmpty() || novoKmTxt.isEmpty()) {
                Toast.makeText(this, getString(R.string.preenchaValorAnoKm), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double novoValor = Double.parseDouble(novoValorTxt);
                int novoAno = Integer.parseInt(novoAnoTxt);
                int novoKm = Integer.parseInt(novoKmTxt);

                final Modelo modeloFinal = novoModelo;
                final Cidade cidadeFinal = novaCidade;

                Anuncio atualizado = new Anuncio();
                atualizado.setId(anuncio.getId());
                atualizado.setDescricao(novaDescricao);
                atualizado.setCor(novaCor);
                atualizado.setValor(novoValor);
                atualizado.setAno(novoAno);
                atualizado.setKm(novoKm);
                atualizado.setIdModelo(modeloFinal.getId());
                atualizado.setIdCidade(cidadeFinal.getId());

                serviceAnuncio.alterar(anuncio.getId(), atualizado).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            anuncio.setDescricao(novaDescricao);
                            anuncio.setCor(novaCor);
                            anuncio.setValor(novoValor);
                            anuncio.setAno(novoAno);
                            anuncio.setKm(novoKm);
                            anuncio.setModelo(modeloFinal);
                            anuncio.setCidade(cidadeFinal);
                            adapterAnuncio.notifyDataSetChanged();
                            Toast.makeText(AnunciosActivity.this,
                                    getString(R.string.editadoSucesso), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AnunciosActivity.this,
                                    getString(R.string.erroAtualizarServidor), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(AnunciosActivity.this, getString(R.string.erroRede), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.valorAnoKmInvalido), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    void remover(Anuncio anuncio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.certezaRemocao);

        builder.setPositiveButton(R.string.sim, (dialog, which) ->
                serviceAnuncio.remover(anuncio.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            adapterAnuncio.remove(anuncio);
                            adapterAnuncio.notifyDataSetChanged();
                            Toast.makeText(AnunciosActivity.this,
                                    getString(R.string.remocaoSucesso), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AnunciosActivity.this,
                                    getString(R.string.erroRemoverServidor), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable throwable) {
                        Toast.makeText(AnunciosActivity.this, getString(R.string.erroRede), Toast.LENGTH_SHORT).show();
                    }
                }));

        builder.setNegativeButton(R.string.nao, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.opcoesAnuncio);
        menu.add(0, 1, 0, R.string.editarAnuncio);
        menu.add(0, 2, 1, R.string.removerAnuncio);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);
        Anuncio anuncio = anuncios.get(info.position);

        switch (item.getItemId()) {
            case 1:
                alterar(anuncio);
                return true;
            case 2:
                remover(anuncio);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
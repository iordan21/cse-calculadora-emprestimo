package com.cse.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cse.calculadora.ui.CseViewModel
import com.cse.calculadora.ui.EmprestimoUiState
import com.cse.calculadora.ui.MargemUiState
import com.cse.calculadora.ui.PortabilidadeUiState
import com.cse.calculadora.ui.theme.CSETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CSETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CSEApp()
                }
            }
        }
    }
}

private const val NOME_APP = "CSE - Calculadora Simples de Empréstimo"

private val titulosAbas = listOf("Portabilidade", "Margem", "Empréstimo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CSEApp(viewModel: CseViewModel = viewModel()) {
    val abaSelecionada by viewModel.abaSelecionada.collectAsStateWithLifecycle()
    val portabilidade by viewModel.portabilidade.collectAsStateWithLifecycle()
    val margem by viewModel.margem.collectAsStateWithLifecycle()
    val emprestimo by viewModel.emprestimo.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(NOME_APP) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingInterno ->
        Column(modifier = Modifier.padding(paddingInterno)) {
            TabRow(selectedTabIndex = abaSelecionada) {
                titulosAbas.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = abaSelecionada == indice,
                        onClick = { viewModel.selecionarAba(indice) },
                        text = { Text(titulo) }
                    )
                }
            }

            when (abaSelecionada) {
                0 -> AbaPortabilidade(
                    estado = portabilidade,
                    aoAlterarParcela = viewModel::alterarParcelaPortabilidade,
                    aoAlterarJuros = viewModel::alterarJurosPortabilidade,
                    aoAlterarQuantoFoi = viewModel::alterarQuantoFoi,
                    aoAlterarQuantoResta = viewModel::alterarQuantoResta
                )

                1 -> AbaMargem(
                    estado = margem,
                    aoAlterarSalario = viewModel::alterarSalario,
                    aoAlterarPercentual = viewModel::alterarPercentualMargem,
                    aoAlterarParcela = viewModel::alterarParcelaComprometida,
                    aoAdicionarParcela = viewModel::adicionarParcela,
                    aoRemoverParcela = viewModel::removerParcela
                )

                2 -> AbaEmprestimo(
                    estado = emprestimo,
                    margemSugerida = margem.margemRealDisponivel,
                    aoAlterarParcela = viewModel::alterarParcelaEmprestimo,
                    aoAlterarPrazo = viewModel::alterarPrazo,
                    aoAlterarJuros = viewModel::alterarJurosEmprestimo,
                    aoAlternarIof = viewModel::alternarIof,
                    aoUsarMargemSugerida = viewModel::usarMargemSugerida
                )
            }
        }
    }
}

/* ---------------------------------------------------------------------- */
/*  ABA 1: PORTABILIDADE                                                   */
/* ---------------------------------------------------------------------- */

@Composable
fun AbaPortabilidade(
    estado: PortabilidadeUiState,
    aoAlterarParcela: (String) -> Unit,
    aoAlterarJuros: (String) -> Unit,
    aoAlterarQuantoFoi: (String) -> Unit,
    aoAlterarQuantoResta: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dados do contrato atual",
            style = MaterialTheme.typography.titleLarge
        )

        CampoNumerico(
            valor = estado.parcelaTexto,
            aoAlterar = aoAlterarParcela,
            rotulo = "Parcela (R$)"
        )
        CampoNumerico(
            valor = estado.jurosTexto,
            aoAlterar = aoAlterarJuros,
            rotulo = "Juros a.m. (%)"
        )
        CampoNumerico(
            valor = estado.quantoFoiTexto,
            aoAlterar = aoAlterarQuantoFoi,
            rotulo = "Quanto foi (x pagas)"
        )
        CampoNumerico(
            valor = estado.quantoRestaTexto,
            aoAlterar = aoAlterarQuantoResta,
            rotulo = "Quanto resta (x)"
        )

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo do Contrato") {
            LinhaResumo("Prazo Original", "${estado.prazoOriginal} parcelas")
            LinhaResumo(
                "Total Já Pago",
                "${CalculadoraUtils.formatarMoeda(estado.totalJaPago)} (${estado.quantoFoi} parcelas)"
            )
            LinhaResumo("Término Estimado", estado.terminoEstimado)
            LinhaResumo(
                rotulo = "Saldo Devedor Estimado",
                valor = CalculadoraUtils.formatarMoeda(estado.saldoDevedor),
                corValor = MaterialTheme.colorScheme.primary,
                destaque = true
            )
        }
    }
}

/* ---------------------------------------------------------------------- */
/*  ABA 2: MARGEM (bruta x real, descontando o que já está comprometido)   */
/* ---------------------------------------------------------------------- */

@Composable
fun AbaMargem(
    estado: MargemUiState,
    aoAlterarSalario: (String) -> Unit,
    aoAlterarPercentual: (String) -> Unit,
    aoAlterarParcela: (Int, String) -> Unit,
    aoAdicionarParcela: () -> Unit,
    aoRemoverParcela: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dados para cálculo da margem",
            style = MaterialTheme.typography.titleLarge
        )

        CampoNumerico(
            valor = estado.salarioTexto,
            aoAlterar = aoAlterarSalario,
            rotulo = "Salário Bruto (R$)"
        )
        CampoNumerico(
            valor = estado.margemTexto,
            aoAlterar = aoAlterarPercentual,
            rotulo = "Margem (%)"
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Parcelas já comprometidas",
            style = MaterialTheme.typography.titleLarge
        )

        estado.parcelasTexto.forEachIndexed { indice, texto ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CampoNumerico(
                    valor = texto,
                    aoAlterar = { novoTexto -> aoAlterarParcela(indice, novoTexto) },
                    rotulo = "Parcela ${indice + 1} (R$)",
                    modifier = Modifier.weight(1f)
                )
                if (estado.podeRemoverParcela) {
                    IconButton(onClick = { aoRemoverParcela(indice) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remover parcela")
                    }
                }
            }
        }

        OutlinedButton(
            onClick = aoAdicionarParcela,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar parcela")
        }

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo da Margem") {
            LinhaResumo("Margem Bruta", CalculadoraUtils.formatarMoeda(estado.margemBruta))
            LinhaResumo(
                "Parcela Já Comprometida",
                "- ${CalculadoraUtils.formatarMoeda(estado.parcelaComprometida)}"
            )
            LinhaResumo(
                rotulo = if (estado.margemEstourada) "Margem Estourada" else "Margem Real Disponível",
                valor = CalculadoraUtils.formatarMoeda(estado.margemReal),
                corValor = if (estado.margemEstourada) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                destaque = true
            )
        }
    }
}

/* ---------------------------------------------------------------------- */
/*  ABA 3: EMPRÉSTIMO (parcela + prazo + juros, com alternador de IOF)     */
/* ---------------------------------------------------------------------- */

@Composable
fun AbaEmprestimo(
    estado: EmprestimoUiState,
    margemSugerida: Double,
    aoAlterarParcela: (String) -> Unit,
    aoAlterarPrazo: (String) -> Unit,
    aoAlterarJuros: (String) -> Unit,
    aoAlternarIof: () -> Unit,
    aoUsarMargemSugerida: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dados para simulação do empréstimo",
            style = MaterialTheme.typography.titleLarge
        )

        if (margemSugerida > 0.0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Margem Real (aba Margem): ${CalculadoraUtils.formatarMoeda(margemSugerida)}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = aoUsarMargemSugerida) {
                    Text("Usar")
                }
            }
        }

        CampoNumerico(
            valor = estado.parcelaTexto,
            aoAlterar = aoAlterarParcela,
            rotulo = "Parcela Disponível (R$)"
        )
        CampoNumerico(
            valor = estado.prazoTexto,
            aoAlterar = aoAlterarPrazo,
            rotulo = "Prazo (meses)"
        )
        CampoNumerico(
            valor = estado.jurosTexto,
            aoAlterar = aoAlterarJuros,
            rotulo = "Juros a.m. (%)"
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = aoAlternarIof,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (estado.iofAtivo) {
                    "Modo atual: Descontar IOF (toque para usar Valor Bruto)"
                } else {
                    "Modo atual: Valor Bruto (toque para descontar IOF)"
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo do Empréstimo") {
            LinhaResumo(
                "Valor Bruto Financiável",
                CalculadoraUtils.formatarMoeda(estado.valorBrutoFinanciavel)
            )

            if (estado.iofAtivo) {
                LinhaResumo(
                    rotulo = "Desconto de IOF Estimado (~3%)",
                    valor = "- ${CalculadoraUtils.formatarMoeda(estado.descontoIof)}",
                    corValor = MaterialTheme.colorScheme.error
                )
            }

            LinhaResumo(
                rotulo = if (estado.iofAtivo) "Valor Líquido na Conta" else "Valor Máximo Liberado",
                valor = CalculadoraUtils.formatarMoeda(estado.valorFinal),
                corValor = MaterialTheme.colorScheme.primary,
                destaque = true
            )
        }
    }
}

/* ---------------------------------------------------------------------- */
/*  COMPONENTES REUTILIZÁVEIS                                              */
/* ---------------------------------------------------------------------- */

@Composable
private fun CampoNumerico(
    valor: String,
    aoAlterar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoAlterar,
        label = { Text(rotulo) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun CartaoResumo(
    titulo: String,
    conteudo: ColumnScopeResumo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            conteudo()
        }
    }
}

private typealias ColumnScopeResumo = @Composable () -> Unit

@Composable
private fun LinhaResumo(
    rotulo: String,
    valor: String,
    corValor: Color = Color.Unspecified,
    destaque: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (destaque) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.Medium,
            color = corValor
        )
    }
}

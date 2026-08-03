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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cse.calculadora.ui.theme.CSETheme
import java.util.Calendar
import java.util.Locale

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

private val titulosAbas = listOf("Portabilidade", "Margem", "Empréstimo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CSEApp() {
    var abaSelecionada by rememberSaveable { mutableIntStateOf(0) }

    // Margem Real calculada na Aba Margem, repassada como sugestão para a Aba Empréstimo.
    var margemRealDisponivel by rememberSaveable { mutableDoubleStateOf(0.0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResourceAppName()) },
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
                        onClick = { abaSelecionada = indice },
                        text = { Text(titulo) }
                    )
                }
            }

            when (abaSelecionada) {
                0 -> AbaPortabilidade()
                1 -> AbaMargem(aoAtualizarMargemReal = { margemRealDisponivel = it })
                2 -> AbaEmprestimo(margemSugerida = margemRealDisponivel)
            }
        }
    }
}

@Composable
private fun stringResourceAppName(): String = "CSE - Calculadora Simples de Empréstimo"

/* ---------------------------------------------------------------------- */
/*  ABA 1: PORTABILIDADE                                                   */
/* ---------------------------------------------------------------------- */

@Composable
fun AbaPortabilidade() {
    var parcelaTexto by rememberSaveable { mutableStateOf("") }
    var jurosTexto by rememberSaveable { mutableStateOf("") }
    var quantoFoiTexto by rememberSaveable { mutableStateOf("") }
    var quantoRestaTexto by rememberSaveable { mutableStateOf("") }

    val parcela = CalculadoraUtils.parseValorDigitado(parcelaTexto)
    val juros = CalculadoraUtils.parseValorDigitado(jurosTexto)
    val quantoFoi = CalculadoraUtils.parseValorDigitado(quantoFoiTexto).toInt()
    val quantoResta = CalculadoraUtils.parseValorDigitado(quantoRestaTexto).toInt()

    val prazoOriginal = quantoFoi + quantoResta
    val totalJaPago = parcela * quantoFoi
    val terminoEstimado = calcularTerminoEstimado(quantoResta)
    val saldoDevedor = CalculadoraUtils.calcularPortabilidade(parcela, quantoResta, juros).saldoDevedor

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
            valor = parcelaTexto,
            aoAlterar = { parcelaTexto = CalculadoraUtils.sanitizarEntradaNumerica(it) },
            rotulo = "Parcela (R$)"
        )
        CampoNumerico(
            valor = jurosTexto,
            aoAlterar = { jurosTexto = CalculadoraUtils.sanitizarEntradaNumerica(it) },
            rotulo = "Juros a.m. (%)"
        )
        CampoNumerico(
            valor = quantoFoiTexto,
            aoAlterar = { quantoFoiTexto = it.filter { c -> c.isDigit() } },
            rotulo = "Quanto foi (x pagas)"
        )
        CampoNumerico(
            valor = quantoRestaTexto,
            aoAlterar = { quantoRestaTexto = it.filter { c -> c.isDigit() } },
            rotulo = "Quanto resta (x)"
        )

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo do Contrato") {
            LinhaResumo("Prazo Original", "$prazoOriginal parcelas")
            LinhaResumo(
                "Total Já Pago",
                "${CalculadoraUtils.formatarMoeda(totalJaPago)} ($quantoFoi parcelas)"
            )
            LinhaResumo("Término Estimado", terminoEstimado)
            LinhaResumo(
                rotulo = "Saldo Devedor Estimado",
                valor = CalculadoraUtils.formatarMoeda(saldoDevedor),
                corValor = MaterialTheme.colorScheme.primary,
                destaque = true
            )
        }
    }
}

/**
 * Calcula o mês/ano estimado de término do contrato, somando [quantoResta] meses
 * à data atual (ex.: "julho de 2029").
 */
private fun calcularTerminoEstimado(quantoResta: Int): String {
    val calendario = Calendar.getInstance()
    calendario.add(Calendar.MONTH, quantoResta)

    val formatoLocalePtBr = Locale("pt", "BR")
    val nomeMes = calendario.getDisplayName(Calendar.MONTH, Calendar.LONG, formatoLocalePtBr)
        ?: ""
    val nomeMesCapitalizado = nomeMes.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(formatoLocalePtBr) else it.toString()
    }
    val ano = calendario.get(Calendar.YEAR)
    return "$nomeMesCapitalizado de $ano"
}

/* ---------------------------------------------------------------------- */
/*  ABA 2: MARGEM (bruta x real, descontando o que já está comprometido)   */
/* ---------------------------------------------------------------------- */

@Composable
fun AbaMargem(aoAtualizarMargemReal: (Double) -> Unit = {}) {
    var salarioTexto by rememberSaveable { mutableStateOf("") }
    var margemTexto by rememberSaveable { mutableStateOf("35") }
    var parcelasTexto by rememberSaveable { mutableStateOf(listOf("")) }

    val salarioBruto = CalculadoraUtils.parseValorDigitado(salarioTexto)
    val margemPercentual = CalculadoraUtils.parseValorDigitado(margemTexto)
    val parcelaComprometida = parcelasTexto.sumOf { CalculadoraUtils.parseValorDigitado(it) }

    val margemBruta = salarioBruto * (margemPercentual / 100.0)
    val margemReal = margemBruta - parcelaComprometida
    val margemEstourada = margemReal < 0.0

    // Repassa a Margem Real para a Aba Empréstimo assim que ela é recalculada.
    LaunchedEffect(margemReal) {
        aoAtualizarMargemReal(margemReal.coerceAtLeast(0.0))
    }

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
            valor = salarioTexto,
            aoAlterar = { salarioTexto = CalculadoraUtils.sanitizarEntradaNumerica(it) },
            rotulo = "Salário Bruto (R$)"
        )
        CampoNumerico(
            valor = margemTexto,
            aoAlterar = { margemTexto = CalculadoraUtils.sanitizarEntradaNumerica(it) },
            rotulo = "Margem (%)"
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Parcelas já comprometidas",
            style = MaterialTheme.typography.titleLarge
        )

        parcelasTexto.forEachIndexed { indice, texto ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CampoNumerico(
                    valor = texto,
                    aoAlterar = { novoTexto ->
                        parcelasTexto = parcelasTexto.toMutableList().also {
                            it[indice] = CalculadoraUtils.sanitizarEntradaNumerica(novoTexto)
                        }
                    },
                    rotulo = "Parcela ${indice + 1} (R$)",
                    modifier = Modifier.weight(1f)
                )
                if (parcelasTexto.size > 1) {
                    IconButton(onClick = {
                        parcelasTexto = parcelasTexto.filterIndexed { i, _ -> i != indice }
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remover parcela")
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { parcelasTexto = parcelasTexto + "" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar parcela")
        }

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo da Margem") {
            LinhaResumo("Margem Bruta", CalculadoraUtils.formatarMoeda(margemBruta))
            LinhaResumo(
                "Parcela Já Comprometida",
                "- ${CalculadoraUtils.formatarMoeda(parcelaComprometida)}"
            )
            LinhaResumo(
                rotulo = if (margemEstourada) "Margem Estourada" else "Margem Real Disponível",
                valor = CalculadoraUtils.formatarMoeda(margemReal),
                corValor = if (margemEstourada) {
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
fun AbaEmprestimo(margemSugerida: Double = 0.0) {
    var parcelaTexto by rememberSaveable { mutableStateOf("") }
    var prazoTexto by rememberSaveable { mutableStateOf("84") }
    var jurosTexto by rememberSaveable { mutableStateOf("") }
    var iofAtivo by rememberSaveable { mutableStateOf(false) }

    val parcela = CalculadoraUtils.parseValorDigitado(parcelaTexto)
    val prazoMeses = CalculadoraUtils.parseValorDigitado(prazoTexto).toInt()
    val juros = CalculadoraUtils.parseValorDigitado(jurosTexto)

    val valorBrutoFinanciavel = CalculadoraUtils.valorPresente(parcela, prazoMeses, juros)
    val descontoIof = valorBrutoFinanciavel * 0.03
    val valorFinal = if (iofAtivo) valorBrutoFinanciavel - descontoIof else valorBrutoFinanciavel

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
                TextButton(onClick = {
                    parcelaTexto = CalculadoraUtils.formatarParaEdicao(margemSugerida)
                }) {
                    Text("Usar")
                }
            }
        }

        CampoNumerico(
            valor = parcelaTexto,
            aoAlterar = { parcelaTexto = CalculadoraUtils.sanitizarEntradaNumerica(it) },
            rotulo = "Parcela Disponível (R$)"
        )
        CampoNumerico(
            valor = prazoTexto,
            aoAlterar = { prazoTexto = it.filter { c -> c.isDigit() } },
            rotulo = "Prazo (meses)"
        )
        CampoNumerico(
            valor = jurosTexto,
            aoAlterar = { jurosTexto = CalculadoraUtils.sanitizarEntradaNumerica(it) },
            rotulo = "Juros a.m. (%)"
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = { iofAtivo = !iofAtivo },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (iofAtivo) "Modo atual: Descontar IOF (toque para usar Valor Bruto)" else "Modo atual: Valor Bruto (toque para descontar IOF)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo do Empréstimo") {
            LinhaResumo("Valor Bruto Financiável", CalculadoraUtils.formatarMoeda(valorBrutoFinanciavel))

            if (iofAtivo) {
                LinhaResumo(
                    rotulo = "Desconto de IOF Estimado (~3%)",
                    valor = "- ${CalculadoraUtils.formatarMoeda(descontoIof)}",
                    corValor = MaterialTheme.colorScheme.error
                )
            }

            LinhaResumo(
                rotulo = if (iofAtivo) "Valor Líquido na Conta" else "Valor Máximo Liberado",
                valor = CalculadoraUtils.formatarMoeda(valorFinal),
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
            style = if (destaque) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyLarge,
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

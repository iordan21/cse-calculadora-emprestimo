package com.cse.calculadora

import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow

/**
 * Resultado de uma simulação de portabilidade: saldo devedor estimado
 * a valor presente das parcelas restantes.
 */
data class ResultadoSimulacao(
    val saldoDevedor: Double
)

/**
 * Lógica de cálculo pura do CSE — sem dependência de Compose.
 * Compartilhada entre as abas Portabilidade e Empréstimo.
 */
object CalculadoraUtils {

    /** Alíquota de IOF estimada aplicada sobre o valor bruto financiável. */
    const val ALIQUOTA_IOF = 0.03

    private val localePtBr = Locale("pt", "BR")

    private val formatoMoedaBr: NumberFormat = NumberFormat.getCurrencyInstance(localePtBr)

    /**
     * Valor presente de uma série de parcelas fixas — fórmula da Tabela Price.
     * VP = PMT × [1 - (1+i)^-n] / i, com i = taxa mensal decimal, n = parcelas.
     */
    fun valorPresente(parcela: Double, prazo: Int, taxaMensalPercentual: Double): Double {
        val i = taxaMensalPercentual / 100.0
        if (i == 0.0) return parcela * prazo
        return parcela * (1 - (1 + i).pow(-prazo)) / i
    }

    // Campo da UI é "Juros a.m." (mensal); reaproveita a mesma fórmula Price do empréstimo.
    fun calcularPortabilidade(
        parcelaAtual: Double,
        prazoResta: Int,
        taxaJurosMensal: Double
    ): ResultadoSimulacao {
        return ResultadoSimulacao(
            saldoDevedor = valorPresente(parcelaAtual, prazoResta, taxaJurosMensal)
        )
    }

    /**
     * Mês/ano estimado de término do contrato, somando [quantoResta] meses
     * à data atual (ex.: "Julho de 2029").
     */
    fun calcularTerminoEstimado(quantoResta: Int): String {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.MONTH, quantoResta)

        val nomeMes = calendario.getDisplayName(Calendar.MONTH, Calendar.LONG, localePtBr) ?: ""
        val nomeMesCapitalizado = nomeMes.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(localePtBr) else it.toString()
        }
        return "$nomeMesCapitalizado de ${calendario.get(Calendar.YEAR)}"
    }

    fun formatarMoeda(valor: Double): String = formatoMoedaBr.format(valor)

    /** Converte texto digitado (vírgula decimal, ponto de milhar) em Double. */
    fun parseValorDigitado(texto: String): Double {
        if (texto.isBlank()) return 0.0
        val normalizado = texto.replace(".", "").replace(",", ".")
        return normalizado.toDoubleOrNull() ?: 0.0
    }

    /**
     * Filtra digitação em tempo real: só dígitos e um único separador decimal.
     *
     * O ponto é aceito como separador decimal e convertido em vírgula. Quem
     * digita num teclado de layout en-US escreve "1.89", e descartar o ponto
     * transformaria a taxa em 189% sem aviso nenhum na tela.
     */
    fun sanitizarEntradaNumerica(texto: String): String {
        val resultado = StringBuilder()
        var separadorUsado = false
        for (c in texto) {
            when {
                c.isDigit() -> resultado.append(c)
                (c == ',' || c == '.') && !separadorUsado -> {
                    resultado.append(',')
                    separadorUsado = true
                }
            }
        }
        return resultado.toString()
    }

    /** Formata um Double calculado de volta pra texto editável (vírgula decimal, sem separador de milhar). */
    fun formatarParaEdicao(valor: Double): String {
        return String.format(Locale.US, "%.2f", valor).replace(".", ",")
    }
}

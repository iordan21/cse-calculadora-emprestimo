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

    /**
     * IOF de operação de crédito para mutuário pessoa física, conforme o
     * Decreto 6.306/2007 (Regulamento do IOF), texto compilado consultado em
     * 23/08/2026:
     *
     * - **Alíquota diária de 0,0082%** — art. 7º, inciso I, alínea "b", item 2,
     *   redação dada pelo Decreto 8.392/2015.
     * - **Adicional de 0,38%**, que incide independentemente do prazo — § 15,
     *   redação dada pelo Decreto 12.466/2025. O mesmo decreto subiu o adicional
     *   da pessoa jurídica para 0,95% e não mexeu no da pessoa física.
     * - **Teto de 365 dias** para a parte diária — § 1º, redação dada pelo
     *   Decreto 6.391/2008: o imposto "não excederá o valor resultante da
     *   aplicação da alíquota diária a cada valor de principal (...) multiplicada
     *   por trezentos e sessenta e cinco dias (...) ainda que a operação seja de
     *   pagamento parcelado".
     *
     * O teto é *por parcela*, e é isso que os 3% fixos que estavam aqui erravam:
     * num contrato de 84 meses o número dá perto de 3%, mas num de 12 meses o
     * IOF real é quase metade disso, porque nenhuma parcela chega aos 365 dias.
     */
    private const val IOF_ALIQUOTA_DIARIA = 0.000082
    private const val IOF_ALIQUOTA_ADICIONAL = 0.0038
    private const val IOF_DIAS_MAXIMOS = 365

    /** Dias corridos até o vencimento de cada parcela. Mês comercial de 30 dias. */
    private const val DIAS_POR_MES = 30

    // forLanguageTag no lugar de Locale("pt","BR"): o construtor está depreciado
    // desde o Java 19 e sai com aviso a cada build.
    private val localePtBr: Locale = Locale.forLanguageTag("pt-BR")

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

    /**
     * IOF estimado de um empréstimo parcelado, somando parcela a parcela.
     *
     * A base de cálculo de cada parcela é o principal que ela carrega — o valor
     * presente dela, a mesma conta da Tabela Price (art. 7º, I, "b": "quando
     * previsto mais de um pagamento, o valor do principal de cada uma das
     * parcelas"). Sobre esse principal incide a alíquota diária pelos dias até o
     * vencimento, limitada a 365, mais o adicional de 0,38%.
     *
     * É estimativa: o cálculo do banco usa as datas reais de vencimento, e aqui
     * o mês tem 30 dias. A diferença aparece na casa dos centavos.
     */
    fun calcularIof(parcela: Double, prazo: Int, taxaMensalPercentual: Double): Double {
        if (parcela <= 0.0 || prazo <= 0) return 0.0

        val i = taxaMensalPercentual / 100.0
        var iof = 0.0
        for (numeroDaParcela in 1..prazo) {
            val principalDaParcela =
                if (i == 0.0) parcela else parcela / (1 + i).pow(numeroDaParcela)
            val dias = minOf(numeroDaParcela * DIAS_POR_MES, IOF_DIAS_MAXIMOS)
            iof += principalDaParcela * (IOF_ALIQUOTA_DIARIA * dias + IOF_ALIQUOTA_ADICIONAL)
        }
        return iof
    }

    /**
     * CET mensal — a taxa que iguala o que entra na conta ao que sai dela.
     *
     * A Resolução CMN 4.881/2020 (art. 4º) define o CET pela equivalência entre
     * o valor do crédito concedido (FC0) e o fluxo de tudo que é cobrado do
     * tomador (FCj): juros, tributos, tarifas, seguros. Não há fórmula fechada
     * para essa taxa — ela sai por tentativa, e aqui é por bisseção.
     *
     * **O CET daqui é parcial.** O app conhece juros e IOF; tarifa de cadastro,
     * seguro prestamista e registro de contrato ele não tem como saber. Por isso
     * este número é piso: o CET que o banco informar vem igual ou maior, e um
     * CET declarado *abaixo* deste é sinal de que a proposta não fecha.
     *
     * A resolução manda divulgar em taxa anual (art. 4º, parágrafo único). O
     * mensal está aqui porque é como consignado se compara no balcão; anualizar
     * é [cetAnual].
     */
    fun calcularCetMensal(parcela: Double, prazo: Int, valorLiberado: Double): Double {
        if (parcela <= 0.0 || prazo <= 0 || valorLiberado <= 0.0) return 0.0
        // Liberado igual ou maior que a soma das parcelas: não há custo a apurar.
        if (valorLiberado >= parcela * prazo) return 0.0

        var minima = 0.0
        var maxima = 1.0 // 100% a.m. — teto de busca, não de contrato
        repeat(80) {
            val meio = (minima + maxima) / 2
            // valorPresente cai quando a taxa sobe, então a raiz está à direita
            // enquanto o valor presente ainda for maior que o liberado.
            if (valorPresente(parcela, prazo, meio * 100.0) > valorLiberado) {
                minima = meio
            } else {
                maxima = meio
            }
        }
        return (minima + maxima) / 2
    }

    /** Converte a taxa mensal em anual equivalente, com juros compostos. */
    fun cetAnual(cetMensal: Double): Double = (1 + cetMensal).pow(12) - 1

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

    /** Percentual com duas casas, no formato "3,04%". */
    fun formatarPercentual(fracao: Double): String =
        String.format(localePtBr, "%.2f%%", fracao * 100)

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

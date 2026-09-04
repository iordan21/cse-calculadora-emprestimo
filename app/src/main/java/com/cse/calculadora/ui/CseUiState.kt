package com.cse.calculadora.ui

import com.cse.calculadora.CalculadoraUtils
import java.io.Serializable

/**
 * Estado da aba Portabilidade: os campos digitados e os valores derivados deles.
 *
 * Os campos são guardados como texto (é o que o usuário digita) e os resultados
 * saem de [CalculadoraUtils] — a UI só exibe, não calcula.
 *
 * É [Serializable] para que o SavedStateHandle consiga guardá-lo quando o
 * sistema mata o processo do app em segundo plano.
 *
 * O idiomático no Android seria `@Parcelize`, que gera a escrita e a leitura em
 * tempo de compilação em vez de resolver por reflexão. Não dá, neste projeto: o
 * Kotlin embutido do AGP 9 aceita o plugin parcelize na configuração, mas não
 * roda o plugin de compilação — a anotação resolve e mesmo assim `writeToParcel`
 * nunca é gerado. Sair disso exigiria desligar o Kotlin embutido do módulo
 * inteiro; com três estados pequenos, a reflexão não custa nada perto do risco.
 */
data class PortabilidadeUiState(
    val parcelaTexto: String = "",
    val jurosTexto: String = "",
    val quantoFoiTexto: String = "",
    val quantoRestaTexto: String = ""
) : Serializable {
    private val parcela: Double get() = CalculadoraUtils.parseValorDigitado(parcelaTexto)
    private val juros: Double get() = CalculadoraUtils.parseValorDigitado(jurosTexto)

    val quantoFoi: Int get() = CalculadoraUtils.parseValorDigitado(quantoFoiTexto).toInt()
    val quantoResta: Int get() = CalculadoraUtils.parseValorDigitado(quantoRestaTexto).toInt()

    val prazoOriginal: Int get() = quantoFoi + quantoResta
    val totalJaPago: Double get() = parcela * quantoFoi
    val terminoEstimado: String get() = CalculadoraUtils.calcularTerminoEstimado(quantoResta)

    val saldoDevedor: Double
        get() = CalculadoraUtils.calcularPortabilidade(parcela, quantoResta, juros).saldoDevedor

    /**
     * Sem os campos que sustentam a conta, não existe saldo devedor — existe
     * campo vazio. A tela mostra um traço em vez de "R$ 0,00", porque zero é
     * uma resposta e o app ainda não tem resposta nenhuma.
     *
     * Os juros ficam de fora: contrato sem juros informado é conta legítima
     * (o saldo vira parcela × parcelas restantes), campo em branco não é.
     */
    val temEntrada: Boolean
        get() = parcelaTexto.isNotBlank() && quantoRestaTexto.isNotBlank()
}

/**
 * Estado da aba Margem. [margemRealDisponivel] é o valor repassado como
 * sugestão para a aba Empréstimo (nunca negativo).
 */
data class MargemUiState(
    val salarioTexto: String = "",
    val margemTexto: String = "35",
    val parcelasTexto: List<String> = listOf("")
) : Serializable {
    private val salarioBruto: Double get() = CalculadoraUtils.parseValorDigitado(salarioTexto)
    private val margemPercentual: Double get() = CalculadoraUtils.parseValorDigitado(margemTexto)

    val parcelaComprometida: Double
        get() = parcelasTexto.sumOf { CalculadoraUtils.parseValorDigitado(it) }

    val margemBruta: Double get() = salarioBruto * (margemPercentual / 100.0)
    val margemReal: Double get() = margemBruta - parcelaComprometida
    val margemEstourada: Boolean get() = margemReal < 0.0

    val margemRealDisponivel: Double get() = margemReal.coerceAtLeast(0.0)

    val podeRemoverParcela: Boolean get() = parcelasTexto.size > 1

    /** Sem salário não há margem: nem bruta, nem estourada. Ver [PortabilidadeUiState.temEntrada]. */
    val temEntrada: Boolean get() = salarioTexto.isNotBlank()
}

/**
 * Estado da aba Empréstimo. Com [iofAtivo] ligado, o valor final já sai
 * líquido do IOF estimado.
 */
data class EmprestimoUiState(
    val parcelaTexto: String = "",
    val prazoTexto: String = "84",
    val jurosTexto: String = "",
    val iofAtivo: Boolean = false
) : Serializable {
    private val parcela: Double get() = CalculadoraUtils.parseValorDigitado(parcelaTexto)
    private val juros: Double get() = CalculadoraUtils.parseValorDigitado(jurosTexto)
    private val prazoMeses: Int get() = CalculadoraUtils.parseValorDigitado(prazoTexto).toInt()

    val valorBrutoFinanciavel: Double
        get() = CalculadoraUtils.valorPresente(parcela, prazoMeses, juros)

    val descontoIof: Double
        get() = CalculadoraUtils.calcularIof(parcela, prazoMeses, juros)

    /**
     * Quanto o IOF representa do valor financiado, para a tela poder dizer o
     * percentual em vez de repetir um "~3%" que não vale para todo prazo.
     */
    val aliquotaIofEfetiva: String
        get() = if (valorBrutoFinanciavel > 0.0) {
            CalculadoraUtils.formatarPercentual(descontoIof / valorBrutoFinanciavel)
        } else {
            ""
        }

    val valorFinal: Double
        get() = if (iofAtivo) valorBrutoFinanciavel - descontoIof else valorBrutoFinanciavel

    /** Ver [PortabilidadeUiState.temEntrada]. */
    val temEntrada: Boolean get() = parcelaTexto.isNotBlank() && prazoTexto.isNotBlank()

    /** Resumo da entrada, do jeito que se fala: "84x de R$ 480,00". */
    val parcelamento: String
        get() = "${prazoMeses}x de ${CalculadoraUtils.formatarMoeda(parcela)}"

    /**
     * CET sobre o que de fato cai na conta, em taxa mensal e anual.
     *
     * Com o IOF descontado, o dinheiro que entra é menor que o principal, mas as
     * parcelas continuam as mesmas — o custo real sobe acima do juros digitado,
     * e é esse número que compara duas propostas.
     */
    val cetTexto: String
        get() {
            val mensal = CalculadoraUtils.calcularCetMensal(parcela, prazoMeses, valorFinal)
            if (mensal <= 0.0) return ""
            val anual = CalculadoraUtils.cetAnual(mensal)
            return "${CalculadoraUtils.formatarPercentual(mensal)} a.m. · " +
                "${CalculadoraUtils.formatarPercentual(anual)} a.a."
        }
}

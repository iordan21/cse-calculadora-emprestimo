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

    val descontoIof: Double get() = valorBrutoFinanciavel * CalculadoraUtils.ALIQUOTA_IOF

    val valorFinal: Double
        get() = if (iofAtivo) valorBrutoFinanciavel - descontoIof else valorBrutoFinanciavel
}

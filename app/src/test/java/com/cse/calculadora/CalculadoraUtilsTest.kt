package com.cse.calculadora

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes da lógica de cálculo do CSE. Como [CalculadoraUtils] não depende de
 * Compose nem do Android, roda na JVM local (`./gradlew test`).
 */
class CalculadoraUtilsTest {

    private val tolerancia = 0.01

    @Test
    fun `valor presente segue a formula da Tabela Price`() {
        // 84 parcelas de R$ 500,00 a 1,5% a.m. -> VP = 500 * [1 - 1,015^-84] / 0,015
        val vp = CalculadoraUtils.valorPresente(parcela = 500.0, prazo = 84, taxaMensalPercentual = 1.5)

        assertEquals(23_789.32,vp, tolerancia)
    }

    @Test
    fun `taxa zero devolve a soma simples das parcelas`() {
        val vp = CalculadoraUtils.valorPresente(parcela = 300.0, prazo = 12, taxaMensalPercentual = 0.0)

        assertEquals(3_600.0, vp, tolerancia)
    }

    @Test
    fun `portabilidade estima o saldo devedor das parcelas restantes`() {
        val resultado = CalculadoraUtils.calcularPortabilidade(
            parcelaAtual = 500.0,
            prazoResta = 84,
            taxaJurosMensal = 1.5
        )

        assertEquals(23_789.32,resultado.saldoDevedor, tolerancia)
    }

    @Test
    fun `parse aceita virgula decimal e ponto de milhar`() {
        assertEquals(1_234.56, CalculadoraUtils.parseValorDigitado("1.234,56"), tolerancia)
    }

    @Test
    fun `parse devolve zero para texto vazio ou invalido`() {
        assertEquals(0.0, CalculadoraUtils.parseValorDigitado(""), tolerancia)
        assertEquals(0.0, CalculadoraUtils.parseValorDigitado("abc"), tolerancia)
    }

    @Test
    fun `sanitizar mantem digitos e uma unica virgula`() {
        assertEquals("1234,56", CalculadoraUtils.sanitizarEntradaNumerica("R$ 1a2b3c4,56"))
        // A partir da segunda vírgula, só a vírgula é descartada — os dígitos seguem.
        assertEquals("12,3456", CalculadoraUtils.sanitizarEntradaNumerica("12,34,56"))
    }

    @Test
    fun `ponto digitado vale como virgula decimal`() {
        // Teclado com layout en-US entrega ponto. Descartá-lo transformava
        // 1,89% em 189% sem nada na tela indicando a troca.
        assertEquals("1,89", CalculadoraUtils.sanitizarEntradaNumerica("1.89"))
        assertEquals("0,5", CalculadoraUtils.sanitizarEntradaNumerica("0.5"))
    }

    @Test
    fun `ponto e virgula levam ao mesmo valor`() {
        val comPonto = CalculadoraUtils.sanitizarEntradaNumerica("1.89")
        val comVirgula = CalculadoraUtils.sanitizarEntradaNumerica("1,89")

        assertEquals(
            CalculadoraUtils.parseValorDigitado(comVirgula),
            CalculadoraUtils.parseValorDigitado(comPonto),
            tolerancia
        )
    }

    @Test
    fun `formatar para edicao usa virgula e duas casas`() {
        assertEquals("1500,00", CalculadoraUtils.formatarParaEdicao(1_500.0))
        assertEquals("1234,57", CalculadoraUtils.formatarParaEdicao(1_234.567))
    }
}

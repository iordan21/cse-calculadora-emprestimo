package com.cse.calculadora

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `IOF de parcela unica soma a aliquota de 30 dias com o adicional`() {
        // Sem juros, o principal é a própria parcela: R$ 1.000,00 vencendo em
        // 30 dias. Decreto 6.306/2007: 30 x 0,0082% = 0,246%, mais 0,38% de
        // adicional = 0,626% -> R$ 6,26.
        val iof = CalculadoraUtils.calcularIof(parcela = 1_000.0, prazo = 1, taxaMensalPercentual = 0.0)

        assertEquals(6.26, iof, tolerancia)
    }

    @Test
    fun `IOF nunca passa do teto de 365 dias mais o adicional`() {
        // § 1º do art. 7º: a parte diária para em 365 dias, ainda que a operação
        // seja parcelada. O teto é 365 x 0,0082% + 0,38% = 3,373% do principal.
        val prazo = 120
        val taxa = 1.8
        val principal = CalculadoraUtils.valorPresente(500.0, prazo, taxa)

        val iof = CalculadoraUtils.calcularIof(parcela = 500.0, prazo = prazo, taxaMensalPercentual = taxa)

        assertTrue("IOF de $iof passou do teto", iof <= principal * 0.03373 + tolerancia)
    }

    @Test
    fun `prazo curto paga IOF proporcionalmente menor que prazo longo`() {
        // A diferença que os 3% fixos não enxergavam: em 12 meses nenhuma
        // parcela chega aos 365 dias, então a alíquota efetiva fica bem abaixo
        // do teto que um contrato de 84 meses alcança.
        val curto = CalculadoraUtils.calcularIof(500.0, 12, 1.8) /
            CalculadoraUtils.valorPresente(500.0, 12, 1.8)
        val longo = CalculadoraUtils.calcularIof(500.0, 84, 1.8) /
            CalculadoraUtils.valorPresente(500.0, 84, 1.8)

        assertTrue("curto=$curto longo=$longo", curto < longo)
        assertTrue("curto=$curto deveria ficar longe do teto", curto < 0.025)
    }

    @Test
    fun `sem custo nenhum o CET devolve o proprio juros`() {
        // Se o que cai na conta é o valor presente das parcelas à taxa digitada,
        // não há encargo além do juros — e a bisseção tem que voltar nele.
        val liberado = CalculadoraUtils.valorPresente(500.0, 84, 1.8)

        val cet = CalculadoraUtils.calcularCetMensal(500.0, 84, liberado)

        assertEquals(0.018, cet, 0.00001)
    }

    @Test
    fun `IOF descontado empurra o CET acima do juros contratado`() {
        val bruto = CalculadoraUtils.valorPresente(500.0, 84, 1.8)
        val liquido = bruto - CalculadoraUtils.calcularIof(500.0, 84, 1.8)

        val cet = CalculadoraUtils.calcularCetMensal(500.0, 84, liquido)

        assertTrue("CET de $cet deveria passar de 1,8% a.m.", cet > 0.018)
        // O IOF é ~3% do principal diluído em 84 meses: some pouco por mês.
        assertTrue("CET de $cet subiu demais para um IOF de 3%", cet < 0.019)
    }

    @Test
    fun `CET anual e o mensal composto por doze meses`() {
        assertEquals(0.2387, CalculadoraUtils.cetAnual(0.018), 0.0001)
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

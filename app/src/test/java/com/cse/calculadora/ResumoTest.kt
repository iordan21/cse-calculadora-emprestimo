package com.cse.calculadora

import com.cse.calculadora.ui.EmprestimoUiState
import com.cse.calculadora.ui.MargemUiState
import com.cse.calculadora.ui.PortabilidadeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes do texto que o botão "Copiar resumo" joga na área de transferência.
 *
 * Os valores esperados são montados com [CalculadoraUtils.formatarMoeda], e não
 * escritos à mão: o separador entre "R$" e o número muda conforme a versão do
 * CLDR do JDK, e um teste que fixasse `"R$ 1.000,00"` passaria numa máquina e
 * quebraria na outra sem que nada do app tivesse mudado.
 *
 * O que estes testes travam é o contrato do resumo — primeira linha com o
 * rótulo e o resultado, e nenhuma linha sobre número que a tela não mostrou.
 */
class ResumoTest {

    @Test
    fun `resumo da portabilidade abre pelo rotulo e pelo saldo devedor`() {
        val estado = PortabilidadeUiState(
            parcelaTexto = "500",
            jurosTexto = "1,5",
            quantoFoiTexto = "24",
            quantoRestaTexto = "84"
        )

        assertEquals(
            "Saldo devedor estimado: ${CalculadoraUtils.formatarMoeda(estado.saldoDevedor)}",
            estado.resumo.lines().first()
        )
    }

    @Test
    fun `resumo da portabilidade repete os juros do jeito que foram digitados`() {
        val estado = PortabilidadeUiState(
            parcelaTexto = "500",
            jurosTexto = "1,89",
            quantoRestaTexto = "60"
        )

        assertTrue(estado.resumo.contains("Juros: 1,89% a.m."))
    }

    @Test
    fun `resumo da portabilidade omite a linha de juros quando o campo esta vazio`() {
        // Contrato sem juros informado é conta legítima (saldo = parcela x
        // parcelas), e o resumo não deve afirmar "Juros: 0%" — isso seria
        // inventar um dado que ninguém digitou.
        val estado = PortabilidadeUiState(
            parcelaTexto = "500",
            quantoRestaTexto = "10"
        )

        assertTrue(estado.resumo.lines().none { it.startsWith("Juros:") })
    }

    @Test
    fun `resumo da margem anuncia o estouro quando a margem fica negativa`() {
        // Salário 1.000 x 35% = 350 de margem bruta, contra 500 comprometidos.
        val estado = MargemUiState(
            salarioTexto = "1000",
            parcelasTexto = listOf("500")
        )

        assertEquals("Margem estourada", estado.rotuloResultado)
        assertEquals(
            "Margem estourada: ${CalculadoraUtils.formatarMoeda(-150.0)}",
            estado.resumo.lines().first()
        )
    }

    @Test
    fun `resumo do emprestimo esconde IOF e CET com o desconto desligado`() {
        val estado = EmprestimoUiState(
            parcelaTexto = "500",
            prazoTexto = "84",
            jurosTexto = "1,5",
            iofAtivo = false
        )

        val linhas = estado.resumo.lines()
        assertEquals("Valor máximo liberado", estado.rotuloResultado)
        assertTrue(linhas.none { it.startsWith("IOF estimado") })
        assertTrue(linhas.none { it.startsWith("CET") })
        assertTrue(linhas.none { it.startsWith("Valor bruto financiável") })
    }

    @Test
    fun `resumo do emprestimo traz IOF e CET com o desconto ligado`() {
        val estado = EmprestimoUiState(
            parcelaTexto = "500",
            prazoTexto = "84",
            jurosTexto = "1,5",
            iofAtivo = true
        )

        val linhas = estado.resumo.lines()
        assertEquals("Valor líquido na conta", estado.rotuloResultado)
        assertEquals(
            "Valor líquido na conta: ${CalculadoraUtils.formatarMoeda(estado.valorFinal)}",
            linhas.first()
        )
        assertTrue(linhas.any { it.startsWith("Valor bruto financiável") })
        assertTrue(linhas.any { it.startsWith("IOF estimado (") })
        assertTrue(linhas.any { it.startsWith("CET (juros + IOF): ") })
    }

    @Test
    fun `resumo do emprestimo repete o parcelamento como ele aparece no cartao`() {
        val estado = EmprestimoUiState(
            parcelaTexto = "480",
            prazoTexto = "84",
            jurosTexto = "1,5"
        )

        assertTrue(estado.resumo.contains("Parcelas: ${estado.parcelamento}"))
    }

    @Test
    fun `formulario vazio ainda produz resumo em vez de quebrar`() {
        // O botão fica desabilitado sem entrada, mas o resumo é propriedade
        // derivada e é lido na recomposição — não pode estourar no estado zero.
        assertTrue(PortabilidadeUiState().resumo.isNotEmpty())
        assertTrue(MargemUiState().resumo.isNotEmpty())
        assertTrue(EmprestimoUiState().resumo.isNotEmpty())
    }
}

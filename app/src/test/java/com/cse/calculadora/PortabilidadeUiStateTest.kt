package com.cse.calculadora

import com.cse.calculadora.ui.PortabilidadeUiState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes dos valores derivados da aba Portabilidade.
 *
 * O estado guarda texto e calcula o resto na leitura; estes testes travam
 * justamente essa tradução — inclusive o caso do formulário ainda vazio, que é
 * o primeiro estado que a tela assume.
 */
class PortabilidadeUiStateTest {

    private val tolerancia = 0.01

    @Test
    fun `prazo original soma o que ja foi pago com o que resta`() {
        val estado = PortabilidadeUiState(
            quantoFoiTexto = "24",
            quantoRestaTexto = "60"
        )

        assertEquals(84, estado.prazoOriginal)
    }

    @Test
    fun `total ja pago multiplica a parcela pelas parcelas quitadas`() {
        val estado = PortabilidadeUiState(
            parcelaTexto = "500",
            quantoFoiTexto = "24"
        )

        assertEquals(12_000.0, estado.totalJaPago, tolerancia)
    }

    @Test
    fun `saldo devedor ignora as parcelas ja pagas`() {
        // 24 parcelas quitadas e 84 restantes: o saldo é o valor presente das
        // 84 que faltam, não das 108 do contrato inteiro.
        val estado = PortabilidadeUiState(
            parcelaTexto = "500",
            jurosTexto = "1,5",
            quantoFoiTexto = "24",
            quantoRestaTexto = "84"
        )

        assertEquals(23_789.32, estado.saldoDevedor, tolerancia)
    }

    @Test
    fun `formulario vazio devolve zeros em vez de quebrar`() {
        val estado = PortabilidadeUiState()

        assertEquals(0, estado.prazoOriginal)
        assertEquals(0.0, estado.totalJaPago, tolerancia)
        assertEquals(0.0, estado.saldoDevedor, tolerancia)
    }

    @Test
    fun `juros zerado faz o saldo virar a soma simples das parcelas restantes`() {
        val estado = PortabilidadeUiState(
            parcelaTexto = "500",
            quantoRestaTexto = "10"
        )

        assertEquals(5_000.0, estado.saldoDevedor, tolerancia)
    }
}

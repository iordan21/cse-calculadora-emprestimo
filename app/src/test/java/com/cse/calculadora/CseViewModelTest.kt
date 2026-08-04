package com.cse.calculadora

import com.cse.calculadora.ui.CseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes do estado das abas. Lê o valor corrente de cada StateFlow direto,
 * sem precisar de dispatcher de teste.
 */
class CseViewModelTest {

    private val tolerancia = 0.01

    @Test
    fun `entrada invalida e filtrada antes de virar estado`() {
        val viewModel = CseViewModel()

        viewModel.alterarParcelaPortabilidade("R$ 1a2b3,45")
        viewModel.alterarQuantoResta("60x")

        assertEquals("123,45", viewModel.portabilidade.value.parcelaTexto)
        assertEquals("60", viewModel.portabilidade.value.quantoRestaTexto)
    }

    @Test
    fun `margem real desconta as parcelas ja comprometidas`() {
        val viewModel = CseViewModel()

        viewModel.alterarSalario("5000")   // margem padrão de 35% -> R$ 1.750,00
        viewModel.adicionarParcela()
        viewModel.alterarParcelaComprometida(indice = 0, texto = "500")
        viewModel.alterarParcelaComprometida(indice = 1, texto = "250")

        val margem = viewModel.margem.value
        assertEquals(1_750.0, margem.margemBruta, tolerancia)
        assertEquals(750.0, margem.parcelaComprometida, tolerancia)
        assertEquals(1_000.0, margem.margemReal, tolerancia)
        assertFalse(margem.margemEstourada)
    }

    @Test
    fun `margem estourada nao sugere valor negativo para o emprestimo`() {
        val viewModel = CseViewModel()

        viewModel.alterarSalario("2000")   // margem bruta de R$ 700,00
        viewModel.alterarParcelaComprometida(indice = 0, texto = "900")

        val margem = viewModel.margem.value
        assertTrue(margem.margemEstourada)
        assertEquals(-200.0, margem.margemReal, tolerancia)
        assertEquals(0.0, margem.margemRealDisponivel, tolerancia)
    }

    @Test
    fun `usar margem sugerida preenche a parcela da aba emprestimo`() {
        val viewModel = CseViewModel()

        viewModel.alterarSalario("5000")   // margem real de R$ 1.750,00
        viewModel.usarMargemSugerida()

        assertEquals("1750,00", viewModel.emprestimo.value.parcelaTexto)
    }

    @Test
    fun `alternar IOF desconta a aliquota do valor bruto`() {
        val viewModel = CseViewModel()

        viewModel.alterarParcelaEmprestimo("500")
        viewModel.alterarJurosEmprestimo("1,5")   // prazo padrão de 84 meses

        val bruto = viewModel.emprestimo.value.valorBrutoFinanciavel
        assertEquals(bruto, viewModel.emprestimo.value.valorFinal, tolerancia)

        viewModel.alternarIof()

        val comIof = viewModel.emprestimo.value
        assertTrue(comIof.iofAtivo)
        assertEquals(bruto * 0.03, comIof.descontoIof, tolerancia)
        assertEquals(bruto * 0.97, comIof.valorFinal, tolerancia)
    }

    @Test
    fun `o que foi digitado sobrevive a troca de aba`() {
        val viewModel = CseViewModel()

        viewModel.alterarParcelaPortabilidade("800")
        viewModel.selecionarAba(2)
        viewModel.selecionarAba(0)

        assertEquals("800", viewModel.portabilidade.value.parcelaTexto)
    }
}

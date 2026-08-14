package com.cse.calculadora

import androidx.lifecycle.SavedStateHandle
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
        val viewModel = CseViewModel(SavedStateHandle())

        viewModel.alterarParcelaPortabilidade("R$ 1a2b3,45")
        viewModel.alterarQuantoResta("60x")

        assertEquals("123,45", viewModel.portabilidade.value.parcelaTexto)
        assertEquals("60", viewModel.portabilidade.value.quantoRestaTexto)
    }

    @Test
    fun `juros digitado com ponto rende o mesmo saldo que com virgula`() {
        val viewModel = CseViewModel(SavedStateHandle())

        viewModel.alterarParcelaPortabilidade("500")
        viewModel.alterarJurosPortabilidade("1.5")   // ponto, não vírgula
        viewModel.alterarQuantoResta("84")

        // Antes, o ponto sumia e a taxa virava 15% a.m.: saldo de R$ 3.333,31.
        assertEquals("1,5", viewModel.portabilidade.value.jurosTexto)
        assertEquals(23_789.32, viewModel.portabilidade.value.saldoDevedor, tolerancia)
    }

    @Test
    fun `campo de meses para em tres digitos e nao estoura o prazo`() {
        val viewModel = CseViewModel(SavedStateHandle())

        viewModel.alterarQuantoFoi("9999999999")
        viewModel.alterarQuantoResta("9999999999")
        viewModel.alterarPrazo("9999999999")

        // Sem o limite, cada campo virava Int.MAX_VALUE e a soma estourava
        // o Int: a tela mostrava "-2 parcelas".
        val portabilidade = viewModel.portabilidade.value
        assertEquals("999", portabilidade.quantoFoiTexto)
        assertEquals("999", portabilidade.quantoRestaTexto)
        assertEquals(1_998, portabilidade.prazoOriginal)
        assertEquals("999", viewModel.emprestimo.value.prazoTexto)
    }

    @Test
    fun `margem real desconta as parcelas ja comprometidas`() {
        val viewModel = CseViewModel(SavedStateHandle())

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
        val viewModel = CseViewModel(SavedStateHandle())

        viewModel.alterarSalario("2000")   // margem bruta de R$ 700,00
        viewModel.alterarParcelaComprometida(indice = 0, texto = "900")

        val margem = viewModel.margem.value
        assertTrue(margem.margemEstourada)
        assertEquals(-200.0, margem.margemReal, tolerancia)
        assertEquals(0.0, margem.margemRealDisponivel, tolerancia)
    }

    @Test
    fun `usar margem sugerida preenche a parcela da aba emprestimo`() {
        val viewModel = CseViewModel(SavedStateHandle())

        viewModel.alterarSalario("5000")   // margem real de R$ 1.750,00
        viewModel.usarMargemSugerida()

        assertEquals("1750,00", viewModel.emprestimo.value.parcelaTexto)
    }

    @Test
    fun `alternar IOF desconta a aliquota do valor bruto`() {
        val viewModel = CseViewModel(SavedStateHandle())

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
    fun `o que foi digitado sobrevive a recriacao do ViewModel`() {
        val estadoSalvo = SavedStateHandle()

        val antes = CseViewModel(estadoSalvo)
        antes.alterarParcelaPortabilidade("800")
        antes.alterarSalario("5000")
        antes.selecionarAba(2)

        // Mesmo SavedStateHandle: é o que o sistema devolve quando recria o
        // processo que havia matado em segundo plano.
        val depois = CseViewModel(estadoSalvo)

        assertEquals("800", depois.portabilidade.value.parcelaTexto)
        assertEquals("5000", depois.margem.value.salarioTexto)
        assertEquals(2, depois.abaSelecionada.value)
    }

    @Test
    fun `o que foi digitado sobrevive a troca de aba`() {
        val viewModel = CseViewModel(SavedStateHandle())

        viewModel.alterarParcelaPortabilidade("800")
        viewModel.selecionarAba(2)
        viewModel.selecionarAba(0)

        assertEquals("800", viewModel.portabilidade.value.parcelaTexto)
    }

    @Test
    fun `remover parcela tira a linha certa e preserva as outras`() {
        val viewModel = CseViewModel(SavedStateHandle())

        viewModel.adicionarParcela()
        viewModel.adicionarParcela()
        viewModel.alterarParcelaComprometida(indice = 0, texto = "100")
        viewModel.alterarParcelaComprometida(indice = 1, texto = "200")
        viewModel.alterarParcelaComprometida(indice = 2, texto = "300")

        viewModel.removerParcela(indice = 1)

        val margem = viewModel.margem.value
        assertEquals(listOf("100", "300"), margem.parcelasTexto)
        assertEquals(400.0, margem.parcelaComprometida, tolerancia)
    }

    @Test
    fun `a ultima parcela nao pode ser removida`() {
        val viewModel = CseViewModel(SavedStateHandle())

        // Uma linha só: a tela esconde o botão de remover.
        assertFalse(viewModel.margem.value.podeRemoverParcela)

        viewModel.adicionarParcela()
        assertTrue(viewModel.margem.value.podeRemoverParcela)

        viewModel.removerParcela(indice = 0)
        assertFalse(viewModel.margem.value.podeRemoverParcela)
    }
}

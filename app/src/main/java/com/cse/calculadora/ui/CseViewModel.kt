package com.cse.calculadora.ui

import androidx.lifecycle.ViewModel
import com.cse.calculadora.CalculadoraUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Dono do estado das três abas do CSE.
 *
 * Fica no escopo da Activity, então o que foi digitado sobrevive à troca de aba
 * e à rotação da tela. Também é o que permite a aba Empréstimo ler a margem
 * calculada na aba Margem sem que uma composable precise avisar a outra.
 */
class CseViewModel : ViewModel() {

    private val _abaSelecionada = MutableStateFlow(0)
    val abaSelecionada: StateFlow<Int> = _abaSelecionada.asStateFlow()

    private val _portabilidade = MutableStateFlow(PortabilidadeUiState())
    val portabilidade: StateFlow<PortabilidadeUiState> = _portabilidade.asStateFlow()

    private val _margem = MutableStateFlow(MargemUiState())
    val margem: StateFlow<MargemUiState> = _margem.asStateFlow()

    private val _emprestimo = MutableStateFlow(EmprestimoUiState())
    val emprestimo: StateFlow<EmprestimoUiState> = _emprestimo.asStateFlow()

    fun selecionarAba(indice: Int) {
        _abaSelecionada.value = indice
    }

    /* ---------------- Aba Portabilidade ---------------- */

    fun alterarParcelaPortabilidade(texto: String) {
        _portabilidade.update { it.copy(parcelaTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto)) }
    }

    fun alterarJurosPortabilidade(texto: String) {
        _portabilidade.update { it.copy(jurosTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto)) }
    }

    fun alterarQuantoFoi(texto: String) {
        _portabilidade.update { it.copy(quantoFoiTexto = apenasDigitos(texto)) }
    }

    fun alterarQuantoResta(texto: String) {
        _portabilidade.update { it.copy(quantoRestaTexto = apenasDigitos(texto)) }
    }

    /* ---------------- Aba Margem ---------------- */

    fun alterarSalario(texto: String) {
        _margem.update { it.copy(salarioTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto)) }
    }

    fun alterarPercentualMargem(texto: String) {
        _margem.update { it.copy(margemTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto)) }
    }

    fun alterarParcelaComprometida(indice: Int, texto: String) {
        _margem.update { estado ->
            val atualizadas = estado.parcelasTexto.toMutableList().also {
                it[indice] = CalculadoraUtils.sanitizarEntradaNumerica(texto)
            }
            estado.copy(parcelasTexto = atualizadas)
        }
    }

    fun adicionarParcela() {
        _margem.update { it.copy(parcelasTexto = it.parcelasTexto + "") }
    }

    fun removerParcela(indice: Int) {
        _margem.update { estado ->
            estado.copy(parcelasTexto = estado.parcelasTexto.filterIndexed { i, _ -> i != indice })
        }
    }

    /* ---------------- Aba Empréstimo ---------------- */

    fun alterarParcelaEmprestimo(texto: String) {
        _emprestimo.update { it.copy(parcelaTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto)) }
    }

    fun alterarPrazo(texto: String) {
        _emprestimo.update { it.copy(prazoTexto = apenasDigitos(texto)) }
    }

    fun alterarJurosEmprestimo(texto: String) {
        _emprestimo.update { it.copy(jurosTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto)) }
    }

    fun alternarIof() {
        _emprestimo.update { it.copy(iofAtivo = !it.iofAtivo) }
    }

    /** Joga a Margem Real calculada na aba Margem para o campo de parcela. */
    fun usarMargemSugerida() {
        val sugestao = CalculadoraUtils.formatarParaEdicao(_margem.value.margemRealDisponivel)
        _emprestimo.update { it.copy(parcelaTexto = sugestao) }
    }

    private fun apenasDigitos(texto: String): String = texto.filter { it.isDigit() }
}

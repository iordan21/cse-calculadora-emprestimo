package com.cse.calculadora.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cse.calculadora.CalculadoraUtils
import kotlinx.coroutines.flow.StateFlow

/**
 * Dono do estado das três abas do CSE.
 *
 * O estado vive no [SavedStateHandle], então sobrevive à troca de aba, à rotação
 * da tela e também à morte do processo — quando o sistema mata o app em segundo
 * plano para liberar memória, o que já tinha sido digitado volta.
 *
 * Também é o que permite a aba Empréstimo ler a margem calculada na aba Margem
 * sem que uma composable precise avisar a outra.
 */
class CseViewModel(private val estadoSalvo: SavedStateHandle) : ViewModel() {

    val abaSelecionada: StateFlow<Int> =
        estadoSalvo.getStateFlow(CHAVE_ABA, 0)

    val portabilidade: StateFlow<PortabilidadeUiState> =
        estadoSalvo.getStateFlow(CHAVE_PORTABILIDADE, PortabilidadeUiState())

    val margem: StateFlow<MargemUiState> =
        estadoSalvo.getStateFlow(CHAVE_MARGEM, MargemUiState())

    val emprestimo: StateFlow<EmprestimoUiState> =
        estadoSalvo.getStateFlow(CHAVE_EMPRESTIMO, EmprestimoUiState())

    fun selecionarAba(indice: Int) {
        estadoSalvo[CHAVE_ABA] = indice
    }

    /* ---------------- Aba Portabilidade ---------------- */

    fun alterarParcelaPortabilidade(texto: String) = atualizarPortabilidade {
        it.copy(parcelaTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto))
    }

    fun alterarJurosPortabilidade(texto: String) = atualizarPortabilidade {
        it.copy(jurosTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto))
    }

    fun alterarQuantoFoi(texto: String) = atualizarPortabilidade {
        it.copy(quantoFoiTexto = apenasDigitos(texto))
    }

    fun alterarQuantoResta(texto: String) = atualizarPortabilidade {
        it.copy(quantoRestaTexto = apenasDigitos(texto))
    }

    /* ---------------- Aba Margem ---------------- */

    fun alterarSalario(texto: String) = atualizarMargem {
        it.copy(salarioTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto))
    }

    /**
     * Margem é fatia do salário: passar de 100% não é margem, é erro de
     * digitação, e a tela mostraria uma margem maior que o próprio salário.
     * O campo trava em "100" em vez de recusar a tecla, para a pessoa ver
     * onde parou.
     */
    fun alterarPercentualMargem(texto: String) = atualizarMargem {
        val limpo = CalculadoraUtils.sanitizarEntradaNumerica(texto)
        val percentual = CalculadoraUtils.parseValorDigitado(limpo)
        it.copy(margemTexto = if (percentual > MARGEM_MAXIMA) "100" else limpo)
    }

    /**
     * O índice vem de uma lambda capturada na composição. Se a linha foi
     * removida e um evento do teclado ainda chega para ela, o índice já não
     * existe — e `it[indice]` derrubava o app. Evento atrasado é descartado.
     */
    fun alterarParcelaComprometida(indice: Int, texto: String) = atualizarMargem { estado ->
        if (indice !in estado.parcelasTexto.indices) return@atualizarMargem estado
        val atualizadas = estado.parcelasTexto.toMutableList().also {
            it[indice] = CalculadoraUtils.sanitizarEntradaNumerica(texto)
        }
        estado.copy(parcelasTexto = atualizadas)
    }

    fun adicionarParcela() = atualizarMargem {
        it.copy(parcelasTexto = it.parcelasTexto + "")
    }

    /** A lista nunca fica vazia: a tela sempre mostra ao menos uma linha. */
    fun removerParcela(indice: Int) = atualizarMargem { estado ->
        if (!estado.podeRemoverParcela) return@atualizarMargem estado
        estado.copy(parcelasTexto = estado.parcelasTexto.filterIndexed { i, _ -> i != indice })
    }

    /* ---------------- Aba Empréstimo ---------------- */

    fun alterarParcelaEmprestimo(texto: String) = atualizarEmprestimo {
        it.copy(parcelaTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto))
    }

    fun alterarPrazo(texto: String) = atualizarEmprestimo {
        it.copy(prazoTexto = apenasDigitos(texto))
    }

    fun alterarJurosEmprestimo(texto: String) = atualizarEmprestimo {
        it.copy(jurosTexto = CalculadoraUtils.sanitizarEntradaNumerica(texto))
    }

    fun alternarIof() = atualizarEmprestimo {
        it.copy(iofAtivo = !it.iofAtivo)
    }

    /** Joga a Margem Real calculada na aba Margem para o campo de parcela. */
    fun usarMargemSugerida() {
        val sugestao = CalculadoraUtils.formatarParaEdicao(margem.value.margemRealDisponivel)
        atualizarEmprestimo { it.copy(parcelaTexto = sugestao) }
    }

    /* ---------------- Escrita no estado salvo ---------------- */

    private fun atualizarPortabilidade(
        transformar: (PortabilidadeUiState) -> PortabilidadeUiState
    ) {
        estadoSalvo[CHAVE_PORTABILIDADE] = transformar(portabilidade.value)
    }

    private fun atualizarMargem(transformar: (MargemUiState) -> MargemUiState) {
        estadoSalvo[CHAVE_MARGEM] = transformar(margem.value)
    }

    private fun atualizarEmprestimo(
        transformar: (EmprestimoUiState) -> EmprestimoUiState
    ) {
        estadoSalvo[CHAVE_EMPRESTIMO] = transformar(emprestimo.value)
    }

    /**
     * Campos de quantidade de meses: só dígitos, no máximo [MAX_DIGITOS_MESES].
     *
     * O limite não é estético. Sem ele, dez dígitos viram `Int.MAX_VALUE` no
     * [PortabilidadeUiState], e a soma `quantoFoi + quantoResta` estoura o Int:
     * a tela mostra "-2 parcelas" e um término estimado no ano 178954946.
     * Três dígitos cobrem 999 meses, oitenta e três anos de contrato.
     */
    private fun apenasDigitos(texto: String): String =
        texto.filter { it.isDigit() }.take(MAX_DIGITOS_MESES)

    private companion object {
        const val MAX_DIGITOS_MESES = 3
        const val MARGEM_MAXIMA = 100.0

        const val CHAVE_ABA = "aba_selecionada"
        const val CHAVE_PORTABILIDADE = "estado_portabilidade"
        const val CHAVE_MARGEM = "estado_margem"
        const val CHAVE_EMPRESTIMO = "estado_emprestimo"
    }
}

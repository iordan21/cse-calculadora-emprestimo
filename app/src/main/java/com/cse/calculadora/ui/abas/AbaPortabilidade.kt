package com.cse.calculadora.ui.abas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cse.calculadora.CalculadoraUtils
import com.cse.calculadora.ui.PortabilidadeUiState
import com.cse.calculadora.ui.componentes.CampoNumerico
import com.cse.calculadora.ui.componentes.CartaoResumo
import com.cse.calculadora.ui.componentes.LinhaResumo

/**
 * Aba Portabilidade: a partir do contrato atual, estima o saldo devedor de hoje
 * — o valor presente das parcelas que ainda faltam.
 *
 * Recebe estado pronto e devolve eventos; não conhece o ViewModel.
 */
@Composable
fun AbaPortabilidade(
    estado: PortabilidadeUiState,
    aoAlterarParcela: (String) -> Unit,
    aoAlterarJuros: (String) -> Unit,
    aoAlterarQuantoFoi: (String) -> Unit,
    aoAlterarQuantoResta: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dados do contrato atual",
            style = MaterialTheme.typography.titleLarge
        )

        CampoNumerico(
            valor = estado.parcelaTexto,
            aoAlterar = aoAlterarParcela,
            rotulo = "Parcela (R$)"
        )
        CampoNumerico(
            valor = estado.jurosTexto,
            aoAlterar = aoAlterarJuros,
            rotulo = "Juros a.m. (%)"
        )
        CampoNumerico(
            valor = estado.quantoFoiTexto,
            aoAlterar = aoAlterarQuantoFoi,
            rotulo = "Quanto foi (x pagas)"
        )
        CampoNumerico(
            valor = estado.quantoRestaTexto,
            aoAlterar = aoAlterarQuantoResta,
            rotulo = "Quanto resta (x)"
        )

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo do Contrato") {
            LinhaResumo("Prazo Original", "${estado.prazoOriginal} parcelas")
            LinhaResumo(
                "Total Já Pago",
                "${CalculadoraUtils.formatarMoeda(estado.totalJaPago)} (${estado.quantoFoi} parcelas)"
            )
            LinhaResumo("Término Estimado", estado.terminoEstimado)
            LinhaResumo(
                rotulo = "Saldo Devedor Estimado",
                valor = CalculadoraUtils.formatarMoeda(estado.saldoDevedor),
                corValor = MaterialTheme.colorScheme.primary,
                destaque = true
            )
        }
    }
}

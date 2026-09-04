package com.cse.calculadora.ui.abas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cse.calculadora.CalculadoraUtils
import com.cse.calculadora.ui.PortabilidadeUiState
import com.cse.calculadora.ui.componentes.CampoNumerico
import com.cse.calculadora.ui.componentes.CartaoResultado
import com.cse.calculadora.ui.componentes.LinhaResumo
import com.cse.calculadora.ui.componentes.RotuloSecao
import com.cse.calculadora.ui.componentes.SEM_VALOR
import com.cse.calculadora.ui.componentes.SEM_VALOR_MOEDA

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
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val temEntrada = estado.temEntrada

        CartaoResultado(
            rotulo = "Saldo devedor estimado",
            valor = if (temEntrada) {
                CalculadoraUtils.formatarMoeda(estado.saldoDevedor)
            } else {
                SEM_VALOR_MOEDA
            },
            corValor = if (temEntrada) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ) {
            LinhaResumo(
                "Prazo original",
                if (temEntrada) "${estado.prazoOriginal} parcelas" else SEM_VALOR
            )
            LinhaResumo(
                "Total já pago",
                if (temEntrada) {
                    "${CalculadoraUtils.formatarMoeda(estado.totalJaPago)} em ${estado.quantoFoi}x"
                } else {
                    SEM_VALOR
                }
            )
            LinhaResumo(
                "Término estimado",
                if (temEntrada) estado.terminoEstimado else SEM_VALOR
            )
        }

        RotuloSecao(texto = "CONTRATO ATUAL", modifier = Modifier.padding(top = 14.dp))

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
    }
}

package com.cse.calculadora.ui.abas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cse.calculadora.CalculadoraUtils
import com.cse.calculadora.ui.EmprestimoUiState
import com.cse.calculadora.ui.componentes.CampoNumerico
import com.cse.calculadora.ui.componentes.CartaoResumo
import com.cse.calculadora.ui.componentes.LinhaResumo

/**
 * Aba Empréstimo: a partir da parcela que cabe no bolso, do prazo e da taxa,
 * calcula quanto dá para financiar.
 *
 * [margemSugerida] vem da aba Margem. É por isso que o estado das três abas
 * vive no mesmo ViewModel: sem isso, uma composable teria que avisar a outra.
 */
@Composable
fun AbaEmprestimo(
    estado: EmprestimoUiState,
    margemSugerida: Double,
    aoAlterarParcela: (String) -> Unit,
    aoAlterarPrazo: (String) -> Unit,
    aoAlterarJuros: (String) -> Unit,
    aoAlternarIof: () -> Unit,
    aoUsarMargemSugerida: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dados para simulação do empréstimo",
            style = MaterialTheme.typography.titleLarge
        )

        if (margemSugerida > 0.0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Margem Real (aba Margem): ${CalculadoraUtils.formatarMoeda(margemSugerida)}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = aoUsarMargemSugerida) {
                    Text("Usar")
                }
            }
        }

        CampoNumerico(
            valor = estado.parcelaTexto,
            aoAlterar = aoAlterarParcela,
            rotulo = "Parcela Disponível (R$)"
        )
        CampoNumerico(
            valor = estado.prazoTexto,
            aoAlterar = aoAlterarPrazo,
            rotulo = "Prazo (meses)"
        )
        CampoNumerico(
            valor = estado.jurosTexto,
            aoAlterar = aoAlterarJuros,
            rotulo = "Juros a.m. (%)"
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(
            onClick = aoAlternarIof,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (estado.iofAtivo) {
                    "Modo atual: Descontar IOF (toque para usar Valor Bruto)"
                } else {
                    "Modo atual: Valor Bruto (toque para descontar IOF)"
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo do Empréstimo") {
            LinhaResumo(
                "Valor Bruto Financiável",
                CalculadoraUtils.formatarMoeda(estado.valorBrutoFinanciavel)
            )

            if (estado.iofAtivo) {
                LinhaResumo(
                    rotulo = "Desconto de IOF Estimado (~3%)",
                    valor = "- ${CalculadoraUtils.formatarMoeda(estado.descontoIof)}",
                    corValor = MaterialTheme.colorScheme.error
                )
            }

            LinhaResumo(
                rotulo = if (estado.iofAtivo) "Valor Líquido na Conta" else "Valor Máximo Liberado",
                valor = CalculadoraUtils.formatarMoeda(estado.valorFinal),
                corValor = MaterialTheme.colorScheme.primary,
                destaque = true
            )
        }
    }
}

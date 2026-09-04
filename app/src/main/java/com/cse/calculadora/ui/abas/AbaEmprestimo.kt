package com.cse.calculadora.ui.abas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cse.calculadora.CalculadoraUtils
import com.cse.calculadora.ui.EmprestimoUiState
import com.cse.calculadora.ui.componentes.CampoNumerico
import com.cse.calculadora.ui.componentes.CartaoResultado
import com.cse.calculadora.ui.componentes.LinhaResumo
import com.cse.calculadora.ui.componentes.RotuloSecao
import com.cse.calculadora.ui.componentes.SEM_VALOR
import com.cse.calculadora.ui.componentes.SEM_VALOR_MOEDA

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
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val temEntrada = estado.temEntrada

        CartaoResultado(
            rotulo = if (estado.iofAtivo) "Valor líquido na conta" else "Valor máximo liberado",
            valor = if (temEntrada) {
                CalculadoraUtils.formatarMoeda(estado.valorFinal)
            } else {
                SEM_VALOR_MOEDA
            },
            corValor = if (temEntrada) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ) {
            LinhaResumo("Parcelas", if (temEntrada) estado.parcelamento else SEM_VALOR)

            // Com o IOF desligado, o bruto e o valor de cima são o mesmo número
            // por definição — repetir seria imprimir o resultado duas vezes, uma
            // grande e uma pequena. Só entra quando o desconto o separa do topo.
            if (estado.iofAtivo) {
                LinhaResumo(
                    "Valor bruto financiável",
                    if (temEntrada) {
                        CalculadoraUtils.formatarMoeda(estado.valorBrutoFinanciavel)
                    } else {
                        SEM_VALOR
                    }
                )
                LinhaResumo(
                    rotulo = if (temEntrada) {
                        "IOF estimado (${estado.aliquotaIofEfetiva})"
                    } else {
                        "IOF estimado"
                    },
                    valor = if (temEntrada) {
                        "- ${CalculadoraUtils.formatarMoeda(estado.descontoIof)}"
                    } else {
                        SEM_VALOR
                    },
                    corValor = MaterialTheme.colorScheme.error
                )
                if (temEntrada && estado.cetTexto.isNotEmpty()) {
                    LinhaResumo(
                        rotulo = "CET (juros + IOF)",
                        valor = estado.cetTexto,
                        corValor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        RotuloSecao(texto = "SIMULAÇÃO", modifier = Modifier.padding(top = 14.dp))

        if (margemSugerida > 0.0) {
            SugestaoDeMargem(
                margemSugerida = margemSugerida,
                aoUsar = aoUsarMargemSugerida
            )
        }

        CampoNumerico(
            valor = estado.parcelaTexto,
            aoAlterar = aoAlterarParcela,
            rotulo = "Parcela disponível (R$)"
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

        ChaveIof(ativo = estado.iofAtivo, aoAlternar = aoAlternarIof)
    }
}

/** Faixa que oferece a margem calculada na aba Margem como parcela. */
@Composable
private fun SugestaoDeMargem(margemSugerida: Double, aoUsar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Margem calculada",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = CalculadoraUtils.formatarMoeda(margemSugerida),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        TextButton(onClick = aoUsar) {
            Text("Usar", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Chave do desconto de IOF.
 *
 * Antes era um botão cujo texto tentava explicar em uma frase o modo atual e o
 * que aconteceria ao tocar ("Modo atual: Valor Bruto (toque para descontar
 * IOF)") — quem lê precisa montar a lógica na cabeça. Uma chave mostra o estado
 * sem narrar a consequência.
 *
 * O `toggleable` fica na linha inteira, e não na chave: alvo de toque maior e
 * um único anúncio no TalkBack em vez de dois elementos soltos.
 */
@Composable
private fun ChaveIof(ativo: Boolean, aoAlternar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .toggleable(
                value = ativo,
                role = Role.Switch,
                onValueChange = { aoAlternar() }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Descontar IOF",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "0,0082% ao dia até 365 dias, mais 0,38%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = ativo, onCheckedChange = null)
    }
}

package com.cse.calculadora.ui.abas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cse.calculadora.CalculadoraUtils
import com.cse.calculadora.ui.MargemUiState
import com.cse.calculadora.ui.componentes.CampoNumerico
import com.cse.calculadora.ui.componentes.CartaoResumo
import com.cse.calculadora.ui.componentes.LinhaResumo

/**
 * Aba Margem: margem bruta (salário × percentual) menos o que já está
 * comprometido com outras parcelas.
 *
 * A lista de parcelas comprometidas é dinâmica — daí os eventos de adicionar e
 * remover carregarem o índice da linha.
 */
@Composable
fun AbaMargem(
    estado: MargemUiState,
    aoAlterarSalario: (String) -> Unit,
    aoAlterarPercentual: (String) -> Unit,
    aoAlterarParcela: (Int, String) -> Unit,
    aoAdicionarParcela: () -> Unit,
    aoRemoverParcela: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dados para cálculo da margem",
            style = MaterialTheme.typography.titleLarge
        )

        CampoNumerico(
            valor = estado.salarioTexto,
            aoAlterar = aoAlterarSalario,
            rotulo = "Salário Bruto (R$)"
        )
        CampoNumerico(
            valor = estado.margemTexto,
            aoAlterar = aoAlterarPercentual,
            rotulo = "Margem (%)"
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Parcelas já comprometidas",
            style = MaterialTheme.typography.titleLarge
        )

        estado.parcelasTexto.forEachIndexed { indice, texto ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CampoNumerico(
                    valor = texto,
                    aoAlterar = { novoTexto -> aoAlterarParcela(indice, novoTexto) },
                    rotulo = "Parcela ${indice + 1} (R$)",
                    modifier = Modifier.weight(1f)
                )
                if (estado.podeRemoverParcela) {
                    IconButton(onClick = { aoRemoverParcela(indice) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remover parcela")
                    }
                }
            }
        }

        OutlinedButton(
            onClick = aoAdicionarParcela,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar parcela")
        }

        Spacer(modifier = Modifier.height(8.dp))

        CartaoResumo(titulo = "Resumo da Margem") {
            LinhaResumo("Margem Bruta", CalculadoraUtils.formatarMoeda(estado.margemBruta))
            LinhaResumo(
                "Parcela Já Comprometida",
                "- ${CalculadoraUtils.formatarMoeda(estado.parcelaComprometida)}"
            )
            LinhaResumo(
                rotulo = if (estado.margemEstourada) "Margem Estourada" else "Margem Real Disponível",
                valor = CalculadoraUtils.formatarMoeda(estado.margemReal),
                corValor = if (estado.margemEstourada) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                destaque = true
            )
        }
    }
}

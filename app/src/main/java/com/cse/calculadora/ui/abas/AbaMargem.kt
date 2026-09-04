package com.cse.calculadora.ui.abas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.cse.calculadora.ui.componentes.CartaoResultado
import com.cse.calculadora.ui.componentes.LinhaResumo
import com.cse.calculadora.ui.componentes.RotuloSecao
import com.cse.calculadora.ui.componentes.SEM_VALOR
import com.cse.calculadora.ui.componentes.SEM_VALOR_MOEDA

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
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val temEntrada = estado.temEntrada

        CartaoResultado(
            rotulo = if (temEntrada && estado.margemEstourada) {
                "Margem estourada"
            } else {
                "Margem real disponível"
            },
            valor = if (temEntrada) {
                CalculadoraUtils.formatarMoeda(estado.margemReal)
            } else {
                SEM_VALOR_MOEDA
            },
            corValor = when {
                !temEntrada -> MaterialTheme.colorScheme.onSurfaceVariant
                estado.margemEstourada -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
        ) {
            LinhaResumo(
                "Margem bruta",
                if (temEntrada) CalculadoraUtils.formatarMoeda(estado.margemBruta) else SEM_VALOR
            )
            LinhaResumo(
                "Já comprometido",
                if (temEntrada) {
                    "- ${CalculadoraUtils.formatarMoeda(estado.parcelaComprometida)}"
                } else {
                    SEM_VALOR
                }
            )
        }

        RotuloSecao(texto = "SALÁRIO E MARGEM", modifier = Modifier.padding(top = 14.dp))

        CampoNumerico(
            valor = estado.salarioTexto,
            aoAlterar = aoAlterarSalario,
            rotulo = "Salário bruto (R$)"
        )
        CampoNumerico(
            valor = estado.margemTexto,
            aoAlterar = aoAlterarPercentual,
            rotulo = "Margem (%)"
        )

        RotuloSecao(texto = "PARCELAS JÁ COMPROMETIDAS", modifier = Modifier.padding(top = 14.dp))

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
                    IconButton(
                        onClick = { aoRemoverParcela(indice) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remover parcela")
                    }
                }
            }
        }

        // Contorno fino e texto neutro: adicionar parcela é ação de apoio. O
        // verde continua reservado para o resultado e para a aba ativa.
        OutlinedButton(
            onClick = aoAdicionarParcela,
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar parcela", style = MaterialTheme.typography.labelLarge)
        }
    }
}

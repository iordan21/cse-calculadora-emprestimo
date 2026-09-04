package com.cse.calculadora.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Peças de interface repetidas pelas três abas.
 *
 * Ficam num arquivo só porque são o vocabulário visual do app: um campo de
 * número, o cartão do número que importa, uma linha de apoio e o seletor de
 * abas. Nenhuma delas conhece ViewModel ou cálculo — recebem texto pronto e
 * devolvem eventos.
 */

/** O que aparece nas linhas de apoio enquanto não há entrada para calculá-las. */
internal const val SEM_VALOR = "—"

/**
 * O mesmo, para o número grande do cartão.
 *
 * O traço sozinho em 32sp vira uma barra cinza no meio do cartão, que se parece
 * mais com carregamento do que com ausência de valor. O "R\$" na frente diz de
 * cara que ali vai sair dinheiro, e segura a linha no mesmo lugar quando o
 * valor aparece.
 */
internal const val SEM_VALOR_MOEDA = "R\$ —"

/** Campo de entrada numérica com teclado decimal e uma única linha. */
@Composable
internal fun CampoNumerico(
    valor: String,
    aoAlterar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier
) {
    TextField(
        value = valor,
        onValueChange = aoAlterar,
        label = { Text(rotulo) },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        textStyle = MaterialTheme.typography.titleMedium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        // Sem sublinhado: o campo virou um bloco preenchido, e a moldura do
        // OutlinedTextField competia com o cartão de resultado por atenção. O
        // foco aparece pelo rótulo e pelo cursor em verde, mais o fundo um tom
        // acima — três sinais, nenhum deles só de cor de contorno.
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * O cartão do número que importa: rótulo pequeno, valor grande e, abaixo do
 * divisor, as linhas de apoio passadas em [detalhes].
 *
 * Fica no topo da aba de propósito. É o único resultado que a pessoa abriu o
 * app para ver, e com o teclado aberto ele continua visível enquanto os campos
 * logo abaixo são digitados.
 */
@Composable
internal fun CartaoResultado(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    corValor: Color = MaterialTheme.colorScheme.primary,
    detalhes: @Composable ColumnScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)) {
                ValorEmDestaque(valor = valor, cor = corValor)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Column(
                modifier = Modifier.padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = detalhes
            )
        }
    }
}

/**
 * O valor grande do cartão, encolhido até caber em uma linha.
 *
 * Dinheiro cortado com reticências é pior do que dinheiro em fonte menor, e os
 * campos aceitam número grande o bastante para estourar a largura. Encolhe 6%
 * por passo até caber, com piso em 20sp.
 *
 * O `remember(valor)` recomeça do tamanho cheio a cada valor novo — sem ele a
 * fonte só diminuiria e nunca voltaria.
 *
 * Escrever o tamanho de dentro do `onTextLayout` é escrever estado da fase de
 * layout, que a fase de layout lê de volta. Termina porque só encolhe e tem
 * piso, e só roda para valor que não coube: quando cabe de primeira — o caso
 * de quase todo mundo — o `onTextLayout` não escreve nada e não há passo extra.
 */
@Composable
private fun ValorEmDestaque(valor: String, cor: Color) {
    val estiloCheio = MaterialTheme.typography.displaySmall
    var estilo by remember(valor) { mutableStateOf(estiloCheio) }

    Text(
        text = valor,
        style = estilo,
        color = cor,
        maxLines = 1,
        softWrap = false,
        onTextLayout = { resultado ->
            if (resultado.didOverflowWidth && estilo.fontSize > 20.sp) {
                estilo = estilo.copy(fontSize = estilo.fontSize * 0.94f)
            }
        }
    )
}

/**
 * Título de bloco de campos.
 *
 * Pequeno e espaçado em vez de grande e escuro: o único texto grande da tela é
 * o resultado, e um título de seção do mesmo tamanho disputaria com ele.
 */
@Composable
internal fun RotuloSecao(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/** Uma linha de apoio do cartão: rótulo em cinza à esquerda, valor à direita. */
@Composable
internal fun LinhaResumo(
    rotulo: String,
    valor: String,
    corValor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = corValor
        )
    }
}

/**
 * Seletor de abas em pílula.
 *
 * Substitui a TabRow porque o sublinhado do Material desenha uma régua de ponta
 * a ponta da tela — muita tinta para trocar entre três telas. A pílula ocupa o
 * mesmo espaço e some no fundo quando não está sendo usada.
 *
 * É `Role.Tab` dentro de um `selectableGroup`, então o TalkBack continua
 * anunciando posição e seleção como a TabRow anunciava.
 */
@Composable
internal fun SeletorAbas(
    titulos: List<String>,
    selecionada: Int,
    aoSelecionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        titulos.forEachIndexed { indice, titulo ->
            val ativa = indice == selecionada
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        if (ativa) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    .selectable(
                        selected = ativa,
                        role = Role.Tab,
                        onClick = { aoSelecionar(indice) }
                    )
                    // 40dp de pílula mais os 4dp de folga do trilho em cima e
                    // embaixo fecham os 48dp mínimos de alvo de toque.
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (ativa) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

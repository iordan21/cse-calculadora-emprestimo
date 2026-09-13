package com.cse.calculadora

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cse.calculadora.ui.CseViewModel
import com.cse.calculadora.ui.abrirFichaNaPlayStore
import com.cse.calculadora.ui.abas.AbaEmprestimo
import com.cse.calculadora.ui.abas.AbaMargem
import com.cse.calculadora.ui.abas.AbaPortabilidade
import com.cse.calculadora.ui.componentes.SeletorAbas
import com.cse.calculadora.ui.theme.CSETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // O app é escuro sempre (ver CSETheme), então as duas barras do sistema
        // ficam sobre grafite quase preto. `dark` força ícone branco nelas — o
        // padrão `auto` seguiria o tema do aparelho e pintaria ícone preto sobre
        // fundo escuro em quem usa o celular no claro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            CSETheme {
                CSEApp()
            }
        }
    }
}

private const val NOME_APP = "CSE"

private val titulosAbas = listOf("Portabilidade", "Margem", "Empréstimo")

/**
 * Casca do app: cabeçalho, seletor de abas e a distribuição do estado para a
 * aba visível.
 *
 * Só este arquivo conhece o [CseViewModel]. Cada aba recebe o estado já pronto
 * e devolve eventos, o que permite testá-las e pré-visualizá-las isoladamente.
 */
@Composable
fun CSEApp(viewModel: CseViewModel = viewModel()) {
    val abaSelecionada by viewModel.abaSelecionada.collectAsStateWithLifecycle()
    val portabilidade by viewModel.portabilidade.collectAsStateWithLifecycle()
    val margem by viewModel.margem.collectAsStateWithLifecycle()
    val emprestimo by viewModel.emprestimo.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // `enableEdgeToEdge` desliga o redimensionamento automático da janela, então
        // é o app que precisa descontar o teclado. Sem isto, o campo focado da aba
        // Margem some atrás do teclado e a rolagem já está no fim. `union` toma o
        // maior entre a barra de navegação e o teclado — somar os dois deixaria uma
        // folga inútil acima do teclado.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime)
    ) { paddingInterno ->
        Column(modifier = Modifier.padding(paddingInterno)) {
            Cabecalho(
                titulo = titulosAbas[abaSelecionada],
                aoAvaliar = { abrirFichaNaPlayStore(contexto) }
            )

            SeletorAbas(
                titulos = titulosAbas,
                selecionada = abaSelecionada,
                aoSelecionar = viewModel::selecionarAba,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            when (abaSelecionada) {
                0 -> AbaPortabilidade(
                    estado = portabilidade,
                    aoAlterarParcela = viewModel::alterarParcelaPortabilidade,
                    aoAlterarJuros = viewModel::alterarJurosPortabilidade,
                    aoAlterarQuantoFoi = viewModel::alterarQuantoFoi,
                    aoAlterarQuantoResta = viewModel::alterarQuantoResta
                )

                1 -> AbaMargem(
                    estado = margem,
                    aoAlterarSalario = viewModel::alterarSalario,
                    aoAlterarPercentual = viewModel::alterarPercentualMargem,
                    aoAlterarParcela = viewModel::alterarParcelaComprometida,
                    aoAdicionarParcela = viewModel::adicionarParcela,
                    aoRemoverParcela = viewModel::removerParcela
                )

                2 -> AbaEmprestimo(
                    estado = emprestimo,
                    margemSugerida = margem.margemRealDisponivel,
                    aoAlterarParcela = viewModel::alterarParcelaEmprestimo,
                    aoAlterarPrazo = viewModel::alterarPrazo,
                    aoAlterarJuros = viewModel::alterarJurosEmprestimo,
                    aoAlternarIof = viewModel::alternarIof,
                    aoUsarMargemSugerida = viewModel::usarMargemSugerida
                )
            }
        }
    }
}

/**
 * Nome do app pequeno em cima, nome da aba grande embaixo, e a estrela de
 * avaliar na ponta direita.
 *
 * A barra azul com "CSE - Calculadora Simples de Empréstimo" saiu daqui: o
 * título inteiro ocupava a faixa toda em todas as telas para dizer o que a
 * pessoa já sabia ao abrir o app. Quem precisa se localizar precisa da aba, e
 * é ela que ficou grande.
 */
@Composable
private fun Cabecalho(titulo: String, aoAvaliar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 8.dp na direita, não 20: o IconButton tem alvo de toque de 48.dp
            // com um ícone de 24.dp no meio, então ele já carrega 12.dp de folga
            // interna. Os 20.dp cheios jogariam a estrela para dentro da margem
            // e ela sairia do prumo com o título.
            .padding(start = 20.dp, end = 8.dp, top = 24.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = NOME_APP,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .semantics { heading() }
            )
        }

        IconButton(onClick = aoAvaliar) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Avaliar o app na Play Store",
                // Cinza, não verde. O acento é reservado ao número que decide a
                // compra e à aba ativa (ver Theme.kt); uma estrela verde no
                // canto disputaria o olho com o resultado do cálculo.
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

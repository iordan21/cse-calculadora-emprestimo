package com.cse.calculadora

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cse.calculadora.ui.CseViewModel
import com.cse.calculadora.ui.abas.AbaEmprestimo
import com.cse.calculadora.ui.abas.AbaMargem
import com.cse.calculadora.ui.abas.AbaPortabilidade
import com.cse.calculadora.ui.theme.CSETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A TopAppBar usa `primary` (azul escuro) nos dois temas, e é ela que fica
        // atrás da status bar. O padrão `auto` pintaria ícone preto no tema claro —
        // 3,4:1 de contraste sobre esse azul, abaixo do mínimo de 4,5:1. Fixando em
        // `dark`, o ícone sai branco sempre: 6,1:1.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            CSETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CSEApp()
                }
            }
        }
    }
}

private const val NOME_APP = "CSE - Calculadora Simples de Empréstimo"

private val titulosAbas = listOf("Portabilidade", "Margem", "Empréstimo")

/**
 * Casca do app: barra, abas e a distribuição do estado para a aba visível.
 *
 * Só este arquivo conhece o [CseViewModel]. Cada aba recebe o estado já pronto
 * e devolve eventos, o que permite testá-las e pré-visualizá-las isoladamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CSEApp(viewModel: CseViewModel = viewModel()) {
    val abaSelecionada by viewModel.abaSelecionada.collectAsStateWithLifecycle()
    val portabilidade by viewModel.portabilidade.collectAsStateWithLifecycle()
    val margem by viewModel.margem.collectAsStateWithLifecycle()
    val emprestimo by viewModel.emprestimo.collectAsStateWithLifecycle()

    Scaffold(
        // `enableEdgeToEdge` desliga o redimensionamento automático da janela, então
        // é o app que precisa descontar o teclado. Sem isto, o campo focado da aba
        // Margem some atrás do teclado e a rolagem já está no fim. `union` toma o
        // maior entre a barra de navegação e o teclado — somar os dois deixaria uma
        // folga inútil acima do teclado.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
        topBar = {
            TopAppBar(
                title = { Text(NOME_APP) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingInterno ->
        Column(modifier = Modifier.padding(paddingInterno)) {
            TabRow(selectedTabIndex = abaSelecionada) {
                titulosAbas.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = abaSelecionada == indice,
                        onClick = { viewModel.selecionarAba(indice) },
                        text = { Text(titulo) }
                    )
                }
            }

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

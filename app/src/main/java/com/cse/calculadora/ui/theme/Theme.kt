package com.cse.calculadora.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------
// CORES
// ---------------------------------------------------------------------

/**
 * Paleta monocromática: quatro tons de grafite para as superfícies e um único
 * acento verde.
 *
 * O acento é caro de propósito. Só o número que decide a compra e a aba ativa
 * usam verde — todo o resto é grafite ou cinza. É o que faz o olho cair no
 * saldo devedor sem precisar de seta, moldura ou negrito.
 *
 * Os contrastes abaixo foram medidos sobre [Grafite900] (WCAG AA pede 4,5:1
 * para texto): Neutro400 dá 5,9:1, VerdeAcento 12,1:1, VermelhoAlerta 11,3:1.
 * Se algum tom mudar, refaça a conta — cinza sobre preto passa perto do limite.
 */
val Grafite900 = Color(0xFF0E1013) // fundo da tela
val Grafite800 = Color(0xFF15181C) // campo de entrada
val Grafite700 = Color(0xFF191C21) // cartão de resultado
val Grafite600 = Color(0xFF262A31) // divisor e contorno

val VerdeAcento = Color(0xFF7DE2A8)
val Neutro100 = Color(0xFFF2F4F7) // texto principal
val Neutro400 = Color(0xFF8B9099) // rótulo e texto de apoio
val VermelhoAlerta = Color(0xFFFFB4A9)

// Versão clara da mesma linguagem: um acento só, superfícies neutras. O verde
// muda de tom porque o #7DE2A8 sobre branco dá 1,5:1 — ilegível.
private val VerdeAcentoClaro = Color(0xFF0E7A50)
private val Areia50 = Color(0xFFFAFAF9)
private val Areia100 = Color(0xFFF1F1EE)
private val Areia300 = Color(0xFFDEDEDA)
private val Tinta900 = Color(0xFF16181D)
private val Tinta500 = Color(0xFF6E6E68)

private val EsquemaEscuro = darkColorScheme(
    primary = VerdeAcento,
    onPrimary = Grafite900,
    secondary = Neutro400,
    onSecondary = Grafite900,
    background = Grafite900,
    onBackground = Neutro100,
    surface = Grafite800,
    onSurface = Neutro100,
    surfaceVariant = Grafite700,
    onSurfaceVariant = Neutro400,
    outline = Grafite600,
    outlineVariant = Grafite700,
    error = VermelhoAlerta,
    onError = Grafite900
)

private val EsquemaClaro = lightColorScheme(
    primary = VerdeAcentoClaro,
    onPrimary = Color.White,
    secondary = Tinta500,
    onSecondary = Color.White,
    background = Areia50,
    onBackground = Tinta900,
    surface = Color.White,
    onSurface = Tinta900,
    surfaceVariant = Areia100,
    onSurfaceVariant = Tinta500,
    outline = Areia300,
    outlineVariant = Areia100,
    error = Color(0xFFB3261E),
    onError = Color.White
)

// ---------------------------------------------------------------------
// TIPOGRAFIA
// ---------------------------------------------------------------------

/**
 * A escala inteira vive aqui, não só os dois estilos que o app usava antes.
 *
 * Sobrescrever dois tamanhos e deixar o resto no padrão do Material significa
 * que metade da tela seguia uma régua e a outra metade seguia outra. Com a
 * escala completa, mudar a hierarquia é mexer neste arquivo — nunca em `sp`
 * solto no meio de uma composable.
 */
val CseTypography = Typography(
    displaySmall = TextStyle(
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.8).sp
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.6.sp
    )
)

// ---------------------------------------------------------------------
// FORMAS
// ---------------------------------------------------------------------

// Raio grande é o que separa "cartão de 2019" de cartão de hoje. `large` é o
// cartão de resultado; `small` é campo, botão e faixa.
val CseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ---------------------------------------------------------------------
// TEMA
// ---------------------------------------------------------------------

/**
 * O app é escuro sempre, e não por acaso: o desenho aposta em um acento único
 * sobre superfície escura, e seguir o tema do sistema entregaria dois apps
 * diferentes para a mesma pessoa. Para voltar a seguir o sistema, troque o
 * default por `isSystemInDarkTheme()` — o esquema claro abaixo já está pronto.
 *
 * Material You (`dynamicColor`) saiu de vez. Ele repinta primary e superfícies
 * com a cor do papel de parede, que é justamente o que o desenho monocromático
 * não pode ter: o verde deixaria de ser o único destaque da tela.
 */
@Composable
fun CSETheme(
    temaEscuro: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (temaEscuro) EsquemaEscuro else EsquemaClaro,
        typography = CseTypography,
        shapes = CseShapes,
        content = content
    )
}

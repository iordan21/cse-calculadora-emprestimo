package com.cse.calculadora.ui

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

/**
 * Joga o resumo do cálculo na área de transferência, para colar no sistema, no
 * e-mail ou na conversa com o cliente.
 *
 * Área de transferência, e não compartilhamento pela folha do sistema: o número
 * calculado aqui é digitado de volta em algum campo, e colar funciona em
 * qualquer destino — inclusive nos que não aparecem na folha.
 *
 * Nada é gravado em disco e nada sai do aparelho por conta do app; quem decide
 * o destino do texto é quem colar. É o que a política de privacidade promete.
 *
 * Quem avisa que a cópia aconteceu é o próprio botão, trocando de texto, e não
 * um Toast. O aviso do sistema não serve: do Android 13 em diante o AOSP mostra
 * uma prévia do que foi copiado, mas nem toda ROM mostra — conferido no
 * HyperOS/Android 16, onde nada aparece. Um Toast resolveria esse caso e
 * duplicaria o aviso onde a ROM se comporta; o texto do botão é visível nos
 * dois mundos e não concorre com nenhum deles.
 *
 * É suspensa porque a API de área de transferência do Compose passou a ser (o
 * ClipboardManager antigo foi descontinuado). Quem chama lança numa corrotina
 * da tela; a escrita em si é instantânea.
 */
suspend fun copiarResumo(resumo: String, areaDeTransferencia: Clipboard) {
    areaDeTransferencia.setClipEntry(
        ClipEntry(ClipData.newPlainText("Resumo do cálculo", resumo))
    )
}

package com.cse.calculadora.ui

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString

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
 */
fun copiarResumo(resumo: String, areaDeTransferencia: ClipboardManager) {
    areaDeTransferencia.setText(AnnotatedString(resumo))
}

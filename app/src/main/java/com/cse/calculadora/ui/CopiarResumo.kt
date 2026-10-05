package com.cse.calculadora.ui

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * Joga o resumo do cálculo na área de transferência, para colar no sistema, no
 * e-mail ou na conversa com o cliente.
 *
 * Área de transferência, e não compartilhamento pela folha do sistema: o número
 * calculado aqui é digitado de volta em algum campo, e colar funciona em
 * qualquer destino — inclusive nos que não aparecem na folha de compartilhar.
 *
 * Nada é gravado em disco e nada sai do aparelho por conta do app; quem decide
 * o destino do texto é quem colar. É o que a política de privacidade promete.
 */
fun copiarResumo(
    resumo: String,
    areaDeTransferencia: ClipboardManager,
    context: Context
) {
    areaDeTransferencia.setText(AnnotatedString(resumo))

    // A partir do Android 13 o próprio sistema mostra a confirmação de cópia,
    // com uma prévia do que foi copiado. Um Toast nosso apareceria junto e o
    // aviso sairia duplicado — então ele só entra onde o sistema se cala.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, "Resumo copiado", Toast.LENGTH_SHORT).show()
    }
}

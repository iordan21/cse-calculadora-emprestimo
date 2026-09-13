package com.cse.calculadora.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

/**
 * Abre a ficha do app na Play Store, para quem quiser avaliar.
 *
 * Não usa a In-App Review API de propósito, e a escolha não é por preguiça. A
 * API tem cota por usuário e por período: se a pessoa já viu o cartão de
 * avaliação nas últimas semanas, a chamada não abre nada e o botão vira botão
 * morto. A documentação do Google diz isso com todas as letras e manda
 * redirecionar para a loja quando a avaliação parte de um botão.
 * https://developer.android.com/guide/playcore/in-app-review
 */
fun abrirFichaNaPlayStore(context: Context) {
    val pacote = context.packageName

    // `market://` cai direto no app da Play Store, sem escala pelo navegador.
    // Não há <queries> no manifesto porque nada aqui consulta o PackageManager:
    // quem resolve o destino é o startActivity, e a exceção abaixo é o teste.
    val naLoja = Intent(Intent.ACTION_VIEW, "market://details?id=$pacote".toUri())
    val naWeb = Intent(
        Intent.ACTION_VIEW,
        "https://play.google.com/store/apps/details?id=$pacote".toUri()
    )

    for (destino in listOf(naLoja, naWeb)) {
        try {
            context.startActivity(destino)
            return
        } catch (_: ActivityNotFoundException) {
            // Aparelho sem Play Store (emulador sem Google, ROM alternativa):
            // tenta o próximo destino.
        }
    }

    // Sem loja e sem navegador. Raro, mas botão que não faz nada nem avisa é
    // pior do que botão que avisa.
    Toast.makeText(
        context,
        "Não encontrei a Play Store neste aparelho.",
        Toast.LENGTH_SHORT
    ).show()
}

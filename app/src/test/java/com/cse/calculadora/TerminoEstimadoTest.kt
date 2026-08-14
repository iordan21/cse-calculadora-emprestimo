package com.cse.calculadora

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes da data estimada de término do contrato.
 *
 * O cálculo parte da data de hoje, então não dá para fixar um resultado
 * literal. O que se verifica são as propriedades: somar zero mês não muda nada,
 * somar doze avança exatamente um ano, e o mês sai capitalizado em português.
 */
class TerminoEstimadoTest {

    @Test
    fun `zero mes restante cai no mes corrente`() {
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)

        val termino = CalculadoraUtils.calcularTerminoEstimado(quantoResta = 0)

        assertTrue(
            "esperava terminar em $anoAtual, veio \"$termino\"",
            termino.endsWith("de $anoAtual")
        )
    }

    @Test
    fun `doze meses avancam um ano e mantem o mes`() {
        val hoje = CalculadoraUtils.calcularTerminoEstimado(quantoResta = 0)
        val daquiUmAno = CalculadoraUtils.calcularTerminoEstimado(quantoResta = 12)

        assertEquals(hoje.substringBefore(" de "), daquiUmAno.substringBefore(" de "))
        assertEquals(
            hoje.substringAfter(" de ").toInt() + 1,
            daquiUmAno.substringAfter(" de ").toInt()
        )
    }

    @Test
    fun `o nome do mes vem capitalizado`() {
        val termino = CalculadoraUtils.calcularTerminoEstimado(quantoResta = 3)
        val mes = termino.substringBefore(" de ")

        assertTrue("mês vazio em \"$termino\"", mes.isNotEmpty())
        assertTrue("mês não capitalizado: \"$mes\"", mes.first().isUpperCase())
    }
}

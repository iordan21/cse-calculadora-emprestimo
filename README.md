# CSE — Calculadora Simples de Empréstimo

App Android nativo (Kotlin + Jetpack Compose) para simular portabilidade de empréstimo consignado, calcular margem consignável disponível e simular a tomada de um novo empréstimo.

## Funcionalidades

**Portabilidade** — estima o saldo devedor de um contrato existente a valor presente, pra avaliar se vale a pena portar o empréstimo.

**Margem** — calcula a margem consignável bruta a partir do salário e do percentual de margem, descontando as parcelas já comprometidas (é possível adicionar quantas parcelas forem necessárias). O resultado é sugerido automaticamente na aba de Empréstimo.

**Empréstimo** — simula o valor financiável de uma nova parcela ao longo do prazo, com opção de visualizar o valor bruto ou já descontando o IOF estimado (~3%).

Todos os cálculos de valor presente usam a fórmula da Tabela Price:

```
VP = PMT × [1 - (1 + i)^-n] / i
```

## Stack

- Kotlin
- Jetpack Compose (Material 3)
- Gradle (Kotlin DSL)

## Estrutura

- `MainActivity.kt` — UI Compose: navegação por abas e os três formulários/resumos.
- `calculadoraportabilidade.kt` — lógica de cálculo pura (`CalculadoraUtils`), sem dependência de Compose.
- `ui/theme/` — paleta de cores e tipografia Material 3.

## Build

```bash
./gradlew assembleDebug
```

O build de release usa R8 (`isMinifyEnabled = true`) e `shrinkResources` para reduzir o tamanho final do APK/AAB.

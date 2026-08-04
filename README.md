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
- MVVM com `ViewModel` + `StateFlow`
- JUnit
- Gradle (Kotlin DSL)

## Arquitetura

O app segue MVVM em três camadas bem separadas:

- **Cálculo** — `CalculadoraUtils` é um objeto puro, sem dependência de Compose nem do
  framework Android. É o que torna as fórmulas testáveis na JVM.
- **Estado** — `CseViewModel` é o dono do estado das três abas e expõe cada uma como um
  `StateFlow`. Por viver no escopo da Activity, o que foi digitado sobrevive à troca de aba
  e à rotação da tela. É também o que permite a aba Empréstimo ler a margem calculada na
  aba Margem sem que uma tela precise avisar a outra.
- **UI** — as composables recebem o estado pronto e emitem eventos. Não calculam nada.

## Estrutura

- `MainActivity.kt` — UI Compose: navegação por abas e os três formulários/resumos.
- `calculadoraportabilidade.kt` — lógica de cálculo pura (`CalculadoraUtils`).
- `ui/CseViewModel.kt` — estado das três abas e os eventos vindos da tela.
- `ui/CseUiState.kt` — os campos digitados e os valores derivados de cada aba.
- `ui/theme/` — paleta de cores e tipografia Material 3.

## Testes

```bash
./gradlew test
```

13 testes de JVM cobrindo a fórmula da Tabela Price, o parsing de moeda em pt-BR, a
sanitização da digitação e o comportamento das abas (margem estourada, sugestão de
parcela, desconto de IOF e estado preservado entre abas).

## Build

```bash
./gradlew assembleDebug
```

O build de release usa R8 (`isMinifyEnabled = true`) e `shrinkResources` para reduzir o tamanho final do APK/AAB.

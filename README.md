# PR - Nomeclatura

## Branches e Pull Requests — Padrão Organizacional

Aplicável a todos os repositórios da organização.

***

### Estratégia de branch

O modelo adotado é **GitHub Flow** com branches de curta duração.

```
main (protegida, sempre estável)
  ├── feature/S1-14
  ├── bugfix/S1-22
  ├── hotfix/S1-31
  ├── docs/S1-08
  └── refactor/S1-19
```

#### Regras gerais

* Toda branch nasce de `main`
* Toda branch morre no merge do PR — sem reaproveitamento
* Vida útil máxima recomendada: **3 dias**; branches abertas há mais de 3 dias devem ser divididas ou encerradas
* Push direto em `main` é proibido para todos, incluindo administradores
* Force push é proibido em qualquer branch com PR aberto

***

### Nomenclatura de branch

```
<prefixo>/<ticket-jira>
```

| Prefixo     | Quando usar                                        |
| ----------- | -------------------------------------------------- |
| `feature/`  | Desenvolvimento de funcionalidade nova             |
| `bugfix/`   | Correção de bug em desenvolvimento ou homologação  |
| `hotfix/`   | Correção urgente diretamente para produção         |
| `docs/`     | Apenas documentação                                |
| `refactor/` | Refatoração sem alteração de comportamento externo |

#### Exemplos

```
feature/S1-14
bugfix/S1-22
hotfix/S1-31
docs/S1-08
refactor/S1-19
```

#### Regras de nomenclatura

* Sempre minúsculas
* Ticket do Jira obrigatório — branch sem ticket não é aceita em PR
* Sem caracteres especiais além de `/` e `-`
* Sem descrição adicional após o ticket — o título do PR cumpre esse papel

***

### Estratégia de merge

O merge adotado é **merge commit**.

* Preserva o histórico completo de commits da branch
* Cada PR gera um merge commit identificável em `main`
* O título do merge commit segue o título do PR
* Rebase e squash são desabilitados na configuração do repositório

***

### Proteção de branch

Configurações obrigatórias em `main` para todos os repositórios:

```
Require pull request before merging
  └── Required approvals: 2
  └── Dismiss stale approvals on new commits: true
  └── Require review from Code Owners: true (quando CODEOWNERS existir)

Require status checks to pass before merging
  └── Todos os checks de CI obrigatórios devem passar
  └── Require branches to be up to date before merging: true

Require conversation resolution before merging: true

Do not allow bypassing the above settings: true
```

***

### Pull Request

#### Título

O título do PR segue obrigatoriamente o padrão Conventional Commits:

```
<tipo>(<scope>): <descrição em PT-BR>
```

Exemplos:

```
feat(usuario): adiciona autenticação via OAuth2
fix(carrinho): corrige total exibido após remoção de item
refactor(pedido): extrai lógica de validação para classe dedicada
```

> O título do PR é o que fica registrado no histórico. Escreva como se fosse o único lugar onde alguém vai entender o que mudou.

#### Vínculo com o Jira

O ticket do Jira deve ser referenciado na descrição do PR usando o formato abaixo.

A integração Jira + GitHub reconhece a chave do ticket automaticamente e vincula o PR à issue correspondente.

```
Refs: S1-14
```

ou quando o PR resolve completamente o ticket:

```
Closes: S1-14
```

***

### Template de PR

Salvar em `.github/pull_request_template.md` na raiz de cada repositório.

```markdown
## O que muda

<!-- Descreva em 2 a 4 linhas o que este PR faz e por quê.
     Foque no "o quê" e no "porquê", não no "como". -->

## Ticket

Refs: <!-- ex: S1-14 -->

## Tipo de mudança

- [ ] `feat` — nova funcionalidade
- [ ] `fix` — correção de bug
- [ ] `refactor` — refatoração sem mudança de comportamento
- [ ] `perf` — melhoria de performance
- [ ] `docs` — documentação
- [ ] `test` — testes
- [ ] `build` — dependências ou build
- [ ] `ci` — pipeline
- [ ] `chore` — tarefa mecânica
- [ ] Breaking change (marcar se aplicável em conjunto com outro tipo)

## Checklist

- [ ] Título do PR segue Conventional Commits (`tipo(scope): descrição`)
- [ ] Branch nomeada corretamente (`prefixo/ticket`)
- [ ] Ticket do Jira referenciado na seção acima
- [ ] Testes adicionados ou justificativa para ausência documentada aqui
- [ ] Documentação atualizada se comportamento externo mudou
- [ ] Sem credenciais, secrets ou chaves expostas
- [ ] PR atômico — uma responsabilidade por PR

## Como testar

<!-- Passos exatos para reproduzir e validar a mudança.
     Se for refactor puro ou docs, escreva "ver CI". -->

## Riscos e impactos

<!-- O que pode quebrar ou ser afetado por esta mudança.
     Se nenhum, escreva "nenhum identificado". -->

## Evidência

<!-- Screenshot, log, resultado de teste, link de ambiente.
     Opcional para mudanças internas; obrigatório para feat e fix visíveis. -->
```

***

### Fluxo completo resumido

```
1. Criar branch a partir de main
   └── prefixo/ticket  ex: feature/S1-14

2. Desenvolver com commits seguindo Conventional Commits
   └── feat(usuario): adiciona endpoint de cadastro
   └── test(usuario): adiciona testes de integração para cadastro
   └── fix(usuario): corrige validação de e-mail duplicado

3. Abrir PR para main
   └── Título: feat(usuario): adiciona cadastro de usuário
   └── Preencher template completo
   └── Referenciar ticket: Refs: S1-14

4. Review
   └── 2 aprovações obrigatórias
   └── Todos os checks de CI passando
   └── Todas as conversas resolvidas

5. Merge commit em main
   └── Branch deletada após merge
```

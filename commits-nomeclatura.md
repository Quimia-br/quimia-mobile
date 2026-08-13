# Commits - Nomeclatura

## Conventional Commits — Padrão Organizacional

Aplicável a todos os repositórios da organização. Scopes, módulos e contextos são definidos por cada projeto em seu próprio `CONTRIBUTING.md`.

***

### Estrutura canônica

```
<tipo>(<scope>): <descrição em PT-BR>

[corpo opcional — explica o "por quê", não o "o quê"]

[footer opcional: BREAKING CHANGE, Refs, Co-authored-by]
```

| Parte     | Idioma | Obrigatoriedade                                    |
| --------- | ------ | -------------------------------------------------- |
| Tipo      | Inglês | Obrigatório                                        |
| Scope     | Inglês | Obrigatório para `feat`, `fix`, `perf`, `refactor` |
| Descrição | PT-BR  | Obrigatório                                        |
| Corpo     | PT-BR  | Opcional                                           |
| Footer    | PT-BR  | Opcional                                           |

***

### Tipos permitidos

| Tipo       | Quando usar                                                  |
| ---------- | ------------------------------------------------------------ |
| `feat`     | Nova funcionalidade visível para o usuário ou sistema        |
| `fix`      | Correção de bug com impacto observável                       |
| `perf`     | Melhoria de performance sem alterar comportamento externo    |
| `refactor` | Mudança interna de código sem alterar comportamento          |
| `docs`     | Apenas documentação (README, comentários, wikis)             |
| `test`     | Adição ou correção de testes, sem alterar código de produção |
| `build`    | Mudanças em build, dependências, Docker, Gradle, npm         |
| `ci`       | Mudanças em pipelines, workflows, GitHub Actions             |
| `chore`    | Tarefas mecânicas que não se encaixam nos tipos acima        |
| `revert`   | Reversão de commit anterior                                  |
| `tipo!`    | Sufixo `!` em qualquer tipo indica breaking change           |

Tipos fora desta lista são proibidos.

***

### Regras de formatação

* **Header**: máximo 72 caracteres incluindo tipo, scope e descrição
* **Descrição**: minúscula, modo imperativo ("adiciona", "corrige", "remove"), sem ponto final
* **Idioma**: tipos e scopes sempre em inglês; descrição, corpo e footer em PT-BR
* **Scope**: obrigatório para `feat`, `fix`, `perf`, `refactor`; opcional para os demais; sempre em minúsculas
* **Corpo**: separado do header por linha em branco; explica o _porquê_, não o _como_; wrap em 100 caracteres
* **Breaking change**: declarado no footer como `BREAKING CHANGE: descrição` ou com `!` após o tipo

***

### Scope — como cada projeto define o seu

O scope representa o **módulo de domínio** afetado pela mudança. Cada projeto mantém sua própria lista fechada de scopes no `CONTRIBUTING.md` local.

```
Exemplos de scopes por tipo de domínio (não exaustivo):
→ domínio de negócio:   usuario, empresa, carrinho, produto, pedido, pagamento
→ infraestrutura:       auth, config, infra, ci, build
→ camada transversal:   cache, log, seguranca, notificacao
```

> Scopes fora da lista do projeto devem ser discutidos e aprovados antes de uso.

***

### Exemplos por stack

#### Java / Spring

```
feat(usuario): adiciona endpoint de atualização de perfil
fix(pagamento): corrige cálculo de desconto quando cupom é nulo
refactor(pedido): extrai lógica de validação para classe dedicada
test(produto): adiciona testes de integração para busca por categoria
perf(carrinho): substitui consulta N+1 por JOIN no repositório
build: atualiza Spring Boot para 3.x.x
chore: remove beans não utilizados de configuração
```

Breaking change:

```
feat!(auth): migra autenticação de JWT para OAuth2

BREAKING CHANGE: clientes devem atualizar cabeçalho Authorization para o novo formato Bearer.
Refs: #123
```

***

#### Kotlin / Mobile

```
feat(perfil): adiciona tela de edição de foto de perfil
fix(notificacao): corrige crash ao receber push com payload vazio
refactor(carrinho): migra ViewModel para StateFlow
perf(produto): implementa lazy loading na lista de produtos
test(auth): adiciona testes unitários para fluxo de login
build: atualiza Kotlin para x.x.x e Compose para x.x.x
chore: remove dependências não utilizadas do build.gradle
```

***

#### TypeScript

```
feat(usuario): adiciona validação de CPF no formulário de cadastro
fix(carrinho): corrige total exibido após remoção de item
refactor(produto): separa lógica de filtro em hook customizado
test(pedido): adiciona testes de componente para tela de confirmação
perf(home): adiciona memoização em lista de produtos em destaque
build: atualiza TypeScript para x.x e eslint para x
ci: adiciona verificação de tipos ao pipeline de PR
```

***

#### Python

```
feat(usuario): adiciona endpoint de desativação de conta
fix(pagamento): corrige conversão de moeda em pedidos internacionais
refactor(produto): extrai serializer para módulo separado
test(pedido): adiciona testes de integração para fluxo de checkout
build: atualiza pydantic para x.x.x
ci: adiciona ruff e mypy ao workflow de pull request
chore: atualiza dependências de desenvolvimento
```

***

### O que é proibido

| Proibido                                     | Exemplo                                 |
| -------------------------------------------- | --------------------------------------- |
| Commit sem tipo                              | `arruma bug do login`                   |
| Tipo inventado fora do vocabulário           | `update:`, `wip:`, `hotfix:`            |
| Descrição com ponto final                    | `feat(usuario): adiciona endpoint.`     |
| Descrição em inglês                          | `feat(usuario): add user endpoint`      |
| Header acima de 72 caracteres                | —                                       |
| Múltiplos contextos num único commit         | Separe em commits atômicos              |
| Scope fora da lista do projeto sem aprovação | —                                       |
| Breaking change sem declaração explícita     | Sem `!` e sem footer `BREAKING CHANGE:` |

***

### Referência rápida

```
feat(usuario): adiciona autenticação via OAuth2
─────┬───── ────┬──── ────────────────────┬──────────────────
     │          │                         └ PT-BR · imperativo · sem ponto · ≤72 chars total
     │          └ módulo de domínio · definido por projeto · inglês
     └ vocabulário fechado · sempre inglês
```

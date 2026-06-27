# ADR 001 – Refactorar VeiculoService

## Contexto

O `VeiculoService` (arquivo `src/main/java/com/rotalog/service/VeiculoService.java`) contém a lógica de negócio para veículos, mas foi escrito como um **serviço legado** que mistura várias responsabilidades:

- CRUD de veículos
- Validação de dados de entrada
- Notificações a serviços externos (`NotificacaoClient`)
- Regras de manutenção preventiva e cálculo de custos
- Logging (SLF4J e `System.out` simultaneamente)
- Hard‑coded valores de status, limites e custos
- Operações de auditoria e soft‑delete ausentes
- Falta de paginação e cache nas consultas
- Exceções genéricas (`RuntimeException`)
- Injeção de dependências via `@Autowired` em campos

Essas decisões foram feitas “intencionalmente” para o curso (conforme comentários `TODO/FIXME`), porém impedem manutenção, testabilidade e escalabilidade em produção.

## Decisão

Refatorar o `VeiculoService` dividindo‑o em **serviços especializados** e introduzindo boas práticas do Spring:

1. **Separar responsabilidades**
   - `VeiculoCrudService` – operações básicas de CRUD.
   - `VeiculoValidator` – validações de placa, modelo, ano, etc.
   - `VeiculoNotificationService` – encapsula toda a comunicação com `NotificacaoClient` (inclui retry, circuit‑breaker).
   - `VeiculoMaintenanceService` – regras de manutenção preventiva, cálculo de custos e verificação de necessidade de manutenção.
2. **Injeção por construtor** (`@RequiredArgsConstructor`).
3. **Exceções de domínio** (`VeiculoNotFoundException`, `PlacaInvalidException`, `VeiculoDuplicadoException`).
4. **Externalizar propriedades** (status, limites, custos, intervalos) em `application.yml`.
5. **Paginação e cache** nas consultas (`Pageable`, `@Cacheable`).
6. **Uniformizar logging** somente via SLF4J.
7. **Soft‑delete e auditoria** (campos `deletedAt`, `deletedBy` + entidade `VeiculoAudit`).
8. **Resiliência nas integrações externas** usando Resilience4j ou Spring Retry.
9. **DTOs e resposta tipada** – retornar objetos DTO ao invés de `String` JSON.
10. **Controle de acesso** (`@PreAuthorize`).
11. **Cobertura de testes** unitários e de integração.

## Alternativas consideradas

| Alternativa | Descrição | Por que foi descartada |
|-------------|-----------|------------------------|
| **Manter o código atual** | Continuar usando o serviço monolítico como está. | Não atende aos requisitos de produção (escalabilidade, testabilidade, observabilidade). Resultaria em débito técnico crescente. |
| **Extrair apenas validações** | Criar apenas um `VeiculoValidator` e deixar o resto como está. | Ainda há forte acoplamento ao cliente de notificação, hard‑coded valores e falta de paginação; a refatoração parcial seria insuficiente. |
| **Migrar para arquitetura de microsserviços** (ex.: separar notificações, manutenção em serviços próprios). | Reestruturar todo o módulo em múltiplos micro‑services. | Muito ambicioso para o escopo atual do projeto; aumentaria complexidade e esforço de implantação sem benefício imediato. |
| **Usar um framework de AOP** para aspectos transversais (logging, auditoria, retry). | Aplicar aspectos ao serviço existente. | Ainda não resolve a mistura de responsabilidades latentes; a solução completa requer separar a lógica de negócio em serviços dedicados. |

## Consequências

### Benefícios
- **Código mais legível e coeso** – cada classe tem *single responsibility*.
- **Testabilidade** – mocks fáceis de injetar, testes unitários claros.
- **Configuração externa** – mudar limites ou custos sem recompilar.
- **Resiliência** – notificações com retry/circuit‑breaker evitam perdas silenciosas.
- **Escalabilidade** – paginação evita OOM com grandes frotas.
- **Auditoria e compliance** – histórico de alterações e soft‑delete.
- **Segurança** – controle de acesso centralizado.
- **Observabilidade** – métricas de contagem de veículos, alertas de manutenção, falhas de notificação.

### Custos / Riscos
- **Esforço de implementação** – criação de múltiplas classes, migração de chamadas existentes, atualização dos controllers.
- **Quebra de compatibilidade** – APIs REST que retornam `String` JSON precisarão ser ajustadas para DTOs (versão maior da API).
- **Tempo de entrega** – a refatoração pode atrasar entregas de curto prazo; recomenda‑se realizar em um *feature branch* com PR separado.
- **Necessidade de testes adicionais** – será preciso atualizar ou criar testes de integração.

## Plano de ação sugerido
1. Criar o diretório `docs/adr` (já feito) e registrar este ADR.
2. Implementar `VeiculoValidator` e as exceções de domínio.
3. Refatorar `VeiculoService` em `VeiculoCrudService` (CRUD + busca) e `VeiculoMaintenanceService` (manutenção, custo, alertas).
4. Introduzir `VeiculoNotificationService` com Resilience4j.
5. Atualizar `VeiculoController` para injetar os novos serviços.
6. Adicionar paginação nos endpoints de listagem e configurar cache.
7. Escrever testes unitários para cada novo serviço.
8. Documentar a nova API (OpenAPI) e publicar a nova versão.

---
*Este ADR será versionado junto ao código e revisado a cada sprint que envolver o módulo de frotas.*

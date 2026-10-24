# Contribuindo com Lux Mods

Projeto em pré-produção. Issues descrevem desejos/regras; PRs contêm mudanças verificáveis.

## Fluxo de trabalho
1. Criar issue com problema, contexto, requisitos, dependências, casos extremos e critérios de aceite.
2. Fazer branch feature/nome-curto ou docs/nome-curto a partir de main.
3. Alterar documentação antes de quebrar contrato público.
4. Rodar os testes existentes; anexar logs e passos de reprodução quando possível.
5. Abrir PR com resumo, impacto no multiplayer, saves, clientes, performance e screenshots para UI.
6. Integrar após revisão; preferir mudanças pequenas e reversíveis.

## Regras de engenharia
- Código e jogo usam mod IDs únicos e dados migráveis.
- Servidor controla fatos, contratos e inventário. Nunca confiar em valores arbitrários do cliente.
- Mods opcionais não podem ser dependências rígidas sem decisão explícita.
- Features devem ser desligáveis quando não quebrarem integridade do mundo.
- Não adicionar mods/arte de terceiros sem conferir permissão/licença.
- Controvérsias de balanceamento ficam configuráveis ou anotadas na seção Decisões.
- Se um protótipo for apenas simulado, deixar isso explícito.

## Padrão de issue
Contexto / Objetivo / Comportamento / Não-objetivos / Critérios de aceite / Testes / Dependências / Risco.

## Primeira contribuição recomendada
Criar workspace NeoForge validado e mod Lux Engine que detecta um módulo fictício. Só então implementar a primeira notificação do Lux Discovery.

# ADR-006 — Interface procedural com assets pixel-art opcionais

**Decisão:** separar layout e dados dinâmicos (Java/NeoForge) de detalhes visuais originais (PNG).

**Motivo:** preserve uma GUI que funciona quando os assets não foram publicados, evitando a checkerboard magenta, sendo escalável, multi-módulo e leve. `LuxGuiStyle` é client-only dentro do Engine e nunca é carregado por entrypoints do servidor.

**Alternativas rejeitadas:** imagens estáticas enormes para reproduzir toda HUD (não respondem bem à escala, texto e estado do jogador); dependência de API de imagem externa em runtime (custo/rede/falha); sprites de terceiros sem licença.

**Consequências:** os ícones artísticos são distribuídos separadamente no pacote ZIP; depois de enviar PNGs à pasta `assets/lux_engine/textures/gui/lux/` é obrigatório recompilar o Engine e reiniciar o jogo. Sistemas de rede e persistência não são tocados.

**Gate:** screenshots in-game e build real antes de merge PR #14.

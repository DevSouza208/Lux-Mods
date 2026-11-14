# Lux Boot — protótipo visual SEGURO e separado

Arquivos: index.html e theme.json. Abrir index.html no navegador local. Não precisa instalar Java ou mods para ver esta prévia.

**IMPORTANTE:** este protótipo não roda dentro do jogo e não mede o carregamento. O contador 0-100% e as mensagens são **simulados** para avaliar apenas hierarquia, tipografia, proporção 854x480, cores e molduras. Nunca publicar vídeo dele alegando que o NeoForge carrega desse jeito.

Botões Reiniciar e Pausar funcionam somente na simulação. O navegador não acessa pastas do Minecraft, não altera o modpack e não instala o provider FML.

Estilo-alvo: grafite e roxo Clean Fantasy v2, detalhes champagne, molduras pixeladas de 1-3px, legibilidade e bom contraste. A fonte é monospace do sistema, sem redistribuir fontes externas.

Futuro: portar a composição para um provedor de inicialização FML 1.21.1 isolado, renderizando métricas **reais** de StartupNotificationManager em vez do timer simulado. Não tentar rodar classes net.minecraft.client antes do GAME layer nem copiar o sistema theme JSON do FML main. Não criar provider sem estratégia de desinstalação que funcione mesmo quando o Minecraft não inicia.

Ver docs/32-LUX-BOOT-CARREGAMENTO.md e docs/30-CHECKLIST-DE-TESTES.md.

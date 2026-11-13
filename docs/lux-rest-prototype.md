# Lux Rest — regras após testes in-game (Minecraft 1.21.1 / NeoForge)

## Regra confirmada
- Comida recupera fome/saturação, **não** vida. Para impedir a regeneração vanilla enquanto Lux Healing não controla a regra, use `/gamerule naturalRegeneration false` em cada mundo de teste.
- Curativos/medicamentos devem ser o meio principal de cura. O item de curativo **ainda não foi implementado**.
- Dormir por pelo menos 80 ticks e acordar restaura 100% da vida (código no Lux HUD).
- **Sentar, ficar parado e ficar próximo de fogueira NÃO regeneram vida.**
- Futuramente, sentar e fogueira acelerarão somente a recuperação de estamina.
- Poções e outros efeitos específicos de cura continuam funcionando.
- Sem fraturas, doenças, sede extra ou lesões complexas.

## Próximas etapas
1. Implementar curativo, testar cura vanilla e possíveis mods que concedem regeneração adicional.
2. Implementar Lux Stamina com estado server-side, sincronização e regeneração contextual.
3. Integrar ao mod de assento instalado e ao Lux HUD sem sobrepor a hotbar.

Status: correção de Lux Rest enviada ao GitHub; compilação e teste in-game ainda pendentes.

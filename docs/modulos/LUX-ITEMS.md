# Lux Items — Conteúdo por pacotes e itens customizados

## Objetivo
Instalar o Lux Items uma vez, adicionar itens novos exportados pelo Studio ou criados manualmente via pasta, **sem recompilar o JAR** a cada item. Os IDs só entram em registro na inicialização do Minecraft.

## Tipos iniciais
Armas: espadas, martelos, arcos, escudos; armaduras; relíquias/acessórios com integração opcional à Curios API; objetos usáveis; alimentos/bebidas via Food; blocos colocáveis quando templates disponíveis. Não afirmar suporte universal para toda animação/arma ou item externo em v1.

## Estrutura do pack proposta
~~~text
config/lux/items/
  aventura-medieval/
    manifest.json
    items/
      espada-prata.json
      machado-curto.json
    textures/
      espada-prata.png
    models/
      espada-prata.json
    lang/
      pt_br.json
~~~
Categorias como swords/, bows/, curios/ são organização opcional. O tipo do item vem da definição, não do nome da pasta.

## Manifesto e schema
- packId namespace próprio e estável, schemaVersion, nome, versão, requisito mínimo do Lux Items, definições de item, hashes de assets, requisitos opcionais.
- IDs não colidem. Validação rejeita caminho fora do pack, nomes proibidos, arquivos gigantes, duplicatas, tipo não suportado e referência quebrada.
- Registros novos exigem restart, mesmo que texturas/strings possam ser recarregadas posteriormente em fluxo aprovado.
- Dedicated server determina pack manifest/hash; cliente compatível possui assets corretos. Não carregar scripts Java arbitrários de packs.

## APIs de comportamento
templates de item: melee, projectile-bow, shield, armor, accessory-curios, consumable, held-usable, block-item. Cada template implementa regras seguras fixas. Um template avançado requer código Java próprio no Lux Items antes de virar conteúdo configurável.

## Render/colocação
Definir displays distintos (GUI, mão 1P/3P, chão, moldura), modelos 3D, texturas transparentes/animadas conforme pipeline. Blocos têm propriedades distintas de itens: onde podem ser apoiados, colisão, luz, orientação piso/parede/teto e estado. O Studio previsualiza com regras do manifesto; o jogo continua autoridade final.

## Curios
Não substituir a API Curios; detectar instalação/versionamento no modpack escolhido e oferecer slots/modelos compatíveis via adapter. Equipamento renderizado em corpo precisa rig/ancoragem com controles de escala/posição.

## Aceite
Adicionar simples item novo em pack, iniciar cliente/servidor, vê-lo com ID válido, pegar/usar sem crash, verificar texturas, trocar de mundo, falhar graciosamente com manifesto inválido. Sem recompilar Lux Items.

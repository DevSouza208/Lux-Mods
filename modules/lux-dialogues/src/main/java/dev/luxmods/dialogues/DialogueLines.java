package dev.luxmods.dialogues;

import java.util.UUID;

/**
 * PT-BR dialogue bank. No rumors trigger quests, manipulate reputations,
 * or assert real events in the player's world yet: they are ambient folklore.
 * Deterministic selection prevents flicker when a screen is rebuilt.
 */
public final class DialogueLines {
    private DialogueLines() {}

    private static final String[] GREETINGS = {
        "Bom dia, viajante! Precisa de alguma coisa?",
        "Ora, ora! Um rosto novo por aqui.",
        "Que os caminhos sejam gentis com você.",
        "Sinta-se em casa. Só não mexa nos meus baús!",
        "Faz tempo que não vejo alguém com esse ânimo.",
        "Bem-vindo! As portas da vila estão abertas.",
        "Você parece ter histórias para contar.",
        "Uma boa conversa sempre melhora o dia.",
        "Se veio descansar, chegou ao lugar certo.",
        "Olá! Cuidado ao viajar depois do pôr do sol.",
        "Por aqui seguimos trabalhando, um dia de cada vez.",
        "Que bom ver outra pessoa pelas redondezas.",
        "Saudações! Já conheceu a praça da vila?",
        "Entre, viajante. Há sempre lugar para mais um.",
        "Espero que sua jornada esteja sendo tranquila.",
        "A vida é simples por aqui, mas nunca monótona.",
        "Ouvi passos e achei que fosse o ferreiro novamente.",
        "Olá! Que a próxima estrada seja segura."
    };

    private static final String[] RUMORS = {
        "Dizem que certas ruínas ainda guardam marcas de antigos reinos.",
        "Já ouvi que cavernas profundas escondem coisas que não gostam da luz.",
        "Alguns viajantes juram que os melhores caminhos não aparecem nos mapas.",
        "Conta-se que os sinos de vilas distantes já salvaram muita gente.",
        "Dizem que os gatos sabem muito mais sobre as aldeias do que aparentam.",
        "Ouvi falar de montanhas tão altas que parecem tocar as nuvens.",
        "Um mercador me disse que toda estrada leva a uma boa história.",
        "Há quem diga que uma fogueira acesa é melhor que uma armadura para o coração cansado.",
        "As tavernas guardam mais histórias do que qualquer biblioteca, dizem por aí.",
        "Meu avô dizia que o melhor tesouro é voltar inteiro para casa.",
        "Alguns juram que o vento muda de direção antes das grandes tempestades.",
        "Dizem que o som de uma bigorna pode guiar um viajante perdido.",
        "Já ouvi histórias sobre pontes esquecidas além das colinas.",
        "Um pescador falou de rios que desaparecem sob a terra.",
        "Há quem acredite que certas árvores são mais velhas que as próprias vilas.",
        "Dizem que mineiros experientes sabem ouvir quando uma caverna é perigosa.",
        "Uma viajante me contou que viu ruínas escondidas entre as flores.",
        "Contam que as estrelas ajudam quem não tem bússola.",
        "Ouvi dizer que alguns lobos acompanham viajantes solitários.",
        "Há histórias sobre fortalezas antigas que ninguém conseguiu encontrar duas vezes.",
        "Os ferreiros dizem que uma boa ferramenta dura mais que uma promessa apressada.",
        "Dizem que a melhor receita de sopa muda de vila para vila.",
        "Um andarilho me contou que até os desertos escondem oásis inesperados.",
        "Algumas pessoas acreditam que as flores aparecem onde grandes heróis descansaram.",
        "Quem passa muito tempo nas estradas acaba aprendendo o valor de uma cama.",
        "Dizem que os corvos sempre chegam primeiro quando há confusão.",
        "Acho que toda vila tem pelo menos uma história sobre um fantasma.",
        "Há quem jure que certos lagos ficam prateados à luz da lua.",
        "Um velho explorador disse que mapas são lembranças desenhadas.",
        "Dizem que o pão de uma vila nunca tem exatamente o gosto do pão de outra.",
        "Um aldeão me contou que já viu galinhas nascerem de ovos arremessados no chão.",
        "Dizem que um portal de obsidiana leva a um lugar onde a água desaparece num instante.",
        "Há quem jure que os piglins trocam coisas valiosas por ouro.",
        "Ouvi falar de um viajante que tentou dormir no Nether. A cama não gostou da ideia.",
        "Dizem que os endermans ficam furiosos quando alguém os encara nos olhos.",
        "Um explorador contou que viu uma cidade inteira escondida nas profundezas.",
        "Já ouvi que a lã pode abafar sons em lugares onde é melhor ficar em silêncio.",
        "Um cartógrafo disse que os oceanos guardam monumentos de pedra habitados por guardiões.",
        "Dizem que o ferro protege contra muitos perigos, mas não contra a própria imprudência.",
        "Um pescador jurou que encontrou um naufrágio cheio de mapas encharcados.",
        "Há rumores de que algumas fortalezas possuem salas com portais antigos.",
        "Um aventureiro falou de um dragão que vive além de um portal misterioso.",
        "Dizem que olhos estranhos podem ajudar a encontrar fortalezas esquecidas.",
        "Uma comerciante me disse que existem torres onde os saqueadores se reúnem.",
        "Ouvi falar de um viajante que domesticou um lobo usando ossos.",
        "Alguns dizem que golfinhos sabem onde há tesouros perdidos no mar.",
        "Dizem que tartarugas retornam à praia onde nasceram.",
        "Um lenhador afirma ter visto uma raposa fugir com uma espada na boca.",
        "Há quem diga que uma abóbora entalhada pode ajudar a criar um protetor de ferro.",
        "Um ferreiro contou que as melhores ferramentas precisam de manutenção constante.",
        "Ouvi falar de cogumelos que crescem em ilhas onde quase não aparecem monstros.",
        "Dizem que um raio pode transformar certas criaturas em outras bem diferentes.",
        "Um sujeito jurou que vacas gostam tanto de trigo que seguem qualquer um que o carregue.",
        "Uma viajante disse que barcos deslizam tão bem no gelo que parecem voar.",
        "Ouvi dizer que certos blocos de areia desabam quando você tira o apoio.",
        "Dizem que um esqueleto pode acabar acertando um creeper por engano.",
        "Alguém contou que discos de música podem aparecer depois de uma briga muito esquisita.",
        "Há quem diga que um balde de leite acaba com muitos efeitos estranhos.",
        "Um minerador jurou que encontrou diamantes perto de rios de lava.",
        "Dizem que algumas cavernas parecem respirar quando o vento passa por elas.",
        "Um velho pescador falou sobre criaturas luminosas que vivem nas águas profundas.",
        "Ouvi histórias de lobos que protegem seus donos até o último suspiro.",
        "Dizem que os cavalos mais velozes nem sempre são os mais fortes.",
        "Um mercador me contou que selas são mais difíceis de encontrar do que parecem.",
        "Já ouvi falar de trilhos que atravessam minas abandonadas por quilômetros.",
        "Dizem que algumas minas ainda estão cheias de teias de aranha e madeira apodrecida.",
        "Um aventureiro afirmou que há portais quebrados espalhados pela superfície.",
        "Ouvi dizer que certas ruínas submersas escondem mapas para locais enterrados.",
        "Dizem que as bruxas costumam carregar poções para situações inesperadas.",
        "Um rapaz jurou que se curou de um veneno bebendo leite.",
        "Há quem acredite que os villagers conhecem atalhos que não contam a estranhos.",
        "Uma viajante me contou sobre abelhas que defendem sua colmeia com a própria vida.",
        "Dizem que mel serve para muito mais do que adoçar receitas.",
        "Um explorador disse que a neve fofa pode engolir até um aventureiro distraído.",
        "Ouvi falar de montanhas onde cabras empurram viajantes para longe das trilhas.",
        "Dizem que certas flores só aparecem nos lugares mais inesperados.",
        "Um mago contou que livros encantados guardam segredos de ferramentas antigas.",
        "Há rumores de que bibliotecas escondidas existem sob fortalezas de pedra.",
        "Um aventureiro dizia que escudos salvam mais vidas do que espadas afiadas.",
        "Dizem que correr de um creeper às vezes é a decisão mais inteligente do dia.",
        "Um minerador contou que esqueletos morrem sob a luz do sol, se não encontrarem sombra.",
        "Há quem diga que zumbis conseguem chamar outros zumbis para uma confusão.",
        "Um pastor jurou que encontrou ovelhas de cores que ninguém havia visto antes.",
        "Dizem que os lhamas dos mercadores têm temperamento próprio.",
        "Ouvi falar de um viajante que levou tanto tempo para dormir que começou a ver criaturas no céu.",
        "Uma senhora me disse que os gatos mantêm certos monstros afastados.",
        "Dizem que as trilhas antigas ligavam vilas muito antes de existirem mapas.",
        "Um construtor falou de engrenagens capazes de mover oficinas inteiras.",
        "Ouvi histórias sobre moinhos que trabalham só com a força do vento.",
        "Dizem que uma boa cozinha salva uma expedição mais facilmente do que um baú de ouro.",
        "Um viajante contou que carregar água pode ser tão importante quanto levar comida.",
        "Já ouvi falar de aventureiros que aprenderam receitas diferentes em cada vila.",
        "Dizem que a melhor maneira de conhecer uma região é conversar com quem vive nela.",
        "Um ferreiro comentou que cobre envelhecido pode contar a idade de uma construção.",
        "Alguns juram que certas pontes antigas foram construídas para gigantes.",
        "Ouvi falar de uma torre isolada que ecoa sons de ossos se chocando.",
        "Um sujeito disse que desceu ao submundo por um portal e voltou correndo, sem as botas.",
        "Dizem que existem cavernas tão grandes que caberia uma vila inteira dentro delas.",
        "Uma cozinheira falou de pratos que exigem mais paciência do que ingredientes.",
        "Um explorador me disse que toda ruína tem uma história, mesmo quando ninguém a conhece.",
        "Dizem que a bússola aponta caminhos, mas nunca diz se eles são seguros."
    };

    private static final String[] GUARD_GREETINGS = {
        "Saudações, viajante. Mantenha sua arma embainhada.",
        "Tudo tranquilo por aqui. Por enquanto.",
        "Bem-vindo. Estamos de olho nas estradas.",
        "A patrulha continua. Precisa de informações?",
        "Fique atento depois do anoitecer.",
        "É bom ver alguém chegando sem problemas.",
        "A vila está sob nossa proteção.",
        "Se ouvir o sino, procure abrigo."
    };

    private static final String[] GUARD_RUMORS = {
        "Já ouvi rumores de saqueadores rondando caminhos distantes.",
        "Dizem que algumas patrulhas usam velhas torres como pontos de vigia.",
        "Mercadores contam histórias estranhas sobre florestas escuras.",
        "Há quem diga que o som de um sino atravessa até a neblina mais densa.",
        "Pelos caminhos, todo viajante tem uma história sobre monstros.",
        "Ouvi dizer que antigas muralhas ainda escondem passagens.",
        "Dizem que boas armaduras começam com um bom ferreiro.",
        "Algumas histórias falam de guardas que nunca abandonaram seus postos.",
        "Dizem que uma fogueira acesa pode salvar uma noite difícil.",
        "Um velho guarda dizia: proteja a vila antes de proteger a própria fama."
    };

    public static String greeting(UUID npc, long day, boolean guard) {
        return pick(guard ? GUARD_GREETINGS : GREETINGS, npc, day, 7);
    }

    public static String rumor(UUID npc, long day, boolean guard) {
        return pick(guard ? GUARD_RUMORS : RUMORS, npc, day, 31);
    }

    public static String howAreYou(UUID npc, long day, boolean guard) {
        if (guard) return "Estou de serviço. Uma vila segura é uma vila tranquila.";
        String[] answers = {
            "Tudo bem, obrigado por perguntar. E sua viagem?",
            "Um pouco cansado, mas nada que uma noite de sono não resolva.",
            "Trabalhando bastante! Sempre há algo a fazer por aqui.",
            "Estou bem. É bom conversar com alguém de fora.",
            "A vida segue, entre boas colheitas e dias chuvosos."
        };
        return pick(answers, npc, day, 49);
    }

    private static String pick(String[] lines, UUID npc, long day, int salt) {
        long hash = npc.getMostSignificantBits() ^ Long.rotateLeft(npc.getLeastSignificantBits(), 17)
                ^ (day * 0x9E3779B97F4A7C15L) ^ salt;
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        return lines[Math.floorMod(hash, lines.length)];
    }
}

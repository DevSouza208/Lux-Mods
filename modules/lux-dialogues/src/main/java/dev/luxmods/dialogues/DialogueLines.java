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
        "Dizem que o pão de uma vila nunca tem exatamente o gosto do pão de outra."
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

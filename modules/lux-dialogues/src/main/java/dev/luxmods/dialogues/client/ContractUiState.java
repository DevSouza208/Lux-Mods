package dev.luxmods.dialogues.client;

/**
 * Pure Java view rules for the NPC contract screen.
 * Snapshot values are advisory; the server still validates every action.
 */
public final class ContractUiState {
    private ContractUiState() {}

    public static boolean canAccept(int status) {
        return status == 0; // AVAILABLE
    }

    public static boolean canDeliver(int status, boolean mine) {
        return status == 1 && mine; // ACCEPTED by the local player
    }

    /** Separate delivery confirmation from the current or next NPC offer. */
    public static String deliveryMessage(boolean success) {
        return success
                ? "Entrega concluída! A recompensa foi enviada ao inventário. "
                    + "Confira o chat para ver a quantidade recebida."
                : "Não foi possível concluir a entrega. Confira os materiais "
                    + "e a mensagem do servidor no chat.";
    }

    public static String itemLabel(String objective) {
        return switch (objective) {
            case "minecraft:coal" -> "carvão";
            case "minecraft:bread" -> "pão";
            case "minecraft:iron_ingot" -> "barra de ferro";
            default -> "material desconhecido";
        };
    }

    public static String statusLabel(int status, boolean mine, long expiryDay) {
        return switch (status) {
            case 0 -> "Disponível até o dia " + expiryDay;
            case 1 -> mine ? "Aceito por você" : "Reservado por outro jogador";
            case 2 -> "Concluído";
            case 3 -> "Expirado";
            case 4 -> "Entrega em revisão — procure o administrador";
            default -> "Indisponível";
        };
    }

    public static String description(String objective, int quantity,
                                     long expiryDay, int status, boolean mine) {
        return "Pedido: " + quantity + " de " + itemLabel(objective) + ". "
                + statusLabel(status, mine, expiryDay) + ".";
    }
}

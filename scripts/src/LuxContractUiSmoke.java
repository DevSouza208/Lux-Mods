import dev.luxmods.dialogues.client.ContractUiState;

/** Offline UI state contract checks; run with Java 21, no Minecraft runtime. */
public final class LuxContractUiSmoke {
    private LuxContractUiSmoke() {}

    private static void check(boolean condition) {
        if (!condition) throw new AssertionError("invalid contract UI state");
    }

    public static void main(String[] args) {
        check(ContractUiState.canAccept(0));
        check(!ContractUiState.canAccept(1) && !ContractUiState.canAccept(2));
        check(ContractUiState.canDeliver(1, true));
        check(!ContractUiState.canDeliver(1, false));
        check(!ContractUiState.canDeliver(2, true));
        check(!ContractUiState.canDeliver(0, true));
        check("carvão".equals(ContractUiState.itemLabel("minecraft:coal")));
        check(ContractUiState.statusLabel(1, false, 13).contains("outro jogador"));
        check(ContractUiState.statusLabel(1, true, 13).contains("você"));
        check(ContractUiState.description("minecraft:bread", 12, 15, 0, false)
                .contains("12 de pão"));
        check(ContractUiState.description("minecraft:bread", 12, 15, 0, false)
                .contains("dia 15"));
        check(ContractUiState.statusLabel(2, true, 15).equals("Concluído"));
        System.out.println("Lux contract UI smoke OK");
    }
}

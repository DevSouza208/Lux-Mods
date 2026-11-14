import dev.luxmods.dialogues.NpcContractLifecycle;
import java.util.UUID;

public class LuxNpcContractSmoke {
    static void check(boolean condition) { if (!condition) throw new AssertionError("contract lifecycle"); }
    public static void main(String[] args) {
        UUID npc = UUID.fromString("10000000-0000-0000-0000-000000000001");
        UUID player = UUID.fromString("20000000-0000-0000-0000-000000000002");
        UUID another = UUID.fromString("30000000-0000-0000-0000-000000000003");
        var offered = NpcContractLifecycle.create(npc, 12);
        check(offered.equals(NpcContractLifecycle.create(npc, 12)));
        check(NpcContractLifecycle.refresh(offered, offered.expiresDay() - 1).equals(offered));
        check(NpcContractLifecycle.refresh(offered, offered.expiresDay()).status() == NpcContractLifecycle.Status.EXPIRED);
        check(NpcContractLifecycle.accept(offered, player, offered.expiresDay()).status() == NpcContractLifecycle.Status.EXPIRED);
        var accepted = NpcContractLifecycle.accept(offered, player, 12);
        check(accepted.status() == NpcContractLifecycle.Status.ACCEPTED);
        check(NpcContractLifecycle.validTransition(offered, accepted));
        check(!NpcContractLifecycle.validTransition(offered, offered));
        check(!NpcContractLifecycle.validTransition(accepted, offered));
        check(!NpcContractLifecycle.validTransition(accepted,
                new NpcContractLifecycle.Offer(npc, offered.issuedDay(),
                        offered.expiresDay(), "minecraft:diamond", offered.quantity(),
                        NpcContractLifecycle.Status.COMPLETED, player)));

        check(NpcContractLifecycle.accept(accepted, another, 12).equals(accepted));
        check(NpcContractLifecycle.refresh(accepted, 1000).equals(accepted));
        check(!NpcContractLifecycle.shouldRotate(offered, offered.expiresDay() - 1));
        check(NpcContractLifecycle.shouldRotate(offered, offered.expiresDay()));
        check(!NpcContractLifecycle.shouldRotate(accepted, 1000));

        check(NpcContractLifecycle.complete(accepted, another, true).equals(accepted));
        check(NpcContractLifecycle.complete(accepted, player, true).equals(accepted));
        var settling = NpcContractLifecycle.beginSettlement(accepted, player);
        check(settling.status() == NpcContractLifecycle.Status.SETTLING);
        check(NpcContractLifecycle.validTransition(accepted, settling));
        check(!NpcContractLifecycle.validTransition(accepted,
                NpcContractLifecycle.complete(settling, player, true)));
        check(!NpcContractLifecycle.shouldRotate(settling, 1000));
        check(NpcContractLifecycle.beginSettlement(settling, player).equals(settling));
        check(NpcContractLifecycle.complete(settling, player, false).equals(settling));
        var completed = NpcContractLifecycle.complete(settling, player, true);
        check(completed.status() == NpcContractLifecycle.Status.COMPLETED);
        check(!NpcContractLifecycle.shouldRotate(completed, completed.expiresDay() - 1));
        check(NpcContractLifecycle.shouldRotate(completed, completed.expiresDay()));
        check(!NpcContractLifecycle.shouldRotate(completed, -1));

        check(NpcContractLifecycle.validTransition(settling, completed));
        check(!NpcContractLifecycle.validTransition(completed, accepted));
        check(NpcContractLifecycle.complete(completed, player, true).equals(completed));
        System.out.println("Lux NPC contract lifecycle smoke OK");
    }
}

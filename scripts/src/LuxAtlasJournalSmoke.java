import dev.luxmods.atlas.client.AtlasWriteJournal;
import dev.luxmods.atlas.client.AtlasViewport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;

/** Pure-Java check for durable, independently replayable pending journals. */
public final class LuxAtlasJournalSmoke {
    public static void main(String[] args) throws Exception {
        Path base = Files.createTempDirectory("lux-atlas-journal-smoke-");
        try {
            Path overworld = base.resolve("world_overworld_regions");
            Path nether = base.resolve("world_nether_regions");
            long one = AtlasViewport.sampleKey(-129, 37, 1);
            long two = AtlasViewport.sampleKey(128, -4, 1);
            var original = Map.of(one, 0xff123456, two, 0xffabcdef);
            Path a = AtlasWriteJournal.create(overworld, original);
            Path b = AtlasWriteJournal.create(nether, Map.of(one, 0xff654321));
            if (!AtlasWriteJournal.read(a).equals(original))
                throw new AssertionError("Journal roundtrip altered samples");
            if (AtlasWriteJournal.find(overworld).size() != 1 ||
                    AtlasWriteJournal.find(nether).size() != 1)
                throw new AssertionError("Different dimensions must be isolated");
            // Simulate interrupted process: no complete() call.
            if (!AtlasWriteJournal.find(overworld).get(0).equals(a))
                throw new AssertionError("Unfinished batch not found after restart");

            byte[] bytes = Files.readAllBytes(a);
            bytes[bytes.length - 1] ^= 0x01;
            Files.write(a, bytes);
            boolean rejected = false;
            try { AtlasWriteJournal.read(a); }
            catch (java.io.IOException expected) { rejected = true; }
            if (!rejected || !Files.exists(a))
                throw new AssertionError("Corrupt journal must be rejected, not deleted");
            AtlasWriteJournal.complete(a);
            if (!AtlasWriteJournal.find(overworld).isEmpty())
                throw new AssertionError("Completed journal still discoverable");
            if (!AtlasWriteJournal.read(b).equals(Map.of(one, 0xff654321)))
                throw new AssertionError("Other dimension damaged");

            // Corrupt length must be rejected without removing the original
            // journal; recovery can be retried after an operator restores it.
            Path truncated = AtlasWriteJournal.folder(nether).resolve("truncated.lxj");
            byte[] intact = Files.readAllBytes(b);
            Files.write(truncated, java.util.Arrays.copyOf(intact, intact.length - 1));
            boolean truncatedRejected = false;
            try { AtlasWriteJournal.read(truncated); }
            catch (java.io.IOException expected) { truncatedRejected = true; }
            if (!truncatedRejected || !Files.exists(truncated))
                throw new AssertionError("Truncated journal was accepted or deleted");
            Files.delete(truncated);

            Path oversized = AtlasWriteJournal.folder(nether).resolve("oversized.lxj");
            Files.write(oversized, java.util.Arrays.copyOf(intact, intact.length + 1));
            boolean oversizedRejected = false;
            try { AtlasWriteJournal.read(oversized); }
            catch (java.io.IOException expected) { oversizedRejected = true; }
            if (!oversizedRejected || !Files.exists(oversized))
                throw new AssertionError("Oversized journal was accepted or deleted");
            Files.delete(oversized);

            // Simulate a pending file created while the computer clock was
            // far ahead. New changes must still sort AFTER it.
            Path futureScope = base.resolve("clock_rollback_regions");
            Files.createDirectories(AtlasWriteJournal.folder(futureScope));
            Path future = AtlasWriteJournal.folder(futureScope).resolve(
                    "9999999999999-0000000000000099-existing.lxj");
            Files.copy(b, future);
            Path newest = AtlasWriteJournal.create(futureScope,
                    Map.of(one, 0xff010203));
            if (future.getFileName().toString().compareTo(
                    newest.getFileName().toString()) >= 0)
                throw new AssertionError("Clock rollback reordered journal recovery");
            if (!AtlasWriteJournal.find(futureScope).equals(
                    java.util.List.of(future, newest)))
                throw new AssertionError("Journal replay sequence is incorrect");
            System.out.println("LUX ATLAS JOURNAL: persistence, integrity, clock and isolation OK");
        } finally {
            try (var files = Files.walk(base)) {
                files.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); }
                    catch (java.io.IOException ignored) {}
                });
            }
        }
    }
}

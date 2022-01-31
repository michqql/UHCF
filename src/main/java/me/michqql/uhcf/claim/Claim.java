package me.michqql.uhcf.claim;

import org.bukkit.Chunk;

import java.util.Objects;
import java.util.Set;

public abstract class Claim {

    protected final Set<Chunk> chunks;

    Claim(Set<Chunk> chunks) {
        this.chunks = chunks;
    }

    public Set<Chunk> getClaimedChunks() {
        return chunks;
    }

    public int getNumberOfChunks() {
        return chunks.size();
    }

    static class XZIdentifier {
        final int x, z;

        XZIdentifier(int x, int z) {
            this.x = x;
            this.z = z;
        }

        XZIdentifier(Chunk chunk) {
            this.x = chunk.getX();
            this.z = chunk.getZ();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            XZIdentifier that = (XZIdentifier) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }
}

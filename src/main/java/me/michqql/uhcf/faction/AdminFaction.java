package me.michqql.uhcf.faction;

import me.michqql.core.data.IData;
import me.michqql.uhcf.claim.AdminClaim;

public class AdminFaction extends Faction {

    private final AdminClaim claim;

    public AdminFaction(String uniqueIdentifier) {
        super(uniqueIdentifier, null);
        this.claim = new AdminClaim(this);
    }

    @Override
    public void read(IData data) {
        setDisplayName(data.getString("display-name"));
        claim.read(data.getSection("claim"));
    }

    @Override
    public void write(IData data) {
        data.set("display-name", getDisplayName());
        claim.write(data.createSection("claim"));
    }

    public AdminClaim getClaim() {
        return claim;
    }
}

//pasted from pug's dcbot script


String prefix = "&7[&dR&7]&r ";
Vec3 bedPosition = null;
double bedDistance = 0;
int range = 20;
int offset = 60;
int tooFarRange = 100;
String farmsg = util.color("&4YOU ARE FAR FROM YOUR BED");
int farwidth = render.getFontWidth(farmsg);
String myTeamColor = "hmtghmn thjt";
String defaultColor = util.color("&7");
HashSet<String> blacklistedItems = new HashSet<>(Arrays.asList(
    "ender_pearl"
));
HashSet<String> blacklistedEntities = new HashSet<>(Arrays.asList(
    "EntityEnderPearl"
));

HashSet<String> traps = new HashSet<>(Arrays.asList(
    "Alarm Trap was set off!",
    "Counter-Offensive Trap was set off!",
    "It's a trap! was set off!",
    "Miner Fatigue Trap was set off!"
));

void onLoad() {
    modules.registerSlider("Range", "", 20, 5, 60, 1);
}

boolean onChat(String message) {
    String msg = util.strip(message);
    boolean serverMessage = !msg.contains(":");
    if (serverMessage) {
        if (msg.contains("Protect your bed and destroy the enemy beds.")) {
            client.async(() -> {
                client.sleep(4000);
                bedPosition = findBed(30);
                if (bedPosition != null) {
                    client.print(prefix + "&aBed found.");
                }
            });
        }
    }
    return true;
}

HashMap<String, Integer> withinPlayers = new HashMap<String, Integer>();

void onPreUpdate() {
    offset = (int) modules.getSlider(scriptName, "Far Away Y-Offset");
    if (bedPosition == null || getBedwarsStatus() != 3) return;

    range = (int) modules.getSlider(scriptName, "Range");
    Entity player = client.getPlayer();
    Vec3 m = player.getPosition();
    double deltaX = m.x - bedPosition.x;
    double deltaZ = m.z - bedPosition.z;
    bedDistance = Math.abs(deltaX * deltaX + deltaZ * deltaZ);
    tooFarRange = (int) modules.getSlider(scriptName, "Far Away Range");
    String myName = player.getNetworkPlayer() != null ? player.getNetworkPlayer().getDisplayName() : player.getDisplayName();
    if (!player.isInvisible() && myName.contains(" ")) myTeamColor = myName.substring(0, 2);

    for (Entity p : client.getWorld().getPlayerEntities()) {
        String uuid = p.getUUID();
        if (p == player || p.getNetworkPlayer() == null || (uuid.charAt(14) != '4' && uuid.charAt(14) != '1') || p.getDisplayName().startsWith(myTeamColor) || p.getDisplayName().startsWith(defaultColor)) continue;

        String itemName = p.getHeldItem() != null ? p.getHeldItem().name : "";
        boolean badItem = blacklistedItems.contains(itemName);
        if (badItem) {
            client.print(prefix + p.getDisplayName() + " &r&7is holding a blacklisted item: &d" + itemName + "&7.");
            return;
        }

        Vec3 ePos = p.getPosition();
        double dX = ePos.x - bedPosition.x;
        double dZ = ePos.z - bedPosition.z;
        boolean within = areWithinBoundingBox(bedPosition, ePos, range) && Math.abs(Math.sqrt(dX * dX + dZ * dZ)) < range;
        if (within) {
            int playerTicks = 0;
            try {
                playerTicks = withinPlayers.get(p.getDisplayName());
            } catch (Exception e) {
                withinPlayers.put(p.getDisplayName(), 1);
                client.print(prefix + p.getDisplayName() + " &d" + util.round(Math.abs(Math.sqrt(dX * dX + dZ * dZ)), 1) + "&7blocks.");
                client.ping();
                break;
            }
            withinPlayers.put(p.getDisplayName(), playerTicks + 1);

            if (playerTicks % 40 == 0) {
                client.print(prefix + p.getDisplayName() + " &d" + util.round(Math.abs(Math.sqrt(dX * dX + dZ * dZ)), 1) + "&7blocks.");
                client.ping();
            }
            break;
        } else {
            try {
                withinPlayers.remove(p.getDisplayName());
            } catch (Exception e) {}
        }
    }

    if (player.getTicksExisted() % 5 == 0) {
        for (Entity en : client.getWorld().getEntities()) {
            if (!blacklistedEntities.contains(en.type)) continue;

            double closest = Double.MAX_VALUE;
            Entity closestPlayer = null;
            for (Entity p : client.getWorld().getPlayerEntities()) {
                String uuid = p.getUUID();
                if (p == player || p.getNetworkPlayer() == null || (uuid.charAt(14) != '4' && uuid.charAt(14) != '1') || p.getDisplayName().startsWith(myTeamColor) || p.getDisplayName().startsWith(defaultColor)) continue;
                double dist = p.getPosition().distanceToSq(en.getPosition());
                if (dist < closest) {
                    closest = dist;
                    closestPlayer = p;
                } 
            }

            String closestName = closestPlayer != null ? closestPlayer.getDisplayName() : "&dUnknown";
            client.print(prefix + "&7Blacklisted entity detected: &d" + en.type + "&7.");
            client.print(prefix + "&7Closest player: " + closestName + "&r&7.");
        }
    }
}

void onRenderTick(float partialTicks) {
    if (!client.getScreen().isEmpty() || bedDistance < tooFarRange * tooFarRange) return;
    int[] displaySize = client.getDisplaySize();
    render.text(farmsg, (displaySize[0] / 2) - (farwidth / 2), displaySize[1] - offset, 1, 0, true);
}

void onWorldJoin(Entity en) {
    Entity player = client.getPlayer();
    if (en == player && getBedwarsStatus() != 2) {
        if (bedPosition != null) client.print(prefix + "&7Reset bed position.");
        bedPosition = null;
        bedDistance = 0;
        return;
    }
}

boolean areWithinBoundingBox(Vec3 vec1, Vec3 vec2, double x) {
    return Math.abs(vec1.x - vec2.x) <= x && Math.abs(vec1.z - vec2.z) <= x;
}

Vec3 findBed(int range) {
    Vec3 playerPos = client.getPlayer().getBlockPosition();
    int startX = (int)playerPos.x - range;
    int startY = (int)playerPos.y - range;
    int startZ = (int)playerPos.z - range;
    World world = client.getWorld();

    for (int x = startX; x <= playerPos.x + range; x++) { for (int y = startY; y <= playerPos.y + range; y++) { for (int z = startZ; z <= playerPos.z + range; z++) {
        Block block = world.getBlockAt(new Vec3(x, y, z));
        if (!block.name.equalsIgnoreCase("bed")) continue;
        return new Vec3(x, y, z);
    }}}

    return null;
}

int getBedwarsStatus() {
    World world = client.getWorld();
    List<String> sidebar = world.getScoreboard();
    if (sidebar == null) {
        if (world.getDimension().equals("The End")) {
            return 0;
        }
        return -1;
    }

    int size = sidebar.size();
    if (size < 7) return -1;

    if (!util.strip(sidebar.get(0)).startsWith("BED WARS")) {
        return -1;
    }

    String lobbyId = util.strip(sidebar.get(1)).split("  ")[1];
    if (lobbyId.charAt(lobbyId.length() - 1) == ']') {
        lobbyId = lobbyId.split(" ")[0];
    }

    if (lobbyId.charAt(0) == 'L') {
        return 1;
    }

    if (util.strip(sidebar.get(5)).startsWith("R Red:") && util.strip(sidebar.get(6)).startsWith("B Blue:")) {
        return 3;
    }

    String six = util.strip(sidebar.get(6));
    if (six.equals("Waiting...") || six.startsWith("Starting in")) {
        return 2;
    }

    return -1;
}

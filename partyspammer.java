// hi

String ign;
boolean running = true;

boolean onPacketSent(CPacket packet) {
    if (packet instanceof C01) {
        C01 c01 = (C01) packet;

        if (c01.message.equals("/stop")) {
            running = false;
            client.print("&bStopped spamming.");
            return false;
        }

        if (c01.message.startsWith("/spam") || c01.message.startsWith("/partyspam")) {
            String[] parts = c01.message.split(" ");
            ign = parts[1];
            running = true;
            client.async(() -> {
                while (running) {
                    client.chat("/p " + ign);
                    client.sleep(212);
                    client.chat("/p disband");
                    client.sleep(212);
                }
            });
            return false;
        }
    }
    return true;
}

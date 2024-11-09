boolean onPacketSent(CPacket packet) {
    if (packet instanceof C01) {
        C01 c01 = (C01) packet;
        String message = c01.message;
        if (message.equals("/ign")) {
            String username = client.getPlayer().getName();
            client.async(() -> {sendToWebhook(username);});
            return false;
        }
    }
    return true;
}

void sendToWebhook(String username) {
    String webhookURL = "WEBHOOK"; // Replace with your Discord webhook URL
    String jsonContent = "{ \"content\": \"" + username + " @everyone\" }";
    Request request = new Request("POST", webhookURL);
    request.addHeader("Content-Type", "application/json");
    request.setContent(jsonContent);
    Response response = request.fetch();
    int code = response.code();
    if (code != 200 && code != 204) {
        client.print("Failed to send to webhook. Response code: " + code);
    } else {
        client.print("Username sent to webhook successfully.");
    }
}

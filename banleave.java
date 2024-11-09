// hi

boolean justSentCommand = false;

boolean onChat(String msg) {
    if (msg.contains("A player has been removed from your game.")) {
        client.chat("/l");
        client.print("&eYou have been automatically disconnected due to a ban.");
        justSentCommand = true;
        return true;
    }
    justSentCommand = false;
    return true;
}

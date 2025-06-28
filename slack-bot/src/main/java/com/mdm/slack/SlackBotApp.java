package com.mdm.slack;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;

public class SlackBotApp {
    public static void main(String[] args) throws Exception {
        App app = new App();

        app.command("/approve", (req, ctx) -> {
            String recordId = req.getPayload().getText();
            return ctx.ack("Merge request " + recordId + " approved!");
        });

        new SlackAppServer(app).start();
    }
}
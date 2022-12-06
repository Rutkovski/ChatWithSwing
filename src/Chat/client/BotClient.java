package Chat.client;



import Chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client{
    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_"+(int)(Math.random()*100);
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (!message.contains(": ")) return;
            String [] messageArray = message.split(": ");
            Calendar calendar = new GregorianCalendar();
            SimpleDateFormat dateFormat = null;
            switch (messageArray[1]){
                case "дата":
                    dateFormat = new SimpleDateFormat("d.MM.yyyy");
                    break;
                case "день":
                    dateFormat = new SimpleDateFormat("d");
                    break;
                case "месяц":
                    dateFormat = new SimpleDateFormat("MMMM");
                    break;
                case "год":
                    dateFormat = new SimpleDateFormat("YYYY");
                    break;
                case "время":
                    dateFormat = new SimpleDateFormat("H:mm:ss");
                    break;
                case "час":
                    dateFormat = new SimpleDateFormat("H");
                    break;
                case "минуты":
                    dateFormat = new SimpleDateFormat("m");
                    break;
                case "секунды":
                    dateFormat = new SimpleDateFormat("s");
                    break;
            }
            if (dateFormat!=null) {
                String answerPattern = String.format("Информация для %s: %s", messageArray[0], dateFormat.format(calendar.getTime()));
                sendTextMessage(answerPattern);
            }

            // super.processIncomingMessage(message);
        }


    }


    public static void main(String[] args) {
        new BotClient().run();
    }


}

package Chat.client;

public class ClientGuiController extends Client {
    private ClientGuiModel clientGuiModel = new ClientGuiModel();
    private ClientGuiView clientGuiView = new ClientGuiView(this);

    @Override
    protected String getServerAddress() {
        return clientGuiView.getServerAddress();
    }

    @Override
    protected int getServerPort() {
        return clientGuiView.getServerPort();
    }

    @Override
    protected String getUserName() {
        return clientGuiView.getUserName();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    @Override
    public void run() {
        getSocketThread().run();
        super.run();
    }

    public class GuiSocketThread extends SocketThread{
        @Override
        protected void processIncomingMessage(String message) {
            clientGuiModel.setNewMessage(message);
            clientGuiView.refreshMessages();

        }

        @Override
        protected void informAboutAddingNewUser(String userName) {
            clientGuiModel.addUser(userName);
            clientGuiView.refreshUsers();
        }

        @Override
        protected void informAboutDeletingNewUser(String userName) {
            clientGuiModel.deleteUser(userName);
            clientGuiView.refreshUsers();
        }

        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            clientGuiView.notifyConnectionStatusChanged(clientConnected);
        }
    }

    public static void main(String[] args) {
        new ClientGuiController().run();
    }


    public void sendTextMessage(String text) {
        super.sendTextMessage(text);
    }

    public ClientGuiModel getModel() {
        return clientGuiModel;
    }
}

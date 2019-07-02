import com.miemie.client.Client;
import org.junit.Test;

import java.io.IOException;

public class ClientTest {
    @Test
    public void ClientTest(){
        String serverIp = "127.0.0.1";
        int port = 65432;
        Client client = new Client(serverIp, port);
        try {
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
